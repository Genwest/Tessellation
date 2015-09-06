package hbl.jag.tri.lib;

/**
 A public Class representing a basic Splot Object. A
 Splot contains an index (giving its reference to the
 list position that it was generated from), longitude 
 (given in micro-degrees) and latitude (given in micro-
 degrees)
 @author jag
 */
public class Splot {

	public int index;
	public long longitude;
	public long latitude;
	
	/**
	 * Constructor builds object from components
	 * @param i
	 * @param longIn
	 * @param latIn
	 */
	Splot(int i,double longIn,double latIn){
		index = i;
		longitude = (long)(1e6*longIn);
		latitude = (long)(1e6*latIn);
	}
	
	/**
	 Utility to present Arc class variables as String
	 */
	public String toString(){
		return Integer.toString(index)+"  "
				+Long.toString(longitude)+"  "
				+Long.toString(latitude);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
