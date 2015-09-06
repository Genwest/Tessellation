package hbl.jag.tri.sam;


import hbl.jag.tri.lib.CoordTransform;
import hbl.jag.tri.lib.DagData;
import hbl.jag.tri.lib.DisplayWindow;
import hbl.jag.tri.lib.Edge;
import hbl.jag.tri.lib.Node;
import hbl.jag.tri.lib.PlotContours;
import hbl.jag.tri.lib.PlotTri;
import hbl.jag.tri.lib.TriNeighbor;

import java.awt.Color;
import java.util.ArrayList;

public class SampleLtoEContour {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	/**
	 * This class will presents a demonstration sample of
	 * the tesselation process using the DagData formulation -
	 * this will outline the basic processes of constructing
	 * the DagData structure on stocastic model output and 
	 * converting the Lagrangian point data to Eulerian
	 * field data - smoothing the notoriously patchy output
	 * from the stocastic model and contouring the results
	 * of the final Eulerian field
	 * @param dagDate
	 */
	static long epsSq = 10000;
	
	// function to define the stocastic model's two
	// dimensional distribution density ref: Csanady
	static double dist(double x,double y){
		double u = 2.5;
		double D = 1;
		double f1 = u/(4*Math.PI*D*x*x);
		double f2 = u*u*(x*x+(y-0.5)*(y-0.5))/(x*x);
		return f1*Math.exp(-f2);
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		// create a set of vertex points - that fit a
		// probability distribution for an advective 
		// steady state diffusive plume.
		int nv=10000;
		long[] x=new long[nv];
		long[] y=new long[nv];
		int index = 0;
		double x0,y0,prob,key;
		while(index<nv){
			x0 = Math.random();
			y0 = Math.random();
			prob = dist(x0,y0);
			key = Math.random();
			if(key<=prob){
				x[index]=(long)(1e7*x0);
				y[index]=(long)(1e7*y0);
				index++;
			}
		}
		// since there is no constraining boundary we can
		// procede with the minimal constructor to create
		// a DagData object
		DagData dat = new DagData(x,y,CoordTransform.CARTESIAN);
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
		double runTime = (System.currentTimeMillis()-start)/1000.0;
		System.out.println("run time "+runTime);
		// the construction of the tesselation has now been
		// completed and if needed the Delaunay triangles and
		// Veronoi point set areas can be filled in by calling
		// utility routine
		dat.setArea_vA_AndTriISOS();		
		System.out.println(dat.toString());
		// weight each of the paticles with the inverse of
		// its Voronoi point set to create a Eulerian field
		double[] z = new double[nv];
		double zmax = Double.MIN_VALUE;
		double zmin = Double.MAX_VALUE;
		for(int i=0;i<nv;i++){
			double zVal = 1.0;
			double area = dat.getvA()[i];
			if(area<zVal)area=zVal;
			z[i]=zVal/area;
			if(z[i]>zmax)zmax = z[i];
			if(z[i]<zmin)zmin = z[i];
		}		
		// optional smooth of notoriously noisy results 
		//from stotastic models - the number of applitions
		int numSmooths = 5;
		int count = 0;
		while(count<numSmooths){z = dat.smooth(dat,z);count++;}
		// diagonostially output max and min values		
		System.out.println("z field data "+zmax+"  "+zmin);
		
		// generate some contours
		ArrayList<ArrayList<Edge>> contours 
			= new ArrayList<ArrayList<Edge>>();
		ArrayList<Double> vals = new ArrayList<Double>();
		ArrayList<Color> colors = new ArrayList<Color>();		
		double val = (zmax+zmin)/2.0;
		double area ;
		
		// add some contours
		
		ArrayList<Edge> clineR = dat.contourLine(z, val*1e-7,val);
		if(clineR!=null){
			area = dat.contourArea(clineR);
			contours.add(clineR);			
			System.out.println("clineR total area "+area);
			colors.add(Color.RED);
		}else{
			contours.add(null);
			colors.add(null);
		}
		
		ArrayList<Edge> clineB = dat.contourLine(z, val*1e-8,val);
		if(clineB!=null){
			area = dat.contourArea(clineB);
			contours.add(clineB);			
			System.out.println("clineB total area "+area);
			colors.add(Color.BLUE);
		}else{
			contours.add(null);
			colors.add(null);
		}
		
		ArrayList<Edge> clineG = dat.contourLine(z, val*1e-9,val);
		if(clineG!=null){
			area = dat.contourArea(clineG);
			contours.add(clineG);			
			System.out.println("clineG total area "+area);
			colors.add(Color.GREEN);
		}else{
			contours.add(null);
			colors.add(null);
		}
		
		// as a diagnostic check we may want to put out
		// a drawing of the triangles
		PlotTri mypanel1 = new PlotTri(dat,500,500);
		DisplayWindow wnd1= new DisplayWindow(dat,mypanel1);
		PlotContours mypanel2 = new PlotContours(dat,500,500,contours,vals,colors);
		DisplayWindow wnd2= new DisplayWindow(dat,mypanel2);
	}

}