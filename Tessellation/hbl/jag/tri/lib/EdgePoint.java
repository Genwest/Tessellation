package hbl.jag.tri.lib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 A simple public Class representing a list of vertices's that
 implements the "Comparable" interface allowing for easy sort
 and find options. An EdgePoint has an index (which identifies
 its original position in a list of input points), x (the value
 of its x component), y (the value of its y component) and 
 orientation (a boolean flag true if it represents the start of
 and Edge and false if it represents the end of an Edge)
 @author jag
 *
 */
public class EdgePoint implements Comparable<EdgePoint>, Serializable{

	private static final long serialVersionUID = 1L;
	public ArrayList<Edge> parentContour;
	public long index; // original index from parentContour
	public long x; // longitude coordinates in micro-degrees
	public long y; // latitude coordinate in micro degrees
	public boolean orientation; // true = start, false = end of Edge
	// static variables used in recursion
	static public long diff=-1;
	
	/**
	 Constructor built from component data
	 * @param xIn
	 * @param yIn
	 * @param indexIn
	 * @param endIn
	 */
	public EdgePoint(long xIn,long yIn,int indexIn,int endIn){
		index = (long)indexIn;
		if((endIn!=1)&&(endIn!=2)){
			System.out.println("error is EdgePoint constructor");
			System.exit(-1);
		}
		if(endIn==1){
			orientation = true;
		}else{
			orientation = false;
		}
		x = xIn; y = yIn;
	}
	
	/**
	 Aternate constructor built from component of a contour 
	 line (a ArrayList of Edge objects)
	 @param contour list of Edge objects
	 @param indexIn index into the contour list
	 @param endIn 1 (start) or 2 (end) of Edge object	 
	 */
	public EdgePoint(ArrayList<Edge> contour,int indexIn,int endIn){
		Edge eg = contour.get(indexIn);
		index = (long)indexIn;
		if(endIn!=1){
			if(endIn!=2){
				System.out.println("error is EdgePoint constructor");
				System.exit(-1);
			}
		}
		if(endIn==1){
			orientation = true;
		}else{
			orientation = false;
		}
		if(orientation){
			x = eg.x1; y = eg.y1;
		}else{
			x = eg.x2; y = eg.y2;
		}		
	}
	
	/**
	 Required method to support the Comparable Interface
	 which orders first on x value then on y value.
	 */
	public int compareTo(EdgePoint edgeIn) {
		if(x<edgeIn.x)return -1;
		if(x>edgeIn.x)return 1;
		if(x==edgeIn.x){
			if(y<edgeIn.y)return -1;
			if(y>edgeIn.y)return 1;
		}
		return 0;
	}
	
	static public int find(ArrayList<EdgePoint> points,EdgePoint match,int r1,int r2){
		int target = (r1+r2)/2;
		// found the match
		if(points.get(target).compareTo(match)==0){
			diff = 0;
			return target;
		}
		// list has gone to length one
		if((r2-r1)<=1){
			long dx = points.get(target).x-match.x;
			long dy = points.get(target).y-match.y;
			diff = dx*dx+dy*dy;
			return target;						
		}
		if(points.get(target).compareTo(match)>0){
			target = find(points,match,r1,target);
			return target;
		}
		if(points.get(target).compareTo(match)<0){
			target = find(points,match,target,r2);
			return target;
		}
		return -1;		
	}
	
	//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	public static ArrayList<Polygon> EdgePtLstToPolyLst(DagData datIn,ArrayList<Edge> contour,double val){
		// create diff array and initialize value
		ArrayList<Long> diff = new ArrayList<Long>();
		for(int i=0;i<contour.size();i++){diff.add(new Long(0));}
		
		// print out initial list of Edges
		/*
		System.out.println("\nEDGE-data starting");
		for(int i=0;i<contour.size();i++){
			Edge Eg = contour.get(i);
			long length = (Eg.x1-Eg.x2)*(Eg.x1-Eg.x2)+(Eg.y1-Eg.y2)*(Eg.y1-Eg.y2);
			System.out.println(i+" "+Eg.toString()+" length "+length);
		}
		*/
		
		// reorder list so that each Edge is followed by an edge whose
		// first point is as close as  possible to the previous edge's
		// second point		
		for(int i=0;i<contour.size()-1;i++){
			Edge Eg = contour.get(i);
			long diff0 = Long.MAX_VALUE;
			int hit = -1;
			for(int j=i+1;j<contour.size();j++){
				Edge testEg = contour.get(j);
				long dx = Eg.x2-testEg.x1;
				long dy = Eg.y2-testEg.y1;
				long delta = dx*dx+dy*dy;
				if(delta<diff0){
					diff0 = delta; 
					hit = j;
				}
			}
			Edge temp = contour.get(i+1);
			diff.set(i+1,diff0);
			contour.set(i+1,contour.get(hit));
			contour.set(hit,temp);		
		}
		/*
		// print out initial list of Edges
		System.out.println("\nEDGE-data SORTED");
		for(int i=0;i<contour.size();i++){
			Edge Eg = contour.get(i);
			long length = (Eg.x1-Eg.x2)*(Eg.x1-Eg.x2)+(Eg.y1-Eg.y2)*(Eg.y1-Eg.y2);
			if(length==0)continue;
			System.out.println(i+" "+Eg.toString()+" length "+length+" diff "+diff.get(i));
		}
		*/
		
		// turn list of Edges into contours
		ArrayList<Polygon> polys = new ArrayList<Polygon>();
		Polygon poly = new Polygon(datIn,val);
		for(int i=0;i<contour.size();i++){	
			Edge Eg = contour.get(i);
			long length = (Eg.x1-Eg.x2)*(Eg.x1-Eg.x2)+(Eg.y1-Eg.y2)*(Eg.y1-Eg.y2);
			if(length==0)continue;
			//MicroPoint firstPt = new MicroPoint(Eg.x1,Eg.y1);
			// add point to polygon
			if(diff.get(i)<2){ 
				MicroPoint pt = new MicroPoint(Eg.x1,Eg.y1);
				poly.addPoint(pt);
			}
			// close polygon if multiple polygons are present
			if(diff.get(i)>2){
				poly.closePoly(datIn.coordTrans.DiffArea);
				polys.add(poly);
				poly = new Polygon(datIn,val);
			}
		}
		// close final polygon
		poly.closePoly(datIn.coordTrans.DiffArea);
		polys.add(poly);
		return polys;
	}
	//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	
	public static ArrayList<Polygon> EdgePtLst2PolyLst(double diffArea,ArrayList<Edge> contour,double val){
		// create the polyContour list of polygons
		ArrayList<Polygon> polycontours = new ArrayList<Polygon>();
		
		// load start of Edges into EdgePoint array
		ArrayList<EdgePoint> firstPts = new ArrayList<EdgePoint>();
		for(int i=0;i<contour.size();i++){
			Long ptx = contour.get(i).x1;
			Long pty = contour.get(i).y1;
			firstPts.add(new EdgePoint(ptx,pty,i,1));
		}
		// sort the EdgePoint array
		Collections.sort(firstPts);
		
		System.out.println("\nEDGE-data enumerating over sorted first points");
		for(int i=0;i<firstPts.size();i++){
			EdgePoint eg = firstPts.get(i);
			Edge Eg = contour.get((int)eg.index);
			long length = (Eg.x1-Eg.x2)*(Eg.x1-Eg.x2)+(Eg.y1-Eg.y2)*(Eg.y1-Eg.y2);
			if(length==0)continue;
			System.out.println(i+" edgeInontour "+eg.index+" "+Eg.toString()+" length "+length);
		}
		
		
		// process firstPts	
		boolean foundPositive = true;
		int indInfirstPts = -1;
		while(foundPositive){
			foundPositive = false;
			// find the first point that has not be marked neg
			for(int i=0;i<firstPts.size();i++){
				if(firstPts.get(i).index>0){
					indInfirstPts = i;
					foundPositive = true;
					break;} // exit for loop
			}
			// if no points are positive we are done with list
			if(!foundPositive){
				System.out.println("finished marking the list");
				break; // exit while loop
			}
			// found starting point for this contour segment
			int segmentbegin = indInfirstPts;
			EdgePoint start = firstPts.get(segmentbegin);
			
			System.out.println("beginning of segment "+segmentbegin+"  index in EdgeLst "+start.index);
			
			// get data for the other end of start point
			int indInEdgeLst = (int)start.index;
			long xpt = contour.get(indInEdgeLst).x2;
			long ypt = contour.get(indInEdgeLst).y2;
			// set initial end point
			EdgePoint end = new EdgePoint(xpt,ypt,indInEdgeLst,2);
			
			
			// set index Edge reference in first point to neg
			firstPts.get(indInfirstPts).index*= -1;
			
			// create the polygon object poly and add start point
			Polygon poly = new Polygon();
			poly.addPoint(new MicroPoint(start.x,start.y));
			
			// follow the trail of the end point
			while(start!=end){
				int nearestIndex = ImportPolygons.find(firstPts,end,0,firstPts.size());					
				diff = ImportPolygons.diff;
				EdgePoint foundLink = firstPts.get(nearestIndex);
				// add found point to poly
				poly.addPoint(new MicroPoint(foundLink.x,foundLink.y));
				int edgeIndex = (int)foundLink.index;
				
				System.out.println("edgeInd "+indInEdgeLst+" nearest "+nearestIndex+" => "+edgeIndex+"  diff^2  "+diff);
				indInEdgeLst = nearestIndex;
				
				if(foundLink.index<0){
					// close poly
					poly.closePoly(diffArea);
					break;
				}
									
				// get new value for end of edge and cycle
				Edge edgefound = contour.get((int)foundLink.index);
				xpt = edgefound.x2;
				ypt = edgefound.y2;
				end = new EdgePoint(xpt,ypt,(int)foundLink.index,2);
				foundLink.index*=-1;
			}

			
			// add poly to polycontours
			polycontours.add(poly);
			
			//System.exit(0);
			
			System.out.println(poly);
						
		}
		return polycontours;
	}
	
	public static void main(String[] args) {
		
	}

}