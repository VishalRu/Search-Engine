package edu.asu.irs13;

//package lab5;

import java.util.*;

/**
 * Document clustering
 * @author qyuvks
 *
 */
public class Clustering {
	private int numberOfClusters;
	private int totalDoc;
	private HashMap<String,Integer> wordsMap=new HashMap<String,Integer>();
	private HashMap<Integer,Doc> docWordMap=new HashMap<Integer,Doc>();
	private double meanVector[][];
	private double centriodVector[];
	private HashMap<Integer,Integer> DocClusterMaping;
	//Declare attributes here
	
	/**
	 * Constructor for attribute initialization
	 * @param numC number of clusters
	 */
	public Clustering(int numC)
	{
		//TO BE COMPLETED
		numberOfClusters=numC;
		this.meanVector=new double[numC][50];
		centriodVector=new double[numC];
		DocClusterMaping=new HashMap<Integer,Integer>();
	}
	
	/**
	 * Load the documents to build the vector representations
	 * @param docs
	 */
	public void preprocess(String[] docs){
		//TO BE COMPLETED
		int clusterSize=numberOfClusters;
		setWordsMap(docs);
		this.meanVector[0]=copyArray(this.docWordMap.get(0).getTFIDFvector(),50);
		DocClusterMaping.put(0, 0);
		DocClusterMaping.put(9, 1);
		DocClusterMaping.put(1, -1);
		DocClusterMaping.put(2, -1);
		DocClusterMaping.put(3, -1);
		DocClusterMaping.put(4, -1);
		DocClusterMaping.put(5, -1);
		DocClusterMaping.put(6, -1);
		DocClusterMaping.put(7, -1);
		DocClusterMaping.put(8, -1);
		this.meanVector[1]=copyArray(this.docWordMap.get(9).getTFIDFvector(),50);
		
		for(int cluster=0;cluster<numberOfClusters;cluster++) {
			int tempSum=0;
			for (int j=0;j<meanVector[cluster].length;j++) 
				{
				tempSum+= Math.pow(meanVector[cluster][j], 2);
				}
			this.centriodVector[cluster]=tempSum;
		}
		
		System.out.println("HI");
	}
	
	public  double[] copyArray(double[] arr,int k) // to calculate the Transpose Matrix
	{
		double[] temp=new double[k];
		for(int row=0; row< k; row++){
			
				temp[row]=arr[row];
			
		}
		return temp;
	}
	
	public void setWordsMap(String[] docs)
	{
		int counter=0;
		for(int i=0;i<docs.length;i++)
		{
			int docId=i;
			for(String w: docs[i].split(" "))
			{
				if(wordsMap.containsKey(w))
				{
				
				}
				else
				{
					wordsMap.put(w, counter++);
				}
			}
			Doc newDoc=new Doc();
			for(String w: docs[i].split(" "))
			{
				int position=wordsMap.get(w);  // GET IDF OF TERM AND MULTILPLY WITH TF(i.e. w)
				newDoc.setTFIDFvector(position, 1);
			}
			this.docWordMap.put(docId, newDoc);		
			
		}
	}
	
	public  int findNewCluster(int docNumber)
	{

		double[] tf = docWordMap.get(docNumber).getTFIDFvector(); //{2,3,4,5,6,,223,23,12,}
			double[] distance = new double[numberOfClusters];
			int sumOfSqOfDoc=0;
			for(int cluster=0;cluster<numberOfClusters;cluster++)
			{
				double[] tfidfCentroid = this.meanVector[cluster];//{1,2,33,11,22,11,}
				int tempSum=0;
				for(int i=0;i<50;i++) 
				{
					distance[cluster] += tf[i]*tfidfCentroid[i];
					tempSum+=Math.pow(tf[i], 2);
				}
				sumOfSqOfDoc=tempSum;
			}
			for(int i=0;i<distance.length;i++) {
				distance[i] = distance[i]/(Math.sqrt(sumOfSqOfDoc) * Math.sqrt(this.centriodVector[i]));
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
	/**
	 * Cluster the documents
	 * For kmeans clustering, use the first and the ninth documents as the initial centroids
	 */
	public void cluster(){
		//TO BE COMPLETED
		boolean flag;
		int COUNTER=0;
		//printClusterMap();
		
		do
		{
			System.out.println("*************ITERATION NUMBER********** " + COUNTER++);
			flag=false;
			for(int docId=0;docId<10;docId++)
			{
				
			
				int prevCluster=this.DocClusterMaping.get(docId);
				
				int currentCluster=findNewCluster(docId);
				System.out.println("prev cluster is "+ prevCluster + " CurrentCluster "+ currentCluster);
				if(prevCluster!=currentCluster)
				{
					DocClusterMaping.put(docId, currentCluster);
					flag=true;
				}
			}
			CalcMeanVal();
			
		}while(flag);
		
		System.out.println("Print Me");
 	}
	public void CalcMeanVal()
	{
		//System.out.println("*************IN CALCULATE MEAN VALUE**********");
		int[] numberInCluster = new int[numberOfClusters];
		for(int i=0;i<numberOfClusters;i++) {
			for (int j=0;j<50;j++) {
				meanVector[i][j] = 0;
			}
		}
		
		for(Map.Entry<Integer,Doc> e : docWordMap.entrySet()) {
			double[] tf = e.getValue().getTFIDFvector();
			int clusterId =this.DocClusterMaping.get(e.getKey());
			numberInCluster[clusterId]++;
			for (int j=0;j<tf.length;j++) {
				meanVector[clusterId][j] += tf[j];
			}
		
		}
		
		for(int cluster=0;cluster<numberOfClusters;cluster++) {
			
			for (int j=0;j<meanVector[cluster].length;j++) {
			
				if(numberInCluster[cluster]!=0)
					meanVector[cluster][j] = meanVector[cluster][j] / numberInCluster[cluster];
				else
					meanVector[cluster][j]=0;
			}
		
		}
		
		for(int cluster=0;cluster<numberOfClusters;cluster++) {
			double tempSum=0;
			for (int j=0;j<meanVector[cluster].length;j++) {
				tempSum+= Math.pow(meanVector[cluster][j], 2);
			
			}
			this.centriodVector[cluster]=tempSum;
		}
	//	System.out.println("*************OUT OF CALCULATE MEAN VALUE**********");
	}
	


	
	public static void main(String[] args){
		String[] docs = {"hot chocolate cocoa beans",
				 "cocoa ghana africa",
				 "beans harvest ghana",
				 "cocoa butter",
				 "butter truffles",
				 "sweet chocolate can",
				 "brazil sweet sugar can",
				 "suger can brazil",
				 "sweet cake icing",
				 "cake black forest"
				};
		Clustering c = new Clustering(2);
		
		c.preprocess(docs);

		c.cluster();
		/*
		 * Expected result:
		 * Cluster: 0
			0	1	2	3	4	
		   Cluster: 1
			5	6	7	8	9	
		 */
	}
}

/**
 * 
 * @author qyuvks
 * Document class for the vector representation of a document
 */
class Doc{
	//TO BE COMPLETED
	private double[] TFIDFvector;
	
	public Doc() {
		this.TFIDFvector=new double[50];
		}
		
	public double[] getTFIDFvector() {
		return TFIDFvector;
	}
	public void setTFIDFvector(double[] tFIDFvector) {
		TFIDFvector = tFIDFvector;
	}
	public void setTFIDFvector(int index, int val) {
		TFIDFvector[index] = val;
	}
	
	
}