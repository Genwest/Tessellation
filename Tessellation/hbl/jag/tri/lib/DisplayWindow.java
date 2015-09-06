package hbl.jag.tri.lib;

import java.awt.Point;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
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

public class DisplayWindow {
		
	protected static Point upperLeft = new Point(75,75);
	protected DagData dd = null;
	protected JFrame frame = null;
	protected int width = 500;
	protected int height = 500;
	
	/**
	 Constructor which creates a window and identifies
	 a panel to place in the drawing area of the window
	 */
	
	public DisplayWindow(DagData ddIn,JPanel myPanel){
		JFrame.setDefaultLookAndFeelDecorated(false);
		
		dd = ddIn;
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		width = myPanel.getWidth();
		height = myPanel.getHeight();
		frame.setLocation(upperLeft);
		upperLeft.translate(50,50);
		frame.setSize(width,height+25);
		frame.add(myPanel);
		frame.setVisible(true);
	}

}

