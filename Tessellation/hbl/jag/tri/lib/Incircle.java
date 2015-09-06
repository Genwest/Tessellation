package hbl.jag.tri.lib;

import java.math.BigInteger;

/**
This class will implement the Incircle predicate described by D. E. Knuth
in the Lecture Notes in Computer Science  �Axioms and Hulls� published
by Springer-Verlag   This method checks the position of 4 points and answers
the questions associated with the "Counter-Clock-Wise" predicate and the 
"Incircle" predicate returning true if the circle points p,q,r (s test pt) 
are presented in CCW order.  If the initial  point index q is set to the 
defined constant (Incircle.INF = -1) the method returns the CCW for q,r,s 
(as defined ref: p61 steps 1-4), otherwise it returns the Incircle predicate 
(as defined ref: p85 steps 1-4).  p,q,r,and s are indices for point data whose 
x and y coordinate positions are given in  the integer arrays x[nv] and y[nv].  
The x and y arrays should contain the same number of points (nv coordinate 
pairs) and the data is intended to be in Cartesian space. As an alternative 
x0,y0 can replace index q (point) if q=Incircle.AuxPt
@author jag
*/
public class Incircle {

	// class defined constants
	static int INF=-1;
	
	/**
	 Basic test method for Incircle as described in class document
	 @param p
	 @param q
	 @param r
	 @param s
	 @param x
	 @param y
	 @return boolean
	 */
	public static boolean test(int p,int q,int r,int s,long[] x,long[] y)
	{
		// convert the last three point to BigInterger
		BigInteger xq,yq,xr,yr,xs,ys;
		xq=BigInteger.valueOf(x[q]);yq=BigInteger.valueOf(y[q]);
		xr=BigInteger.valueOf(x[r]);yr=BigInteger.valueOf(y[r]);
		xs=BigInteger.valueOf(x[s]);ys=BigInteger.valueOf(y[s]);
		// handle the initial case where point p is at infinity		
		if(p==Incircle.INF) // handles the CCW test p61
		{
			BigInteger t1,t2,t3,t4,d1,d2,fac;
			boolean b=true;
			t1=xq.subtract(xs);
			t2=yr.subtract(ys);
			d1=t1.multiply(t2);
			t3=xr.subtract(xs);
			t4=yq.subtract(ys);
			d2=t3.multiply(t4);
			fac=d1.subtract(d2);
			int c1=fac.signum();
			if(c1!=0){if(c1>0){return b;}else{return !b;}}
			//System.out.println("degenerate case in CCW");
			// fac=0 degenerate case order points
			int temp;
			if(q>r){temp=q;q=r;r=temp;b=!b;}
			if(r>s){temp=r;r=s;s=temp;b=!b;}
			if(q>r){temp=q;q=r;r=temp;b=!b;}
			// check conditions
			c1=(xq.subtract(xr)).signum();
			if(c1!=0){if(c1>0){return !b;}else{return b;}}
			c1=(yq.subtract(yr)).signum();
			if(c1!=0){if(c1>0){return !b;}else{return b;}}
			c1=(xs.subtract(xq)).signum();
			if(c1!=0){if(c1>0){return !b;}else{return b;}}
			c1=(ys.subtract(yq)).signum();
			if(c1!=0){if(c1>0){return !b;}else{return b;}}
			return b;			
		}
		// general case Incircle p 85
		// convert the first point into BigIntegrs
		BigInteger xp,yp;
		xp=BigInteger.valueOf(x[p]);
		yp=BigInteger.valueOf(y[p]);
		BigInteger a11,a12,a13,a21,a22,a23,a31,a32,a33,fac;
		BigInteger t1,t2;
		a11=xp.subtract(xs);
		a12=yp.subtract(ys);
		t1=a11.multiply(a11);
		t2=a12.multiply(a12);
		a13=t1.add(t2);
		a21=xq.subtract(xs);
		a22=yq.subtract(ys);
		t1=a21.multiply(a21);
		t2=a22.multiply(a22);
		a23=t1.add(t2);
		a31=xr.subtract(xs);
		a32=yr.subtract(ys);
		t1=a31.multiply(a31);
		t2=a32.multiply(a32);
		a33=t1.add(t2);
		t1=a22.multiply(a33);
		fac=a11.multiply(t1);
		t1=a23.multiply(a31);
		t2=a12.multiply(t1);
		fac=fac.add(t2);
		t1=a32.multiply(a13);
		t2=a21.multiply(t1);
		fac=fac.add(t2);
		t1=a22.multiply(a13);
		t2=a31.multiply(t1);
		fac=fac.subtract(t2);
		t1=a32.multiply(a23);
		t2=a11.multiply(t1);
		fac=fac.subtract(t2);
		t1=a12.multiply(a33);
		t2=a21.multiply(t1);
		fac=fac.subtract(t2);
		boolean b=true;
		int ck=fac.signum();
		if(ck!=0){if(ck>0){return b;}else{return !b;}}
		// fac=0 degenerate case order points
		//System.out.println("degeneerate case in Incircle");
		// order points
		int temp;
		if(p>q){temp=p;p=q;q=temp;b=!b;}
		if(q>r){temp=q;q=r;r=temp;b=!b;}
		if(r>s){temp=r;r=s;s=temp;b=!b;}
		if(p>q){temp=p;p=q;q=temp;b=!b;}
		if(q>r){temp=q;q=r;r=temp;b=!b;}
		if(p>q){temp=p;p=q;q=temp;b=!b;}
		// check conditions
		ck=fgchk(p,q,r,s,x,y);
		if(ck!=0){if(ck<0){return !b;}else{return b;}}
		ck=fgchk(p,q,r,s,y,x);
		if(ck!=0){if(ck<0){return !b;}else{return b;}}
		ck=fgchk(p,q,r,s,x,y);
		if(ck!=0){if(ck<0){return !b;}else{return b;}}
		ck=fgchk(p,q,r,s,y,x);
		if(ck!=0){if(ck<0){return !b;}else{return b;}}	
		ck=fgchk(p,q,r,s,x,y);
		if(ck!=0){if(ck<0){return !b;}else{return b;}}
		ck=fgchk(p,q,r,s,y,x);
		if(ck!=0){if(ck<0){return !b;}else{return b;}}
		ck=hchk(p,q,r,s,y,x);
		if(ck!=0){if(ck<0){return !b;}else{return b;}}
		ck=jchk(p,q,r,s,y,x);
		if(ck!=0){if(ck<0){return !b;}else{return b;}}
		ck=hchk(p,q,r,s,y,x);
		if(ck!=0){if(ck<0){return !b;}else{return b;}}
		ck=jchk(p,q,r,s,y,x);
		if(ck!=0){if(ck<0){return !b;}else{return b;}}
		ck=jchk(p,q,r,s,y,x);
		if(ck!=0){if(ck<0){return !b;}else{return b;}}		
		return b;
	}
	
	/**
	 This is an alternate utility method for evaluating
	 the CCW predicate that does not assume the point
	 data is in a DagData structure.  Coordinate values
	 are entered directly.
	 Given two points identified by their indices p1,p2
	 pointing to the DagData structure and presented in 
	 such an order that p1<p2 (representing a line segment).
	 The x-y coordinate values followed by the coordinates 
	 of a test point are entered into this routine.  The
	 counter clockwise predicate is evaluated using the 
	 methods of "simplicity" and exact integer math returning
	 true if the text point is CCW from the line segment and
	 false otherwise.
	 @param p1x x-coordinate of beginning of line segment
	 @param p1y y-coordinate of beginning of line segment
	 @param p2x x-coordinate of ending of line segment
	 @param p2y y-coordinate of ending of line segment
	 @param x2  x-coordinate of test point
	 @param y2  y-coordinate of test point
	 @param PtOrder
	 @return boolean
	 */
	public static boolean CCW(long p1x,long p1y,long p2x,long p2y,
			long x2,long y2,boolean PtOrder){
		// convert this edge to BigIntegers
		boolean b=true;
		BigInteger xq,yq,xr,yr,xs,ys;
		xq=BigInteger.valueOf(p1x);yq=BigInteger.valueOf(p1y);
		xr=BigInteger.valueOf(p2x);yr=BigInteger.valueOf(p2y);
		// convert first pt of test edge to BigInteger and test
		xs=BigInteger.valueOf(x2);ys=BigInteger.valueOf(y2);
		BigInteger t1,t2,t3,t4,d1,d2,fac;
		t1=xq.subtract(xs);
		t2=yr.subtract(ys);
		d1=t1.multiply(t2);
		t3=xr.subtract(xs);
		t4=yq.subtract(ys);
		d2=t3.multiply(t4);
		fac=d1.subtract(d2);
		int c1=fac.signum();
		/*
		if(c1==0){
			System.out.println("deg in CCW p1 ("+p1x+","+p1y+") p2 ("+p2x+","+p2y+
					") x2 ("+x2+","+y2+")");
		}
		*/
		if(c1!=0){if(c1>0){return b;}else{return !b;}}
		// convert second pt of test edge to BigInteger and test
		// check conditions
		BigInteger temp;
		if(!PtOrder){
			temp=xq;xq=xr;xr=temp;
			temp=yq;yq=yr;yr=temp;
			b=PtOrder;
		}
		c1=(xq.subtract(xr)).signum();
		if(c1!=0){if(c1>0){return !b;}else{return b;}}
		c1=(yq.subtract(yr)).signum();
		if(c1!=0){if(c1>0){return !b;}else{return b;}}
		c1=(xs.subtract(xq)).signum();
		if(c1!=0){if(c1>0){return !b;}else{return b;}}
		c1=(ys.subtract(yq)).signum();
		if(c1!=0){if(c1>0){return !b;}else{return b;}}
		return b;
	}
	
	/**
	The following method will evaluate both the f(p,q,r,s) and g(p,q,r,s) function 
	defined in ref: p85.  To invoke the f function the calling parameter list is given as 
	shown.  To invoke the g function the calling parameter list is called with the order
	of the x and y arrays reversed.
	*/
	private static int fgchk(int p,int q,int r,int s,long[] x,long[] y)
	{
		BigInteger a11,a12,a21,a22,xp,xq,xr,xs,yp,yq,yr,ys,dx,dy,fac;
		// initialize
		xp=BigInteger.valueOf(x[p]);yp=BigInteger.valueOf(y[p]);
		xq=BigInteger.valueOf(x[q]);yq=BigInteger.valueOf(y[q]);
		xr=BigInteger.valueOf(x[r]);yr=BigInteger.valueOf(y[r]);
		xs=BigInteger.valueOf(x[s]);ys=BigInteger.valueOf(y[s]);
		// a11
		a11=xp.subtract(xr);
		// a21		
		a21=xq.subtract(xr);
		// a12
		dx=xp.subtract(xs);dx=dx.multiply(dx);
		dy=yp.subtract(ys);dy=dy.multiply(dy);
		a12=dx.add(dy);
		dx=xr.subtract(xs);dx=dx.multiply(dx);
		dy=yr.subtract(ys);dy=dy.multiply(dy);
		fac=dx.multiply(dy);
		a12=a12.subtract(fac);
		// a22
		dx=xq.subtract(xs);dx=dx.multiply(dx);
		dy=yq.subtract(ys);dy=dy.multiply(dy);
		a22=dx.add(dy);
		a22=a22.subtract(fac);
		// det
		fac=(a11.multiply(a22)).subtract(a12.multiply(a21));
		int chk=fac.signum();
		return chk;
	}
	/**
	The following method will evaluate both the h(p,q,r,s) 
	function defined in ref: p85.  This method is used 
	internally and should need to be called by the user
	directly
	*/
	private static int hchk(int p,int q,int r,int s,long[] y,long[] x)
	{
		BigInteger xp,xq,yr,ys,fac;
		// initialize
		xp=BigInteger.valueOf(y[p]);
		xq=BigInteger.valueOf(y[q]);
		yr=BigInteger.valueOf(x[r]);
		ys=BigInteger.valueOf(x[s]);
		fac=(xq.subtract(xp)).multiply(yr.subtract(ys));
		int chk=fac.signum();
		return chk;
	}
	/**
	The following method will evaluate both the h(p,q,r,s) 
	function defined in ref: p85.  This method is used 
	internally and should need to be called by the user
	directly
	*/
	private static int jchk(int p,int q,int r,int s,long[] y,long[] x)
	{
		BigInteger xp,xq,xr,yp,yq,ys,d1,fac;
		// initialize
		xp=BigInteger.valueOf(y[p]);yp=BigInteger.valueOf(x[p]);
		xq=BigInteger.valueOf(y[q]);yq=BigInteger.valueOf(x[q]);
		xr=BigInteger.valueOf(y[r]);
		ys=BigInteger.valueOf(x[s]);
		d1=xq.subtract(xr);d1=d1.multiply(d1);
		fac=d1;
		d1=yq.subtract(ys);d1=d1.multiply(d1);
		fac=fac.add(d1);
		d1=xp.subtract(xr);d1=d1.multiply(d1);
		fac=fac.subtract(d1);
		d1=yp.subtract(ys);d1=d1.multiply(d1);
		fac=fac.subtract(d1);
		int chk=fac.signum();
		return chk;
	}

}
