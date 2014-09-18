package edu.asu.irs13;

/***********
 * Program to sort the similarity values for corresponding documents
 * @author vishalruhela
 *
 */

public class MapHelper implements Comparable<MapHelper>{
	public double val;
	public int id;
	
	public MapHelper(double v, int d)
	{
		val = v;
		id = d;
	}

	@Override
	public int compareTo(MapHelper arg0) {
		// TODO Auto-generated method stub
		MapHelper value2 = (MapHelper)arg0;
		return Double.compare(value2.val, val);
	}
}
