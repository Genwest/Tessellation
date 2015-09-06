package hbl.jag.tri.lib;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/** 
 Public utility class that with an method to read in a moss4 file 
 output from GNOME and produce a Splot list with an append
 method for reading additional moss4 files and appending the 
 results to the same splot file.
 @author jag
 */

public class MOSS2SPLOTLST {
	public ArrayList<Splot> splotlist = new ArrayList<Splot>();
	public ArrayList<Integer> beachedIndex = new ArrayList<Integer>();
	
	/**
	 Public method that will read in moss file (output from
	 GNOME for GIS) and create a splot file. This routine will read
	 the moss.ms4 file to get the position data for the splots
	 and then read the moss.ms5 file to flag whether splots are
	 floating or beached.
	 @param fileName name of the input file
	 @return number of splots in the created splot file
	 */
	public int readMoss4(String fileName){
		try{
			FileReader file4 = new FileReader(fileName+".ms4");
			BufferedReader in4 = new BufferedReader(file4);
			String line=null;
			int count = 0;
			while((line = in4.readLine())!=null){
				StringTokenizer tok = new StringTokenizer(line," ");
				String str = tok.nextToken(); // data not used from this line
				line = in4.readLine();
				tok = new StringTokenizer(line," ");
				str = tok.nextToken();
				double longD = Double.parseDouble(str);
				str = tok.nextToken();
				double latD = Double.parseDouble(str);
				splotlist.add(new Splot(count,longD,latD));
				count++;
			}			
			in4.close();
			
			FileReader file5 = new FileReader(fileName+".ms5");
			BufferedReader in5 = new BufferedReader(file5);
			line=null;
			count = 0;
			while((line = in5.readLine())!=null){
				StringTokenizer tok = new StringTokenizer(line," ");
				String str = tok.nextToken(); // index
				str = tok.nextToken(); // type
				str = tok.nextToken(); // pollutant
				str = tok.nextToken(); // depth
				str = tok.nextToken(); // mass
				str = tok.nextToken(); // density
				str = tok.nextToken(); // age
				str = tok.nextToken(); // status
				if(str.contentEquals("INWATER")){beachedIndex.add(0);}
				if(str.contentEquals("ONBEACH")){beachedIndex.add(1);}
				if(str.contentEquals("OFFMAP")){beachedIndex.add(-1);}
				count++;
			}			
			in5.close();
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return splotlist.size();
	}
	
	/**
	 Public method that will read in a moss4 file (output from
	 GNOME) and append the list of new splots to the existing splot 
	 file.
	 @param fileName name of the input file
	 @return number of splots in the created splot file
	 */
	// appends a moss4 file to the splotlist
	public int appendMOSS4(String fileName){
		int count = splotlist.size();
		try{
			FileReader file = new FileReader(fileName);
			BufferedReader in = new BufferedReader(file);
			String line=null;
			while((line = in.readLine())!=null){
				StringTokenizer tok = new StringTokenizer(line," ");
				String str = tok.nextToken();
				line = in.readLine();
				tok = new StringTokenizer(line," ");
				str = tok.nextToken();
				double longD = Double.parseDouble(str);
				str = tok.nextToken();
				double latD = Double.parseDouble(str);
				splotlist.add(new Splot(count,longD,latD));
				count++;
			}			
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return splotlist.size();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}