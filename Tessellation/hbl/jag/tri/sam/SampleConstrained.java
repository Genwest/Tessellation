package hbl.jag.tri.sam;

import java.awt.Color;
import java.util.ArrayList;

import hbl.jag.tri.lib.BNA2VERDAT;
import hbl.jag.tri.lib.CoordTransform;
import hbl.jag.tri.lib.DagData;
import hbl.jag.tri.lib.DisplayWindow;
import hbl.jag.tri.lib.Edge;
import hbl.jag.tri.lib.EdgePoint;
import hbl.jag.tri.lib.Node;
import hbl.jag.tri.lib.PlotContours;
import hbl.jag.tri.lib.PlotTri;
import hbl.jag.tri.lib.Polygon;
import hbl.jag.tri.lib.TriNeighbor;
import hbl.jag.tri.lib.Verdat;


public class SampleConstrained {

	/**
	 * this class will give an example of performing the 
	 * constrained tessellation problem where a triangulation
	 * is to take place confined to the point set interior
	 * to a (possibly) multiply connected domain
	 * @param args
	 */
	
	static long epsSq = 500;
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		// read in the map boundary vertices from a small
		// test .bna file as defined below.
		String file = "test.bna";
		BNA2VERDAT verdatFromBNA = new BNA2VERDAT(file);
		Verdat vd = verdatFromBNA.getVerdat();
		/* optional check on read -----------------
		// echo the points to make sure the bna read routine works
		for(int i=0;i<vd.nver;i++){
			String str = String.format("%10d,%10d \n",vd.x[i],vd.y[i]);
			System.out.print(str);
		}
		*/
		//-----------------------------------------s
		// at this point we need to introduce a number of
		// Lagrangian particles that we wish to analyze.  In
		// general these would be the output from a model that 
		// should run in the interior of the domain, but which
		// mar come from a slightly different map representation
		// then the one we just entered.  To demonstrate we will
		// just take the output from a random Gaussian distribution
		// as a "simple model" output.		
		int numberofLEs = 2000;	
		long[] xLe = new long[numberofLEs];
		long[] yLe = new long[numberofLEs];
		int ii = 0;
		int trys =0;
		while(ii < numberofLEs){
			long x = (long)(1e6*(3.0*Math.log(0.7*Math.random())*(Math.random()-0.5)+2.5));
			long y = (long)(1e6*(3.0*Math.log(0.7*Math.random())*(Math.random()-0.5)+6.5));
			trys++;
			// only record points interior to the map
			if(vd.pointInside(x,y)){
				xLe[ii]=x;
				yLe[ii]=y;
				ii++;
			}
		}
		// echo the number of tries on an isotropic gaussian it
		// took to get the required number of interior points
		System.out.println("number of tries "+trys);
				// merge the verdat boundary points and the model LEs
		int nv = vd.nver+numberofLEs;
		long[] x = new long[nv];
		long[] y = new long[nv];
		double[] z = new double[nv];
		for(int i=0;i<vd.nver;i++){
			x[i] = vd.x[i];
			y[i] = vd.y[i];
			z[i] = 0.0;
		}
		int index = 0;
		for(int i=vd.nver;i<nv;i++){
			x[i] = xLe[index];
			y[i] = yLe[index];
			z[i] = 1.0;
			index++;
		}			
		// with the vertices defined and the bSeg array
		// form of the DagData constructor that is 
		// appropriate for boundary and floating LE points
		DagData dat = new DagData(x,y,CoordTransform.LAT_LONG,z,vd.bseg);
		double totalMass= numberofLEs;		
		int unusedSplots = 0;
		for(int i=2;i<nv;i++){
			Node termNode=dat.getTerminalNode(i);
			// Anynumber of checks can be made in this loop to either
			// include or exclude points (LE,s), set flags related to whether
			// partical statis is "beached", "evaporated",etc. Here we
			// check if a location point is inside of the map that is used
			// for output graphics. This step is necessary if a different
			// map is used for the analyst then was used for the GNOME run			
			if(i>dat.getbSeg()[dat.getbSeg().length-1]){				
				if(!dat.pointInside(dat.getX()[i],dat.getY()[i])){
					// diagnostic output
					System.out.println("point "+i+"  "+dat.getX()[i]+"  "+dat.getY()[i]+
							" outside <<<<<<<<<<<<");
					unusedSplots++;
					dat.getZ()[i] = DagData.SplotOutside;
					totalMass-= 1.0;					
					continue;
				}
				// at this point in the algorithm we would
				// typically check to see that the point to be 
				// added is not a duplicate of an LE that is 
				// already in the tessellation or in this case simply
				// at the same location so their mass should be added
				TriNeighbor triNeighbor = dat.minSeparation(termNode,i);
				if(triNeighbor.separation<epsSq){
					dat.getZ()[triNeighbor.neighbor]+= 1.0;
					dat.getZ()[i] = DagData.SplotMerged;
					System.out.println("splot "+i+" added to splot "+triNeighbor.neighbor);
					unusedSplots++;
					continue;
				}
			}												
			dat.setNewEdges(termNode,i);
		}
		// tesselation completed LE's either outside or merged have be flagged in their
		// z fields for reference - echo number of flagged particles
		System.out.println("unused splots outside or murged "+unusedSplots+" <<<");		
		// the construction of the tesselation has now been
		// completed and if needed the Delaunay triangles and
		// Veronoi point set areas can be filled in by calling
		// utility routine
		dat.setArea_vA_AndTriISOS();
		// print out summary for check
		System.out.println(dat.toString());	
		
		// set total mass of spill - insert algorithm if mass of splots 
		// defferent then one. Choose one value in 
		int ForAll = numberofLEs-unusedSplots;
		int ForFloating = numberofLEs-unusedSplots; // don't count beached
		// sq-km per stretched sq-micro-deg
		double km2Factor = 0.012345e-6/dat.getCoordTrans().DiffArea; 
		// at this point the dat.z field contains the positive mass of each
		// splot and negitive dat.z values indicate unused splots
		double[] mass = new double[dat.getNv()];
		double Mmax = Double.MIN_VALUE;
		double Mmin = Double.MAX_VALUE;
		double TotalArea = 0.0;
		for(int i=0;i<dat.getNv();i++){
			// initial values of splot point
			double zVal = dat.getZ()[i];
			// set boundary points to zero values or finite values if needed 
			// for model
			if(i<vd.nver)zVal = 0.0;
			// set all unused particals to zero
			if(dat.getZ()[i]<0)zVal = 0.0;
			// use Voronoi (Thessian polygon) area to normalize mass
			double area = dat.getvA()[i]; // area in micro-deg^2
			if(area<=0){
				mass[i]=0.0;
			}else{
				TotalArea+=area;
				// fraction of splots per km^2 relative to ForAll or ForFloating
				mass[i]=(z[i]/ForFloating)/(area*km2Factor);
			}
			if(mass[i]>Mmax)Mmax = mass[i];
			if(mass[i]<Mmin)Mmin = mass[i];
		}
		// copy back scratch mass field to z array
		for(int i =0;i<dat.getNv();i++){dat.getZ()[i]=mass[i];}
		// optional smooth of notoriously noisy results 
		//from stotastic models - the number of applications
		int numSmooths = 5;
		int count = 0; 
		while(count<numSmooths){dat.setZ(dat.smooth(dat,dat.getZ()));count++;}
		// diagnostic output - echo the range of z values
		System.out.println("z field data max "+Mmax+"  min "+Mmin);	
		System.out.println("area covered by particles in km^2 "+TotalArea*km2Factor);
		// The Eulerian density data will now be contoured. The z values at
		// this point are given in geophysical units of 
		// (fraction of spilled mass)/km^2.  Actual numerical values will 
		// depend on the size of the domain. For this demonstration contour
		// lines will be created around a central value "val" using ln(base2)
		// steps. A characteristic value should be around the recriplical of
		// the area in km^2 covered by the relevant portions of the Thessian 
		// poligons. In this example we choose 1e-6	
		double val = 1e-6;		
		System.out.println("central contour value "+val);
		// create a list of polygon lists
		ArrayList<ArrayList<Edge>> edges = new ArrayList<ArrayList<Edge>>();
		ArrayList<Polygon> thepolys = new ArrayList<Polygon>();
		double area;
		// set up contour colors "null" if edgeLst has no members
		ArrayList<Color> colors = new ArrayList<Color>();
		// generate the first contour		
		ArrayList<Edge> edgeLst1_25 = dat.contourLine(dat.getZ(),0.125*val,1.25);
		if(edgeLst1_25!=null){
			edges.add(edgeLst1_25);
			area = dat.contourArea(edgeLst1_25);
			System.out.println("edgeLst1_25 total area "+area);
			ArrayList<Polygon> polys1_25 = EdgePoint.EdgePtLstToPolyLst(dat,edgeLst1_25,0.125);
			thepolys.addAll(polys1_25);
			colors.add(new Color(0,255,255));
		}else{
			edges.add(null);
			colors.add(null);
		}
		// second contour
		ArrayList<Edge> edgeLst2_5 = dat.contourLine(dat.getZ(), 0.25*val,25);
		if(edgeLst2_5!=null){
			edges.add(edgeLst2_5);
			area = dat.contourArea(edgeLst2_5);
			System.out.println("edgeLst2_5 total area "+area);
			ArrayList<Polygon> polys2_5 = EdgePoint.EdgePtLstToPolyLst(dat,edgeLst2_5,0.25);
			thepolys.addAll(polys2_5);
			colors.add(new Color(42,212,255));
		}else{
			edges.add(null);
			colors.add(null);
		}
		// third contour
		ArrayList<Edge> edgeLst5 = dat.contourLine(dat.getZ(), 0.5*val,5);
		if(edgeLst5!=null){
			edges.add(edgeLst5);
			area = dat.contourArea(edgeLst5);
			System.out.println("edgeLst5 total area "+area);
			ArrayList<Polygon> polys5 = EdgePoint.EdgePtLstToPolyLst(dat,edgeLst5,0.5);
			thepolys.addAll(polys5);
			colors.add(new Color(85,170,255));
		}else{
			edges.add(null);
			colors.add(null);
		}
		// forth contour - central value
		ArrayList<Edge> edgeLst10 = dat.contourLine(dat.getZ(), 1*val,10);
		if(edgeLst10!=null){
			edges.add(edgeLst10);
			area = dat.contourArea(edgeLst10);
			System.out.println("edgeLst10 total area "+area);
			ArrayList<Polygon> polys10 = EdgePoint.EdgePtLstToPolyLst(dat,edgeLst10,1);
			thepolys.addAll(polys10);
			colors.add(new Color(127,127,255));
		}else{
			edges.add(null);
			colors.add(null);
		}
		// fifth contour
		ArrayList<Edge> edgeLst20 = dat.contourLine(dat.getZ(), 2*val,20);
		if(edgeLst20!=null){
			edges.add(edgeLst20);
			area = dat.contourArea(edgeLst20);
			System.out.println("edgeLst20 total area "+area);
			ArrayList<Polygon> polys20 = EdgePoint.EdgePtLstToPolyLst(dat,edgeLst20,2);
			thepolys.addAll(polys20);
			colors.add(new Color(170,85,255));
		}else{
			edges.add(null);
			colors.add(null);
		}
		// sixth contour
		ArrayList<Edge> edgeLst40 = dat.contourLine(dat.getZ(), 4*val,40);
		if(edgeLst40!=null){
			edges.add(edgeLst40);
			area = dat.contourArea(edgeLst40);
			System.out.println("edgeLst40 total area "+area);
			ArrayList<Polygon> polys40 = EdgePoint.EdgePtLstToPolyLst(dat,edgeLst40,4);
			thepolys.addAll(polys40);
			colors.add(new Color(212,42,255));
		}else{
			edges.add(null);
			colors.add(null);
		}
		// seventh contour
		ArrayList<Edge> edgeLst80 = dat.contourLine(dat.getZ(), 8*val,80);
		if(edgeLst80!=null){
			edges.add(edgeLst80);
			area = dat.contourArea(edgeLst80);
			System.out.println("edgeLst80 total area "+area);
			ArrayList<Polygon> polys80 = EdgePoint.EdgePtLstToPolyLst(dat,edgeLst80,8);
			thepolys.addAll(polys80);
			colors.add(new Color(255,0,255));
		}else{
			edges.add(null);
			colors.add(null);
		}
		// eight contour
		ArrayList<Edge> edgeLst160 = dat.contourLine(dat.getZ(), 16*val,160);
		if(edgeLst160!=null){
			edges.add(edgeLst160);
			area = dat.contourArea(edgeLst160);
			System.out.println("edgeLst160 total area "+area);
			ArrayList<Polygon> polys160 = EdgePoint.EdgePtLstToPolyLst(dat,edgeLst160,16);
			thepolys.addAll(polys160);
			colors.add(new Color(255,0,0));
		}else{
			edges.add(null);
			colors.add(null);
		}
		// build array list of values to be contoured and the order in which
		// they are to be drawn
		ArrayList<Double> vals = new ArrayList<Double>();
		vals.add(160.0);vals.add(80.0);vals.add(40.0);vals.add(20.0);
		vals.add(10.0);vals.add(5.0);vals.add(2.5);vals.add(1.25);		
		// as a diagnostic check we may want to put out
		// a drawing of the triangles
		PlotTri mypanel1 = new PlotTri(dat,500,500);
		DisplayWindow wnd1= new DisplayWindow(dat,mypanel1);
		PlotContours mypanel2 = new PlotContours(dat,500,500,edges,vals,colors);
		DisplayWindow wnd2= new DisplayWindow(dat,mypanel2);
	}
}
