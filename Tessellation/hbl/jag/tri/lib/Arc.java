package hbl.jag.tri.lib;

import java.io.Serializable;

/**
 This class represents the arc structure used in DagData
 routines described described by D. E. Knuth in the 
 Lecture Notes in Computer Science  <em>Axioms and Hulls</em> 
 published by Springer-Verlag.  An Arc has a <em>vert</em> (index to 
 the vertex that starts the edge), <em>next</em> (index to the next
 Arc in a ccw direction around the triangle) and <em>inst</em> (an
 index to the terminal instruction node that points to
 the triangle)
 @author jag
 */

public class Arc implements Serializable{


	private static final long serialVersionUID = 1L; 
	protected int vert; // vertex index of the arc
	protected int next; // next arc in a CCW direction around the triangle
	protected int inst; // terminal instruction node that points to tri
	
	/**
	 Default constructor
	 */
	public Arc(){
		vert=0;
		next=0;
		inst=0;
	}
	/**
	 Alternate constructor with parameter input options
	 */
	public Arc(int vert0,int next0,int inst0){
		vert = vert0;
		next = next0;
		inst = inst0;
	}
	
	/**
	 Utility to present Arc class variables as String
	 */
	public String toString(){
		return "vert= "+vert+" next= "+next+" inst= "+inst;
	}

	/**
	 Getter to access class field as read only
	 @return Arc.vert
	 */
	public int getVert() {
		return vert;
	}
	
	/**
	 Getter to access class field as read only
	 @return Arc.next
	 */
	public int getNext() {
		return next;
	}
	
	/**
	 Getter to access class field as read only
	 @return Arc.inst
	 */
	public int getInst() {
		return inst;
	}
	
}