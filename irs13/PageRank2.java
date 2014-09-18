package edu.asu.irs13;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
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
public class PageRank2 {
	
	private DoubleMatrix2D AdjacencyMatrix;
	//private double[] tempMvector;
	List<Integer> pageRank;
	private double[] rankVector;
	private double C;
	private double K;
	private double Z;
	private double resetValue;
	final private int  totalDocs=25054;
	double size=25054;
	//HashMap<Character, Integer> xx=new HashMap<Character, Integer>();
	public void setAdjcacencyMat()
	{
		LinkAnalysis.numDocs = 25054;
		LinkAnalysis link=new LinkAnalysis();
		setResetVector(1);
		
		AdjacencyMatrix=DoubleFactory2D.sparse.make(totalDocs,totalDocs);
		System.out.println("in SetAdjacency function");
		try {
			for(int doc=0;doc<totalDocs;doc++)
			{
				for(Integer inLink : link.getLinks(doc))
				{
					AdjacencyMatrix.setQuick(doc, inLink, 1);
				}
				for(Integer citation : link.getCitations(doc))
				{
					AdjacencyMatrix.setQuick(citation, doc, 1);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void setResetVector(double initResetVal)
	{
		this.rankVector=new double[totalDocs];
		Arrays.fill(this.rankVector, initResetVal);
	}
	public void getTransponse() // to calculate the Transpose Matrix
	{
		System.out.println("in getTranspose function");
			for(int row=0; row< totalDocs; row++){
				for (int col=0; col<totalDocs ;col++)
				{
					double temp=AdjacencyMatrix.getQuick(row, col);
					double temp2=AdjacencyMatrix.getQuick(col, row);
					AdjacencyMatrix.setQuick(col, row, temp);
					AdjacencyMatrix.setQuick(row, col, temp2);
				}
			}
			System.out.println(" getTranspose function Done");
		
	}
	public void setMmatrix()
	{
		LinkAnalysis.numDocs = 25054;
		LinkAnalysis link=new LinkAnalysis();
		for(int row=0; row<totalDocs;row++)
		{
			if(link.getLinks(row).length==0)
			{
				for(int k=0;k<totalDocs;k++)
				{
					AdjacencyMatrix.setQuick(k, row, this.Z*this.C+this.resetValue);
					
				}
			}
				if(link.getLinks(row).length>0)//to put the values in columns that will sum up to 1, making it stochostic column matrix
				{
					int sum=link.getLinks(row).length;
				for(int k=0;k<totalDocs;k++)
				{
					if(AdjacencyMatrix.getQuick(k, row)==1)
						AdjacencyMatrix.setQuick(k, row, (1/sum)*this.C+this.resetValue);
					}
			}
			
		}
		System.out.println("Set Mmatrix is done");
	}
	public void setKZresetVector(double cVal)
	{
		this.C=cVal;
		this.K=(double)1/totalDocs;
		this.Z=this.K;
		this.resetValue=(1-this.C) * this.K;
		
	}
	public void iterate() throws IOException
	{
		int powerIterLimit=50; 
		int numberOfIteration=0;
		double[] tempRankVector=new double[totalDocs];
		//tempMvector=new double[totalDocs];
		for(int i=0; i<powerIterLimit;i++)
		{
			tempRankVector=rankVector.clone();
			numberOfIteration=i;
					
			for(int row=0; row<totalDocs;row++)
			{
					double tempSum=0;
					for(int col=0;col<totalDocs;col++)
					{
					if(this.AdjacencyMatrix.getQuick(row, col)	==0)
						tempSum+=this.resetValue	*  rankVector[col];
					else 
						tempSum+=this.AdjacencyMatrix.getQuick(row, col)*this.rankVector[col];
					}
					System.out.println("Value of tempSum is " + tempSum + " for row " + row);
					this.rankVector[row]=tempSum;
			}
			if(!MatrixOperations.diff(tempRankVector, this.rankVector, totalDocs))
			{
				break;
			}
			
		}
		System.out.println("Converged  in :  " + numberOfIteration);

		
	}
	public void normalize() throws IOException
	{
		double min = rankVector[0];
		double max = rankVector[0];
	
		for(int index = 1;index < totalDocs; index++)
		{
			if(min > rankVector[index])
				min = rankVector[index];
			
			if(max < rankVector[index])
				max = rankVector[index];
		}		
		double d = max - min;
		for(int index = 0;index < totalDocs; index++)
		{
			rankVector[index] = (rankVector[index] - min)/d;
			//rowString.append(resetVector[index]).append(" ");
			System.out.println("Index is "+ index + "rankVector value is :"+ rankVector[index]);
		}		
	
	}


	public static void main(String[] args) throws Throwable
	{
		
			PageRank2 temp=new PageRank2();
			temp.setAdjcacencyMat();
			temp.getTransponse();
			temp.setKZresetVector(0.8);
			temp.setMmatrix();
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
					System.out.println("The DOC Id is: "+ docId + "  TF value is : "+ TFval + " Page Value is : " + pageVal);
					double compVal= 0.4 * pageVal + 0.6 *TFval; // w*pageRankValue + (1-w) TF similarity
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
			


