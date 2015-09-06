package hbl.jag.tri.lib;
import hbl.jag.tri.lib.MicroPoint;
import hbl.jag.tri.lib.Polygon;

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
import java.util.ArrayList;

import javax.swing.JPanel;


public class PlotMaps extends JPanel{
		private static final long serialVersionUID = 1L;
		protected ArrayList<Polygon> mapOne = null;
		protected ArrayList<Polygon> mapTwo = null;
		double delta;
		double eps;
		int ptCount1;
		int ptCount2;
		AffineTransform affNow =null;
		int xC,yC;
		protected ArrayList<Integer> selected = new ArrayList<Integer>();
		protected double scale0;
		protected int x_cord;
		protected int y_cord;
		protected int width,height;
		protected Container wnd;
		
		public PlotMaps(int widthIn,int heightIn,
				ArrayList<Polygon> mapOneIn,ArrayList<Polygon> mapTwoIn,
				double scale0In, double delta0,double eps0,
				int ptCountIn1,int ptCountIn2){
		super();
		scale0 = scale0In;
		delta = delta0;
		eps = eps0;
		ptCount1 = ptCountIn1;
		ptCount2 = ptCountIn2;
		x_cord = widthIn/2;y_cord = heightIn/2;
		mapOne = mapOneIn;
		mapTwo = mapTwoIn;
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
				// mouse click centeres figure
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
		if(mapOne!=null){
			for(int i=0;i<mapOne.size();i++){
				Polygon poly = mapOne.get(i);
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
				}
			}
		}
		
		// change color
		g2.setColor(Color.red);
		if(mapTwo!=null){
			for(int i=0;i<mapTwo.size();i++){
				Polygon poly = mapTwo.get(i);
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
				}					
			}
		}
		
		
		g2.setColor(nowColor);
		g2.setTransform(afftran0);
		g2.translate(wnd.getWidth()/2,wnd.getHeight()/2);
		g2.scale(1.0,-1.0);
		g2.fillOval(-2,-2,4,4);
		
		g2.setTransform(afftran0);
		g2.setFont(new Font("Monospaced",Font.BOLD,12));
		String lable = null;
		String lable1 = "Original Map ";
		String lable2 = "Thinning Map with - delta= "+delta
				+" eps= "+eps+" ptsin = "+ptCount1+" ptsout = "+ptCount2;
		if(mapTwo==null){lable = lable1;}else{lable = lable2;}
		g2.drawString(lable,20,20);
		g2.setTransform(afftran0);
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
