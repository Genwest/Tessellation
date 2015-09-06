package hbl.jag.tri.sam;

import hbl.jag.tri.lib.CoordTransform;
import hbl.jag.tri.lib.DagData;

/**
 * Simple example of a user built driving program that uses
 * the triangle.lib.  It initializes a DagData object and
 * checks to see that it is non-null then prints out a
 * summary string.  All followed by the standard "Greeting"
 * @author jag
 *
 */
public class HelloTess {
	
	public static void main(String[] args){
		// define some points
		long[] x = new long[100];
		long[] y = new long[100];
		for(int i=0;i<100;i++){
			x[i] = (long)(1e6*Math.random());
			y[i] = (long)(1e6*Math.random());
		}
		// make call to library
		DagData dd = new DagData(x,y,CoordTransform.CARTESIAN);
		// echo output to show it works
		if(dd!=null){
			System.out.println("dd is not null");
			System.out.println(dd);
		}
		System.out.println("Hello Tess");
	}

}
