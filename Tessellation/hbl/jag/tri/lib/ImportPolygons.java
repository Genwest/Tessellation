package hbl.jag.tri.lib;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ImportPolygons {

	/**
	 These static methods will:
	 1) read the ASCII file written out
	 by DagData.exportContour.csv.  The return value will be an
	 ArrayList<Polygons> contours.
	 2) recursively go through a sorted list of of EdgePoints 
	 associated with a ArrayList<Edget> values generated in DagData's
	 contourLine method. Used in Class EdgePoint.EdgePtLst2PolyLst
 
	 */
	
	static long diff=-1;
	
	/** >>>>>>>>>>>>>>>>>> re write to read contoursOut.csv format >>>>>>>>>>>>>>>>>>>>>>>>
	 This static method will read the ASCII file written out
	 by DagData.exportContour.csv The return value will be an
	 ArrayList<Polygon> polygons.
	 @param fileIn path name of the contourOut file to read
	 @return ArrayList<Polygon> a list of polygons 
	 */
	static public ArrayList<Polygon> read(String fileIn){
		FileReader file=null;
		try {
			file = new FileReader(fileIn);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader in = new BufferedReader(file);
		String line;
		String str;
		
		ArrayList<Polygon> polygons = new ArrayList<Polygon>();
		
		
		//int contoursCount=0;
		Polygon newpoly = null;
		try {
			while((line = in.readLine())!= null){ 
				StringTokenizer tok = new StringTokenizer(line,", ");
				str = tok.nextToken();
				if(str.contentEquals("Polygon")){ // start of a new polygon
					if(newpoly!= null)polygons.add(newpoly);
					newpoly = new Polygon();
					continue;
				}
				if(str.contentEquals("aux")){ // line with polygon data
					newpoly.aux = Double.parseDouble(tok.nextToken());
					str = tok.nextToken();
					newpoly.area = Double.parseDouble(tok.nextToken());
					continue;
				}
				// discard index;
				str = tok.nextToken();
				str.trim();
				long x = Long.parseLong(str);
				long y = Long.parseLong(tok.nextToken());
				newpoly.poly.add(new MicroPoint(x,y));
			}
			// pick up the last newpoly
			polygons.add(newpoly);
			in.close();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("polygons "+polygons.size());
		return polygons;		
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
	
	public static void main(String[] args) {
		
		ArrayList<Polygon> polygons = ImportPolygons.read("/home/jag/Desktop/contourOut.csv");
		
		for(int i=0;i<5;i++){System.out.print(polygons.get(i).toString());
		
		
		/*
		ArrayList<Double> aux = ImportContours.getAuxThickness();
		for(Double a:aux){System.out.println(a);}
		
		ArrayList<Edge> contour = contours.get(1);
		// load start of Edges into EdgePoint array
		ArrayList<EdgePoint> firstPts = new ArrayList<EdgePoint>();
		for(int i=0;i<contour.size();i++){
			Long ptx = contour.get(i).x1;
			Long pty = contour.get(i).y1;
			firstPts.add(new EdgePoint(ptx,pty,i,1));
		}
		// sort the EdgePoint array
		Collections.sort(firstPts);
		// process firstPts
		
		
		int ind = 0;
		while(ind>=0){
			Boolean foundPos = false;
			for(int i=0;i<firstPts.size();i++){
				if(firstPts.get(i).index>0){
					ind = i;foundPos = true;
					break;}
			}
			if(!foundPos){
				System.out.println("finished marking the list");
				break;
			}
			EdgePoint start = firstPts.get(ind);
			System.out.println("start "+start.index);
			int index = (int)start.index;
			long xpt = contour.get(index).x2;
			long ypt = contour.get(index).y2;
			EdgePoint end = new EdgePoint(xpt,ypt,index,2);
			firstPts.get(ind).index = -index;
			
			while(start!=end){
				int nearestIndex = ImportContours.find(firstPts,end,0,firstPts.size());					
				EdgePoint foundLink = firstPts.get(nearestIndex);
				index = (int)foundLink.index;
				System.out.println("near "+index+"  diff^2  "+diff);
				if(index<0){break;}else{foundLink.index=-index;}
				Edge edgefound = contour.get(index);
				xpt = edgefound.x2;
				ypt = edgefound.y2;
				end = new EdgePoint(xpt,ypt,index,2);
			}
			System.out.println("--------------");
			*/
		}

	}
}


