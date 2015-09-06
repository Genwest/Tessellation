package hbl.jag.tri.lib;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * This Class  will take as input a BNA
 * map (typically one generated for use in Gnome and 
 * exported from it during trajectory runs. (Before doing 
 * anything the method will check whether "thinned map" 
 * already exists in a specified target file and if that
 * file exists no action will be taken.)  The thinning 
 * algorithm will be based on a two parameter method that
 * will insure that points are no farther apart then "delta"
 * and that replacement boundaries are never moved laterally
 * more then "eps" 
 */

public class ThinBNAMap {

	public ThinBNAMap(String mapIn,String newTarget,
			double delta0,double eps0){
		
		
		// first step is to check if a target file already exists.
		//File target = new File(newTarget);		
		//if(target.exists())return;
		
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
		/*
		// plot original map that is read in		
		@SuppressWarnings("unused")
		PlotMaps mymaps = new PlotMaps(800,600,mapOne,null,scale0,0
				,0,totalPtsIn,totalPtsIn);
		DisplayWindow wnd = new DisplayWindow(null,mymaps);
		*/		
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
		DisplayWindowWithSave wnd2 = new DisplayWindowWithSave(mymaps2,polys,trans,"fileOut");
		
	}

	private class DisplayWindowWithSave extends DisplayWindow implements WindowListener{
		String OutputBNAMap = "";
		ArrayList<Polygon> polys;
		CoordTransform trans;
		public DisplayWindowWithSave(JPanel myPanel,ArrayList<Polygon> polysIn,
				CoordTransform transIn,String fileOut){
			super(null,myPanel);
			polys = polysIn;
			trans = transIn;
			OutputBNAMap = fileOut;
			frame.addWindowListener(this);			
		}
		@Override
		public void windowActivated(WindowEvent arg0) {	
		}
		@Override
		public void windowClosed(WindowEvent arg0) {
		}
		@Override
		public void windowClosing(WindowEvent arg0) {
			System.out.println("getting ready to quit");
			JFileChooser chooser = new JFileChooser();	
			chooser.setCurrentDirectory(new File("."));
			chooser.setSelectedFile(new File("ThinnedBNAMap"));
			int result = chooser.showSaveDialog(null);
			if(result==JFileChooser.APPROVE_OPTION){
				OutputBNAMap = chooser.getSelectedFile().getPath();
			}else{
				System.out.println("no map file choosen");
				System.exit(0);
			}
			Polygon.exportContourBNA(polys,trans,OutputBNAMap);
			System.exit(0);
		}
		@Override
		public void windowDeactivated(WindowEvent arg0) {	
		}
		@Override
		public void windowDeiconified(WindowEvent arg0) {	
		}
		@Override
		public void windowIconified(WindowEvent arg0) {	
		}
		@Override
		public void windowOpened(WindowEvent arg0) {
		}
	}

	public static void main(String[] args) {
		String InputMap = "";
		String OutputBNAMap = "/home/jag/Desktop/GABriefing/Thin.bna";
		int delta = 5000;
		int eps = 500;
		// open up a BNA map to thin
		JFileChooser chooser = new JFileChooser();	
		chooser.setCurrentDirectory(new File("."));
		chooser.setSelectedFile(new File("map-file ??"));
		int result = chooser.showOpenDialog(null);
		if(result==JFileChooser.APPROVE_OPTION){
			InputMap = chooser.getSelectedFile().getPath();
		}else{
			System.out.println("no map file choosen");
			System.exit(0);
		}
		
		String strCount = JOptionPane.showInputDialog("Enter delta ",5000);
		delta=Integer.parseInt(strCount);
		strCount = JOptionPane.showInputDialog("Enter eps ",500);
		eps=Integer.parseInt(strCount);


		@SuppressWarnings("unused")
		ThinBNAMap myMap = new ThinBNAMap(InputMap,OutputBNAMap,delta,eps);
	}

}