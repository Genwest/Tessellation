package hbl.jag.tri.lib;

import java.io.Serializable;

/**
 This a simple public Class representing a Edge Object. An
 Edge contains p1 (a possible reference to the DagData
 vertex of the start x,y pair - may be -1 indicating no
 overall DagData support), p2 (a possible reference to the 
 DagData vertex of the end x,y pair - may be -1 indicating no
 overall DagData support),x1 (x value of the start point), y1 (y
 value of the start point), x2 (x value of the end point), and
 y2 (y value of the end point).
 @author jag
 */
public class Edge implements Serializable {

	private static final long serialVersionUID = 1L;
	public int p1; //lower ordered point index
	public int p2; //higher ordered point index
	public long x1; //lower point
	public long y1;
	public long x2; //higher point
	public long y2;
	public int v1=-1,v2=-1,v3=-1;
	
	
	/**
	 Constructor builds object out of components
	 @param p10 DagData index to start point
	 @param p20 DagData index to end point i.e. edge
	 	is based on a triangle edge
	 @param x10 x value of start point
	 @param y10 y value of start point
	 @param x20 x value of end point
	 @param y20 y value of end point
	 */
	
	public Edge(int p10,int p20,long x10,long y10,long x20,long y20){		
		p1=p10;
		p2=p20;
		x1=x10;
		y1=y10;
		x2=x20;
		y2=y20;
	}
	
	/**
	 Alternate constructor builds object out of components
	 when no underlying DagData structure is assumed.
	 @param x10 x value of start point
	 @param y10 y value of start point
	 @param x20 x value of end point
	 @param y20 y value of end point
	 */
	public Edge(long x10,long y10,long x20,long y20){		
		p1=-1;
		p2=-1;
		x1=x10;
		y1=y10;
		x2=x20;
		y2=y20;
	}
	
	/**
	 Alternate constructor builds object and setting the
	 vertex data for the triangle that contains the edge
	 @param x10 x value of start point
	 @param y10 y value of start point
	 @param x20 x value of end point
	 @param y20 y value of end point
	 */
	public Edge(long x10,long y10,long x20,long y20,int v1In,int v2In,int v3In){		
		p1=-1;
		p2=-1;
		x1=x10;
		y1=y10;
		x2=x20;
		y2=y20;
		v1=v1In;
		v2=v2In;
		v3=v3In;
	}
	
	/**
	 Utility to present Arc class variables as String
	 */
	
	public String toString(){
		return "start ("+x1+","+y1+") end ("+x2+","+y2+") verts ("
				+v1+","+v2+","+v3+")";
	}
}
