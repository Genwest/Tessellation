package hbl.jag.tri.lib;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
/**
 This is a utility class that reads in a BNA file of map segments in
 its constructor and provides a method for outputting a verdat file in the 
 standard GNOME format.  A BNA2VERDAT class has a input (String with the
 absolute address of the BNA map to be used as input) 
 @author jag
 */
public class BNA2VERDAT {
	String input;
	protected ArrayList<Segment> segs = new ArrayList<Segment>();
	protected ArrayList<Integer> endseg = new ArrayList<Integer>();
	protected int numberOfSegments = 0;
	protected int numberOfPoints = 0;
	protected Verdat myVerdat = null;

	/**
	 Constructor builds object using a BNA map boundary file
	 name for input
	 @param Inputfile
	 */
	
	public BNA2VERDAT(String Inputfile) {
		input = Inputfile;
		FileReader file = null;
		try {
			file = new FileReader(input);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}		
		BufferedReader in = new BufferedReader(file);
		String line;
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
				// create a set of ArrayLists 
				Segment seg = new Segment();
				// skip the first redundant line
				line = in.readLine();
				// get actual point count for segment
				int segCount = bnaSegCount-1;
				// read lines to fill in segment data given in cw order
				for(int i=0;i<segCount;i++){
					line = in.readLine();
					if(line == null){
						System.out.println("line read failed in seg read");
					}
					tok = new StringTokenizer(line,",");
					long longitude = (long) (1e6*Double.parseDouble(tok.nextToken()));
					long latitude = (long) (1e6*Double.parseDouble(tok.nextToken()));
					seg.mdlong.add(longitude);
					seg.mdlat.add(latitude);
				}
				segs.add(seg);		
			}
			in.close();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		numberOfSegments = segs.size();
		System.out.println("number of bna segments "+segs.size());
		// calculate the total number of verdat boundary pts
		numberOfPoints = 0;
		for(int i =0;i<numberOfSegments;i++){
			Segment seg = segs.get(i);
			numberOfPoints+=seg.mdlong.size();
			endseg.add(numberOfPoints-1);
			System.out.println("seg "+i+" has "+seg.mdlong.size()+
					" points  - cumulative total is "+numberOfPoints);
		}
		// save points in ccw order
		myVerdat = new Verdat(numberOfPoints,numberOfSegments);
		int pt = 0;
		for(int s=0;s<numberOfSegments;s++){
			Segment seg = segs.get(s);
			for(int v=seg.mdlong.size()-1;v>-1;v--){
				myVerdat.x[pt]= seg.mdlong.get(v);
				myVerdat.y[pt]= seg.mdlat.get(v);
				pt++;
			}
		}
		for(int i=0;i<endseg.size();i++){
			myVerdat.bseg[i] = endseg.get(i);
		}
	}
	
	/**
	 Getter to access the verdat structure that is created
	 * @return Verdat
	 */
	public Verdat getVerdat(){
		return myVerdat;
	}
	
	/**
	 Internal class used by BNA2VERDAT
	 */
	private class Segment{
		ArrayList<Long> mdlong = new ArrayList<Long>();
		ArrayList<Long> mdlat = new ArrayList<Long>();		
	}
	
	
	
	/**
	diagnostic test program
	 */
	public static void main(String[] args) {
		String file = "/home/jag/Desktop/BI_TWN.BNA";
		BNA2VERDAT verFromBNA = new BNA2VERDAT(file);
		Verdat verdat = verFromBNA.myVerdat;
		verdat.writeVer("/home/jag/Desktop/BI_verdat");
		
		System.out.println(verdat.x.length+"  "+verdat.bseg.length);

	}

}
