package hbl.jag.tri.lib;

/**
 * simple structure class to return nearest neighbor information
 * @author jag
 *
 */
public class TriNeighbor {


	public int neighbor = -1;
	public double separation = -1;
	
	TriNeighbor(int index,double dist)
	{ 
		neighbor=index; 
		separation=dist;
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
