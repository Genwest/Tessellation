package hbl.jag.tri.lib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

public class MapEdit {
		// static variable to stager window positions
		protected static Point upperLeft = new Point(75,75);
		// global access to radio menu buttons
		JRadioButtonMenuItem navigate,select,clearselect,delete,add,move;
		// global access to MapEdit file data
		protected ArrayList<Integer> selected = new ArrayList<Integer>();		
		protected String defaultMapDirectory = "/home/jag/Desktop";
		protected String defaultMapFileName = "Map.BNA";
		protected String defaultMapFileNameOut = "ModMap.BNA";
		// global reference to working state variables
		protected String mapFileIn = null;
		protected String mapFileOut = null;
		protected ArrayList<Polygon> mapIn = null;
		protected int focusPolygon = -1;
		protected int totalPtsIn = -1;
		protected long xmax,xmin,ymax,ymin;
		double scale0;
		CoordTransform trans;
	
		
		/**
		 * Constructor that sets up application window and gets
		 * user input about the Gnome BNA map that is to be 
		 * editeded
		 */		
		MapEdit(){
			openBNAfile();
			/*
			// diagnostice print out used during development
			System.out.println(" pts in boundary "+mapIn.get(0).poly.size());
			*/
		}
		
		/**
		 * Method to open file used in constructor and also by the 
		 * edit->openMapFile menu to explicitely open a BNA map file to edit
		 */
		void openBNAfile(){
			JFileChooser chooser = new JFileChooser();	
			chooser.setCurrentDirectory(new File(defaultMapDirectory));
			chooser.setSelectedFile(new File(defaultMapFileName));
			int result = chooser.showOpenDialog(null);
			if(result==JFileChooser.APPROVE_OPTION){
				mapFileIn = chooser.getSelectedFile().getPath();
			}else{
				System.out.println("no map file choosen");
				System.exit(0);
			}
			// diagnostic out <<<<<<<<<<<<<<<<<<<<<<<
			//System.out.println(mapFileIn);
			//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

			// A mapfile has been choosen open the file and read
			// it in as an ArrayList<Polygon> mapIn
			ArrayList<Polygon> polys = Polygon.bnaToLatLongPolys(mapFileIn);
			// transform the point data to cartesian form
			trans = Polygon.transformPolys(polys,CoordTransform.LAT_LONG);
			mapIn =polys;
			focusPolygon = 0;			
			// Echo input map to get bounding box and scale values
			totalPtsIn = 0;
			xmax=Long.MIN_VALUE; xmin=Long.MAX_VALUE;
			ymax=Long.MIN_VALUE; ymin=Long.MAX_VALUE;
			for(int p=0;p<mapIn.size();p++){
				Polygon poly = mapIn.get(p);
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
			scale0 = 1.0/Math.max(dx1,dy1);			
			// diagnostic out <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
			//mapIn.get(focusPolygon).savePolygonToFile("/home/jag/Desktop/tempFocusPolygon");
			// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
			
			// create the JPanel for the window 
			MapEdit.MapsPanel panel = new MapEdit.MapsPanel(800,600);
			// attach the panel to a JFrame
			@SuppressWarnings("unused")
			MapEdit.MapFrame frame = new MapEdit.MapFrame(panel);			
		}
		
		
		void saveBNAfile(){
			JFileChooser chooser = new JFileChooser();	
			chooser.setCurrentDirectory(new File(defaultMapDirectory));
			chooser.setSelectedFile(new File(defaultMapFileNameOut));
			int result = chooser.showSaveDialog(null);
			if(result==JFileChooser.APPROVE_OPTION){
				mapFileOut = chooser.getSelectedFile().getPath();
			}else{
				System.out.println("no map file choosen");
				System.exit(0);
			}
			
			//System.out.println(mapFileOut);
			
			Polygon.exportContourBNA(mapIn,trans,mapFileOut);
			
			//System.out.println("export finished");
			
		}
		
		
		
		/**
		 *  Method to add a point to the selected list and mark the point
		 * @param datpt a scaled version of the click point to use in 
		 * 		finding the nearest actual data point which will be 
		 * 		identified as the new selected point
		 */
		void AddToSelectionAction(Point datpt){
			//System.out.println("find and mark selected point");
			int target = -1;
			long x0 = datpt.x; long y0 = datpt.y;
			long dx, dy;
			long dist = Long.MAX_VALUE;
			int segLength = mapIn.get(focusPolygon).poly.size();
			for(int i=0;i<segLength;i++){
				MicroPoint pt = mapIn.get(focusPolygon).poly.get(i);
				dx = pt.x-x0; dy = pt.y-y0;
				long val = (long)Math.sqrt(dx*dx+dy*dy);
				if(val<dist){
					dist = val;
					target = i;
				}
			}			
			/*
			//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			System.out.println("target index "+target+" dist "+dist);
			System.out.println("targetX "+mapIn.get(focusPolygon).poly.get(target).x
					+" targetY "+mapIn.get(focusPolygon).poly.get(target).y);
			//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			 */

			selected.add(target);
			// make sure selected list contains either both ends if needed
			if(target==0)selected.add(segLength-1);
			if(target==(segLength-1))selected.add(0);
		}
		
		/**
		 * Method to delete all of the points on the selected list
		 */
		void DoDeleteSelectedPtsMenu(){
			// mark selected MicroPoints as null
			Polygon poly = mapIn.get(focusPolygon);
			ArrayList<MicroPoint> pts = poly.poly; 
			if(selected.size()==0){
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			for(int i=0;i<selected.size();i++){
				pts.set(selected.get(i),null);
			}
			// if end point is null matching end value
			// should be also
			if(pts.get(0)==null){
				pts.set(selected.size()-1,null);
			}
			if((pts.get(selected.size()-1))==null){
				pts.set(0,null);			
			}
			// get rid of all segments < 1.1 meter	
			for(int i=1;i<pts.size()-1;i++){
				if(pts.get(i-1)==null)continue;
				if(pts.get(i)==null)continue;
				long dx = pts.get(i).x-pts.get(i-1).x;
				long dy = pts.get(i).y-pts.get(i-1).y;
				long segLength = dx*dx+dy*dy;
				if(segLength<100)pts.set(i,null);
			}
			// compress the point list of the polygon
			ArrayList<MicroPoint> newpts = new ArrayList<MicroPoint>();
			for(int i=0;i<poly.poly.size();i++){
				MicroPoint pt = poly.poly.get(i);
				if(pt==null)continue;
				newpts.add(pt);
			}
			// the beginning and end repeat
			long x1 = newpts.get(0).x; 
			long y1 = newpts.get(0).y;
			long x2 = newpts.get(newpts.size()-1).x; 
			long y2 = newpts.get(newpts.size()-1).y;
			if((x1!=x2)&&(y1!=y2))newpts.add(new MicroPoint(x1,y1));
			selected.clear();
			mapIn.get(focusPolygon).poly = newpts;
		}
		
		/**
		 * Method to add a new point in the focusPolygon that is 
		 * mid way between the two points that are selected. Note
		 * that if one of the points is a terminal end of the Polygon
		 * object the selected list will actually have three points 
		 * selected.
		 */
		void DoAddPtBetweenMenu(){
			boolean chk = true;
			if(selected.size()==2){
				//System.out.println("size sel "+selected.size());
				//System.out.println("two points - add one in middle");
				
				long x1 = mapIn.get(focusPolygon).poly.get(selected.get(0)).x;
				long y1 = mapIn.get(focusPolygon).poly.get(selected.get(0)).y;
				long x2 = mapIn.get(focusPolygon).poly.get(selected.get(1)).x;
				long y2 = mapIn.get(focusPolygon).poly.get(selected.get(1)).y;
				MicroPoint newPt = new MicroPoint((x1+x2)/2,(y1+y2)/2);
				if(selected.get(0)<selected.get(1)){
					mapIn.get(focusPolygon).poly.add(selected.get(1),newPt);
				}else{
					mapIn.get(focusPolygon).poly.add(selected.get(0),newPt);
				}
				chk = false;
			}
			if(selected.size()==3){
				//System.out.println("size sel "+selected.size());
				//System.out.println("three points - add one in middle");
				int diff = Math.abs(selected.get(0)-selected.get(1));
				if(diff==1){
					long x1 = mapIn.get(focusPolygon).poly.get(selected.get(0)).x;
					long y1 = mapIn.get(focusPolygon).poly.get(selected.get(0)).y;
					long x2 = mapIn.get(focusPolygon).poly.get(selected.get(1)).x;
					long y2 = mapIn.get(focusPolygon).poly.get(selected.get(1)).y;
					MicroPoint newPt = new MicroPoint((x1+x2)/2,(y1+y2)/2);
					if(selected.get(0)<selected.get(1)){
						mapIn.get(focusPolygon).poly.add(selected.get(1),newPt);
					}else{
						mapIn.get(focusPolygon).poly.add(selected.get(0),newPt);
					}
					chk = false;
				}
				diff = Math.abs(selected.get(1)-selected.get(2));
				if(diff==1){
					long x1 = mapIn.get(focusPolygon).poly.get(selected.get(1)).x;
					long y1 = mapIn.get(focusPolygon).poly.get(selected.get(1)).y;
					long x2 = mapIn.get(focusPolygon).poly.get(selected.get(2)).x;
					long y2 = mapIn.get(focusPolygon).poly.get(selected.get(2)).y;
					MicroPoint newPt = new MicroPoint((x1+x2)/2,(y1+y2)/2);
					if(selected.get(1)<selected.get(2)){
						mapIn.get(focusPolygon).poly.add(selected.get(2),newPt);
					}else{
						mapIn.get(focusPolygon).poly.add(selected.get(1),newPt);
					}								
					chk = false;
				}
				diff = Math.abs(selected.get(2)-selected.get(0));
				if(diff==1){
					long x1 = mapIn.get(focusPolygon).poly.get(selected.get(2)).x;
					long y1 = mapIn.get(focusPolygon).poly.get(selected.get(2)).y;
					long x2 = mapIn.get(focusPolygon).poly.get(selected.get(0)).x;
					long y2 = mapIn.get(focusPolygon).poly.get(selected.get(0)).y;
					MicroPoint newPt = new MicroPoint((x1+x2)/2,(y1+y2)/2);
					if(selected.get(2)<selected.get(0)){
						mapIn.get(focusPolygon).poly.add(selected.get(0),newPt);
					}else{
						mapIn.get(focusPolygon).poly.add(selected.get(2),newPt);
					}
					chk = false;
				}
			}
			if(chk){
				//System.out.println("wrong number of points");
				Toolkit.getDefaultToolkit().beep();
			}
		}
				
		/**
		 * Internal class to create application frame
		 Utility Class to set up an application window to display
		 a graphic presentation of tessellation data. A DisplayWindow
		 will have a Static field upperLeft (indication the the upper left
		 hand corner of the window - subsequent instances of the class
		 will be offset right and down on the screen.), dd ( a reference 
		 basic DagData structure), frame (a reference to the basic window
		 that is created), width (an initial width of the window) and height 
		 (an initial height of the window)
		 @author jag
		 */
		private class MapFrame extends JFrame{
			
			private static final long serialVersionUID = 1L;
			//protected static Point upperLeft = new Point(75,75);
			//protected DagData dd = null;
			protected JFrame frame = null;
			protected int width = 500;
			protected int height = 500;			
			/**
			 Constructor which creates a window and identifies
			 a panel to place in the drawing area of the window
			 */			
			public MapFrame(JPanel myPanel){
				JFrame.setDefaultLookAndFeelDecorated(false);
				frame = new JFrame("miniMapEditor");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				width = myPanel.getWidth();
				height = myPanel.getHeight();
				frame.setLocation(upperLeft);
				upperLeft.translate(50,50);
				frame.setSize(width,height+25);
				
				// build menues
				JMenu edit = new JMenu("edit");
				JMenuItem openmap = new JMenuItem("openMapFile");				
				openmap.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						frame.dispose();
						openBNAfile();						
					}
				});					
				JMenuItem savemap = new JMenuItem("saveMap");
				savemap.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						saveBNAfile();						
					}
				});	
				
				JMenuItem steppoly = new JMenuItem("stepFocusPoly");				
				steppoly.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						focusPolygon = (focusPolygon+1)%mapIn.size();
						frame.repaint();
						
						// diagnostic out <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
						mapIn.get(focusPolygon).savePolygonToFile("/home/jag/Desktop/tempFocusPolygon");
						// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
						
						return;
					}
				});	
				JMenuItem reverse = new JMenuItem("reverseISOS");
				reverse.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						// flip the polygon order for each of the polygons/
						for(int i=0;i<mapIn.size();i++){
							ArrayList<MicroPoint> pts = mapIn.get(i).poly;
							ArrayList<MicroPoint> newPts = new ArrayList<MicroPoint>();
							for(int ii=(pts.size()-1);ii>-1;ii--){
								newPts.add(pts.get(ii));
							}
							mapIn.get(i).poly = newPts;
						}
						frame.repaint();						
						return;
					}
				});
				
				edit.add(openmap); edit.add(savemap);
				edit.add(steppoly);edit.add(reverse);			
				JMenu points = new JMenu("points");
				ButtonGroup group = new ButtonGroup();
				navigate = new JRadioButtonMenuItem("navigate",true);
				select = new JRadioButtonMenuItem("select");
				clearselect = new JRadioButtonMenuItem("clearSelect");				
				clearselect.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						selected.clear();
						frame.repaint();
						return;
					}
				});				
				delete = new JRadioButtonMenuItem("deleteSelection");
				delete.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						DoDeleteSelectedPtsMenu();
						frame.repaint();
						// diagnostic out <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
						//mapIn.get(focusPolygon).savePolygonToFile("/home/jag/Desktop/tempFocusPolygon");
						// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
						
						return;
					}
				});					
				add = new JRadioButtonMenuItem("addPtBetween");
				add.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						DoAddPtBetweenMenu();
						selected.clear();
						frame.repaint();
						return;
					}
				});	
				move = new JRadioButtonMenuItem("moveSelectedPt");
				
				group.add(navigate);group.add(select);group.add(clearselect);
				group.add(delete);group.add(add);group.add(move);				
				points.add(navigate);points.add(select);points.add(clearselect);
				points.add(delete);points.add(add);points.add(move);
				JMenuBar menubar = new JMenuBar();
				menubar.add(edit);
				menubar.add(points);
				frame.setJMenuBar(menubar);				
				frame.add(myPanel);
				frame.setVisible(true);
				frame.repaint();
			}
	
		}

	
	
		/**
		 * Internal class to create a window
		 */
		private class MapsPanel extends JPanel{
			private static final long serialVersionUID = 1L;
			//double delta;
			//double eps;
			//int ptCount1;
			//int ptCount2;
			AffineTransform affNow =null;
			int xC,yC;
			protected int x_cord;
			protected int y_cord;
			protected int width,height;
			protected Container wnd;
			
			public MapsPanel(int widthIn,int heightIn /*,
					ArrayList<Polygon> mapOneIn,ArrayList<Polygon> mapTwoIn,
					double scale0In, double delta0,double eps0,
					int ptCountIn1,int ptCountIn2*/){
			super();
			x_cord = widthIn/2;y_cord = heightIn/2;
			width = widthIn;
			height = heightIn;
			this.setBounds(new Rectangle(width,height));				
			addKeyListener(new KeyListener(){				
				public void keyPressed(KeyEvent event){
					if(navigate.isSelected()){
						int keyCode = event.getKeyCode();
						if(keyCode==KeyEvent.VK_UP){scale0*=1.1;}
						if(keyCode==KeyEvent.VK_DOWN){scale0/=1.1;}
					}					
					repaint();
				}
				public void keyReleased(KeyEvent arg0) {}
				public void keyTyped(KeyEvent arg0) {}
			});
			addMouseListener(new MouseListener(){
				public void mouseReleased(MouseEvent event) {
					// mouse click centeres figure with navigation menu checked
					if(navigate.isSelected()){
						int xclick=event.getX()-wnd.getWidth()/2;
						int yclick=wnd.getHeight()/2-event.getY();
						double xx = (xclick)/(0.9*wnd.getWidth()*scale0);
						double yy = yclick/(0.9*wnd.getHeight()*scale0);
						x_cord+= xx; y_cord+= yy;
						repaint();
						return;
					}					
					// mouse-click marks point as selected if select menu is checked
					if(select.isSelected()){
						//System.out.println("select a point to add");
						//System.out.println("x= "+event.getX()+" y= "+event.getY());
						Point clickpt = new Point(event.getX(),event.getY());
						Point datpt = new Point();
						try {
							affNow.inverseTransform(clickpt,datpt);
						} catch (NoninvertibleTransformException e) {
							e.printStackTrace();
							System.exit(-1);
						}
						datpt.x+=xC; datpt.y+=yC;
						AddToSelectionAction(datpt);	
						repaint();
						return;
					}
					if(move.isSelected()){
						boolean makeMove = false;
						if(selected.size()==1)makeMove = true;
						if((selected.size()==2)&&(selected.get(0)==0)) makeMove = true;
						if((selected.size()==2)&&(selected.get(1)==0)) makeMove = true;					
						if(makeMove){
							//System.out.println("selected point to be moved");
							//System.out.println("x= "+event.getX()+" y= "+event.getY());
							
							Point clickpt = new Point(event.getX(),event.getY());
							Point datpt = new Point();
							try {
								affNow.inverseTransform(clickpt,datpt);
							} catch (NoninvertibleTransformException e) {
								e.printStackTrace();
								System.exit(0);
							}
							datpt.x+=xC; datpt.y+=yC;
							MicroPoint newPosition = new MicroPoint(datpt.x,datpt.y);
							for(int i=0;i<selected.size();i++){
								mapIn.get(focusPolygon).poly.set(selected.get(i),newPosition);
							}														
						}
						repaint();
						return;
					}					
				}
				public void mouseClicked(MouseEvent arg0) {}
				public void mouseEntered(MouseEvent arg0) {}
				public void mouseExited(MouseEvent arg0) {}
				public void mousePressed(MouseEvent arg0) {}		
			});
			setFocusable(true);
		}
			
		public void paintComponent(Graphics g){
			super.paintComponent(g);			
			Graphics2D g2 = (Graphics2D)g;
			wnd = getParent();
			// put up title
			g2.setFont(new Font("Monospaced",Font.BOLD,24));
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			// save local copy of present transformation
			AffineTransform afftran0 = g2.getTransform();
			// draw central coordinates
			g2.translate(wnd.getWidth()/2,wnd.getHeight()/2);
			g2.scale(1.0,-1.0);
			// adjust line width for scale
			float lineWidth = (float)(1.5/(wnd.getHeight()*scale0));
			g2.setStroke(new BasicStroke(lineWidth));
			Color nowColor = g2.getColor();		
			xC = x_cord;
			yC = y_cord;
			// fac scales local mercator to flat earth for lat in micro-deg
			g2.scale(0.9*width*scale0,0.9*height*scale0);			
			affNow = g2.getTransform();
			// draw polygons
			// loop on individual polygons
			g2.setColor(Color.black);
			if(mapIn!=null){
				for(int i=0;i<mapIn.size();i++){
					Polygon poly = mapIn.get(i);
					// draw polygon
					ArrayList<MicroPoint> points = poly.poly;
					for(int ii=1;ii<points.size();ii++){
						MicroPoint pt1 = points.get(ii-1);
						if(pt1==null)continue;
						int x1 = (int)pt1.x-xC;
						int y1 = (int)pt1.y-yC;
						MicroPoint pt2 =points.get(ii);
						if(pt2==null)continue;
						int x2 = (int)pt2.x-xC;
						int y2 = (int)pt2.y-yC;
						g2.drawLine(x1,y1,x2,y2);
						if(i==focusPolygon)g2.setColor(Color.green);
						g2.fillOval((int)(x1-2*lineWidth),(int)(y1-2*lineWidth)
								,(int)(4*lineWidth),(int)(4*lineWidth));
						g2.setColor(Color.black);
					}
					// indicate selected points
					Polygon polySel = mapIn.get(focusPolygon);
					g2.setColor(Color.red);
					for(int ii=0;ii<selected.size();ii++){
						MicroPoint pt = polySel.poly.get(selected.get(ii));
						if(pt==null)continue;
						int x1 = (int)pt.x-xC;
						int y1 = (int)pt.y-yC;
						g2.fillOval((int)(x1-2*lineWidth),(int)(y1-2*lineWidth)
								,(int)(4*lineWidth),(int)(4*lineWidth));
					}
					g2.setColor(Color.black);
				}
			}
			// place center point
			g2.setColor(nowColor);
			g2.setTransform(afftran0);
			g2.translate(wnd.getWidth()/2,wnd.getHeight()/2);
			g2.scale(1.0,-1.0);
			g2.fillOval(-2,-2,4,4);
			
			g2.setTransform(afftran0);
			g2.setFont(new Font("Monospaced",Font.BOLD,12));
			String lable = "GNOME Map to Edit";
			g2.drawString(lable,20,20);
			g2.setTransform(afftran0);
		}
	}
	
		
		
	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		MapEdit myedit = new MapEdit();
		
		
		System.out.println("starting");

	}

}