package edu.asu.irs13;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
//This pagerank will calculate query specific pagerank 
public class PageRank
{
	private ArrayList<Integer> Baseset;
	private double[][] AdjacencyMatrix;
	private double[][] TransposeMatrix;
	private double[][] mMatrix;
	List<Integer> pageRank;
	private double[] resetVector;
	private HashMap<Integer, Integer> docMaping=new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> docInverseMaping=new HashMap<Integer, Integer>();
	private int numberUniqueDocs; 	
	private double C;
	private double K;
	private double Z;
	private double resetValue;
	 
	public void generateBaseSet(String query)
	{
		try 
			{
			ArrayList<Integer> temp=new ArrayList<Integer>(TF_IDF.tf_idf_search(query));	
			this.Baseset=new ArrayList<Integer>(temp);
			System.out.println("Size of Baseset is: "+ Baseset.size());
			} 
		catch (Exception e)
			{
				e.printStackTrace();
			}
		int n=numberOfUniqueDocs();
		numberUniqueDocs=n;
	}
	public void setResetVector(double initResetVal, int len)
	{
		this.resetVector=new double[len];
		Arrays.fill(this.resetVector, initResetVal);
	}
	public int  numberOfUniqueDocs()
	{
		int counter=0;int counter1=0;
		LinkAnalysis.numDocs = 25054;
		LinkAnalysis link=new LinkAnalysis();
		for(Integer docID: Baseset)
		{			 
			
			docMaping.put(docID, counter++);
			docInverseMaping.put(counter1++, docID);
		 }
		for(Integer docID: Baseset)
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
		return counter;
	}
	public void setAdjcacencyMat()
	{
		int n=numberUniqueDocs;
		LinkAnalysis.numDocs = 25055;
		LinkAnalysis link=new LinkAnalysis();
		setResetVector(1,n);
		
		this.AdjacencyMatrix=new double[n][n];
		
		for(Integer docID: this.Baseset)
		{			 
			int i=docMaping.get(docID);
			for(Integer links : link.getLinks(docID))
			{
				int j=docMaping.get(links);
				AdjacencyMatrix[i][j]=1;
			}
			for(Integer citation : link.getCitations(docID))
			{
				int k=docMaping.get(citation);
				AdjacencyMatrix[k][i]=1;
			}
			
		 }
		
	}
	public void setMmatrix()
	{
		this.TransposeMatrix=new double[numberUniqueDocs][numberUniqueDocs];
		this.TransposeMatrix=MatrixOperations.getTransponse(AdjacencyMatrix,numberUniqueDocs);
		this.mMatrix=MatrixOperations.getMmatrix(TransposeMatrix, numberUniqueDocs);
	}
	public void setKZresetVector(double cVal)
	{
		this.C=cVal;
		this.K=(double)1/numberUniqueDocs;
		this.Z=this.K;
		this.resetValue=(1-this.C) * this.K;
		
	}
	public void getCproductMplusZ()
	{
		
		for(int row=0; row<numberUniqueDocs;row++)
		{
			int sum=0;
			for(int col=0;col<numberUniqueDocs;col++)
			{
				if(this.mMatrix[col][row]==0)
				{
					sum++;
				}
			}
			if(sum==numberUniqueDocs)
			{
				for(int k=0;k<numberUniqueDocs;k++)
				{
					this.mMatrix[k][row]=this.Z;
				}
			}
		}
		
		this.mMatrix=MatrixOperations.scaleMatrix(this.mMatrix, this.C, numberUniqueDocs);
	}
	public void getFinalMstarMatrix()
	{
		this.mMatrix=MatrixOperations.AddMatrices(this.mMatrix, this.resetValue, numberUniqueDocs);
	}
	public void iterate()
	{
		int powerIterLimit=100;
		double[] tempResetVector=new double[numberUniqueDocs];
		for(int i=0; i<powerIterLimit;i++)
		{
			tempResetVector=MatrixOperations.copyArray(this.resetVector, numberUniqueDocs);
			this.resetVector=MatrixOperations.multiply(this.mMatrix, this.resetVector, numberUniqueDocs);
			if(MatrixOperations.diff(tempResetVector, this.resetVector, numberUniqueDocs))
			{
				break;
			}
			
		}
	}
	public void showTopTenPageRanks()
	{
		List<MapHelper> PageRankVal = new ArrayList<MapHelper>();
		for(int index = 0; index < numberUniqueDocs; index++)
		{
			MapHelper authKV = new MapHelper(resetVector[index],docInverseMaping.get(index));
			PageRankVal.add(authKV);
		}
		Collections.sort(PageRankVal);
		pageRank = new ArrayList<Integer>();
		int i=0;
		for(MapHelper authKV : PageRankVal)
		{
			pageRank.add(authKV.id);
			System.out.println("Document Number is: " +authKV.id+ " with PageRank "+ authKV.val );
			if(i++>20) break;
		}
	}
	public static void main(String[] args)
	{
		Scanner sc = new Scanner(System.in);
		String str ="";
		System.out.print("query> ");
		
		while(!(str = sc.nextLine()).equals("quit"))
		{	
			PageRank temp=new PageRank();
			temp.generateBaseSet(str);
			temp.setAdjcacencyMat();
			temp.setMmatrix();
			temp.setKZresetVector(0.85);
			temp.getCproductMplusZ();
			temp.getFinalMstarMatrix();
			temp.iterate();
			temp.showTopTenPageRanks();
					
			System.out.print("query> ");
		}
		
	}
}



