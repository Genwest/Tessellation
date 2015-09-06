package hbl.jag.tri.lib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
This public class represents a closed polygon
as a list of MicroClass objects (first value repeated
as the last value in the ArrayList<MicroPoint>)given in 
ccw order. It will also carry an area field that will calculate
the enclosed area of the polygon in km^2 and an
aux field typically to used to define the value 
associated with the contour.
*/
public class Polygon implements  Serializable{


	private static final long serialVersionUID = 8621034340278596304L;
	public double area = -1;
	public double aux = -1;
	DagData dat = null;
	public ArrayList<MicroPoint> poly = null;
	
	/**
	 * Default constructor
	 */
	public Polygon(){
		poly = new ArrayList<MicroPoint>();
	}
	
	/**
	 * Alternate constructor supplying DagData object
	 * @param datIn
	 */
	public Polygon(DagData datIn){
		poly = new ArrayList<MicroPoint>();
		dat = datIn;
	}
	
	/**
	 Alternate constructor that sets the aux value
	 @param auxVal double sets aux
	 */
	public Polygon(DagData datIn,double auxVal){
		this(datIn);
		aux = auxVal;
	}

	/**
	 Method to add point to polygon
	 @param pt
	 @return the size of the polygon list after addition
	 */
	public int addPoint(MicroPoint pt){
		poly.add(pt);
		return poly.size();
	}
	
	/**
	 Method to close the polygon by adding the first
	 point as a redundant last point, closing the topological
	 unit.  It then calculates the enclosed area of the
	 polygon and stores the answer in the class variable.
	 This method should only be called once for each polygon.
	 @param double differential area for area calculation
	 @return the size of the polygon list after closure
	 */
	public int closePoly(double diffArea){
		// check if polygon is already closed
		long x1 = poly.get(0).x; 
		long y1 = poly.get(0).y;
		long x2 = poly.get(poly.size()-1).x; 
		long y2 = poly.get(poly.size()-1).y;
		if((x1!=x2)||(y1!=y2))poly.add(poly.get(0));
		area = calculateArea(diffArea);
		return poly.size();
	}
	
	/**
	 This method calculates the area enclosed by the polygon
	 @param double differential area for area calculation
	 @return area of polygon is km^2
	 */
	public double calculateArea(double diffArea){
		// find latitude values.
		Long yMax = Long.MIN_VALUE;
		Long yMin = Long.MAX_VALUE;
		for(MicroPoint pt:poly){
			if(pt.y<yMin)yMin=pt.y;		
			if(pt.y>yMax)yMax=pt.y;
		}
		// calculate areas		
		double area = 0;
		for(int i=1;i<poly.size();i++){
			area+= (poly.get(i).x-poly.get(i-1).x)*
					((poly.get(i).y+poly.get(i-1).y)/2 - yMin);			
		}
		area/=1e12; // convert to degrees squared
		area*= -(111.111*111.111/diffArea); // convert to km^2		
		return area;
	}
	
	/**
	 * This static method will read in a BNA file and return a list 
	 * of polygons with the data in raw Latitude and Longitude micro
	 * degrees
	 * @param bnaFileToRead String giving to path to the bna file
	 * @return ArrayList<Polygon>
	 */
	static public ArrayList<Polygon> bnaToLatLongPolys(String bnaFileToRead){
		String Inputfile = bnaFileToRead;
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
				for(int j =0;j<bnaSegCount;j++){
					ptsInSegCCW.add(ptsInSegCW.get((bnaSegCount-1)-j));
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
		
		//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
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
		//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.
		return polys;
	}
	
	static public CoordTransform transformPolys(ArrayList<Polygon> polysIn,int CoordFlag){
		long xmax=Long.MIN_VALUE;long xmin=Long.MAX_VALUE;
		long ymax=Long.MIN_VALUE;long ymin=Long.MAX_VALUE;
		for(int p=0;p<polysIn.size();p++){
			Polygon poly = polysIn.get(p);
			for(int i=0;i<poly.poly.size();i++){
				MicroPoint pt = poly.poly.get(i);
				if(pt.x>xmax)xmax=pt.x;
				if(pt.x<xmin)xmin=pt.x;
				if(pt.y>ymax)ymax=pt.y;
				if(pt.y<ymin)ymin=pt.y;
			}
		}
		// transform the raw polygon data to cartesian coordinates
		double y0 = (double)(ymax+ymin)/2.0;
		double x0 = (double)(xmax+xmin)/2.0;
		CoordTransform coordTrans = new CoordTransform(y0,x0,CoordFlag);
		for(int j=0;j<polysIn.size();j++){
			Polygon poly = polysIn.get(j);
			Polygon transpoly = new Polygon(poly.dat,poly.aux);
			for(int i=0;i<poly.poly.size();i++){
				MicroPoint pt = poly.poly.get(i);
				long x = coordTrans.XcoordTrans(pt.x);
				long y = coordTrans.YcoordTrans(pt.y);				
				transpoly.addPoint(new MicroPoint(x,y));
			}
			transpoly.closePoly(coordTrans.DiffArea);
			polysIn.set(j,transpoly);
		}
		return coordTrans;
	}
	
	
	static public CoordTransform setBNAcoordinateTransform(ArrayList<Polygon> polysIn){
		long xmax = Long.MIN_VALUE; long xmin = Long.MAX_VALUE;
		long ymax = Long.MIN_VALUE; long ymin = Long.MAX_VALUE;
		for(Polygon poly:polysIn){
			for(MicroPoint pt:poly.poly){
				if(pt.x>xmax)xmax = pt.x;
				if(pt.x<xmin)xmin = pt.x;
				if(pt.y>ymax)ymax = pt.y;
				if(pt.y<ymin)ymin = pt.y;
			}
		}
		long x0 = (xmax+xmin)/2; long y0 = (ymax+ymin)/2;		
		CoordTransform trans = new CoordTransform(y0,x0,CoordTransform.LAT_LONG);
		return trans;
	}
	
	static public ArrayList<MicroPoint> getPtLstFromPolys(ArrayList<Polygon> polysIn){
		ArrayList<MicroPoint> points = new ArrayList<MicroPoint>();
		for(int i=0;i<polysIn.size();i++){
			Polygon poly = polysIn.get(i);
			for(int j=0;j<poly.poly.size()-1;j++){
				MicroPoint pt = poly.poly.get(j);
				points.add(new MicroPoint(pt.x,pt.y));
			}			
		}
		return points;
	}
	
	static public int[] getBSegFromPolys(ArrayList<Polygon> polysIn){
		int[] bseg = new int[polysIn.size()];
		int ptTotal =polysIn.get(0).poly.size()-2;
		bseg[0] = ptTotal;
		for(int i=1;i<polysIn.size();i++){
			Polygon poly = polysIn.get(i);
			ptTotal+= poly.poly.size()-1;
			bseg[i] = ptTotal;
		}			
		return bseg;
	}
	
	/**
	 * Method to check if a test point is inside or outside of a
	 * Polygon object using a line cross count and exact integer
	 * math
	 * @param ptTst the MicroPoint object to test
	 * @param ptOut a MicroPoint object known to be outside the polygon
	 * @return true if point is inside, false is outside
	 */
	public boolean inSideOutside(MicroPoint ptTst,MicroPoint ptOut){		
		long x4 = ptOut.x; long y4 = ptOut.y;
		long x3 = ptTst.x; long y3 = ptTst.y;
		int cross = 0;
		long x1 = poly.get(0).x;long y1 = poly.get(0).y;
		for(int i=1;i<poly.size();i++){
			MicroPoint pt = poly.get(i);
			long x2 = pt.x; long y2 = pt.y;
			boolean b1= Incircle.CCW(x1,y1,x2,y2,x3,y3,true);
			boolean b2= Incircle.CCW(x1,y1,x2,y2,x4,y4,true);
			if(b1==b2)continue;
			boolean b3= Incircle.CCW(x3,x3,x4,y4,x1,y1,true);
			boolean b4= Incircle.CCW(x3,x3,x4,y4,x2,y2,true); //pts out of order
			if(b3==b4)continue;
			cross++;
			x1 = x2; y1 = y2;
		}
		if((cross%2)==1){
			return false;
		}else{
			return true;
		}		
	}
	
	
	/**
	 * standard utility to present contour data as a string
	 */
	public String toString(){
		StringBuffer bf = new StringBuffer("Polygon\n");
		bf.append("aux, "+aux+", area, "+area+"\n");
		for(int i=0;i<poly.size();i++){
			bf.append(i+", "+poly.get(i).x+", "+poly.get(i).y);
			if(i<poly.size()-1)bf.append("\n");
		}
		return bf.toString();
	}
	
	/**
	 * Utility method to output a text version of the 
	 * polygon
	 * @param fileName name of file to be written
	 */
	public void savePolygonToFile(String fileName){
		FileOutputStream file=null;
		try {
			file = new FileOutputStream(fileName);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		PrintWriter out = new PrintWriter(file);
		out.print(this);
		out.close();
	}
	
	/** Utility to write a list of polygons out as a binary file
	 * represented as a Java ArrayList<Polygon>
	 * @param polys ArrayList<Polygon> class list of Polygon objects
	 * @param fileOut name of file to be written out
	 */
	public static void exportPolygonLstBin(ArrayList<Polygon> polys, String fileOut){
		try{
			ObjectOutputStream out = new ObjectOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(fileOut)));
			out.writeObject(polys);
			out.close();			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/** Utility to read a list of polygons from a binary file
	 * represented as a Java ArrayList<Polygon>
	 * @param fileOut name of file to be written out
	 * @return ArrayList<Polygon> list of Polygon objects
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<Polygon> importPolygonLstBin(String fileIn){
		ArrayList<Polygon> polys = null;
		try{
			ObjectInputStream in = new ObjectInputStream(
					new BufferedInputStream(
							new FileInputStream(fileIn)));
			polys = (ArrayList<Polygon>) in.readObject();
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return polys;
	}
	
	/**
	 * static utility to export the ArrayList<Polygon> polygons as a
	 * standard BNA file that can be read by most gis systems
	 * @param polys ArrayList<Polygon> list to polygon objects to write out
	 * @param coordTrans CoordTransformatin class used to convert
	 * 		cartesian micro-degree data to standard Long/Lat
	 * @param fileOut path name to out put .bna file to be written
	 *  */
	public static void exportContourBNA(ArrayList<Polygon> polys,CoordTransform coordTrans,
			String fileOut){
		FileOutputStream file=null;
		try {
			file = new FileOutputStream(fileOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter out = new PrintWriter(file);
		// print out polygons
		//System.out.println(Cx0+"  -----  "+Cy0);
		for(int i=0;i<polys.size();i++){
			Polygon poly = polys.get(i);
			String valStr = String.format("%6.3f",poly.aux);
			String areaStr = String.format("%10.3f",poly.area);
			String ptsStr = String.format("%d",(poly.poly.size()-1));
			StringBuffer bf = new StringBuffer("\""+valStr.trim()+"\"," +
					"\""+areaStr.trim()+"\","+ptsStr+"\n");
			// list points in CC order
			int count = poly.poly.size()-2;
			for(int j=count;j>-1;j--){
				double longitude = 1e-6*(coordTrans.XinverseTrans(poly.poly.get(j).x));
				double latitude = 1e-6*(coordTrans.YinverseTrans(poly.poly.get(j).y));
				String str = String.format("%10.6f, %10.6f",longitude,latitude);
				bf.append(str);
				bf.append("\n");
			}
			out.print(bf.toString());
		}
		out.close();
	}
	
	/** 
	 * Utility to write to disk a mercator projection of the 
	 * of the contours from a list of polygons in CCW orientation
	 * @param polys ArrayList<Polygons>
	 * @param coordTrans CoordTransform object defining the 
	 * 		Mercator projection - member of DagData structure
	 * @param fileOut String of pathname to output file
	 */
	public static void exportCartesianMetersCSV
			(ArrayList<Polygon> polys,CoordTransform coordTrans,String fileOut){
		FileOutputStream file=null;
		try {
			file = new FileOutputStream(fileOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter out = new PrintWriter(file);
		//-----------------------------------------
		// need to convert to METERS from stretched md
		//--------------------------------------------
		double fac = 1/11.11111; // meters/micro-degree
		// print out polygons
		for(int i=0;i<polys.size();i++){			
			Polygon poly = polys.get(i);
			StringBuffer bf = new StringBuffer("Polygon, ");
			bf.append("center Long, "+String.format("%10d,",(long)coordTrans.centerLong)+
					" central Lat, "+String.format("%10d,",(long)coordTrans.centerLat)+
					" in Micro Deg\n");
			bf.append("aux, "+String.format("%6.3f",poly.aux)+", area, "+String.format("%10.6f",poly.area)+"\n");
			for(int j=0;j<poly.poly.size();j++){
				double x = fac*poly.poly.get(j).x;
				double y = fac*poly.poly.get(j).y;
				bf.append(j+", "+String.format("%9.1f",x)+", "+String.format("%9.1f",y));
				bf.append("\n");
			}
			out.print(bf.toString());
		}
		out.close();
	}
	
	/** 
	 * Utility to write to disk a Longitude/Latitude data of the 
	 * of the contours from a list of polygons in CCW orientation
	 * @param polys ArrayList<Polygons>
	 * @param coordTrans CoordTransform object defining the 
	 * 		Mercator projection - member of DagData structure
	 * @param fileOut String of pathname to output file
	 */
	public static void exportLatLongDecDegCSV(
			ArrayList<Polygon> polys,CoordTransform coordTrans,String fileOut){
		FileOutputStream file=null;
		try {
			file = new FileOutputStream(fileOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter out = new PrintWriter(file);		
		// print out polygons
		for(int i=0;i<polys.size();i++){
			Polygon poly = polys.get(i);
			StringBuffer bf = new StringBuffer("Polygon \n");
			bf.append("aux, "+poly.aux+", area, "+String.format("%10.6f",poly.area)+"\n");
			for(int j=0;j<poly.poly.size();j++){
				double longitude = 1e-6*coordTrans.XinverseTrans(poly.poly.get(j).x);
				double latitude = 1e-6*coordTrans.YinverseTrans(poly.poly.get(j).y);
				String str = String.format("%d, %10.6f, %10.6f",j,longitude,latitude);
				bf.append(str);
				bf.append("\n");
			}
			out.print(bf.toString());
		}
		out.close();
	}
	
	/** 
	 * Utility to write to disk a mercator projection of the 
	 * of the contours from a list of polygons in CCW orientation
	 * @param polys ArrayList<Polygons>
	 * @param coordTrans CoordTransform object defining the 
	 * 		Mercator projection - member of DagData structure
	 * @param fileOut String of pathname to output file
	 */
	public static void exportMercatorMicDegCSV
			(ArrayList<Polygon> polys,CoordTransform coordTrans,String fileOut){
		FileOutputStream file=null;
		try {
			file = new FileOutputStream(fileOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter out = new PrintWriter(file);		
		// print out polygons
		for(int i=0;i<polys.size();i++){
			Polygon poly = polys.get(i);
			StringBuffer bf = new StringBuffer("Polygon, ");
			bf.append("center Long, "+String.format("%10d,",(long)coordTrans.centerLong)+
					" central Lat, "+String.format("%10d,",(long)coordTrans.centerLat)+
					" in Micro Deg\n");
			bf.append("aux, "+poly.aux+", area, "+String.format("%10.6f",poly.area)+"\n");
			for(int j=0;j<poly.poly.size();j++){
				bf.append(j+", "+poly.poly.get(j).x+", "+poly.poly.get(j).y);
				bf.append("\n");
			}
			out.print(bf.toString());
		}
		out.close();
	}
	
	/**
	 * static utility to export a 4000X3200 px JPEG image file
	 * of the bounding bna map and the contoured Eulerian density
	 * lines that can be imported to any photo handing application
	 * where it can be zoomed and then have drag-and-drop "scales"
	 * "time and title meta data" and "logo" added for camera ready
	 * briefing material ready to print.
	 * @param bnaMapFile a bna file to input for the map boundary
	 * @param polys ArrayList<Polygon> list of polygons to be drawn
	 * @param jpgFileOut path name of the resulting .jpg file
	 */
	public static void exportMapAndContourJPG(String bnaMapFile,
			ArrayList<Polygon> polys,String jpgFileOut){
		
		// read in the map boundary vertices from bna file
		// and convert to verdat format
		String file = "/home/jag/Desktop/BI_TWNnew2.txt";
		BNA2VERDAT verdatFromBNA = new BNA2VERDAT(file);
		Verdat vd = verdatFromBNA.getVerdat();
		int numberBpts = vd.nver;
		// calculate bounding box and CoordTransform object
		long xmax = Long.MIN_VALUE; long xmin = Long.MAX_VALUE;
		long ymax = Long.MIN_VALUE; long ymin = Long.MAX_VALUE;
		for(int i=0;i<numberBpts;i++){
			if(vd.x[i]<xmin){xmin=vd.x[i];}
			if(vd.x[i]>xmax){xmax=vd.x[i];}
			if(vd.y[i]<ymin){ymin=vd.y[i];}
			if(vd.y[i]>ymax){ymax=vd.y[i];}			
		}
		int xC = (int)((xmax+xmin)/2); int yC = (int)((ymax+ymin)/2);
		CoordTransform coordTrans = new CoordTransform(yC,xC,CoordTransform.LAT_LONG);
		long[] x = new long[numberBpts];
		long[] y = new long[numberBpts];
		for(int i=0;i<numberBpts;i++){
			x[i] = coordTrans.XcoordTrans(vd.x[i]);			
			y[i] = coordTrans.YcoordTrans(vd.y[i]);			
		}
		// calculate scale with central cartesian data
		long dx=coordTrans.XcoordTrans(xmax)-coordTrans.XcoordTrans(ymin);
		long dy=coordTrans.YcoordTrans(ymax)-coordTrans.YcoordTrans(ymin);		
		double scale0 = 1.0/Math.max(dx,dy);
		// set up buffered image to draw on 8"x10" @ 200px/"
		int widthIn = 4000; int heightIn = 3200;
		BufferedImage myImage = new BufferedImage(widthIn,heightIn,BufferedImage.TYPE_INT_RGB);
		// set up graphics	
		Graphics2D g2 = (Graphics2D)myImage.getGraphics();
		// fill canvas background with white
		g2.setColor(Color.white);
		g2.fillRect(0,0,myImage.getWidth(),myImage.getHeight());		
		g2.setFont(new Font("Monospaced",Font.BOLD,24));
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// draw central coordinates
		g2.translate(widthIn/2,heightIn/2);
		g2.scale(1.0,-1.0);		
		float lineWidth = (float)(1.5/(heightIn*scale0));
		g2.setStroke(new BasicStroke(lineWidth));	
		// fac scales local mercator to flat earth for lat in micro-deg
		g2.scale(0.9*widthIn*scale0,0.9*heightIn*scale0);		
		// draw map boundary
		g2.setColor(Color.black);
		int count = 0;
		if(vd.bseg!=null){
			for(int i=0;i<vd.bseg.length;i++){			
				int x1 = (int)x[count];
				int y1 = (int)y[count];
				int x0 = x1, y0 =y1;
				//System.out.println(dd.getbSeg()[i]);
				for(int j = count;j<=(vd.bseg[i]);j++){
					count++;
					int x2;
					int y2;
					if(count==vd.bseg[i]+1){
						g2.drawLine(x1,y1,x0,y0);
						//count++;
						break;
					}else{
						x2 = (int)x[count];
						y2 = (int)y[count];
						g2.drawLine(x1,y1,x2,y2);
					}
					x1 = x2; y1 = y2;	
				}
			}
		}		
		// build array list of values to be contoured and the order in which
		// they are to be drawn
		ArrayList<Double> vals = new ArrayList<Double>();
		vals.add(.125);vals.add(.25);vals.add(.50);vals.add(1.0);
		vals.add(2.0);vals.add(4.0);vals.add(8.0);vals.add(16.0);		
		// create a list of colors corresponding to the contour values 
		ArrayList<Color> colors = new ArrayList<Color>();
		colors.add(new Color(0,255,255));
		colors.add(new Color(42,212,255));
		colors.add(new Color(85,170,255));
		colors.add(new Color(127,127,255));
		colors.add(new Color(170,85,255));
		colors.add(new Color(212,42,255));
		colors.add(new Color(255,0,255));
		colors.add(new Color(255,0,0));		
		// draw the contours
		for(int c=0;c<colors.size();c++){
			g2.setColor(colors.get(c));
			for(int p=0;p<polys.size();p++){
				Polygon poly = polys.get(p);
				if(poly.aux!=vals.get(c))continue;
				// draw polygon
				ArrayList<MicroPoint> points = poly.poly;
				for(int i=1;i<points.size();i++){
					int x1 = (int)points.get(i-1).x;
					int y1 = (int)points.get(i-1).y;
					int x2 = (int)points.get(i).x;
					int y2 = (int)points.get(i).y;
					g2.drawLine(x1,y1,x2,y2);
				}
			}
		}		
		// write out the JPEG format
		try {
			// set up the image writer
			ImageWriter writer = null;
			Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("jpg");
			if(iter.hasNext()) writer = iter.next();
			// attach an output file to the writer
			File fileOutJPG = new File(jpgFileOut);
			ImageOutputStream imageOut = ImageIO.createImageOutputStream(fileOutJPG);
			writer.setOutput(imageOut);
			// write out the image to the file
			writer.write(new IIOImage(myImage,null,null));			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/** 
	 * Utility to write to disk enclosed areas in square kilometers
	 * of various thickness classes from a list of polygons that can
	 * then be read directly into a spread sheet program for additional
	 * analysis and charting
	 * @param polys ArrayList<Polygons>
	 * @param fileOut String of pathname to output file
	 */
	public static void exportAreaStatCSV(ArrayList<Polygon> polys, String fileOut){
		ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
		// scan throught list of polygons
		ArrayList<Double> valLst = new ArrayList<Double>();
		for(int i =0;i<polys.size();i++){
			// for each polygon
			Polygon poly = polys.get(i);
			Double val = poly.aux;
			Double area = poly.area;
			ArrayList<Double> thedataLst = new ArrayList<Double>();
			int index = valLst.indexOf(val);
			if(index!=-1){
				thedataLst = data.get(index);
				thedataLst.set(1,thedataLst.get(1)+area);
				thedataLst.add(area);
				data.set(index,thedataLst);
			}else if(index==-1){
				valLst.add(val);
				thedataLst.add(val);
				thedataLst.add(area);
				thedataLst.add(area);
				data.add(thedataLst);
			}
		}		
		// open a file to write to
		FileOutputStream file=null;
		try {
			file = new FileOutputStream(fileOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter out = new PrintWriter(file);		
		// print out polygons
		String str = "data, contourVal, area\n\n";
		out.print(str);
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<data.size();i++){
			ArrayList<Double> thedataLst = data.get(i);
			sb.append("dataLst ");
			for(int j=0;j<thedataLst.size();j++){
				sb.append(", "+thedataLst.get(j));
			}
			sb.append("\n");
		}
		sb.append("\n");
		out.print(sb.toString());
		out.close();		
	}

	/**
	 * static utility to export a 4000X3200 px JPEG image file
	 * of the bounding bna map and the contoured Eulerian density
	 * lines that can be imported to any photo handing application
	 * where it can be zoomed and then have drag-and-drop "scales"
	 * "time and title meta data" and "logo" added for camera ready
	 * briefing material ready to print.
	 * @param bnaMapFile a bna file to input for the map boundary
	 * @param polys ArrayList<Polygon> list of polygons to be drawn
	 * @param jpgFileOut path name of the resulting .jpg file
	 */
	public static void exportMapAndContourPNG(String bnaMapFile,
			ArrayList<Polygon> polys,String pngFileOut){
		
		// read in the map boundary vertices from bna file
		// and convert to verdat format
		String file = "/home/jag/Desktop/BI_TWNnew2.bna";
		BNA2VERDAT verdatFromBNA = new BNA2VERDAT(file);
		Verdat vd = verdatFromBNA.getVerdat();
		int numberBpts = vd.nver;
		// calculate bounding box and CoordTransform object
		long xmax = Long.MIN_VALUE; long xmin = Long.MAX_VALUE;
		long ymax = Long.MIN_VALUE; long ymin = Long.MAX_VALUE;
		for(int i=0;i<numberBpts;i++){
			if(vd.x[i]<xmin){xmin=vd.x[i];}
			if(vd.x[i]>xmax){xmax=vd.x[i];}
			if(vd.y[i]<ymin){ymin=vd.y[i];}
			if(vd.y[i]>ymax){ymax=vd.y[i];}			
		}
		int xC = (int)((xmax+xmin)/2); int yC = (int)((ymax+ymin)/2);
		CoordTransform coordTrans = new CoordTransform(yC,xC,CoordTransform.LAT_LONG);
		long[] x = new long[numberBpts];
		long[] y = new long[numberBpts];
		for(int i=0;i<numberBpts;i++){
			x[i] = coordTrans.XcoordTrans(vd.x[i]);			
			y[i] = coordTrans.YcoordTrans(vd.y[i]);			
		}
		// calculate scale with central cartesian data
		long dx=coordTrans.XcoordTrans(xmax)-coordTrans.XcoordTrans(ymin);
		long dy=coordTrans.YcoordTrans(ymax)-coordTrans.YcoordTrans(ymin);		
		double scale0 = 1.0/Math.max(dx,dy);
		// set up buffered image to draw on 8"x10" @ 200px/"
		int widthIn = 4000; int heightIn = 3200;
		BufferedImage myImage = new BufferedImage(widthIn,heightIn,BufferedImage.TYPE_INT_RGB);
		// set up graphics	
		Graphics2D g2 = (Graphics2D)myImage.getGraphics();
		// fill canvas background with white
		g2.setColor(Color.white);
		g2.fillRect(0,0,myImage.getWidth(),myImage.getHeight());		
		g2.setFont(new Font("Monospaced",Font.BOLD,24));
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// draw central coordinates
		g2.translate(widthIn/2,heightIn/2);
		g2.scale(1.0,-1.0);		
		float lineWidth = (float)(1.5/(heightIn*scale0));
		g2.setStroke(new BasicStroke(lineWidth));	
		// fac scales local mercator to flat earth for lat in micro-deg
		g2.scale(0.9*widthIn*scale0,0.9*heightIn*scale0);		
		// draw map boundary
		g2.setColor(Color.black);
		int count = 0;
		if(vd.bseg!=null){
			for(int i=0;i<vd.bseg.length;i++){			
				int x1 = (int)x[count];
				int y1 = (int)y[count];
				int x0 = x1, y0 =y1;
				//System.out.println(dd.getbSeg()[i]);
				for(int j = count;j<=(vd.bseg[i]);j++){
					count++;
					int x2;
					int y2;
					if(count==vd.bseg[i]+1){
						g2.drawLine(x1,y1,x0,y0);
						//count++;
						break;
					}else{
						x2 = (int)x[count];
						y2 = (int)y[count];
						g2.drawLine(x1,y1,x2,y2);
					}
					x1 = x2; y1 = y2;	
				}
			}
		}		
		// build array list of values to be contoured and the order in which
		// they are to be drawn
		ArrayList<Double> vals = new ArrayList<Double>();
		vals.add(.125);vals.add(.25);vals.add(.50);vals.add(1.0);
		vals.add(2.0);vals.add(4.0);vals.add(8.0);vals.add(16.0);		
		// create a list of colors corresponding to the contour values 
		ArrayList<Color> colors = new ArrayList<Color>();
		colors.add(new Color(0,255,255));
		colors.add(new Color(42,212,255));
		colors.add(new Color(85,170,255));
		colors.add(new Color(127,127,255));
		colors.add(new Color(170,85,255));
		colors.add(new Color(212,42,255));
		colors.add(new Color(255,0,255));
		colors.add(new Color(255,0,0));		
		// draw the contours
		for(int c=0;c<colors.size();c++){
			g2.setColor(colors.get(c));
			for(int p=0;p<polys.size();p++){
				Polygon poly = polys.get(p);
				if(poly.aux!=vals.get(c))continue;
				// draw polygon
				ArrayList<MicroPoint> points = poly.poly;
				for(int i=1;i<points.size();i++){
					int x1 = (int)points.get(i-1).x;
					int y1 = (int)points.get(i-1).y;
					int x2 = (int)points.get(i).x;
					int y2 = (int)points.get(i).y;
					g2.drawLine(x1,y1,x2,y2);
				}
			}
		}		
		// write out the JPEG format
		try {
			// set up the image writer
			ImageWriter writer = null;
			Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("png");
			if(iter.hasNext()) writer = iter.next();
			// attach an output file to the writer
			File fileOutPNG = new File(pngFileOut);
			ImageOutputStream imageOut = ImageIO.createImageOutputStream(fileOutPNG);
			writer.setOutput(imageOut);
			// write out the image to the file
			writer.write(new IIOImage(myImage,null,null));			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**
	 * static utility to export an ArrayList<Polygon> polygons into
	 * a public domain geoJSON text file of type "FeatureCollection" 
	 * where the "Features" are type Polygon with properties are
	 * "level" double(%/km^2) and "area" double(km^2)
	 * @param polys ArrayList<Polygon> list of polygons
	 * @param coordTrans CoordTransform object defining the 
	 * 		Mercator projection - member of DagData structure
	 * @param fileOut path name of output file of .geoJson
	 */
	public static void exportContoursGeoJSON(ArrayList<Polygon> polys,
			CoordTransform coordTrans,String fileOut){
		// open a file for output
		FileOutputStream file=null;
		try {
			file = new FileOutputStream(fileOut);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter out = new PrintWriter(file);		
		// calculate a bounding box in inverse transformed LONG/LAT
		Double xmin=Double.POSITIVE_INFINITY,xmax=Double.NEGATIVE_INFINITY,
			   ymin=Double.POSITIVE_INFINITY,ymax=Double.NEGATIVE_INFINITY;
		for(int k=0;k<polys.size();k++){
			Polygon poly = polys.get(k);
			ArrayList<MicroPoint> points = poly.poly;
			for(int j=0;j<points.size();j++){
				double x = 1e-6*coordTrans.XinverseTrans(points.get(j).x);
				double y = 1e-6*coordTrans.YinverseTrans(points.get(j).y);
				if(x>xmax)xmax=x;
				if(x<xmin)xmin=x;
				if(y>ymax)ymax=y;
				if(y<ymin)ymin=y;
			}
		}				
		// build header
		
		String.format("%11.6f",xmin);
		String str;
		StringBuffer geojson = new StringBuffer();
		str = "{\n"; geojson.append(str);
		str = "\"type\":\"FeatureCollection\",\n"; geojson.append(str);
		// add bounding box
		str = "\"bbox \":["+String.format("%11.6f ,",xmin)
				+String.format("%11.6f ,",xmax)
				+String.format("%11.6f ,",ymin)
				+String.format("%11.6f",ymax)
				+"],\n"; geojson.append(str);
		// add features	
		str = "\"features\":\n"; geojson.append(str);
		str = "[\n"; geojson.append(str);
		// add golygons as individual features
		for(int i=0;i<polys.size();i++){
			Polygon poly = polys.get(i);
			str = "{\n"; geojson.append(str);
			str = "\"type\":\"Feature\",\n"; geojson.append(str);	
			str = "\"geometry\":\n"; geojson.append(str);
			str = "{\n"; geojson.append(str);
			str = "\"type\":\"Polygon\",\n"; geojson.append(str);
			str = "\"coordinates\": [\n"; geojson.append(str);
			str = "[\n"; geojson.append(str);
			// add points
			ArrayList<MicroPoint> points = poly.poly;
			for(int j=0;j<points.size();j++){
				double x = 1e-6*coordTrans.XinverseTrans(points.get(j).x);
				double y = 1e-6*coordTrans.YinverseTrans(points.get(j).x);
				str = String.format("[ %11.6f, %10.6f ]",x,y);
				geojson.append(str);
				if(j!=points.size()-1)geojson.append(",\n");
			}
			geojson.append("]\n]\n},\n");
			// add properties
			str = "\"properties\":\n"; geojson.append(str);
			str = "{\n"; geojson.append(str);
			str = "\"level\":"+String.format("%6.3f",poly.aux)+",\n"; geojson.append(str);
			str = "\"area\":"+String.format("%10.6f",poly.area)+"\n"; geojson.append(str);
			str = "}\n"; geojson.append(str);	
			str = "}\n"; geojson.append(str);
			if(i!=(polys.size()-1))geojson.append(",\n");
		}
		// terminate file
		str = "]\n"; geojson.append(str);
		str = "}\n"; geojson.append(str);
		// write out StringBuffer		
		out.print(geojson.toString());
		out.close();		
	}
	
	/**
	 * This is a static method that will take as input a BNA
	 * map (typically one generated for use in Gnome and 
	 * exported from it during trajectory runs. (Before doing 
	 * anything the method will check whether "thinned map" 
	 * already exists in a specified target file and if that
	 * file exists no action will be taken.)  The thinning 
	 * algorithm will be based on a two parameter method that
	 * will insure that points are no farther apart then "delta"
	 * and that replacement boundaries are never moved laterally
	 * more then "eps" 
	 * 
	 * @param mapIN String path name to input BNA map
	 * @param newTarget String path name to thinned target BNA map
	 * @param delta maximum distance between boundary points
	 * @param eps maximum lateral movement of boundary points
	 */
	static public void thinBNAmap(String mapIn,String newTarget,
			double delta0,double eps0){
		
		
		// first step is to check if a target file already exists.
		File target = new File(newTarget);		
		if(target.exists())return;
		
		// target does not exist yet open map to thin
		// read in the map boundary vertices
		System.out.println("THINNING del "+delta0+"  eps "+eps0);
		ArrayList<Polygon> mapOne = new ArrayList<Polygon>();
		ArrayList<Polygon> mapTwo = new ArrayList<Polygon>();		
		ArrayList<Polygon> polys = Polygon.bnaToLatLongPolys(mapIn);
		// transform the point data to cartesian form
		CoordTransform trans = Polygon.transformPolys(polys,CoordTransform.LAT_LONG);
		mapOne =polys;
		// Echo input map
		int totalPtsIn = 0;
		long xmax=Long.MIN_VALUE;long xmin=Long.MAX_VALUE;
		long ymax=Long.MIN_VALUE;long ymin=Long.MAX_VALUE;
		for(int p=0;p<polys.size();p++){
			Polygon poly = polys.get(p);
			totalPtsIn+=poly.poly.size();
			for(int i=0;i<poly.poly.size();i++){
				MicroPoint pt = poly.poly.get(i);
				if(pt==null)continue;
				if(pt.x>xmax)xmax=pt.x;
				if(pt.x<xmin)xmin=pt.x;
				if(pt.y>ymax)ymax=pt.y;
				if(pt.y<ymin)ymin=pt.y;
			}
		}
		long dx1=xmax-xmin; long dy1=ymax-ymin;
		double scale0 = 1.0/Math.max(dx1,dy1);
		// plot original map that is read in		
		@SuppressWarnings("unused")
		PlotMaps mymaps = new PlotMaps(800,600,mapOne,null,scale0,0
				,0,totalPtsIn,totalPtsIn);
		//DisplayWindow wnd = new DisplayWindow(null,mymaps);
				
		// for each polygon carry out the thinning operation
		int totalPtsOut = 0;
		for(int j=0;j<polys.size();j++){
			Polygon poly = polys.get(j);
			ArrayList<MicroPoint> ptsOriginal = poly.poly;
			// make working copy of ptsOriginal
			ArrayList<MicroPoint> pts = new ArrayList<MicroPoint>();
			//for(int i=(ptsOriginal.size()-1);i>-1;i--){
			for(int i=0;i<ptsOriginal.size();i++){	
				long x = ptsOriginal.get(i).x; long y = ptsOriginal.get(i).y;
				pts.add(new MicroPoint(x,y));
			}
			ArrayList<MicroPoint> chain = new ArrayList<MicroPoint>();
			ArrayList<Double> epsVal = new ArrayList<Double>();
			int chainStart = 0; int nextPt = 0;			
			// thin this polygon
			while(nextPt<pts.size()){
				// initialize chain if it is empty
				if(chain.size()==0){
					chain.add(pts.get(chainStart));
					nextPt = chainStart+1;
				}
				// add a point to the chain
				if(nextPt==pts.size())break;
				chain.add(pts.get(nextPt));
				nextPt++;
				// calculate metrics
				// length of chain
				long dx = chain.get(0).x-chain.get(chain.size()-1).x;
				long dy = chain.get(0).y-chain.get(chain.size()-1).y;				
				double delta = Math.sqrt((double)(dx*dx+dy*dy));
				// direction cosine of chain length
				double dxU = dx/delta; double dyU = dy/delta;
				// create eps values for chain
				epsVal.clear();
				epsVal.add(0.0);
				for(int i = 1;i<chain.size()-1;i++){					
					// vector from beginning of chain
					double dxi = chain.get(i).x-chain.get(0).x;
					double dyi = chain.get(i).y-chain.get(0).y;
					// vector of normal separation 
					double epsx = dxi*(1.0-dxU); double epsy = dyi*(1.0-dyU);
					double eps = Math.sqrt(epsx*epsx+epsy*epsy);
					// magnitude of separation
					epsVal.add(eps);
				}
				// check metrics to see if delta excedes delta0
				if(delta>delta0){
					if(chain.size()<=3){
						chainStart = chainStart+1; nextPt = chainStart;
						chain.clear();
						continue;
					}
					// set interior chain pts to null
					for(int i=1;i<chain.size()-2;i++){
						pts.set(chainStart+i,null);
					}
					chainStart = chainStart+chain.size()-1; nextPt = chainStart;
					chain.clear();
					continue;					
				}
				// check to see if eps value excedes eps0
				if(chain.size()<=2)continue;
				for(int i=1;i<chain.size()-1;i++){
					if(epsVal.get(i)>eps0){
						for(int k=1;k<i;k++)pts.set(chainStart+k,null);
						chainStart = chainStart+i; nextPt = chainStart;
						chain.clear();
						continue;
					}
				}				
			}
			// compress pts list and built thinned polygon
			Polygon thinnedPoly = new Polygon(null,0);
			thinnedPoly.poly = new ArrayList<MicroPoint>();
			for(int i=0;i<pts.size();i++){
				if(pts.get(i)!=null)thinnedPoly.poly.add(pts.get(i));
			}
			thinnedPoly.closePoly(trans.DiffArea);
			totalPtsOut+=thinnedPoly.poly.size();
			mapTwo.add(thinnedPoly);				
		}
		//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		for(int i=0;i<mapOne.size();i++){
			System.out.println("input map "+mapOne.get(i).poly.size()
					+" new map "+mapTwo.get(i).poly.size());
		}
		//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	
		// print out original map with thinned map in red as overlay
		PlotMaps mymaps2 = new PlotMaps(800,600,mapOne,mapTwo,scale0,
				delta0,eps0,totalPtsIn,totalPtsOut);
		@SuppressWarnings("unused")
		DisplayWindow wnd2 = new DisplayWindow(null,mymaps2);
		// Export the thinned map
		Polygon.exportContourBNA(mapTwo,trans,newTarget);				
	}

	public static void main(String[] args) {
		int delta = 5000;
		int eps = 500;

		Polygon.thinBNAmap("/home/jag/Desktop/ModMap.BNA",
				"/home/jag/Desktop/GABriefing/Thin.bna",delta,eps);
	}
}