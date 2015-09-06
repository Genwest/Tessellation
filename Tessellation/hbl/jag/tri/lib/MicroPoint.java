package hbl.jag.tri.lib;

import java.io.Serializable;

public class MicroPoint implements  Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 Public class representing a geophysical point with
	 right handed coordinates in micro-degrees
	 */
	
	public long x;
	public long y;
	
	public MicroPoint(long longIn, long latIn){
		x = longIn; y = latIn;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
