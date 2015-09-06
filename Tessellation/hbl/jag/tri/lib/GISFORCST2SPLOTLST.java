package hbl.jag.tri.lib;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
Public method that will read in FORCST (output from
GNOME for GNOME Analyst) and create a splot file. This routine will read
the  file to get the position data for the splots
and and the "beachedHeight" field to flag whether splots are
floating or beached.
@param fileName name of the input file
*/

public class GISFORCST2SPLOTLST {

	public ArrayList<Splot> splotlist = new ArrayList<Splot>();
	public ArrayList<Integer> beachedIndex = new ArrayList<Integer>();
	public StringBuffer header = new StringBuffer();
	
	
	public int readFORCAST(String fileName){
		int num = -1;
		try {
			DataInputStream din = new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(fileName)));
			// read in the header
			char[] fileStart = new char[10];
			for(int i=0;i<10;i++){
				fileStart[i] = (char) din.readByte();
			}
			//
			header.append(new String(fileStart)+"\n");
			short day = din.readShort();
			header.append("current day "+day+"\n");
			short mo = din.readShort();
			header.append("current month "+mo+"\n");
			short yr = din.readShort();
			header.append("current year "+yr+"\n");
			// read in the time
			short hr = din.readShort();
			header.append("current hour "+hr+"\n");
			short min = din.readShort();
			header.append("current minute "+min+"\n");
			// read in the reference time
			float refTime = din.readFloat();
			header.append("reference time "+refTime+"\n");
			// read in the version
			float version = din.readFloat();
			header.append("version "+version+"\n");			
			// read in the number of records 
			num = din.readInt();
			header.append("num "+num+"\n");
			// read in some LE records
			int beachedCount = 0;
			double x = -1;
			double y = -1;			
			for(int i=0;i<num;i++){
				float pLat = din.readFloat();
				y = pLat;
				float pLong = din.readFloat();
				x = -pLong;
				splotlist.add(new Splot(i,x,y));
				@SuppressWarnings("unused")
				float releaseTime = din.readFloat();
				@SuppressWarnings("unused")
				float age = din.readFloat();
				float beachedHeight = din.readFloat();
				if(beachedHeight==0.0){
					beachedIndex.add(0);
					beachedCount++;
				}else{
					beachedIndex.add(1);
				}
				// c++ long 4 bytes => int
				@SuppressWarnings("unused")
				int map = din.readInt();
				@SuppressWarnings("unused")
				int pol = din.readInt();
				@SuppressWarnings("unused")
				int windkey = din.readInt();		
			}
			header.append("beached count = "+beachedCount+"\n");	
			din.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return num;
	}
	
	public void showHeader(){
		System.out.print(header.toString());
		
	}
	public static void main(String[] args) {
		String fileName = "/home/jag/Desktop/untitledFORCST";
		GISFORCST2SPLOTLST sl = new GISFORCST2SPLOTLST();
		@SuppressWarnings("unused")
		int numpts = sl.readFORCAST(fileName);		
		sl.showHeader();
		
		for(int i=0;i<10;i++){
			System.out.println(sl.splotlist.get(i).toString()+"\n");
		}
		
		for(int i=sl.splotlist.size()-10;i<sl.splotlist.size();i++){
			System.out.println(sl.splotlist.get(i).toString()+"\n");
		}
	}

}