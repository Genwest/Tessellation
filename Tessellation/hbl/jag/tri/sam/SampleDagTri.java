package hbl.jag.tri.sam;

import hbl.jag.tri.lib.CoordTransform;
import hbl.jag.tri.lib.DagData; 
import hbl.jag.tri.lib.DisplayWindow;
import hbl.jag.tri.lib.Node;
import hbl.jag.tri.lib.PlotTri;
import hbl.jag.tri.lib.TriNeighbor;
import java.io.Serializable;
import javax.swing.JOptionPane;



public class SampleDagTri implements Serializable
{

	private static final long serialVersionUID = 1L;
	/**
	 * This class will presents a demonstration sample of
	 * the tesselation process using the DagData formulation.
	 * This will outline the basic processes for the general,
	 * none constrained case.
	 * @param dagDate
	 */
	static long epsSq = 1000;

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		// ask user for number of vertices
		String strCount = JOptionPane.showInputDialog("Enter number of points",10000);
		int nv=Integer.parseInt(strCount);
		// create a set of vertex points - in this case
		// we will use a random distribution in a square
		// domain of 10000000 microdegrees (10 degrees on
		// the equator)
		long[] x=new long[nv];
		long[] y=new long[nv];
		for(int i=0;i<nv;i++){
			x[i]=(long)(1e7*Math.random());
			y[i]=(long)(1e7*Math.random());
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
		double runTime= (System.currentTimeMillis()-start)/1000.0;
		System.out.println("run time "+runTime);
		// the construction of the tesselation has now been
		// completed and if needed the Delaunay triangles and
		// Veronoi point set areas can be filled in by calling
		// utility routine
		dat.setArea_vA_AndTriISOS();		
		System.out.println(dat.toString());
		// the next common task might be to save a persistent
		// copy of the in a file
		/*
		dat.exportDagData("/home/jag/Desktop/DagDatOut");
		// and as a check we could read it back in as a new
		// DagData object
		DagData datCopy = DagData.importDagData(
				"/home/jag/Desktop/DagDatOut");
		*/
		// as a diagnostic check we may want to put out
		// a drawing of the triangles
		PlotTri mypanel1 = new PlotTri(dat,500,500);
		DisplayWindow wnd1= new DisplayWindow(dat,mypanel1);
	}
}