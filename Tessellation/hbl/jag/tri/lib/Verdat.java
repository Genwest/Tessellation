package hbl.jag.tri.lib;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 This public class represent the verdat structure used in OSSM and
 GNOME.  A verdat object has nver (the number of vertices),nseg
 (the number of boundary segments) x (an array of x coordinate
 data), y (a array of y coordinate data), and bseg (an array 
 containing the indices of each boundary segments end points)
 All boundary segment points are listed consecutively in ccw
 order at the beginning of the array data.  Points in the arrays
 with indices beyond the boundary segments are interior to the 
 domain and in no particular order.
 @author jag
 */

public class Verdat {

	public int  nver=0;
	public int nseg=0;
	public long[] x;
	public long[] y;
	public int[] bseg;
	
	
	/**
	 Constructor builds an empty Verdat object. Data
	 added to public fields by calling algorithms
	 @param nVer number of vertices 
	 @param nSeg number of boundary segments
	 */
	Verdat(int nVer,int nSeg){
		nver = nVer;
		nseg = nSeg;
		x = new long[nver];
		y = new long[nver];
		bseg = new int[nseg];
	}
	
	/**
	 Utility method that returns true if the target
	 point specified as x0,y0 is globally interior to
	 the boundary of the domain, otherwise false 
	 @param x0 x location of test point
	 @param y0 y location of test point
	 @return boolean (true) if test point is inside
	 	or the global boundary, otherwise (false)
	 */
	public boolean pointInside(long x0,long y0){
		if(bseg==null)return true;
		
		// local variables
		ArrayList<Edge> bEdges= new ArrayList<Edge>();  // bEdges is an array 
		// of "Edge" segment objects that  topologically define the 
		// global boundaries of the domain
		long xmax = Long.MIN_VALUE; long xmin = Long.MAX_VALUE;
		long ymax = Long.MIN_VALUE; long ymin = Long.MAX_VALUE;
		
		// find bounding box		
		for(int i=0;i<nver;i++){
			if(x[i]>xmax)xmax=x[i];
			if(x[i]<xmin)xmin=x[i];
			if(y[i]>ymax)ymax=y[i];
			if(y[i]<ymin)ymin=y[i];
		}
		
		/*
		System.out.println("bound in verdat");
		System.out.println(xmax+"  "+xmin+"  "+ymax+"  "+ymin);
		*/
		
		// set edges
		int j=0;
		int start=0;
		for(int i=0;i<=bseg[bseg.length-1];i++){
			int p1=i;int p2=p1+1;
			if(p1==bseg[j]){
				p2=start;
				start=bseg[j]+1;
				j++;
			}
			int lowV= Math.min(p1,p2);
			int highV= Math.max(p1,p2);
			long x10= x[lowV]; long y10= y[lowV];
			long x20= x[highV]; long y20= y[highV];
			bEdges.add(new Edge(lowV,highV,x10,y10,x20,y20));
		}
		
		// set outside point
		int cross= 0;
		long x1=xmax+1000;long y1=ymax+1000;
		// run edge check					
		for(Edge e:bEdges){
			long p1x=e.x1; long p1y=e.y1;
			long p2x=e.x2; long p2y=e.y2;
			boolean b1= Incircle.CCW(p1x,p1y,p2x,p2y,x0,y0,true);
			boolean b2= Incircle.CCW(p1x,p1y,p2x,p2y,x1,y1,true);
			boolean b3= Incircle.CCW(x0,y0,x1,y1,p1x,p1y,true);
			boolean b4= Incircle.CCW(x0,y0,x1,y1,p2x,p2y,true);
			if((b1!=b2)&&(b3!=b4)){
				cross++;
				
				/*
				if(chkoutput){
					System.out.println("edge-seg-crossed "+e.toString());
				}
				*/
			}
		}
		if((cross%2)==1){return true;}else{return false;}			
	}
	
	/**
	 Utility method to write ASCII data file representing the
	 Verdat data
	 @param fileOut a name of the output file where the verdat
	 	data will be written.
	 */
	void writeVer(String fileOut){
		FileWriter file=null;
		try {
			file = new FileWriter(fileOut);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter out = new PrintWriter(file);
		StringBuffer strB = new StringBuffer();
		for(int i=0;i<nver;i++){
			strB.append(i+","+x[i]+","+y[i]+",0\n");
		}
		strB.append("0,0,0,0\n");
		strB.append(nseg+"\n");
		for(int i=0;i<nseg;i++){
			strB.append(bseg[i]+"\n");
		}
		out.print(strB.toString());
		out.close();		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}