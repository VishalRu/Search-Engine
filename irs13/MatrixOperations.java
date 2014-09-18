package edu.asu.irs13;
/*********************************/
/*Methods that will help calculating Auth-Hub and Pagerank values
 * Functions in this class are being used while computing Auth-Hub and Pagerank Classes
 */
public class MatrixOperations
{	
	public static double[][] getTransponse(double[][] matrix,int k) // to calculate the Transpose Matrix
	{
		double[][] temp=new double[k][k];
		for(int row=0; row< k; row++){
			for (int col=0; col<k ;col++){
					swap(matrix,temp,row,col);
			}
		}
		return temp;
	}
	public static double[][] copyMatrix(double[][] matrix,int k) // to copy the  Matrix
	{
		double[][] temp=new double[k][k];
		for(int row=0; row< k; row++){
			for (int col=0; col<k ;col++){
				temp[row][col]=matrix[row][col];
			}
		}
		return temp;
	}
	
	public static double[] copyArray(double[] arr,int k) // to calculate the Transpose Matrix
	{
		double[] temp=new double[k];
		for(int row=0; row< k; row++){
			
				temp[row]=arr[row];
			
		}
		return temp;
	}
		
	public static void swap(double[][] matrix, double[][] x, int row, int col ) //Swap function to be used while calculating Transpose Matrix
	{
		x[col][row]=matrix[row][col];
	}
	public static double[] multiply(double[][] adjacentMatrix, double[] tempMatrix, int n)// To Multiply the Matrices in O(n^2)
	{
		double[] temp=new double[n];
		for(int row=0; row<n;row++)
		{
			double tempSum=0;
			for(int col=0;col<n;col++)
				tempSum+=adjacentMatrix[row][col]*tempMatrix[col];
			temp[row]=tempSum;
		}
		return temp;
			
	}
	public static double[] multiply(double[] tempVector, double value, int n) //To scale a vector
	{ //overloaded multiply function
		double[] newVector=new double[n];
		for(int col=0; col<n;col++)
		{
			newVector[col]=tempVector[col]*value;
		}
		return newVector;
	}
	
	public  static double[][]  AddMatrices(double[][] tempMat, double value, int n)
	{
		double[][] newVector=new double[n][n];
		for(int row=0; row<n;row++)
		{
			for(int col=0;col<n;col++)
			{
				newVector[row][col]=tempMat[row][col]+value;
			}
			
		}
		return newVector;
	}
	
	public static double[][] scaleMatrix(double[][] tempVector, double value, int n) //To scale a matrix
	{ 
		double[][] newVector=new double[n][n];
		for(int row=0; row<n;row++)
		{
			for(int col=0;col<n;col++)
			{
				newVector[row][col]=tempVector[row][col]*value;
			}
			
		}
		return newVector;
	}
	public static double squareRootofVector(double[] tempVector, int n) // To find the norm of Vector
	{
		double tempValue=0;
		for(int i=0; i<n;i++)
		{
			tempValue+=Math.pow(tempVector[i], 2);
		}
		return (double)Math.sqrt(tempValue);
	}
	
	public static double[][] getMmatrix(double[][] mat, int n)
	{
		double[][] temp=new double[n][n];
		for(int row=0; row<n;row++)
		{
			double sum=0;
			for(int col=0;col<n;col++)
			{
				if(mat[col][row]==1)
				{
					sum++;
					temp[col][row]=1;
				}
			}
			if(sum>1)
			{
				for(int k=0;k<n;k++)
				{
					if(mat[k][row]==1)
						temp[k][row]=1/sum;
				}
			}
		}
		return temp;
	}
	public static boolean diff(double[] oldVector, double[] newVector, int n) //To check if old vector and new vector are same
	{
		boolean flag=true;
	
			for(int i=0;i<n;i++){
			//System.out.println("Value of oldVector is "+ oldVector[i] + " New Vector " + newVector[i] + "" + (Math.abs(oldVector[i]-newVector[i])) );
			if(Math.abs(oldVector[i]-newVector[i])<0.0001)
				{
				flag=false;
				continue;
			}
			else{
				flag=true;
				break;
			}
				
		}
       return flag;		
	}
}
	

