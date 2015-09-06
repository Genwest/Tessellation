package hbl.jag.tri.lib;
/*
 * This package bundles a number of components that are used
 * in the construction and manipulation of point data into a
 * triangular mesh that will be composed of Deluanany triangles
 * if the boundary is non specific (in which case it defaults 
 * to a convex hull).  If a boundary is specified then these 
 * routines lead to the constrained problem and yield Delaunay
 * triangle where possible and with modifications as required
 * along the boundaries.
 * version j_1.1
 * reference 20140815
 * JAG
 */


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;

/**
 * This class bundles together the class structures that will
 * be used in the DagTri class as well as a number of utility
 * routines to manipulate and summarize the data. The point 
 * location data is entered in integer micro_degrees in a right
 * handed coordinate system (west Longitude -, south Latitude -.)
 * and all calculation are made assuming a flat Cartesian space.
 * @author JAG
 *
 */

public class DagData implements VariableSplots, Serializable
{

	private static final long serialVersionUID = 1L;
	public static double SplotOutside = -1; // static flag
	public static double SplotMerged = -2;  // static flag
	// class variables associated with the point data
	protected int nv; 		// number of data points
	protected long[] x;		// longitude in micr_odegrees
	protected long[] y;		// latitude in micro_degrees
	protected double[] z;	// depth at vertex point
	protected double[] vA; 	// Voronoi area associate with the 
	protected int[] bSeg;  // bSeg is a array of the end point indexes of each boundary
							// segment.
	protected ArrayList<Edge> bEdges= new ArrayList<Edge>();  // bEdges is an array 
						// of "Edge" segment objects that  topologically define the 
						// global boundaries of the domain
	// bounding rectangle and 1/(max dimension of bounding rectangle)
	protected long xmax=Long.MIN_VALUE,xmin=Long.MAX_VALUE;
	protected long ymax=Long.MIN_VALUE,ymin=Long.MAX_VALUE;	
	protected double scale;
	protected CoordTransform coordTrans = null;

	// class varible indicating whether area values have been
	// set in the DagData structure
	private boolean AreaSet = false;
	// class varibles defining the DagTree structure
	protected Arc[] arc; // array of Arc objects
	protected ArrayList<Node> nodes= new ArrayList<Node>();	
	protected ArrayList<Integer> triNode = null; 
	// counts on actual growth of DagData structure
	protected int maxPtCount=0; // index number of points used
	protected int maxArcCount=0; // index number of arcs used 
	protected int maxNodeCount=0; // index number of nodes nodes.size()-1
	
	
	/**
	 Constructor builds and initializes a DagData object
	 @param xIn array of x coordinate variables in micro-degrees
	 @param yIn array of y coordinate variables in micro-degrees
	 @param CoordinateFlag  static CoordTransform constant either 
	 	CARTESIAN or LAT_LONG
	 */
	public DagData(long[] xIn,long[] yIn,int CoordinateFlag){
		nv=xIn.length;
		// get bounding box for input arrays
		long xmaxIn=Long.MIN_VALUE,xminIn=Long.MAX_VALUE;
		long ymaxIn=Long.MIN_VALUE,yminIn=Long.MAX_VALUE;
		for(int i=0;i<xIn.length;i++){
			if(xIn[i]>xmaxIn)xmaxIn=xIn[i];
			if(xIn[i]<xminIn)xminIn=xIn[i];
			if(yIn[i]>ymaxIn)ymaxIn=yIn[i];
			if(yIn[i]<yminIn)yminIn=yIn[i];
		}
		// find center of input array data and set scale
		long X0 = (xmaxIn+xminIn)/2;
		long Y0 = (ymaxIn+yminIn)/2;
		//long dx=xmax-xmin; long dy=ymax-ymin;
		// transfer data to DagData Centered Cartesian Structure
		coordTrans = new CoordTransform((double)Y0,(double)X0,CoordinateFlag);
		// allocate space for DagData centered Cartesian data
		x = new long[xIn.length];
		y = new long[xIn.length];
		z = new double[xIn.length];
		for(int i=0;i<xIn.length;i++){
			x[i] = coordTrans.XcoordTrans(xIn[i]);
			y[i] = coordTrans.YcoordTrans(yIn[i]);	
		}		
		// fill in rest of DagData structure
		z= null;
		bSeg= null;
		vA=null;
		arc= new Arc[6*nv-6];
		setBound();
		setScale();	
		init();
	}
	
	/**
	 Alternate class constructor starts with right 
	 handed integer coordinate data arrays and also
	 includes a optional double[] field of data defined
	 on the same points (nominally a depth coordinate) 
	 @param xIn array of x coordinate variables
	 @param yIn array of y coordinate variables
	 @param CoordinateFlag  static CoordTransform constant either 
	 	CARTESIAN or LAT_LONG
	 @param zIn array of third field associated with
	 	grid points.
	 */	
	public DagData(long[] xIn,long[] yIn,int CoordinateFlag,double[] zIn){
		this(xIn,yIn,CoordinateFlag);
		z= zIn;
	}
	
	/**
	 Alternate class constructor starts with right 
	 handed integer coordinate data arrays and also
	 includes a bSeg array that indicates the fact
	 that the array of verticies's begins with boundary
	 segments listed in ccw order.  This signals the
	 routines that a "constrained" tessellation is 
	 desired. The bSeg array gives "end vertex" indices
	 of the boundary segments. ref: Verdat
	 @param xIn array of x coordinate variables
	 @param yIn array of y coordinate variables
	 @param CoordinateFlag  static CoordTransform constant either 
	 	CARTESIAN or LAT_LONG
	 @param bSegIn array of end vertex indices	 
	 */
	public DagData(long[] xIn,long[] yIn,int CoordinateFlag,int[] bSegIn){
		this(xIn,yIn,CoordinateFlag);
		bSeg=bSegIn;
		fillEdgeList();
	}
	
	/**
	 Alternate class constructor starts with right 
	 handed integer coordinate data arrays with the
	 inclusion of a third field associated with the 
	 same coordinate data (nominally depth) and then 
	 and also includes a bSeg array that indicates the fact
	 that the array of verticies's begins with boundary
	 segments listed in ccw order.  This signals the
	 routines that a "constrained" tessellation is 
	 desired. The bSeg array gives "end vertex" indices
	 of the boundary segments. ref: Verdat
	 @param xIn array of x coordinate variables
	 @param yIn array of y coordinate variables
	 @param CoordinateFlag  static CoordTransform constant either 
	 	CARTESIAN or LAT_LONG
	 @param zIn array of third field associated with
	 	grid points.
	 @param bSegIn array of end vertex indices
	 */
	public DagData(long[] xIn,long[] yIn,int CoordinateFlag, double[] zIn,int[] bSegIn){
		this(xIn,yIn,CoordinateFlag);
		z= zIn;
		bSeg=bSegIn;
		fillEdgeList();
	}
	
	/**
	 Utility to return summary of DagData as String for
	 diagnostic checking
	 */
	public String toString(){
		StringBuffer str = new StringBuffer("Tesselation Data:\n");
		str.append("  vertices= "+nv+"\n");
		str.append("  arcs= "+arc.length+"\n");
		str.append("  nodes= "+nodes.size()+"\n");
		if(triNode!=null){
			str.append("  triangles= "+triNode.size()+"\n");
		}else{
			str.append("  triangles= 0"+"\n");
		}
		str.append("  xmax= "+xmax+"  xmin= "+xmin+"\n");
		str.append("  ymax= "+ymax+"  ymin= "+ymin+"\n");
		return str.toString();
	}
		
	/**
	 Utility method that takes an index to an "Arc" Object
	 in the DagData structure (representing one side of a 
	 triangle) and returns the index of the "Arc" Object in the
	 adjacent neighboring triangle.
	 @param arcIndex
	 @return index to neighbor Arc object in the adjacent triangle	
	 */
	public int mate(int arcIndex){
		return 6*nv-7-arcIndex;
	}
	
	/**
	 Utility method that returns true if the target
	 point specified as x0,y0 is globally interior to
	 the boundary of the domain, otherwise false 
	 @param x0 x location of test point
	 @param y0 y location of test point
	 @return boolean (true) if test point is inside
	 	or the global boundary, otherwise (false)
	 */
	public boolean pointInside(long x0,long y0){
		if(bSeg==null)return true;
		int cross= 0;
		long x1=xmax+1000;long y1=ymax+1000;
				
		for(Edge e:bEdges){
			long p1x=e.x1; long p1y=e.y1;
			long p2x=e.x2; long p2y=e.y2;
			boolean b1= Incircle.CCW(p1x,p1y,p2x,p2y,x0,y0,true);
			boolean b2= Incircle.CCW(p1x,p1y,p2x,p2y,x1,y1,true);
			boolean b3= Incircle.CCW(x0,y0,x1,y1,p1x,p1y,true);
			boolean b4= Incircle.CCW(x0,y0,x1,y1,p2x,p2y,true);
			if((b1!=b2)&&(b3!=b4)){
				cross++;
				
				/*
				if(chkoutput){
					System.out.println("edge-seg-crossed "+e.toString());
				}
				*/
			}
		}
		if((cross%2)==1){return true;}else{return false;}			
	}
	
	/**
	 This is a utility that should only be used after the
	 DagData structure has been build.  It will perform 
	 three tasks that fill topological information into
	 the DatData structure Note: AreaSet initialized to false.
	 1) it creates a list of the triangles by recording
	 a list of indices to "Node" objects that represent
	 terminal nodes.
	 2) it sets the Vernonoi area associated with the nearest
	 neighbor point set for each of the vertices (this is the
	 topological duo of the triangle set) clipping to the global
	 interior of the domain.
	 3) sets the area value of the terminal "Node" objects in
	 square micro_degrees and assigns a - (negative) value to 
	 the value is the triangle is exterior to the global 
	 boundary
	 */
	public void setArea_vA_AndTriISOS(){
		if(AreaSet==true){
			System.out.println("setArea can not be called more then once");
			System.exit(0);
		}else{
			AreaSet= true;
			vA= new double[nv];for(int i=0;i<nv;i++)vA[i]=0.0;
			triNode = new ArrayList<Integer>();
		}		
		for(int i=0;i<nodes.size();i++){
			Node n= nodes.get(i);
			// identify terminal nodes
			if(n.p==Incircle.INF){
				// set terminal triangle
				int a1=n.q; int v1= arc[a1].vert;
				int a2=arc[a1].next; int v2= arc[a2].vert;
				int a3=arc[a2].next; int v3= arc[a3].vert;
				/*				
				boolean chkoutput = false;
				if(((v1==589)&&(v2==590))||
					((v1==590)&&(v3==589))||
					((v2==589)&&(v3==590))){
					System.out.println("found triangle - node "+n.toString());
					System.out.println("v1 "+v1+" v2 "+v2+" v3 "+v3);
					System.out.println("v1-mass "+z[v1]+" v2-mass "+z[v2]+" v3-mass "+z[v3]);
					chkoutput = true;
				}
				*/
				// if vertex at infinity continue
				if((v1==Incircle.INF)||(v2==Incircle.INF)
						||(v3==Incircle.INF))continue;
				
				// calculate area
				double dx1=x[v2]-x[v1];
				double dy1=y[v2]-y[v1];
				double dx2=x[v3]-x[v1];
				double dy2=y[v3]-y[v1];
				n.area=0.5*(dx1*dy2-dy1*dx2);								
				if(bSeg!=null){
					// calculate tri centroid
					long x0=(x[v1]+x[v2]+x[v3])/3;
					long y0=(y[v1]+y[v2]+y[v3])/3;
					/*
					if(chkoutput){
						System.out.println("x0 "+x0+" Y0 "+y0+" area "+n.area+"  "+
								!pointInside(x0,y0));
						chkoutput = false;
					}
					*/
					if(!pointInside(x0,y0))n.area*=-1;
				}
				triNode.add(i);
				//System.out.printf("area calc %f \n", n.area);				
				// calculate z value area
				double da= 0.0;
				if(n.area>0.0)da=n.area/3.0;
				vA[v1]+=da;vA[v2]+=da;vA[v3]=da;
			}
		}
	}
	
	/**
	 Utility that navigates the Dag Tree of the DagData
	 structure and returns the terminal node that 
	 represents the triangle which contains the test point
	 indexed in the DagData arrays by the nextVert index
	 @param nextVert index into the coordinate array of 
	 	test point 
	 @return terminal Node object that represents the triangle
	 	containing the test point
	 */
	public Node getTerminalNode(int nextVert){
		// implements Step T1 p.75
		Node node = nodes.get(0);
		int termVert=node.p;
		while(termVert != Incircle.INF){
			if(Incircle.test(Incircle.INF,nextVert,node.p,node.q,x,y)){
				node=nodes.get(node.ln);
			}else{
				node=nodes.get(node.rn);
			}
			termVert=node.p;
		}
		return node;
	}
	
	/**
	 Alternate form of the utility that navigates the 
	 Dag Tree of the DagData structure and returns 
	 the terminal node that represents the triangle 
	 which contains the test point specified by x0,y0
	 which may not be an indexed vertex in the DagData
	 structure.
	 @param x0 x coorinate of test point
	 @param y0 y coorinate of test point
	 @return terminal Node object that represents the triangle
	 	containing the test point
	 */
	public Node getTerminalNode(long x0,long y0){
		Node node= nodes.get(0);
		int termVert= node.p;
		while(termVert != Incircle.INF){
			boolean PtOrder=true;
			if(node.p>node.q)PtOrder=false;
			if(Incircle.CCW(x[node.p],y[node.p],x[node.q],y[node.q],
			x0,y0,PtOrder)){
				node=nodes.get(node.ln);
			}else{
				node=nodes.get(node.rn);
			}
		}
		return node;
	}
	
	/**
	 Utility method that checks the coordinates of a 
	 vertex indexed in the point arrays against the 
	 coordinates of a terminal node triangle's vertices
	 and returns the square of the minimum separation
	 @param termNode terminal Node object whoes vertices
	 	are checked for distance from test point
	 @param ptIndex index to test point in DagData 
	 	coordinate arrays
	 @return square of minimum separation distance in
	 	square micro-degrees
	 */
	public TriNeighbor minSeparation(Node termNode,int ptIndex){
		long sepSq = Long.MAX_VALUE;
		int neighbor = -1;
		int a1=termNode.q;
		int v1= arc[a1].vert;
		int a2= arc[a1].next;
		int v2= arc[a2].vert;
		int a3= arc[a2].next;
		int v3= arc[a3].vert;
		long x0 = x[ptIndex],y0 = y[ptIndex];
		long x1,y1,distSq;
		if(v1!=Incircle.INF){
			x1 = x[v1];y1 = y[v1];
			distSq = (x1-x0)*(x1-x0)+(y1-y0)*(y1-y0);
			if(distSq<sepSq){sepSq = distSq;neighbor = v1;}
		}
		if(v2!=Incircle.INF){
			x1 = x[v2]; y1 = y[v2];
			distSq = (x1-x0)*(x1-x0)+(y1-y0)*(y1-y0);
			if(distSq<sepSq){sepSq = distSq;neighbor = v2;}	
		}
		if(v3!=Incircle.INF){
			x1 = x[v3]; y1 = y[v3];
			distSq = (x1-x0)*(x1-x0)+(y1-y0)*(y1-y0);
			if(distSq<sepSq){sepSq = distSq;neighbor = v1;}	
		}
		TriNeighbor triNeighbor = new TriNeighbor(neighbor,sepSq);
		return triNeighbor;
	}
	
	
	/**
	 Utility method that implements a step in the Knuth algorithm
	 implements step T2 p.75 
	 @param termNode  Node to be erconfigured
	 @param newPt index to new point to be added to DagData
	 */
	public void setNewEdges(Node termNode,int newPt){
		// implements Step T2 p.75
		maxPtCount++;
		int p=newPt;
		int a=termNode.q;
		int b=arc[a].next;
		int c=arc[b].next;
		int q=arc[a].vert;
		int r=arc[b].vert;
		int s=arc[c].vert;
		int n1=++maxNodeCount;//lambda'
		int n2=++maxNodeCount;//lambda''
		int n3=++maxNodeCount;//lambda'''
		// indexes to new arcs
		int a1=++maxArcCount, b1=mate(a1);//a(j-2) & b(j-2)
		int a2=++maxArcCount, b2=mate(a2);//a(j-1) & b(j-1)
		int a3=++maxArcCount, b3=mate(a3);//a(j) & b(j)
		// add nodes
		nodes.add(new Node(Incircle.INF,a,-1,-1));
		nodes.add(new Node(Incircle.INF,a3,-1,-1));
		nodes.add(new Node(Incircle.INF,c,-1,-1));
		//add arcs
		arc[a3]=new Arc(q,b,n2);
		arc[a2]=new Arc(r,c,n3);
		arc[a1]=new Arc(s,a,n1);
		arc[b3]=new Arc(p,a1,n1);
		arc[b2]=new Arc(p,a3,n2);
		arc[b1]=new Arc(p,a2,n3);
		// change arc values
		arc[a].next=b3;
		arc[a].inst=n1;
		arc[b].next=b2;
		arc[b].inst=n2;
		arc[c].next=b1;
		arc[c].inst=n3;
		if(q!=Incircle.INF){
			//implements Step T3 p.76
			int nu=++maxNodeCount;
			nodes.add(new Node(q,p,n1,n2));
			int nu1=++maxNodeCount;
			nodes.add(new Node(s,p,n3,n1));
			termNode.p=r;
			termNode.q=p;
			termNode.ln=nu;
			termNode.rn=nu1;
		}else{
			//implements Step T4 p.76
			int n4=++maxNodeCount;
			nodes.add(new Node(s,p,n3,n1));
			termNode.p=r;
			termNode.q=p;
			termNode.ln=n2;
			termNode.rn=n4;
			int mu=n4;
			int dd=arc[mate(a)].next;
			int t=arc[dd].vert;
			while((t!=r)&&(Incircle.test(Incircle.INF,p,s,t,x,y))){
				int nu=++maxNodeCount;
				nodes.add(new Node(Incircle.INF,dd,-1,-1));
				Node tempNode=nodes.get(mu);
				tempNode.rn=arc[dd].inst;
				mu=arc[dd].inst;
				tempNode =nodes.get(mu);
				tempNode.p=t;
				tempNode.q=p;
				tempNode.ln=nu;
				tempNode.rn=n1;
				//System.out.println("flip called in step 4- arc= "+a);
				flip(a,mate(a),dd,s,Incircle.INF,t, p,nu,n1);
				a=arc[mate(a)].next;
				dd=arc[mate(a)].next;
				s=t;
				t=arc[dd].vert;
				tempNode=nodes.get(n1);
				tempNode.q=a;			
			}
			int nu=++maxNodeCount;
			nodes.add(new Node(Incircle.INF,arc[dd].next,-1,-1));
			Node tempNode = nodes.get(arc[dd].inst);
			tempNode.p=s;
			tempNode.q=p;
			tempNode.ln=nu;
			tempNode.rn=n1;
			arc[dd].inst=nu;
			arc[arc[dd].next].inst=nu;
			arc[arc[arc[dd].next].next].inst=nu;
			r=s;
		}
		// implementation of Step T5
		boolean InStep5= true;
		while(InStep5){
			InStep5=false;
			int dd=mate(c);
			int e=arc[dd].next;
			int t=arc[dd].vert;
			int t1=arc[c].vert;
			int t11=arc[e].vert;
			// this is a check to see if an attempt is
			// being made to reorient a sacred side						
			boolean ssCk=false;				
			if(AreaSet){
				//check for change in area parity
				Node cn=nodes.get(arc[c].inst);
				double area1=cn.area;
				Node dn=nodes.get(arc[dd].inst);
				double area2=dn.area;
				if(area1*area2<=0){
					ssCk=true;
					//System.out.printf("ss found via area %f  %f  \n",area1,area2);
				}
			}else{
				// check side list
				ssCk=sacredSide(t,t1);
			}
			if(!ssCk &&(t11!=Incircle.INF)&&(Incircle.test(t11,t1,t,p,x,y))){
				int nu=++maxNodeCount;
				nodes.add(new Node(Incircle.INF,e,-1,-1));
				int nu1=++maxNodeCount;
				nodes.add(new Node(Incircle.INF,dd,-1,-1));
				Node tempNode1=nodes.get(arc[c].inst);
				Node tempNode2=nodes.get(arc[dd].inst);
				tempNode1.p=tempNode2.p=t11;
				tempNode1.q=tempNode2.q=p;
				tempNode1.ln=tempNode2.ln=nu;
				tempNode1.rn=tempNode2.rn=nu1;
				//System.out.println("flip called in step 5- arc= "+c);
				flip(c,dd,e,t,t1,t11,p,nu,nu1);
				c=e;
				InStep5=true;
			}else{
				if(t1!=r){
					c=arc[mate(arc[c].next)].next;
					InStep5=true;
				}				
			}
		}
	}
	
	/**
	 This is a helper utility that takes a point defined
	 by x0,y0 and an array of state data defined on the 
	 same point set as the vertices (for example depth)
	 and using first order shape functions returns an 
	 interpolated value of the state data for that point.
	 @param x0 x coordinate of desired interpolated answer
	 @param y0 y coordinate of desired interpolated answer
	 @param val array of field data to be sampled by the
	  	interpolation procedure
	 @return interpolated value of field (val) at location
	 	x0,y0
	 */
	public double interpolate(long x0,long y0,double[] val){
		int v1,v2,v3;
		double f1,f2,f3;
		Node n= getTerminalNode(x0,y0);
		if(n.area<0.0)return Double.NEGATIVE_INFINITY;
		int a= n.q; v1=arc[a].vert;
		a=arc[a].next; v2=arc[a].vert;
		a=arc[a].next; v3=arc[a].vert;
		double x1=x[v1]-x0,y1=y[v1]-y0;
		double x2=x[v2]-x0,y2=y[v2]-y0;
		double x3=x[v3]-x0,y3=y[v3]-y0;
		f3=(x1*y2-x2*y1)/n.area;
		f2=-(x1*y3-x3*y1)/n.area;
		f1=1.0-f2-f3;
		return f1*val[v1]+f2*val[v2]+f3*val[v3];		
	}

	/**
	 Utility method that takes an array of field data
	 defined on the vertex points of the DagData and a 
	 target value. It returns a list of "Polygon" objects which 
	 will be lists of MicroPoints listed in ccw order such that
	 going from p1 => p2 will leave high ground to the left.
	 The first point will be repeated at  the end of each
	 Polygon
	 @param field and array of field data to be contoured
	 @param val value of desired contour line
	 @param scaleVal separate scaling value available to 
	 	labeling contour in graphics routines if needed
	 @return an array of Polygon objects making up the line 
	 	elements of the contour line (which may be multiply
	 	connected)
	 */
	public ArrayList<Edge>  contourLine(double[] field,double val,double scaleVal){

		Long sX=Long.MAX_VALUE,sY=Long.MAX_VALUE,
			 eX=Long.MAX_VALUE,eY=Long.MAX_VALUE;
		boolean r1chk = false; boolean r2chk =false; boolean r3chk = false;
		Long r1x=Long.MIN_VALUE,r1y=Long.MIN_VALUE;
		long r2x=Long.MIN_VALUE,r2y=Long.MIN_VALUE;
		long r3x=Long.MIN_VALUE,r3y=Long.MIN_VALUE;
		ArrayList<Edge> ans = new ArrayList<Edge>();
		
		double max=Double.MIN_VALUE;
		double min=Double.MAX_VALUE;
		for(double d:field){
			if(d>max)max=d;
			if(d<min)min=d;
		}
		// check if contour val is out of range
		if((val>max)||(val<min))return null;
		// loop on triangles
		for(int i:triNode){
			Node n=nodes.get(i);
			if(n.area<=0.0)continue;
			int v1=-1,v2=-1,v3=-1;
			double f1=0,f2=0,f3=0;
			// build triangle data for ccw view
			try{
				int a= n.q;	v1=arc[a].vert; 	f1= field[v1];
				a=arc[a].next; 	v2=arc[a].vert; 	f2= field[v2];
				a=arc[a].next; 	v3=arc[a].vert; 	f3= field[v3];
			}catch(Exception e){
				System.out.println("found exception");
				System.out.println("triangle node n "+n.toString()
						+"  "+v1+"  "+v2+"  "+v3+" f1 "+f1+" f2 "+f2+" f3 "+f3);
			}
			
			/*
			if(check==14666){
				System.out.println("triangle node n "+n.toString()
						+"  "+v1+"  "+v2+"  "+v3);
				System.out.println("v1-1 "+(v1-1)+" ("+x[(v1-1)]+","+y[(v1-1)]+")");
				System.out.println("v1 "+v1+" ("+x[v1]+","+y[v1]+")");
				System.out.println("v2 "+v2+" ("+x[v2]+","+y[v2]+")");
				System.out.println("v2+1 "+(v2+1)+" ("+x[(v2+1)]+","+y[(v2+1)]+")");
				System.out.println("v3 "+v3+" ("+x[v3]+","+y[v3]+")");
				System.out.println("3794 ("+x[3794]+","+y[3794]+")");
				System.out.println("6277 ("+x[6277]+","+y[6277]+")");
			}
			*/
			
			//if(n.area<=0.0)continue;
			
			// no contour in the trangle continue
			if((val>f1)&&(val>f2)&&(val>f3))continue;
			if((val<f1)&&(val<f2)&&(val<f3))continue;
			
			/*
			//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			if(ans.size()==69){
				System.out.println("about to add edge 70");
			}
			//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			*/
			// side one is a contour
			if((val==f1)&&(val==f2)){
				sX=x[v1];sY=y[v1];
				eX=x[v2];eY=y[v2];
				if(f3>=val){ // triangle flat or higher then edge
					ans.add(new Edge(sX,sY,eX,eY,v1,v2,v3));
				}else{
					ans.add(new Edge(eX,eY,sX,sY,v1,v2,v3));
				}
			}
			if((val==f2)&&(val==f3)){
				sX=x[v2];sY=y[v2];
				eX=x[v3];eY=y[v3];
				if(f1>=val){ // triangle flat or higher then edge
					ans.add(new Edge(sX,sY,eX,eY,v1,v2,v3));
				}else{
					ans.add(new Edge(eX,eY,sX,sY,v1,v2,v3));
				}
			}
			if((val==f3)&&(val==f1)){
				sX=x[v3];sY=y[v3];
				eX=x[v1];eY=y[v1];
				if(f2>=val){ // triangle flat or higher then edge
					ans.add(new Edge(sX,sY,eX,eY,v1,v2,v3));
				}else{
					ans.add(new Edge(eX,eY,sX,sY,v1,v2,v3));
				}
			}
			// at this point the contour crosses two interior side
			// find if contour crosses side one
			r1chk=r2chk=r3chk=false;
			double r1=(f1-val)/(f1-f2);
			if((r1>=0)&&(r1<1.0)){
				r1chk=true;
				r1x=(long)((1.0-r1)*x[v1]+r1*x[v2]);
				r1y=(long)((1.0-r1)*y[v1]+r1*y[v2]);
			}
			// find if contour crosses side two
			double r2=(f2-val)/(f2-f3);
			if((r2>=0)&&(r2<1.0)){
				r2chk=true;
				r2x=(long)((1.0-r2)*x[v2]+r2*x[v3]);
				r2y=(long)((1.0-r2)*y[v2]+r2*y[v3]);
			}
			// find if contour crosses side three
			double r3=(f3-val)/(f3-f1);
			if((r3>=0)&&(r3<1.0)){
				r3chk=true;
				r3x=(long)((1.0-r3)*x[v3]+r3*x[v1]);
				r3y=(long)((1.0-r3)*y[v3]+r3*y[v1]);
			}
			// set contour line
			if(!r3chk){
				if(f2>=val){ // f2 greater then contour
					ans.add(new Edge(r2x,r2y,r1x,r1y,v1,v2,v3));
				}else{
					ans.add(new Edge(r1x,r1y,r2x,r2y,v1,v2,v3));
				}
			}			
			if(!r1chk){
				if(f3>=val){ // f3 greater then contour
					ans.add(new Edge(r3x,r3y,r2x,r2y,v1,v2,v3));				
				}else{
					ans.add(new Edge(r2x,r2y,r3x,r3y,v1,v2,v3));
				}
			}
			if(!r2chk){
				if(f3>=val){ // f3 greater then contour
					ans.add(new Edge(r3x,r3y,r1x,r1y,v1,v2,v3));				
				}else{
					ans.add(new Edge(r1x,r1y,r3x,r3y,v1,v2,v3));
				}
			}
			/*
			//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			if(ans.size()==70){
				System.out.println("edge 70 "+ans.get(ans.size()-1));
			}
			//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			 */
			 
		}
		
		return ans;
		/*
		//  rewrite as list of polygons
		ArrayList<Polygon> polycontour = EdgePoint.EdgePtLst2PolyLst(ans, val,scaleVal);
		return polycontour;
		*/
	}
	
	/**
	 This method will take a list of Edges objects (nominally a contour with
	 ccw ordering) and calculate the enclosed area
	 @param contour a list of edges that represent a multi-polygon countour
	 	with Edge object expressed in micro-degrees.
	 @param Isotropic boolean flag to indicate whether the coordinates are
	 	Cartesian and isotropic (true) or Lat/Long so a map factor is needed (false)
	 @return area enclosed by the contour converted to km^2 
	 */
	public double contourArea(ArrayList<Edge> contour){
		// find latitude values.
		Long yMax = Long.MIN_VALUE;
		Long yMin = Long.MAX_VALUE;
		for(Edge eg:contour){
			if(eg.y1<yMin)yMin=eg.y1;
			if(eg.y2<yMin)yMin=eg.y2;		
			if(eg.y1>yMax)yMax=eg.y1;
			if(eg.y2>yMax)yMax=eg.y2;
		}
		// calculate areas		
		double area = 0;
		for(Edge eg:contour){
			area+= (eg.x2-eg.x1)*((eg.y2+eg.y1)/2 - yMin);			
		}
		area/=1e12; // convert to degrees squared
		area*= -(111.111*111.111/coordTrans.DiffArea); // convert to km^2		
		return area;		
	}
	
	/**
	 * Utilit method to write out a binary copy of the array of 
	 * edges created by the contour utility
	 * @param edges ArrayList<Edge> array of Edge objects
	 * @param fileOut path to file that is written out 
	 */
	public void exportEdgeArray(ArrayList<Edge> edges, String fileOut){
		try{
			ObjectOutputStream out = new ObjectOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(fileOut)));
			out.writeObject(edges);
			out.close();			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Utility to read in a binary copy of an ArrayList<Edges>
	 * @param fileIn
	 * @return ArrayList<Edge>
	 */
	@SuppressWarnings("unchecked")
	static public ArrayList<Edge> importEdgeArray(String fileIn){
		ArrayList<Edge> edges = null;
		try{
			ObjectInputStream in = new ObjectInputStream(
					new BufferedInputStream(
							new FileInputStream(fileIn)));
			edges = (ArrayList<Edge>)in.readObject();
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return edges;
	}
	
	/**
	 This public utility will output a file representing a array of polygon 
	 objects features following the GeoJSON Format Specification (Butler,et.al.)
	 @param contoursIn is a ArrayList<ArrayList<Polygon>> containing polygons
	 @param fileOut String name of the output file that will be created for the file
	 */
	public void exportContoursGeoJSON(ArrayList<ArrayList<Polygon>>polygonsIn,String fileOut){
		// open a file for output
		FileOutputStream file=null;
		try {
			file = new FileOutputStream(fileOut);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter out = new PrintWriter(file);
		
		// calculate a bounding box
		Double xmin=Double.MAX_VALUE,xmax=Double.MIN_VALUE,
				ymin=Double.MAX_VALUE,ymax=Double.MIN_VALUE;
		for(int k=0;k<polygonsIn.size();k++){
			ArrayList<Polygon> polys = polygonsIn.get(k);
			for(int j=0;j<polys.size();j++){				
				Polygon thispoly = polys.get(j);
				ArrayList<MicroPoint> poly = thispoly.poly;
				for(int i=0;i<poly.size();i++){
					double x = 1e-6*poly.get(i).x;
					double y = 1e-6*poly.get(i).y;
					if(x>xmax)xmax=x;
					if(x<xmin)xmin=x;
					if(y>ymax)ymax=y;
					if(y<ymin)ymin=y;
				}
			}
		}				
		// build header
		String str;
		StringBuffer geojson = new StringBuffer();
		str = "{\n"; geojson.append(str);
		str = "\"type\":\"FeatureCollection\",\n"; geojson.append(str);
		str = "\"bbox\":["+xmin.toString()+","+xmax.toString()+","
				+ymin.toString()+","+ymax.toString()+"],\n"; geojson.append(str);
		str = "\"features\":\n"; geojson.append(str);
		str = "[\n"; geojson.append(str);		
		for(int j=0;j<polygonsIn.size();j++){
			ArrayList<Polygon> polys = polygonsIn.get(j);
			Double val=-1.0;
			Double area=-1.0;
			Boolean jumpFlag = false;
			for(int i=0;i<polys.size();i++){				
				if(i!=0)geojson.append(",\n");
				str = "{\n"; geojson.append(str);
				str = "\"type\":\"Feature\",\n"; geojson.append(str);	
				str = "\"geometry\":\n"; geojson.append(str);
				str = "{\n"; geojson.append(str);
				str = "\"type\":\"Polygon\",\n"; geojson.append(str);
				str = "\"coordinates\": [\n"; geojson.append(str);
				str = "[\n"; geojson.append(str);				
				ArrayList<MicroPoint> points = polys.get(i).poly;
				val = polys.get(i).aux;
				area = polys.get(i).area;
				jumpFlag = false;
				if(points.size()<4){
					jumpFlag = true;
					continue; // LinearRing with zero area
				}
				for(int ii=0;ii<points.size();ii++){
					Float x1 = (float)(1e-6*points.get(ii).x);
					Float y1 = (float)(1e-6*points.get(ii).y);
					str = "["+x1.toString()+","+y1.toString()+"]";
					geojson.append(str);
					if(ii!=points.size()-1){str = ","; geojson.append(str);}
					geojson.append("\n");
				}
				str = "]\n"; geojson.append(str);
				str = "]\n"; geojson.append(str);
				str = "},\n"; geojson.append(str);
				if(!jumpFlag){
					str = "\"properties\":\n"; geojson.append(str);
					str = "{\n"; geojson.append(str);
					str = "\"value\":"+val.toString()+",\n"; geojson.append(str);
					str = "\"area\":"+area.toString()+"\n"; geojson.append(str);
					str = "}\n"; geojson.append(str);
					
					str = "}\n"; geojson.append(str);
				}				
			}	
			
			if(j!=polygonsIn.size()-1)geojson.append(",");
			geojson.append("\n");
		}			
		str = "]\n"; geojson.append(str);
		str = "}\n"; geojson.append(str);
		// write out StringBuffer		
		out.print(geojson.toString());
		out.close();
	}
	
	/**
	 This is a utility that will write out the edge segments
	 contained in an ArrayList<ArrayList<Edge>> of contours in a 
	 binary Shapefile as defined in the ESRI Shapefile Technical
	 Description - ESRI White Paper - July 1998.  The format 
	 requires both BIG_ENDIAN and LITTLE_INDIAN byte orders in 
	 the same file (any body's guess why that is the case ???)
	 To do this the routine calls a static method Endian.change()
	 which is included in the tiangle.lib.jar.  The ESRI shape 
	 defined in the Record Content is a single PolyLineM collection
	 if the PolyLine parts all made up of 2 points. The D value
	 associated with each point is the Eulerian density contour 
	 value in (parts per thousand) per square kilometer. For each
	 point it is calculated by baseValue*vals[i]
	 @param contoursIn contours an ArrayList<ArrayList<Edge>>
	 @param fileOut name of output file should end in .shp
	 @param baseValue base value of contour intervals in parts per thousand
	 @param vals array of multiplicative factors for base contour value
	 */
	public void exportContourShapeFile(ArrayList<ArrayList<Edge>>contoursIn,String fileOut,
			Float baseValue,float[] vals){
		// calculate a bounding box and total number of records
		// and arrays needed for record of contents
		ArrayList<Integer> parts = new ArrayList<Integer>();
		ArrayList<Double> xpt = new ArrayList<Double>();
		ArrayList<Double> ypt = new ArrayList<Double>();
		ArrayList<Double> Mval = new ArrayList<Double>();
		double Dmin=Double.MAX_VALUE,Dmax=Double.MIN_VALUE;
		double xmin=Double.MAX_VALUE,xmax=Double.MIN_VALUE,
				ymin=Double.MAX_VALUE,ymax=Double.MIN_VALUE;
		int numRecords = 0;
		int pointCount = 0;
		for(int j=0;j<contoursIn.size();j++){
			ArrayList<Edge> contour = contoursIn.get(j);
			numRecords+=contour.size();
			double D = baseValue*vals[j];
			if(D>Dmax)Dmax=D;if(D<Dmin)Dmin=D;
			for(int i=0;i<contour.size();i++){
				Edge eg = contour.get(i);
				parts.add(pointCount);
				xpt.add(1e-6*eg.x1);ypt.add(1e-6*eg.y1);Mval.add(D);
				;pointCount++;
				xpt.add(1e-6*eg.x2);ypt.add(1e-6*eg.y2);Mval.add(D);
				pointCount++;				
				double x1 = 1e-6*eg.x1;
				if(x1>xmax)xmax=x1;
				if(x1<xmin)xmin=x1;
				double x2 = 1e-6*eg.x2;
				if(x2>xmax)xmax=x2;
				if(x2<xmin)xmin=x2;				
				double y1 = 1e-6*eg.y1;
				if(y1>ymax)ymax=y1;
				if(y1<ymin)ymin=y1;
				double y2 = 1e-6*eg.y2;
				if(y2>ymax)ymax=y2;
				if(y2<ymin)ymin=y2;
			}
		}
		// the file for output
		DataOutputStream out=null;
		try {
			out = new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(fileOut)));
			// write out file header
			int FileCode = 9994;
			out.writeInt(FileCode);
			int pad = 0;
			out.writeInt(pad);out.writeInt(pad);out.writeInt(pad);
			out.writeInt(pad);out.writeInt(pad);
			int FileLength = 84+26*numRecords;
			out.writeInt(FileLength);
			int Version = 1000;
			out.writeInt(Endian.change(Version));
			int ShapeType = 23;
			out.writeInt(Endian.change(ShapeType));
			// bounding boxes
			out.writeDouble(Endian.change(xmin));
			out.writeDouble(Endian.change(ymin));
			out.writeDouble(Endian.change(xmax));
			out.writeDouble(Endian.change(ymax));
			out.writeDouble(Endian.change(0.0));
			out.writeDouble(Endian.change(0.0));
			out.writeDouble(Endian.change(0.0));
			out.writeDouble(Endian.change(0.0));
			
			// write out the record header
			int RecordNumber = 1;
			out.writeInt(RecordNumber);
			int ContentLength = 30+26*numRecords;
			out.writeInt(ContentLength);
			
			// write out the record content
			out.writeInt(Endian.change(ShapeType));
			out.writeDouble(Endian.change(xmin));
			out.writeDouble(Endian.change(ymin));
			out.writeDouble(Endian.change(xmax));
			out.writeDouble(Endian.change(ymax));
			int NumParts = numRecords;
			out.writeInt(Endian.change(NumParts));
			int NumPoints = 2*NumParts;
			out.writeInt(Endian.change(NumPoints));
			// parts array
			for(int i=0;i<parts.size();i++){
				out.writeInt(Endian.change(parts.get(i)));
			}
			// points array of xpt a and ypt data
			for(int i=0;i<xpt.size();i++){
				out.writeDouble(Endian.change(xpt.get(i)));
				out.writeDouble(Endian.change(ypt.get(i)));
			}
			
			// M data
			out.writeDouble(Endian.change(Dmin));
			out.writeDouble(Endian.change(Dmax));
			for(int i=0;i<Mval.size();i++){
				out.writeDouble(Endian.change(Mval.get(i)));
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 This utility will apply a Laplassian smoother to to
	 a field defined by a zIn array - the smoothing kernel 
	 is the set of points in Voronoi diagram associated with
	 the data point after the mean value for the Delaunay 
	 have been calculated. 
	 @param d reference to the DaData being used
	 @param zIn an array of field data defined on DagData vertices's
	 @return an array of the smoothed data field
	 */
	public double[] smooth(DagData d,double[] zIn){
		double[] zOut = new double[d.nv];
		double[] weight = new double[d.nv];
		for(int i:d.triNode){
			Node n= d.nodes.get(i);
			if(n.area<=0.0)continue;
			// set terminal triangle
			int a1=n.q; int v1= d.arc[a1].vert;
			int a2=d.arc[a1].next; int v2= d.arc[a2].vert;
			int a3=d.arc[a2].next; int v3= d.arc[a3].vert;
			double aver = (zIn[v1]+zIn[v2]+zIn[v3])*n.area/3.0;
			zOut[v1]+=aver; weight[v1]+=n.area;
			zOut[v2]+=aver; weight[v2]+=n.area;
			zOut[v3]+=aver; weight[v3]+=n.area;
		}
		for(int i=0;i<zOut.length;i++){
			zOut[i] = zOut[i]/weight[i];
		}
		if(bSeg!=null){ // set boundary values to 0
			int lastBoundPt = bSeg[bSeg.length-1];
			for(int i=0;i<lastBoundPt+1;i++)zOut[i]=0.0;
		}
		return zOut;
	}
	
	/**
	 Utility method to export to a file a binary persistent
	 copy of the DagData object
	 @param fileName for the output
	 */
	public void exportDagData(String fileName){
		try{
			ObjectOutputStream out = new ObjectOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(fileName)));
			out.writeObject(this);
			out.close();			
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	/**
	 Utility method to import a binary persistent file
	 copy of the DagData object
	 @param fileName for the input
	 */
	static public DagData importDagData(String fileName){
		DagData dd = null;
		try{
			ObjectInputStream in = new ObjectInputStream(
					new BufferedInputStream(
							new FileInputStream(fileName)));
			dd = (DagData)in.readObject();
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return dd;
	}
	
	/**
	 Getter to access class field as read only
	 @return DagData.x
	 */
	public long[] getX() {
		return x;
	}
	
	/**
	 Getter to access class field as read only
	 @return DagData.y
	 */
	public long[] getY() {
		return y;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.z
	 */
	public double[] getZ() {
		return z;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.nv
	 */
	public int getNv() {
		return nv;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.bSeg
	 */
	public int[] getbSeg() {
		return bSeg;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.vA
	 */
	public double[] getvA() {
		return vA;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.xmax
	 */
	public long getXmax() {
		return xmax;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.xmin
	 */
	public long getXmin() {
		return xmin;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.ymax
	 */
	public long getYmax() {
		return ymax;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.ymin
	 */
	public long getYmin() {
		return ymin;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.scale
	 */
	public double getScale() {
		return scale;
	}
	
	/**
	 * Getter ot access CoordTrasform
	 * @return
	 */
	public CoordTransform getCoordTrans() {
		return coordTrans;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.AreaSet
	 */
	public boolean isAreaSet() {
		return AreaSet;
	}
	
	/**
	 * reset AreaSet flag;
	 */
	public void unsetAreaSetFlag(){
		if(AreaSet)AreaSet=false;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.arc
	 */
	public Arc[] getArc() {
		return arc;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.node
	 */
	public ArrayList<Node> getNodes() {
		return nodes;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.triN0de
	 */
	public ArrayList<Integer> getTriNode() {
		return triNode;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.maxPtCount
	 */
	public int getMaxPtCount() {
		return maxPtCount;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.maxArcCount
	 */
	public int getMaxArcCount() {
		return maxArcCount;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.maxNodeCount
	 */
	public int getMaxNodeCount() {
		return maxNodeCount;
	}

	/**
	 Getter to access class field as read only
	 @return DagData.bEdges
	 */
	public ArrayList<Edge> getbEdges() {
		return bEdges;
	}

	/**
	 Utility to scan the input vertex point data and set
	 the bounding box values in DagData. The user should
	 never have to call this method directly.
	 */
	private void setBound(){
		for(int i=0;i<nv;i++){
			if(x[i]>xmax)xmax=x[i];
			if(x[i]<xmin)xmin=x[i];
			if(y[i]>ymax)ymax=y[i];
			if(y[i]<ymin)ymin=y[i];
		}
	}
	
	/**
	 Utility to read bounding box data and set
	 the scale value 1/(max dimension of the 
	 bounding rectangle) in the DagData. The
	 user should never have to call this method 
	 directly
	 */
	private void setScale(){
		long dx=xmax-xmin; long dy=ymax-ymin;
		scale= 1.0/Math.max(dx,dy);
	}
	
	/**
	 This method will initialize the Arc and 
	 Node structure using the first two vertex
	 points
	 */
	private void init(){
		// set up first two points ref: p75 (18.1)
		int u=0,v=1;
		int a1=0,b1=mate(a1);
		int a2=1,b2=mate(a2);
		int a3=2,b3=mate(a3);
		int n1=1,n2=2;
		arc[a1]=new Arc(v,a2,n1);
		arc[a2]=new Arc(Incircle.INF,a3,n1);
		arc[a3]=new Arc(u,a1,n1);
		arc[b1]=new Arc(u,b3,n2);
		arc[b2]=new Arc(v,b1,n2);
		arc[b3]=new Arc(Incircle.INF,b2,n2);
		nodes.add(new Node(u,v,n1,n2));
		nodes.add(new Node(Incircle.INF,a2,-1,-1));
		nodes.add(new Node(Incircle.INF,b3,-1,-1));
		maxPtCount=1;//(0-1) two points
		maxArcCount=2;//(0-2) three arcs + three mates
		maxNodeCount=2;//(0-2)	three nodes	
	}
	
	/**
	 Private method used internally by the DagTri generation
	 routines.  The user should never have to call this method
	 directly
	 @param c point index
	 @param d point index
	 @param e point index
	 @param t triangle index
	 @param t1 triangle index
	 @param t11 triangle index
	 @param p point index
	 @param n node index
	 @param n1 node index
	 */
	private void flip(int c,int d,int e,int t,int t1,int t11,int p,int n,int n1){
		int e1=arc[e].next; // node f
		int c1=arc[c].next; // node a
		int c11=arc[c1].next; // node b
		arc[e].next=c;
		arc[c].next=c11;
		arc[c11].next=e;
		arc[e].inst=arc[c].inst=arc[c11].inst=n;
		arc[c].vert=p;
		arc[d].next=e1;
		arc[e1].next=c1;
		arc[c1].next=d;
		arc[d].inst=arc[e1].inst=arc[c1].inst=n1;
		arc[d].vert=t11;
	}
	
	
	/**
	 Private method used internally by the DagTri generation
	 routines.  The user should never have to call this method
	 directly 
	 */
	private void fillEdgeList(){
		int j=0;
		int start=0;
		for(int i=0;i<=bSeg[bSeg.length-1];i++){
			int p1=i;int p2=p1+1;
			if(p1==bSeg[j]){
				p2=start;
				start=bSeg[j]+1;
				j++;
			}
			int lowV= Math.min(p1,p2);
			int highV= Math.max(p1,p2);
			long x10= x[lowV]; long y10= y[lowV];
			long x20= x[highV]; long y20= y[highV];
			bEdges.add(new Edge(lowV,highV,x10,y10,x20,y20));
		}
	}
	
	/**
	 Utility method that returns true if the edge defined 
	 by the line segment from p1 to p2 is a sacred edge that
	 can not be reoriented with a flip operation
	 @param p1
	 @param p2
	 @return boolean (true) edge is sacredSide (false) otherwise
	 */
	private boolean sacredSide(int p1,int p2){
		// this should identify all sacred side
		// that should never be reoriented with a flip
		if(bSeg==null)return false;
		// point at INF does not count
		if((p1==Incircle.INF)||(p2==Incircle.INF))return false;
		int lowV= Math.min(p1,p2);
		int highV= Math.max(p1,p2);
		// check if high point > last seg
		if(highV>bSeg[bSeg.length-1])return false;
		// OK so we have to check bEdges list
		for(int i=0;i<bEdges.size();i++){
			Edge e=bEdges.get(i);
			if((e.p1==lowV)&&(e.p2==highV))return true;
		}
		return false;
	}

	public void setZ(double[] z) {
		this.z = z;
	}

	@Override
	public Double AddSplotMasses(int indexS1, int indexS2) {
		// TODO Auto-generated method stub
		return null;
	}
}