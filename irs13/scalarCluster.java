package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.FSDirectory;
/******************
 * Scalar Clustering program
 * @author vishalruhela
 *
 */
public class scalarCluster {
	//private ArrayList<Integer> TFdocs;
	private HashMap<String,Double> idf_val;
	private double Sqq,Stt, Sqt;
	private double[] queryVector;
	private double[] newTermVector;
	private double[] scalarVector;
	private HashMap<Integer, String> termPositionMap;
	private int lenOfScalarVector;
	
	/********************
	 * Constructor to set up variables 
	 */
	public scalarCluster()
	{
		//TFdocs=new ArrayList<Integer>();
		Sqq=0;
		Stt=0;
		Sqt=0;
		this.queryVector=new double[26000];
		this.newTermVector=new double[26000];
		this.scalarVector=new double[360000];
		idf_val=new HashMap<String,Double>();
		termPositionMap=new HashMap<Integer,String>();
	}
	
	/***************
	 * Function to get the top k pages using TFIDF Similarity
	 * @throws Exception
	 */
	public void getTFdocs() throws Exception
	{
		System.out.println("IN TF IDF");
		SearchFiles TFobj=new SearchFiles();
		TFobj.geneateMagnitudeOfDoc();
		//TFobj.tf_idf_search(query);
		//this.TFdocs=new ArrayList<Integer>(TFobj.getTopDocOfTFIDF());
		this.idf_val=TFobj.getIdfMap();
		System.out.println("OUT TF IDF");
	}

	/*********
	 * Function to compute the clusters
	 * @throws IOException
	 */
	public void ComputeAssociationCluster() throws IOException
	{
		try {
			Set<String> IDFkeyset=this.idf_val.keySet();
			int pos=0;
			String Exception="?";
			IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
			for(String word: IDFkeyset)
			{
				if(StringUtils.isNumeric(word) || word.length()<2 || word.startsWith(Exception) || word.contains(Exception))
				{				
					//pos++;
					continue;
				}			
				Term term = new Term("contents", word);
				TermDocs tdocs = r.termDocs(term);
				clearArray();
				this.termPositionMap.put(pos, term.text());
				while(tdocs.next())
				{
					int doc=tdocs.doc();
					this.newTermVector[doc]=tdocs.freq()*idf_val.get(word);
			//	this.newTermVector[doc]=tdocs.freq();
					}
				this.Stt=getDotProduct(newTermVector,newTermVector);
				this.Sqt=getDotProduct(queryVector, newTermVector);
				double scalarVal=this.Sqt/(this.Sqq+this.Stt-this.Sqt);
				this.scalarVector[pos++]=scalarVal;
				lenOfScalarVector=pos;
				//System.out.println("Position Number "+ pos + "Word is " +word);
			}
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*******
	 * Function to clear the array since new term vector has to be set to 0's after every comparison with query term vector
	 */
	public void clearArray()
	{
		for(int i=0;i<26000;i++)
			this.newTermVector[i]=0;		
	}
	/**********
	 * Function to find the Second corelated word
	 * @param queryWord
	 * @return
	 */
	public String  getSecondHighestTerm(String queryWord)
	{
		double tempVar=0;
		int position=0;
		System.out.println("Query word is "+ queryWord);
		for(int i=0;i<lenOfScalarVector;i++)
		{
			String term=termPositionMap.get(i);
		//	System.out.println("Term is : " + term);
			if(term.equalsIgnoreCase(queryWord))
				continue;
			else
			{
				
				if(this.scalarVector[i]>tempVar)
				{
					tempVar=this.scalarVector[i];
					position=i;
					System.out.println("Temp Var Value is "+ tempVar + " Position is "+ position + " And term here is : "+ this.termPositionMap.get(position));
				}
					
			}
		}
		
		return this.termPositionMap.get(position);
	}
	
	/************
	 * Function to compute the dotProduct of two vectors
	 * @param oneVector
	 * @param secondVector
	 * @return
	 */
	public double getDotProduct(double[] oneVector, double[] secondVector)
	{
		double tempSum=0;
		//double oneNorm=0;
		//double secondNorm=0;
				
		for(int i=0;i<26000;i++)
		{
			tempSum+=oneVector[i]*secondVector[i];
			//oneNorm+=Math.pow(oneVector[i], 2);
			//secondNorm+=Math.pow(secondVector[i], 2);
		}
		return tempSum;//(Math.sqrt(oneNorm)*Math.sqrt(secondNorm));
	}
	
	/************
	 * Function to set the query vector as intialization process
	 * @param query
	 * @throws Exception
	 * @throws IOException
	 */
	public void setQueryVector(String query) throws Exception, IOException
	{
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		
		Term term = new Term("contents", query);
		TermDocs tdocs = r.termDocs(term);
		while(tdocs.next())
		{
		//	this.queryVector[tdocs.doc()]=tdocs.freq();
			this.queryVector[tdocs.doc()]=tdocs.freq()*this.idf_val.get(query);
		}
		
		this.Sqq=getDotProduct(queryVector,queryVector);
		
	}
	/********
	 * Driver Program
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		scalarCluster tempObj=new scalarCluster();
		tempObj.getTFdocs();
		String str ="";
		System.out.print("query> ");
		Scanner sc = new Scanner(System.in);
		while(!(str = sc.nextLine()).equals("quit"))
		{
		tempObj.setQueryVector(str);
		tempObj.ComputeAssociationCluster();
		System.out.println("Nearest Term is:" + tempObj.getSecondHighestTerm(str));
		}
		
	}
	
}
