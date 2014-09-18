package edu.asu.irs13;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.print.attribute.standard.NumberOfDocuments;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.FSDirectory;
/*******************
 * K means clustering implementation to cluster documents on the basis of their terms frequency
 * @author vishalruhela
 *
 */
public class KmeansCluster {
   	private ArrayList<Integer> TFdocs;
	private double meanVector[][];
	private double centriodVector[];
	//private int[][] clusterVector;
	private int numberOfClusters;
	private int totalDoc;
	private HashMap<Integer,Integer> DocClusterMaping;
	private HashMap<String,Double> idf_val=new HashMap<String,Double>();
	private HashMap<Integer, Vector> docVectorMap=new HashMap<Integer,Vector>();
	private HashMap<Integer, Double>TfDocsTermFreq=new HashMap<Integer,Double>();
	private HashMap<Integer,String> termPosMaping=new HashMap<Integer,String>();
	private HashMap<Integer,String> clusterSummaryMap=new  HashMap<Integer,String>();
	SearchFiles TFobj;
	
	public HashMap<Integer, String> getClusterSummaryMap() {
		return clusterSummaryMap;
	}

	public void setClusterSummaryMap(HashMap<Integer, String> clusterSummaryMap) {
		this.clusterSummaryMap = clusterSummaryMap;
	}

	/********************
	 * Constructor to initialize elements 
	 * @param clusterSize
	 * @param totalDocs
	 * @throws Exception 
	 */
	public KmeansCluster(int clusterSize, int totalDocs) throws Exception
	{
		System.out.println("*************IN CONSTRUCTOR**********");
		this.numberOfClusters=clusterSize;
		totalDoc=totalDocs;
		//this.clusterVector=new int[clusterSize][totalDocs];
		this.meanVector=new double[clusterSize][400000];
		this.centriodVector=new double[clusterSize];		
		 TFobj=new SearchFiles();
		 TFobj.geneateMagnitudeOfDoc();
		System.out.println("*************OUT OF CONSTRUCTOR**********");
	}
	
		/*********************
	 * Function to get TfIDF documents for a given query and setting TFdocs arraylist
	 * @param query
	 * @throws Exception
	 */
	public void getTFdocs(String query) throws Exception
	{
		//SearchFiles TFobj=new SearchFiles();
		this.DocClusterMaping=new HashMap<Integer,Integer>();
		//TFobj.geneateMagnitudeOfDoc();
		TFobj.tf_idf_search(query);
		int lastCluster=0;
		this.TFdocs=new ArrayList<Integer>(TFobj.getTopDocOfTFIDF());
		this.idf_val=TFobj.getIdfMap();
		this.TfDocsTermFreq=TFobj.getTermFreqMap();
		for(int cluster=0;cluster<numberOfClusters;cluster++)
		{
			DocClusterMaping.put(this.TFdocs.get(cluster), cluster);
			lastCluster=cluster;
		}
		int onlyRemainingCluster=lastCluster;
		for(int cluster=++lastCluster;cluster<10;cluster++)
		{
			DocClusterMaping.put(this.TFdocs.get(cluster), -1);
		}
	}
	/*****************
	 * Function to check the Mean values of  cluster elements after each iteration
	 */
	public void CalcMeanVal()
	{
		//System.out.println("*************IN CALCULATE MEAN VALUE**********");
		int[] numberInCluster = new int[numberOfClusters];
		for(int i=0;i<numberOfClusters;i++) {
			for (int j=0;j<400000;j++) {
				meanVector[i][j] = 0;
			}
		}
		for(Map.Entry<Integer,Vector> e : docVectorMap.entrySet()) {
			double[] tfidf = e.getValue().getTFIDFvector();
			int clusterId =this.DocClusterMaping.get(e.getKey());
			numberInCluster[clusterId]++;
			for (int j=0;j<tfidf.length;j++) {
				meanVector[clusterId][j] += tfidf[j];
			}
		
		}
		
		for(int cluster=0;cluster<numberOfClusters;cluster++) {
			//int tempSum=0;
			for (int j=0;j<meanVector[cluster].length;j++) {
				//tempSum+= meanVector[cluster][j];
				if(numberInCluster[cluster]!=0)
					meanVector[cluster][j] = meanVector[cluster][j] / (double)numberInCluster[cluster];
				else
					meanVector[cluster][j]=0;
			}
			//this.centriodVector[cluster]=tempSum;
		}
		
		for(int cluster=0;cluster<numberOfClusters;cluster++) {
			int tempSum=0;
			for (int j=0;j<meanVector[cluster].length;j++) {
				tempSum+= Math.pow(meanVector[cluster][j], 2);
				//meanVector[cluster][j] = meanVector[cluster][j] / numberInCluster[cluster];
			}
			this.centriodVector[cluster]=tempSum;
		}
	//	System.out.println("*************OUT OF CALCULATE MEAN VALUE**********");
	}
	public void initializeMeanVector()
	{
	//	System.out.println("*************IN INITIALIZE MEAN VALUE**********");
		int[] numberInCluster = new int[numberOfClusters];
		for(int cluster=0;cluster<numberOfClusters;cluster++)
		{
			this.meanVector[cluster]=MatrixOperations.copyArray(docVectorMap.get(TFdocs.get(cluster)).getTFIDFvector(), 400000);
		}
		for(Map.Entry<Integer,Vector> e : docVectorMap.entrySet()) {
			int clusterId =this.DocClusterMaping.get(e.getKey());
			if(clusterId!=-1)
				numberInCluster[clusterId]++;
			}
		
		
		for(int cluster=0;cluster<numberOfClusters;cluster++) {
			int tempSum=0;
			for (int j=0;j<meanVector[cluster].length;j++) {
				tempSum+= Math.pow(meanVector[cluster][j], 2);
				//meanVector[cluster][j] = meanVector[cluster][j] / numberInCluster[cluster];
			}
			this.centriodVector[cluster]=tempSum;
		}
	//	System.out.println("*************OUT OF INITIALIZE MEAN VALUE**********");
	}
	/*****************
	 * Function to find the new Cluster for the document id
	 * @param docNumber
	 * @return
	 */
	public  int findNewCluster(int docNumber)
	{
			double[] tfidf = docVectorMap.get(docNumber).getTFIDFvector(); //{2,3,4,5,6,,223,23,12,}
			double[] distance = new double[numberOfClusters];
			for(int cluster=0;cluster<numberOfClusters;cluster++)
			{
				double[] tfidfCentroid = this.meanVector[cluster];//{1,2,33,11,22,11,}
				for(int i=0;i<tfidf.length;i++) {
					distance[cluster] += tfidf[i]*tfidfCentroid[i];
				}
			}
			for(int i=0;i<distance.length;i++) {
				distance[i] = distance[i]/(Math.sqrt(this.TfDocsTermFreq.get(docNumber)) * Math.sqrt(this.centriodVector[i]));
			//	System.out.println("Distance is "+  distance[i] + " @ cluster "+ i);
			}
				
		int clusterNumber=0;
		double tempVal=distance[0];
		for(int cluster=0;cluster<numberOfClusters;cluster++)
		{
			if(tempVal<distance[cluster])
			{
				tempVal=distance[cluster];
				clusterNumber=cluster;
			}
		}
		return clusterNumber;
	}
	public void setdocVectoMap() throws IOException
	{
		
		try {
			Set<String> IDFkeyset=this.idf_val.keySet();
			int counter=0;
			IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
			for(String word: IDFkeyset)
			{
			//	System.out.println("======"+word+"======");
				termPosMaping.put( counter,word);
				Term term = new Term("contents", word);
				TermDocs tdocs = r.termDocs(term);
				while(tdocs.next())
				{
					int doc=tdocs.doc();
				
					if(this.DocClusterMaping.containsKey(doc))
					{
						int freqOfTerm=tdocs.freq();
						if(docVectorMap.containsKey(doc)==true)
						{
						//	System.out.println(doc);
							docVectorMap.get(doc).setTFIDFvector(counter, freqOfTerm*idf_val.get(word));
						}
						else
						{
							//System.out.println(doc);
							Vector v=new Vector();
							v.setTFIDFvector(counter, freqOfTerm*idf_val.get(word));
							docVectorMap.put(doc, v);
						}
						
					}
				}
				
				counter++;
			}
	//	System.out.println("***"+counter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		/************************
	 * Function to iterate till the documents settle down in appropriate cluster
	 * @param docNum
	 */
	public void iterate(int docNum)
	{
	//	System.out.println("*************IN ITERATE **********");
		boolean flag;
		int COUNTER=0;
		//printClusterMap();
		this.initializeMeanVector();
		do
		{
			System.out.println("*************ITERATION NUMBER********** " + COUNTER++);
			flag=false;
			for(int docId=0;docId<docNum;docId++)
			{
				
				int docN=this.TFdocs.get(docId);
				int prevCluster=this.DocClusterMaping.get(docN);
				
				int currentCluster=findNewCluster(docN);
				System.out.println("prev cluster is "+ prevCluster + " CurrentCluster "+ currentCluster);
				if(prevCluster!=currentCluster)
				{
					DocClusterMaping.put(docN, currentCluster);
					flag=true;
				}
			}
			CalcMeanVal();
			
		}while(flag);
		
		updateClusterSummary();
		
	//	System.out.println("*************OUT OF ITERATE **********");
	}
	
	/************
	 * Function to update cluster summary
	 */
	public void updateClusterSummary()
	{
		HashMap<String, Integer> clusterTermsSummary=new HashMap<String,Integer>();
		for(int cluster=0;cluster<numberOfClusters;cluster++) 
		{
			 double tempSum=0;int pos=0;
			 double tempSum1=0;int pos1=0;
			 double tempSum2=0;int pos2=0;
			 double tempSum3=0;int pos3=0;
			 double tempSum4=0;int pos4=0;
			 
			 for(int j=-0;j<400000;j++)
			 {
				 if(this.meanVector[cluster][j]>tempSum)
				 {
					tempSum3=tempSum2;
					pos3=pos2;					 
					 tempSum2 = tempSum1;
					 pos2=pos1;
					 tempSum1=tempSum;
					 pos1=pos;
					 tempSum=meanVector[cluster][j];
					 pos=j;
				 }
				 else if(this.meanVector[cluster][j]>tempSum1)
				 {
					 tempSum3=tempSum2;
					 pos3=pos2;					 
					 tempSum2=tempSum1;
					 pos2=pos1;
					 tempSum1=meanVector[cluster][j];
					 pos1=j;
				 }
					 
				 else if(this.meanVector[cluster][j]>tempSum2)
				 {
					tempSum3=tempSum2;
					pos3=pos2;
					 tempSum2=meanVector[cluster][j];
					 pos2=j;
				 }
				 else if(this.meanVector[cluster][j]>tempSum3)
				 {
					tempSum4=tempSum3;
					pos4=pos3;					 
					 tempSum3=meanVector[cluster][j];
					 pos3=j;
				 }
				 else if(this.meanVector[cluster][j]>tempSum3)
				 {
					 tempSum4=meanVector[cluster][j];
					 pos4=j;
				 }
			 }
			 String oneWord=this.termPosMaping.get(pos);
			 if(oneWord.equalsIgnoreCase("li") ||oneWord.equalsIgnoreCase("style4"))
				 oneWord="";
			 
			 String secondWord=this.termPosMaping.get(pos1);
			 if(secondWord.equalsIgnoreCase("li") ||secondWord.equalsIgnoreCase("style4"))
				 secondWord="";
			 String thirdWord=this.termPosMaping.get(pos2);
			 if(thirdWord.equalsIgnoreCase("li") ||thirdWord.equalsIgnoreCase("style4"))
				 thirdWord="";
			 String fourthWord=this.termPosMaping.get(pos3);
		//	 String temp=oneWord+ " ," +secondWord + " ,"+ thirdWord + " ,"+fourthWord;
			 
			 String fivthWord=this.termPosMaping.get(pos4);
			 if(fivthWord.equalsIgnoreCase("li") ||fivthWord.equalsIgnoreCase("style4"))
				 fivthWord="";
			 String temp=oneWord+ " ," +secondWord + " ,"+ thirdWord + " ,"+fourthWord+ ", "+ fivthWord;
			 System.out.println("Summary"+ temp);
			 clusterSummaryMap.put(cluster, temp);
		}		 
			
	}
	/**********
	 * Function to print which document belongs to which cluster
	 */
	public void printClusterMap()
	{
		for(Map.Entry<Integer,Integer> e : DocClusterMaping.entrySet()) {
			System.out.println("The Document Number "+ e.getKey() + " Maped to Cluster "+ e.getValue());
		}
			
	}
		
	public HashMap<Integer, Integer> getDocClusterMaping() {
		return DocClusterMaping;
	}

	public void setDocClusterMaping(HashMap<Integer, Integer> docClusterMaping) {
		DocClusterMaping = docClusterMaping;
	}
	/**************
	 * Driver Program
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		KmeansCluster tempObject=new KmeansCluster(3,10);
		
		//tempObject.setClusterVector(-1);
		String str ="";
		System.out.print("query> ");
		Scanner sc = new Scanner(System.in);
		while(!(str = sc.nextLine()).equals("quit"))
		{	
			Date Checkpoint1=new Date();
		tempObject.getTFdocs(str);
		Date Checkpoint2=new Date();
		tempObject.setdocVectoMap();
		Date Checkpoint3=new Date();
		tempObject.iterate(10);
		Date Checkpoint4=new Date();
		System.out.println("FOr getting TF docs "+ (Checkpoint2.getTime()-Checkpoint1.getTime()));
		System.out.println("FOr settinf document vector map "+ (Checkpoint3.getTime()-Checkpoint2.getTime()));
		System.out.println("Iteration takes "+ (Checkpoint4.getTime()-Checkpoint3.getTime()));
		System.out.println("Total Time "+ (Checkpoint4.getTime()-Checkpoint1.getTime()));
		tempObject.printClusterMap();
		System.out.print("query> ");
		}

	}

}
