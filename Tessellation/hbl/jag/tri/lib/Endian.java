package hbl.jag.tri.lib;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 This is a public utility that provides static methods to switch
 the byte order from either BIG_ENDIAN => LITTLE_ENDIAN or back
 from LITTLE_ENDIAN => BIG_ENDIAN.  It has options for 4-byte int, 
 4-byte float, or 8-byte double input values.  
 Note: Endian.change(Endian.change(val)) => val
 This routine is used in constructing a GIS ShapeFile it looks as
 if ESRI wanted to make it hard
 @author jag
 */

public class Endian {
	
	public static int change(int intVal){ // 4 bytes
		// create byte_buffer and set to native order
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.nativeOrder());
		// put data into buffer
		bb.putInt(intVal);
		// reset the buffer for get
		bb.flip();
		// change the order of the buffer
		if(bb.order()==ByteOrder.BIG_ENDIAN){
			bb.order(ByteOrder.LITTLE_ENDIAN);
		}else{
			bb.order(ByteOrder.BIG_ENDIAN);
		}
		int numOther_ENDIAN = bb.getInt();	
		return numOther_ENDIAN;	
	}
	
	public static float change(float floatVal){ // 4 bytes
		// create byte_buffer and set to native order
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.nativeOrder());
		// put data into buffer
		bb.putFloat(floatVal);
		// reset the buffer for get
		bb.flip();
		// change the order of the buffer
		if(bb.order()==ByteOrder.BIG_ENDIAN){
			bb.order(ByteOrder.LITTLE_ENDIAN);
		}else{
			bb.order(ByteOrder.BIG_ENDIAN);
		}
		float numOther_ENDIAN = bb.getFloat();	
		return numOther_ENDIAN;	
	}
	
	public static double change(double floatVal){ // 8 bytes
		// create byte_buffer and set to native order
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.order(ByteOrder.nativeOrder());
		// put data into buffer
		bb.putDouble(floatVal);
		// reset the buffer for get
		bb.flip();
		// change the order of the buffer
		if(bb.order()==ByteOrder.BIG_ENDIAN){
			bb.order(ByteOrder.LITTLE_ENDIAN);
		}else{
			bb.order(ByteOrder.BIG_ENDIAN);
		}
		double numOther_ENDIAN = bb.getDouble();	
		return numOther_ENDIAN;	
	}
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int myInt = (int)(1000*Math.random());
		int intOE = Endian.change(myInt);
		System.out.println(myInt+"  "+intOE+"  "+Endian.change(intOE));
		
		float myFloat = (float)(1000.0*Math.random());
		float floatOE = Endian.change(myFloat);
		System.out.println(myFloat+"  "+floatOE+"  "+Endian.change(floatOE));
		
		double myDouble = 1000.0*Math.random();
		double doubleOE = Endian.change(myDouble);
		System.out.println(myDouble+"  "+doubleOE+"  "+Endian.change(doubleOE));

	}

}