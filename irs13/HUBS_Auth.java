package edu.asu.irs13;
import java.io.File;
import java.util.Date;

import edu.asu.irs13.MatrixOperations;

import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.document.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;
/*************************
 * This program generates Authority and Hubs for a given query
 * This will take query from user and then compute the TF-IDF documents for query, which are the root set.
 * From this root set, we generate the base set and then do power iteration on baseset to find out top Authority and top hubs
 * @author vishalruhela
 *
 */

public class HUBS_Auth 
{
	private ArrayList<Integer> rootSet;
	private ArrayList<Integer> Baseset;
	List<Integer> Auth;
	private double[] Auths;
	List<Integer> Hub;
	private double[] Hubs;
	private double[][] AdjacencyMatrix;
	private HashMap<Integer, Integer> docMaping=new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> docInverseMaping=new HashMap<Integer, Integer>();
	private int numberUniqueDocs; 
	private ArrayList<Integer> topAuthDocs=new ArrayList<Integer>();
	private ArrayList<Integer> topHubDocs=new ArrayList<Integer>();
	
	/*********************
	 * Generating the root set for the query
	 * @param query
	 */
	public void generateBaseSet(String query)
	{
		try 
			{
			Date Checkpoint2 = new Date();
			ArrayList<Integer> temp=new ArrayList<Integer>(TF_IDF.tf_idf_search(query));	
			this.rootSet=new ArrayList<Integer>(temp);
			System.out.println("Size of RootSet is: "+ rootSet.size());
			Date Checkpoint3 = new Date();
			System.out.println("Time taken for base set "+(Checkpoint3.getTime() - Checkpoint2.getTime())+ " Milliseconds");
			} 
		catch (Exception e)
			{
				e.printStackTrace();
			}
		int n=numberOfUniqueDocs();
		numberUniqueDocs=n;
	}
	
	/***********************
	 * Generate the Base set from the root set
	 * @return
	 */
	public int  numberOfUniqueDocs()
	{
		int counter=0;int counter1=0;
		LinkAnalysis.numDocs = 25054;
		LinkAnalysis link=new LinkAnalysis();
		//Baseset=new double[n];
		//Baseset=MatrixOperations.copyArray(rootSet, 10);
		for(Integer docID: rootSet)
		{			 
			
			docMaping.put(docID, counter++);
			docInverseMaping.put(counter1++, docID);
		 }
		for(Integer docID: rootSet)
		{			 
			for(Integer links : link.getLinks(docID))
			{
				if(!docMaping.containsKey(links))
				{
					docMaping.put(links, counter++);
				}
				if(!docInverseMaping.containsValue(links))
				{
					docInverseMaping.put(counter1++, links);
				}
			}
			for(Integer citation : link.getCitations(docID))
			{
				if(!docMaping.containsKey(citation))
				{
					docMaping.put(citation, counter++);
				}
				if(!docInverseMaping.containsValue(citation))
				{
					docInverseMaping.put(counter1++, citation);
				}
			}
			
		 }
		Set<Integer> docKeys=docMaping.keySet();
		this.Baseset=new ArrayList<Integer>();
		for(Integer doc:docKeys){
			Baseset.add(doc);
		}
		return counter;
	}
	
	/**********************
	 * Setting the Initial Authority vector
	 * @param initAuthVal
	 * @param len
	 */
	public void setAuths(double initAuthVal, int len)
	{
		this.Auths=new double[len];
		Arrays.fill(this.Auths, initAuthVal);
	}
	
	/************************
	 * Setting the Auth vector
	 * @param AuthVector
	 */
	public void setAuths(double[] AuthVector)
	{
		this.Auths=AuthVector;		
	}
	/********************
	 * Setting the initial Hub vector
	 */
	public void setHubs(double initHubVal, int len)
	{
		this.Hubs=new double[len];
		Arrays.fill(this.Hubs, initHubVal);
	}
	
	public void setHubs(double[] HubVector )
	{
		this.Hubs=HubVector;		
	}

	/*******************
	 * Setting the adjacancy Matrix 
	 */
	public void setAdjcacencyMat()
	{
		int n=numberUniqueDocs;
		LinkAnalysis.numDocs = 25054;
		float totalNumber=25054;
		LinkAnalysis link=new LinkAnalysis();
		setAuths(1/totalNumber,n);
		setHubs(1/totalNumber,n);
		this.AdjacencyMatrix=new double[n][n];
		
		for(Integer docID: this.Baseset)
		{			 
			int i=docMaping.get(docID);
			for(Integer links : link.getLinks(docID))
			{
				int j=0;
				if(docMaping.containsKey(links))
				{
					 j=docMaping.get(links);
					 AdjacencyMatrix[i][j]=1;
				}	 
			}
			for(Integer citation : link.getCitations(docID))
			{
				int k=-0;
				if(docMaping.containsKey(citation))
				{
				 k=docMaping.get(citation);
				AdjacencyMatrix[k][i]=1;
				}
			}
			
		 }
		
	}
	
	/**************************
	 * Function to find the Authority and Hub; Power Iteration
	 */
	public void findAuthAndHub()
	{
		double[][] tempMat=MatrixOperations.copyMatrix(this.AdjacencyMatrix,numberUniqueDocs);		
		double[][] tempTransMat=MatrixOperations.getTransponse(this.AdjacencyMatrix, numberUniqueDocs);
		double[] tempHub, tempAuth;
		int powerIteraterLimit=150;
		int temp=0;
		for(int iteration=0;iteration<powerIteraterLimit;iteration++)
		{
			tempHub=MatrixOperations.copyArray(Hubs, numberUniqueDocs);
			tempAuth=MatrixOperations.copyArray(Auths, numberUniqueDocs);
			
			Auths=MatrixOperations.multiply(tempTransMat, Hubs, numberUniqueDocs);
			Hubs=MatrixOperations.multiply(tempMat, Auths, numberUniqueDocs);
			
			double SqrtOfAuth=MatrixOperations.squareRootofVector(Auths, numberUniqueDocs);
			double SqrtOfHub=MatrixOperations.squareRootofVector(Hubs, numberUniqueDocs);
			
			Auths=MatrixOperations.multiply(Auths, 1/SqrtOfAuth, numberUniqueDocs);
			Hubs=MatrixOperations.multiply(Hubs, 1/SqrtOfHub, numberUniqueDocs);
			
			
			/*if(!MatrixOperations.diff(Auths, tempAuth, numberUniqueDocs) && !MatrixOperations.diff(Hubs, tempHub, numberUniqueDocs) )
			{
				temp=iteration;
				break;
			}*/
			if(Arrays.equals(Auths, tempAuth) && Arrays.equals(Hubs, tempHub))
			{
				temp=iteration;System.out.println("Iterated in " + iteration);
				break;
			}
				
		}
		System.out.println("Itearted in " + temp);
		
	}

	public void showTopTenAuths()  throws Exception
	{
		List<MapHelper> authsKV = new ArrayList<MapHelper>();
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		for(int index = 0; index < numberUniqueDocs; index++)
		{
			MapHelper authKV = new MapHelper(Auths[index],docInverseMaping.get(index));
			authsKV.add(authKV);
		}
		Collections.sort(authsKV);
		Auth = new ArrayList<Integer>();
		int i=0;
		System.out.println("Top Ten Authorities are....");
		for(MapHelper authKV : authsKV)
		{
			Auth.add(authKV.id);
			System.out.println("Document Number is: " +authKV.id);
			this.topAuthDocs.add(authKV.id);
		//	System.out.println("Document Number is: " +authKV.id + " with auth value "+ authKV.val );
			String d_url = r.document(authKV.id).getFieldable("path").stringValue().replace("%%", "/");
			System.out.println("Url is:" + d_url);
			if(i++>10) break;
		}
				
	}
	
	public void showTopTenHubs()  throws Exception
	{
		List<MapHelper> authsKV = new ArrayList<MapHelper>();
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		for(int index = 0; index < numberUniqueDocs; index++)
		{
			MapHelper authKV = new MapHelper(Hubs[index],docInverseMaping.get(index));
			authsKV.add(authKV); 
		}
		Collections.sort(authsKV);
		int i=0;
		Hub = new ArrayList<Integer>();System.out.println("'     ");
		System.out.println("Top Ten Hubs are....");
		for(MapHelper authKV : authsKV)
		{
			Hub.add(authKV.id);  
			System.out.println("Document Number is: " +authKV.id);
			this.topHubDocs.add(authKV.id);
		//	System.out.println("Document Number is: " +authKV.id + " with Hub value "+ authKV.val );
			String d_url = r.document(authKV.id).getFieldable("path").stringValue().replace("%%", "/");
			System.out.println("Url is:" + d_url);
			if(i++>10) break;
		}
	}
	
	public ArrayList<Integer> getTopAuth()
	{
		return topAuthDocs;
	}
	public ArrayList<Integer> getTopHub()
	{
		return topHubDocs;
	}
	
	/********************
	 * Driver Program 
	 * @param args
	 */
	public static void main(String[] args)
	{
		Scanner sc = new Scanner(System.in);
		String str ="";
		System.out.print("query> ");
		
		while(!(str = sc.nextLine()).equals("quit"))
		{	
			HUBS_Auth temp=new HUBS_Auth();
			Date Checkpoint2 = new Date();
			
			temp.generateBaseSet(str);
			temp.setAdjcacencyMat();
			temp.findAuthAndHub();
			try {
				temp.showTopTenAuths();
				temp.showTopTenHubs();
				//Date Checkpoint3 = new Date();
				//System.out.println("Pre processing time "+(Checkpoint3.getTime() - Checkpoint2.getTime())+ " Milliseconds");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.print("query> ");
		}
		
	}
}
