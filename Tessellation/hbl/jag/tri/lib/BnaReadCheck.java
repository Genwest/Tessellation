package hbl.jag.tri.lib;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class BnaReadCheck {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String Inputfile = "/home/jag/Desktop/BI_TWNnew2.bna";
		FileReader file = null;
		try {
			file = new FileReader(Inputfile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}		
		BufferedReader in = new BufferedReader(file);
		String line;
		
		
		ArrayList<Polygon> polys = new ArrayList<Polygon>();	
		try {
			while((line = in.readLine())!= null){
				StringTokenizer tok = new StringTokenizer(line,",");
				String str = tok.nextToken();
				// skip over Map Bounds polygons				
				if(str.contains("Map")){
					str = tok.nextToken();
					int segCount = Integer.parseInt(tok.nextToken());
					for(int i=0;i<segCount;i++){
						line = in.readLine();
					}
					continue;
				}
				// skip of SpillableArea polygons
				if(str.contains("Spillable")){
					str = tok.nextToken(); // unused
					int segCount = Integer.parseInt(tok.nextToken());
					for(int i=0;i<segCount;i++){
						line = in.readLine();
						if(line == null){
							System.out.println("line read failed in seg read");
						}
					}
					continue;
				}
				// have found a real boundary segment start and ends
				// with the same point
				str = tok.nextToken(); // unused second field
				int bnaSegCount = Integer.parseInt(tok.nextToken());
				// create an ArrayLists of MicroPoints
				ArrayList<MicroPoint> ptsInSegCW = new ArrayList<MicroPoint>();
				// skip the first redundant line
				//line = in.readLine();
				// read lines to fill in segment data given in cw order
				for(int i=0;i<bnaSegCount;i++){
					line = in.readLine();
					if(line == null){
						System.out.println("line read failed in seg read");
					}
					tok = new StringTokenizer(line,",");
					long longitude = (long) (1e6*Double.parseDouble(tok.nextToken()));
					long latitude = (long) (1e6*Double.parseDouble(tok.nextToken()));
					MicroPoint pt = new MicroPoint(longitude,latitude);
					ptsInSegCW.add(pt);
				}
				// reverse order of points
				ArrayList<MicroPoint> ptsInSegCCW = new ArrayList<MicroPoint>();
				for(int i =0;i<bnaSegCount;i++){
					ptsInSegCCW.add(ptsInSegCW.get(bnaSegCount-1-i));
				}
				// create polygon
				Polygon poly = new Polygon();
				poly.poly = ptsInSegCCW;
				// add poly to polys list
				polys.add(poly);
			}
			in.close();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		int numberOfSegments = polys.size();
		System.out.println("number of bna segments "+numberOfSegments);
		// calculate the total number of boundary points
		int numberOfPoints = 0;
		for(int i =0;i<numberOfSegments;i++){
			int ptsInSeg =polys.get(i).poly.size()-1;
			numberOfPoints+= ptsInSeg;
			System.out.println("seg "+i+" has "+ptsInSeg+
					" points  - cumulative total is "+numberOfPoints);
		}
	}

}