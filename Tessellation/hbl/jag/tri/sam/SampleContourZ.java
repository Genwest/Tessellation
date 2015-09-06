package hbl.jag.tri.sam;

import hbl.jag.tri.lib.CoordTransform;
import hbl.jag.tri.lib.DagData;
import hbl.jag.tri.lib.DisplayWindow;
import hbl.jag.tri.lib.Edge;
import hbl.jag.tri.lib.Node;
import hbl.jag.tri.lib.PlotContours;
import hbl.jag.tri.lib.PlotDepth;
import hbl.jag.tri.lib.PlotTri;
import hbl.jag.tri.lib.TriNeighbor;

import java.awt.Color;
import java.util.ArrayList;


public class SampleContourZ {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	/**
	 * This class will presents a demonstration sample of
	 * the tesselation process using the DagData formulation -
	 * this will enter x, y, and z data and then contour the
	 * z field that is defined on the x, y TIN
	 * @param dagDate
	 */
	static long epsSq = 10000;
	
	// simple analytic form of submarine canyon to get some depths
	static double depth(long x0,long y0){
		double x = x0/1e7;
		double y = y0/1e7;
		double d = -400.0*x*y*(y-1.0)+ 
				400.0*Math.exp(-25*(1-x)*(y-0.5)*(y-0.5));		
		return d;
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		// create a set of vertex points - in this case
		// we will use a random distribution in a square
		// domain of 10000000 microdegrees (10 degrees on
		// the equator)
		int nv=1000;
		long[] x=new long[nv];
		long[] y=new long[nv];
		double[] z = new double[nv];
		for(int i=0;i<nv;i++){
			x[i]=(long)(1e7*Math.random());
			y[i]=(long)(1e7*Math.random());
			z[i]=depth(x[i],y[i]);
		}
		// since there is no constraining boundary we can
		// procede with the minimal constructor to create
		// a DagData object
		DagData dat = new DagData(x,y,CoordTransform.CARTESIAN,z);
		// as a simple diagnostic we will start a timer to
		// give us an ideaa of run time for the algorythm
		long start = System.currentTimeMillis();
		// the constructor incorporated the first two vertex
		// points into the DagData structure - now start a 
		// loop the add the rest of the vertices in order
		for(int i=2;i<dat.getNv();i++){
			Node termNode=dat.getTerminalNode(i);
			// at this point in the algorithm we would
			// typically check to see that the point to be 
			// added is not a duplicate of a point that is 
			// already in the tessellation
			TriNeighbor triNeighbor = dat.minSeparation(termNode,i);
			if(triNeighbor.separation<epsSq)continue;
			dat.setNewEdges(termNode,i);			
		}
		double runTime= (System.currentTimeMillis()-start)/1000.0;
		System.out.println("run time "+runTime);
		// the construction of the tesselation has now been
		// completed and if needed the Delaunay triangles and
		// Veronoi point set areas can be filled in by calling
		// utility routine
		dat.setArea_vA_AndTriISOS();		
		System.out.println(dat.toString());
		// generate some contours
		ArrayList<ArrayList<Edge>> contours 
			= new ArrayList<ArrayList<Edge>>();
		ArrayList<Double> vals = new ArrayList<Double>();
		ArrayList<Color> colors = new ArrayList<Color>();
		// find span of data
		double zmax = Double.MIN_VALUE;
		double zmin = Double.MAX_VALUE;
		for(int i=0;i<nv;i++){
			if(z[i]>zmax)zmax = z[i];
			if(z[i]<zmin)zmin = z[i];			
		}
		double scale = 1.0;
		for(int i=0;i<10;i++){
			double val = 0.09*i*(zmax-zmin)+ 1.05*zmin;
			ArrayList<Edge> cline = dat.contourLine(z, val,scale);
			System.out.println("contour val = "+val+" length ("+
					cline.size()+")");
			contours.add(cline);
			colors.add(Color.magenta);
			vals.add(val);
		}
		// as a diagnostic check we may want to put out
		// a drawing of the triangles
		PlotTri mypanel1 = new PlotTri(dat,500,500);
		DisplayWindow wnd1= new DisplayWindow(dat,mypanel1);
		PlotDepth mypanel2 = new PlotDepth(dat,500,500);
		DisplayWindow wnd2= new DisplayWindow(dat,mypanel2);
		PlotContours mypanel3 = new PlotContours(dat,500,500,contours,vals,colors);
		DisplayWindow wnd3= new DisplayWindow(dat,mypanel3);
	}

}