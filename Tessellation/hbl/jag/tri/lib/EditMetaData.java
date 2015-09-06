package hbl.jag.tri.lib;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Utility the set meta data associated with a trajectory output 
 * product. The Class will check to see if a scratch file exists
 * and if so use it to set default values, which will be used in
 * the next initialization. The constructor takes the path name
 * of the directory where the metadata is stored
 * metadata[0] "path name of initialization MetaData.bin file"
 * metadata[1] "path name of output MetaData.jpg image file"
 * metadata[2] "String showind first line of metadata"
 * metadata[3] "etc"
 * @author jag
 *
 */

public class EditMetaData implements WindowListener{
		
		ArrayList<String> metadata = null;
		JFrame frame = new JFrame();
		
		File scratch = null;
		String MetaData = null;
		
		@SuppressWarnings("unchecked")
		public EditMetaData(String MetaDataIn){
			MetaData = MetaDataIn;
			// check if scratch file exists
			scratch = new File(MetaData+".bin");
			if(scratch.exists()){
				// open and read defaults
				try{
					ObjectInputStream in = new ObjectInputStream(
							new BufferedInputStream(
									new FileInputStream(scratch)));
					metadata = (ArrayList<String>)in.readObject();
					in.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				// load brand new defaults
				metadata = new ArrayList<String>();
				metadata.add(MetaData+".bin"); // path name of scratch file
				metadata.add(MetaData+".jpg"); // path name of output file 
				metadata.add("SCENARIO:  - The BIG ONE!! -"); 
				metadata.add("FORECAST TIME: 2015/04/01-12:00 PST");
				metadata.add("PREPARED AT:   2015/01/01-00:00 PST");
				metadata.add("CONTACT: usually.reliable@trustMe.com");
				metadata.add("1-342-577-2937 or 1-DIAL-PRAYER");
			}
			// present user with editorial options
			JFrame.setDefaultLookAndFeelDecorated(true);
			frame.setTitle("Enter MetaData");
			frame.addWindowListener(this);
			frame.setSize(new Dimension(400,300));
			frame.setLocation(25,25);
			
			JPanel panel = new JPanel(new GridLayout(0,1));
			JTextField scenario = new JTextField(metadata.get(2));
			panel.add(scenario);
			JTextField forecast = new JTextField(metadata.get(3));
			panel.add(forecast);
			JTextField prep = new JTextField(metadata.get(4));
			panel.add(prep);
			JTextField contact = new JTextField(metadata.get(5));
			panel.add(contact);
			JTextField phone = new JTextField(metadata.get(6));
			panel.add(phone);
			frame.getContentPane().add(panel);
			frame.setVisible(true);			
		}

		@Override
		public void windowActivated(WindowEvent arg0) {}

		@Override
		public void windowClosed(WindowEvent arg0) {}

		@Override
		public void windowClosing(WindowEvent arg0) {
			// save out scratch file
			try {
				if(scratch.exists())scratch.deleteOnExit();
				scratch.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try{
				ObjectOutputStream out = new ObjectOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(scratch)));
				out.writeObject(metadata);
				out.close();			
			}catch(Exception e){
				e.printStackTrace();
			}
			// write out the JPEG image
		
			BufferedImage myImage = new BufferedImage(500,200,BufferedImage.TYPE_INT_RGB);
			Graphics g2 = myImage.getGraphics();
			
			g2.setColor(Color.white);
			g2.fillRect(0,0,myImage.getWidth(),myImage.getHeight());
			
			g2.setColor(Color.black);
			g2.setFont(new Font(Font.MONOSPACED,Font.BOLD,18));
			g2.drawString("METADATA:",25,25);
			g2.drawString(metadata.get(2),30,50);
			g2.drawString(metadata.get(3),30,75);
			g2.drawString(metadata.get(4),30,100);
			g2.drawString(metadata.get(5),30,125);
			g2.drawString(metadata.get(6),30,150);
			try {
				
				ImageWriter writer = null;
				Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("jpg");
				if(iter.hasNext()) writer = iter.next();
				
				File fileOutJPG = new File(MetaData+".jpg");
				ImageOutputStream imageOut = ImageIO.createImageOutputStream(fileOutJPG);
				writer.setOutput(imageOut);
				
				writer.write(new IIOImage(myImage,null,null));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			frame.dispose();
			
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {}

		@Override
		public void windowDeiconified(WindowEvent arg0) {}

		@Override
		public void windowIconified(WindowEvent arg0) {}

		@Override
		public void windowOpened(WindowEvent arg0) {}
		
		/**
		 * @param args
		 */
		public static void main(String[] args) {
			//String editFile = "/home/jag/Desktop/GABriefing/MetaData";
			//EditMetaData thedat = new EditMetaData(editFile);

		}


}
