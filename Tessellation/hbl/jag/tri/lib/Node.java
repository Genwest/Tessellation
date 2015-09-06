package hbl.jag.tri.lib;

import java.io.Serializable;

	/**
	This class represents the node structure used in DagData
	 routines described described by D. E. Knuth in the 
	 Lecture Notes in Computer Science  <em>Axioms and Hulls</em> 
	 published by Springer-Verlag. A Node has p (an index to the
	 start vertex of the instruction's line), q (an index to the 
	 end point of the instruction's line), ln (an index to the left
	 node branch of the instruction tree), rn (an index to the right
	 node branch of the instruction tree), area (the area of the 
	 triangle if the instruction is a terminal node or <0 if the 
	 terminal node triangle is outside the bEdge boundary.
	 @author jag
	*/
public class Node implements Serializable{

	private static final long serialVersionUID = 1L;	
	// class variables
	protected int p; // index to start point of the instruction's line
	protected int q; // index to end point of the instruction's line
	protected int ln; // index to left node branch of instruction tree
	protected int rn; // index to right node branch of instruction tree
	protected double area; // triangle area if <0 outside bEdge boundary
	
	/**
	 Constructor builds Node from fields
	 */
	public Node(int pin,int qin,int lnin,int rnin){
		p=pin;
		q=qin;
		ln=lnin;
		rn=rnin;
		area=1;
	}
	
	/**
	 Utility to present Node class variables as String
	 */
	public String toString(){
		if(p==Incircle.INF)return  "node reference arc "+q+" area "+area;
		return "node from= "+p+" to= "+q+" ln= "+ln+" rn= "+rn;
	}

	/**
	 Getter to access class field as read only
	 @return Node.p
	 */
	public int getP() {
		return p;
	}
	/**
	 Getter to access class field as read only
	 @return Node.Q
	 */
	public int getQ() {
		return q;
	}

	/**
	 Getter to access class field as read only
	 @return Node.ln
	 */
	public int getLn() {
		return ln;
	}

	/**
	 Getter to access class field as read only
	 @return Node.rn
	 */
	public int getRn() {
		return rn;
	}

	/**
	 Getter to access class field as read only
	 @return Node.area
	 */
	public double getArea() {
		return area;
	}

}
