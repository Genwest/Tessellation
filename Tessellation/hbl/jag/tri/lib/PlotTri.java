package hbl.jag.tri.lib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import javax.swing.JPanel;

	/**
	This Class will build a panel to fill the drawing face of 
	a DisplayWindow Class object. This panel Class will plot 
	the outline of triangles represented in the DagData
	structure's of arcs and nodes data.  A PlotTri Class will 
	contain dd (a reference to basic DagData structure), scale0 (a
	copy of the present window scale - initialized to the value
	in the DagData structure), fac (a map factor set to 1.0 for 
	isotropic coordinates and a an cosine related to the mean 
	Latitude if the coordinates are given in micro-degrees of 
	Longitude/Latitude),  height,width (suggested panel size in 
	pixels which will override values set in the parent window),
	and wnd (a reference to the parent DisplayWindow Class).
	A user click on the data panel of a visible window will redraw
	the window such that the new view will be centered on the click 
	point. The "up arrow" key will zoom in the view of the window 
	by 10%. The "down arrow" key will zoom out the view of the window by 10%.
	@author jag
	*/

public class PlotTri extends JPanel{

	private static final long serialVersionUID = 1L;
	protected DagData dd = null;
	protected double scale0;
	protected int x_cord;
	protected int y_cord;
	protected int height,width;
	protected Container wnd;
	
	/**
	 Constructor to build panel
	 @param ddIn basic DagData Class structure
	 @param widthIn panel width will override parent window width
	 @param heightIn panel height will override parent window height
	 @param Isotropic boolean flag set to true if the coordinates 
	 	are isotropic and false if the coordinates are given in 
	 	Longitude/Latitude micro-degrees and a map factor should be
	 	used when plotting
	 */
	public PlotTri(DagData ddIn,int widthIn,int heightIn){
		super();
		dd = ddIn; 
		scale0 = dd.scale;
		x_cord = widthIn/2;y_cord = heightIn/2;	
		width = widthIn;
		height = heightIn;	
		this.setBounds(new Rectangle(width,height));
		addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent event){
				int keyCode = event.getKeyCode();
				if(keyCode==KeyEvent.VK_UP){scale0*=1.1;}
				if(keyCode==KeyEvent.VK_DOWN){scale0/=1.1;}
				repaint();
			}
			public void keyReleased(KeyEvent arg0) {}
			public void keyTyped(KeyEvent arg0) {}
		});
		addMouseListener(new MouseListener(){
			public void mouseReleased(MouseEvent event) {
				int xclick=event.getX()-wnd.getWidth()/2;
				int yclick=wnd.getHeight()/2-event.getY();
				double xx = (xclick)/(0.9*wnd.getWidth()*scale0);
				double yy = yclick/(0.9*wnd.getHeight()*scale0);
				x_cord+= xx; y_cord+= yy;
				repaint();
			}
			public void mouseClicked(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}		
		});
		setFocusable(true);
	}
	
	/**
	 Required method that will actually draw data to the
	 graphic object represented by the panel. This overrides
	 the method in the base JPanel Class and is called by
	 Swing window manager 
	 */
	
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
		// draw central coordinates and note left handed panel		
		g2.translate(wnd.getWidth()/2,wnd.getHeight()/2);
		g2.scale(1.0,-1.0);		
		// adjust line width for scale
		float lineWidth = (float)(1.5/(wnd.getHeight()*scale0));
		g2.setStroke(new BasicStroke(lineWidth));
		Color nowColor = g2.getColor();
		g2.setColor(Color.red);
		int xC = x_cord;
		int yC = y_cord;
		// local mercator to flat earth for lat in micro-deg
		g2.scale(0.9*width*scale0,0.9*height*scale0);
		// plot triangles
		
		for(int i=0;i<dd.getTriNode().size();i++){
			Node node = dd.getNodes().get(dd.getTriNode().get(i));
			if(node.area<0)continue;
			//if(node.p!=Incircle.INF)continue; // not a term node
			// identify tri vertices
			int arc = node.q;
			int v1 = dd.getArc()[arc].vert;
			arc = dd.getArc()[arc].next;
			int v2 = dd.getArc()[arc].vert;
			arc = dd.getArc()[arc].next;
			int v3 = dd.getArc()[arc].vert;		
			// get point data
			int x1 = (int)dd.getX()[v1]-xC;int y1 = (int)dd.getY()[v1]-yC;
			int x2 = (int)dd.getX()[v2]-xC;int y2 = (int)dd.getY()[v2]-yC;
			int x3 = (int)dd.getX()[v3]-xC;int y3 = (int)dd.getY()[v3]-yC;
			g2.drawLine(x1,y1,x2,y2);
			g2.drawLine(x2,y2,x3,y3);
			g2.drawLine(x3,y3,x1,y1);
		}
		/*
		// draw map boundary
		g2.setColor(Color.green);
		int count = 0;
		if(dd.getbSeg()!=null){
			//System.out.println(dd.getbSeg().length);
			for(int i=0;i<dd.getbSeg().length;i++){			
				int x1 = (int)dd.x[count]-xC;
				int y1 = (int)dd.y[count]-yC;
				int x0 = x1, y0 =y1;
				//System.out.println(dd.getbSeg()[i]);
				for(int j = count;j<=(dd.getbSeg()[i]);j++){
					count++;
					int x2;
					int y2;
					if(count==dd.getbSeg()[i]+1){
						g2.drawLine(x1,y1,x0,y0);
						//count++;
						break;
					}else{
						x2 = (int)dd.x[count]-xC;
						y2 = (int)dd.y[count]-yC;
						g2.drawLine(x1,y1,x2,y2);
					}
					x1 = x2; y1 = y2;	
				}
			}
		}
		*/
		
		g2.setColor(nowColor);
		g2.setTransform(afftran0);
		g2.translate(wnd.getWidth()/2,wnd.getHeight()/2);
		g2.scale(1.0,-1.0);
		g2.fillOval(-2,-2,4,4);
		
		g2.setTransform(afftran0);
		String lable = "triangles";
		g2.setFont(new Font("Monospaced",Font.BOLD,18));
		g2.drawString(lable,20,20);
		g2.setTransform(afftran0);
	}

}