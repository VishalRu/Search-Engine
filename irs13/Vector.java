package edu.asu.irs13;

public class Vector {
	private double[] TFIDFvector;
	private double centriod;
	private double magnitude;
	
	public double[] getTFIDFvector() {
		return TFIDFvector;
	}
	public void setTFIDFvector(double[] tFIDFvector) {
		TFIDFvector = tFIDFvector;
	}
	public void setTFIDFvector(int index, double val) {
		TFIDFvector[index] = val;
	}
	public Vector() {
		this.TFIDFvector=new double[400000];
		this.centriod=0;
		this.magnitude=0;
	}
	public double getCentriod() {
		return centriod;
	}
	public void setCentriod(double centriod) {
		this.centriod = centriod;
	}
	
	

}
