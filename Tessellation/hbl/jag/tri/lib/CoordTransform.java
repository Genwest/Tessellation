package hbl.jag.tri.lib;

import java.io.Serializable;

/**
 * utility class that will transform raw micro-degree coordinate
 * data to a Cartesian grid in micro degrees centered on Cx0,Cy0.
 * if the CoordinateFlag is set to the static CARTESIAN then the
 * transformation is a simple translation of the origin.  If the 
 * CoordinateFlage is the static LAT_LONG then the transformation
 * is Mercator centered around to same origin.
 * @author jag
 *
 */
public class CoordTransform implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int CARTESIAN = -1;
	public static int LAT_LONG = -2;
	public static double MDinRAD = 180.0*1e6/Math.PI;
	public static double eps = 0.08227185422; // Bowditch p956
	public double DiffArea; // area (dx)(dy) of one micro-degree in Mercator projcetion
	double scale = -1;
	public int CoordinateFlag;
	public double centerLong,centerLat;
	public double Cx0, Cy0;  // in micro-degrees
	
	/**
	 * Constructor for a CoordTransform object
	 * @param centerLat_md central latitude in micro degrees
	 * @param centerLong_md center longitude in micro degrees
	 * @param CoordinateFlagIn static constant either CARTESIAN or LAT_LONG
	 */
	public CoordTransform(double centerLat_md,double centerLong_md,int CoordinateFlagIn) {
		CoordinateFlag = CoordinateFlagIn;
		Cx0 = centerLong_md; 	// in micro-degrees
		Cy0 = centerLat_md;	// in micro-degrees
		centerLong = centerLong_md;
		centerLat = centerLat_md;
		DiffArea = 1.0;
		scale = 1.0;  // converts md Longitude to md Latitude
						  // to decimeter
		if(CoordinateFlag==CoordTransform.LAT_LONG){
			DiffArea = MDinRAD*dyEqDist(centerLat_md);
			double angRad = Math.toRadians(1e-6*centerLat_md);
			Cy0 = centerLat_md*EqDist(angRad)/angRad;
			scale = scale/DiffArea;
		}
	}
	
	/**
	 * Method returns the transformed x coordinate of the input 
	 * longitude in a centered micro degree mapping
	 * @param xIn_md
	 * @return x value of the transformed coordinate in decimeters
	 */
	public long XcoordTrans(long xIn_md){
		return (long)(xIn_md - Cx0);
	}
	
	/**
	 * Method returns the transformed y coordinate of the input 
	 * latitude in a centered micro degree mapping
	 * @param xIn_md
	 * @return x value of the transformed coordinate in decimeters
	 */
	public long  YcoordTrans(long yIn_md){
		long ans = (long)(yIn_md - Cy0);
		if(CoordinateFlag==CoordTransform.LAT_LONG){
			double angRad = Math.toRadians(1e-6*yIn_md);
			double yval = yIn_md*EqDist(angRad)/angRad;
			ans = (long)((yval - Cy0));
		}
		return ans;
	}
	
	/**
	 * Method returns the inverse of the XcoordTrans method
	 * @param xIn transformed longitude value in decimeters
	 * @return original longitude value in micro degrees
	 */
	public long XinverseTrans(long xIn){
		return (long)(xIn + Cx0);
	}
	
	/**
	 * Method returns the inverse of the YcoordTrans method
	 * @param yIn transformed latitude value in decimeters
	 * @return original latitude value in micro degrees
	 */
	public long YinverseTrans(long yIn){
		long ans = (long)(yIn/scale + Cy0);
		if(CoordinateFlag==LAT_LONG){
			double alpha = yIn + Cy0; 	// micro degrees
			double del = 1000;				// micro degrees
			double val = Cy0;				// first guess
			int count =0;
			while(Math.abs(del)>0.01){
				del = -F(alpha,val)/dF(alpha,val);
				val = val+del;
				count++;
				if(count>25)break;
			}
			//System.out.println(count);
			ans = (long)(val);
		}
		return ans;
	}
	
	/**
	 * private internal method used in NewtonRaphson method for inverse
	 * @param alpha
	 * @param ytmd
	 * @return function value
	 */
	private double F(double alpha,double ytmd){
		double angRad = Math.toRadians(1e-6*ytmd);
		return  ytmd*EqDist(angRad)-alpha;
	}
	
	/**
	 * private internal method used in NewtonRaphson method for inverse
	 * @param alpha
	 * @param ytmd
	 * @return function derivative
	 */
	private double dF(double alpha,double ytmd){
		return (F(alpha,ytmd+1)-F(alpha,ytmd-1))/2.0;
	}
	
	/**
	 * private internal method
	 * @param latMD
	 * @return function derivative
	 */
	private double dyEqDist(double latMD){
		double latRad = Math.toRadians(1e-6*latMD);
		double delAng = Math.toRadians(1e-6);
		return (EqDist(latRad+delAng)-EqDist(latRad-delAng))/2;
	}
	
	/**
	 * private internal method
	 * @param latMD
	 * @return function 
	 */
	private static double EqDist(double latRad){
		double fac = Math.pow((1.0-eps*Math.sin(latRad))/(1.0+eps*Math.sin(latRad)),eps/2.0);
		return Math.log(Math.tan(Math.PI/4.0+latRad/2.0)*fac); 
	}
	

	public static void main(String[] args) {
		
		for(int i=0;i<100;i++){
			// angle in radians 
			double a =  0.001*i;
			// epsfac
			double epsfac = Math.pow((1.0-eps*Math.sin(a))/(1.0+eps*Math.sin(a)),eps/2.0);
			// tan factor
			double fac = (Math.tan(Math.PI/4.0+a/2.0)*epsfac);
			// log
			double ratio = Math.log(fac)/a;
			// eqdist
			double eqdist = EqDist(a)/a;
			String str = String.format("%10.6f  %10.6f  %10.6f  %10.6f  %10.6f",a,fac,ratio,epsfac,eqdist);
			System.out.println(str);
		}
		/*
		long[] x = new long[100];
		long[] y = new long[100];
		for(int i=0;i<100;i++){
			x[i] = (long)(5e6*(Math.random()-0.5));
			y[i] = (long)(5e6*Math.random()+5e6);
		}
		CoordTransform trans = new CoordTransform((long)49e6,(long)0,CoordTransform.LAT_LONG);
			
		for(int i=0;i<100;i++){
			long xstart = x[i];
			long ystart = y[i];
			long xtran = trans.XcoordTrans(xstart);
			long ytran = trans.YcoordTrans(ystart);
			long xinv = trans.XinverseTrans(xtran);
			long yinv = trans.YinverseTrans(ytran);
			System.out.println("x "+xstart+" y "+ystart+
					" xtran "+xtran+" ytran "+ytran+
					" xinv "+xinv+" yinv "+yinv);
		}
		*/
	}

}