package edu.asu.irs13;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix1D;
/***********************************************************************
 * This function will calculate the pagerank of entire corpus before the query is being entered
 * This approach will pre compute the pagerank of all the documents in will store them in a text file
 * After, User enters a query program will just check for pre-computed value
 **************************************************************************/
public class PR {
	
	private DoubleMatrix2D AdjacencyMatrix;
	ArrayList<Integer> sinkNodes=new ArrayList<Integer>();
	private double[] Mvector;
	List<Integer> pageRank;
	private double[] rankVector;
	private double C;
	private double K;
	private double Z;
	private double resetValue;
	final private int  totalDocs=25054;
	double size=25054;
	//HashMap<Character, Integer> xx=new HashMap<Character, Integer>();
	
	public void setResetVector(double initResetVal) throws Exception
	{
		this.rankVector=new double[totalDocs];
		Arrays.fill(this.rankVector, initResetVal);
	}
	public void setSinkNodes() throws Exception// to calculate the Transpose Matrix
	{
		System.out.println("in setSinkNOdes Function");
		this.setResetVector(1/size);
		LinkAnalysis.numDocs = 25054;
		LinkAnalysis link=new LinkAnalysis();
			for(int docId=0; docId< totalDocs; docId++){
				if(link.getLinks(docId).length==0)
						sinkNodes.add(docId);
			}
			System.out.println(" Sink Nodes function done");
		
	}
	public void iterate() throws Exception
	{
	LinkAnalysis.numDocs = 25054;
	LinkAnalysis link=new LinkAnalysis();
	int powerIterLimit=100; 
	int convergeIter=0;
	double[] tempRankVector=new double[totalDocs];
	//rankVector=new double[totalDocs];
	//while(true){
		for(int iterater=0; iterater<powerIterLimit;iterater++)
		{
		tempRankVector=rankVector.clone();
		convergeIter=iterater;
		Runtime runtime = Runtime.getRuntime();
	    // Run the garbage collector
	    runtime.gc();
		for(int row=0; row<totalDocs;row++)
		{
			
			double Mstar;
			Mvector=new double[totalDocs];
			int[] cita=link.getCitations(row);
			for(Integer C: cita)
			{
				int [] inLinks=link.getLinks(C);
				int n=inLinks.length;
				Mvector[C]=(double)1/n;
			}
			for(Integer SinkNode : sinkNodes)
			{
				Mvector[SinkNode]=1/this.size;
			}
			double tempSum=0;
			for(int i=0;i<totalDocs;i++)
			{
				Mstar=this.C*Mvector[i]+(1-this.C)/this.size;
				tempSum+=Mstar*rankVector[i];
			}
			
			Mstar=0.0;
			rankVector[row]=tempSum;
			tempSum=0;
			
		}
		long memory = runtime.totalMemory() - runtime.freeMemory();
	    System.out.println("Used memory is bytes: " + memory + "IterCount :"+iterater );
	    
		if(!MatrixOperations.diff(tempRankVector, rankVector, totalDocs))
		{
			System.out.println("Converged in :" + convergeIter);
			break;
		}
	}
}		
	
	public void setKZresetVector(double cVal)
	{
		this.C=cVal;
		this.K=(double)1/totalDocs;
		this.Z=this.K;
		this.resetValue=(1-this.C) * this.K;
		
	}
	
	public void normalize() throws IOException
	{
		double min = rankVector[0];
		double max = rankVector[0];
		int temp=0;
		
		for(int index = 1;index < totalDocs; index++)
		{
			if(min > rankVector[index])
				min = rankVector[index];
			
			if(max < rankVector[index]){
				max = rankVector[index];
				temp=index;
			}
		}		
		System.out.println("HIghest page rank is:" + max + "Index is " +temp);
		double d = max - min;
		for(int index = 0;index < totalDocs; index++)
		{
			rankVector[index] = (rankVector[index] - min)/d;
		}		
	
	}

	public int  Highesthubval()
	{
		int highHub=0;
		int doc_id=0;
		LinkAnalysis.numDocs = 25054;
		LinkAnalysis link=new LinkAnalysis();
		for(int row=0; row<totalDocs;row++)
		{
			int n=link.getLinks(row).length;
			if(n>highHub)
			{
				highHub=n;
				doc_id=row;
			}	
		}
		return doc_id;
	}
	
	
	public ArrayList<Integer> getPageRankDocs(String query) throws Exception
	{
		ArrayList<Integer> pageRankResults=new ArrayList<Integer>();
		setSinkNodes();
		setKZresetVector(0.8);
		iterate();
		normalize();
				
		SearchFiles TFobj=new SearchFiles();
		try {
			TFobj.geneateMagnitudeOfDoc();
		} catch (Exception e) {
				e.printStackTrace();
		}
					
			TFobj.tf_idf_search(query);
			HashMap<Integer,Double> TFdocs=new HashMap<Integer, Double>(TFobj.getTfIDF());
			HashMap<Double,Integer> TFpageRank=new HashMap<Double,Integer>();
			Set<Integer> keyResults2=TFdocs.keySet();
			for (Integer doc : keyResults2)
			{
				int docId=doc;
				double TFval=TFdocs.get(doc);
				double pageVal=this.rankVector[docId];
				System.out.println("The DOC Id is: "+ docId + " TF value is : "+ TFval + " Page Value is :" + pageVal);
				double compVal= 0.4 * pageVal +0.6 *TFval; // w*pageRankValue + (1-w) TF similarity
				TFpageRank.put(compVal, docId);
				}
			LinkedList<Map.Entry<Double,Integer>> tf_linkedlist = new LinkedList<Map.Entry<Double,Integer>>(TFpageRank.entrySet());
			Collections.sort(tf_linkedlist, new Comparator<Map.Entry<Double,Integer>>() {
				@Override
				public int compare(Entry<Double, Integer> o1,
						Entry<Double, Integer> o2) {
					return -(o1.getKey().compareTo(o2.getKey()));
				}
				});
				
			int i=0;
			for(Map.Entry<Double, Integer> ele : tf_linkedlist)
				{
					if(i++>10)break;
					int doc_id=ele.getValue();
					pageRankResults.add(doc_id);
					//System.out.println("Doc ID is  :" + doc_id + "with TFIDF value: "+ TFdocs.get(doc_id));
					System.out.println("Doc ID is  : " + doc_id + " with total value: "+ ele.getKey());
				}
	
		return pageRankResults;		
	}
	public static void main(String[] args) throws Throwable
	{
		
			PR temp=new PR();
			System.out.println(temp.Highesthubval());
		// temp.setAdjcacencyMat();
		//	temp.setResetVector(1/);
			temp.setSinkNodes();
			temp.setKZresetVector(0.8);
			temp.iterate();
			temp.normalize();
		
			
			SearchFiles TFobj=new SearchFiles();
			try {
				TFobj.geneateMagnitudeOfDoc();
			} catch (Exception e) {
					e.printStackTrace();
			}
			Scanner sc = new Scanner(System.in);
			String str ="";
			System.out.print("query> ");
			
			while(!(str = sc.nextLine()).equals("quit"))
			{	
				TFobj.tf_idf_search(str);
				HashMap<Integer,Double> TFdocs=new HashMap<Integer, Double>(TFobj.getTfIDF());
				HashMap<Double,Integer> TFpageRank=new HashMap<Double,Integer>();
				Set<Integer> keyResults2=TFdocs.keySet();
				for (Integer doc : keyResults2)
				{
					int docId=doc;
					double TFval=TFdocs.get(doc);
					double pageVal=temp.rankVector[docId];
					System.out.println("The DOC Id is: "+ docId + " TF value is : "+ TFval + " Page Value is :" + pageVal);
					double compVal= 0.4 * pageVal +0.6 *TFval; // w*pageRankValue + (1-w) TF similarity
					TFpageRank.put(compVal, docId);
					}
				LinkedList<Map.Entry<Double,Integer>> tf_linkedlist = new LinkedList<Map.Entry<Double,Integer>>(TFpageRank.entrySet());
				Collections.sort(tf_linkedlist, new Comparator<Map.Entry<Double,Integer>>() {
					@Override
					public int compare(Entry<Double, Integer> o1,
							Entry<Double, Integer> o2) {
						return -(o1.getKey().compareTo(o2.getKey()));
					}
					});
					
				int i=0;
				for(Map.Entry<Double, Integer> ele : tf_linkedlist)
					{
						if(i++>10)break;
						int doc_id=ele.getValue();
						//System.out.println("Doc ID is  :" + doc_id + "with TFIDF value: "+ TFdocs.get(doc_id));
						System.out.println("Doc ID is  : " + doc_id + " with total value: "+ ele.getKey());
					}
		
				
				
				
				System.out.print("query> ");
			}
			
	}
	
}
			



