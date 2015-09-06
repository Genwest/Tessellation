package hbl.jag.tri.lib;


import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GAxmlHandler extends DefaultHandler implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// local flag variables
	boolean consoleFlag=false;
	boolean mapFlag=false;
	boolean splotFlag=false;
	boolean neigFlag=false;
	boolean massFlag=false;
	boolean smoothFlag=false;
	boolean contoursFlag=false;
	boolean contourFlag=false;
	boolean binpolFlag=false;
	boolean csvpolFlag=false;
	boolean csvLLpolFlag=false;
	boolean bnapolFlag=false;
	boolean geojFlag=false;
	boolean areastatFlag=false;
	boolean picFlag=false;
	boolean metadFlag=false;
	String focus;
	// public data variables reflecting the GAxml state
	boolean verbose = true; // default value is to show diagnostics
	String mapFilePath = null; // pathname to thinned bna map file
	String splotFilePath = null; // pathname to target splot file
	boolean splotAutoFlag = false; // should splots be automatic
	String splotFileToken = "moss"; // either "moss" or "forcst"
	double neigDistLimit = -1; // minimum distance between splots
	boolean implementVarableMass = false; // for future variable mass splots
	String massFractionToken = null;
	double userDefinedMass = 0;
	int numSmooth = -1;
	ArrayList<Double> contVal = new ArrayList<Double>();
	ArrayList<Color> contCol = new ArrayList<Color>();
	int tempR,tempG,tempB;
	HashMap<String,String> outMap = new HashMap<String,String>();
	String tempFile = null;
	boolean metaactive = true;
	String metafilePath = null;
	ArrayList<String> metaStrings = new ArrayList<String>();
	
	public GAxmlHandler(){		
		File xmlFile = new File("/home/jag/Desktop/GABriefing/GAconfig.xml");
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser parser = null;
		spf.setNamespaceAware(true);
		spf.setValidating(true);
		System.out.println("Parser namespace aware = "+(spf.isNamespaceAware() ));
		System.out.println("Parser validating XML = "+(spf.isValidating()));
		try{
			parser = spf.newSAXParser();
			System.out.println("Parser object is "+parser);			
		}catch(SAXException e){
			e.printStackTrace();
			System.exit(1);
		} catch (ParserConfigurationException e) {	
			e.printStackTrace();
			System.exit(1);
		}		
		System.out.println("Starting the parsing of "+xmlFile+"\n");
		try {
			parser.parse(xmlFile,this);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void exportState(String fileName){
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
	
	static public GAxmlHandler importState(String fileName){
		GAxmlHandler hdl = null;
		try{
			ObjectInputStream in = new ObjectInputStream(
					new BufferedInputStream(
							new FileInputStream(fileName)));
			hdl = (GAxmlHandler)in.readObject();
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return hdl;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer("State variables:\n");
		sb.append("verbose = "+verbose+"\n");
		sb.append("map Path = "+mapFilePath+"\n");
		sb.append("splot-Flag = "+splotAutoFlag+"; path = "+splotFilePath+"; type = "+splotFileToken+"\n");
		sb.append("min-neighbor-dis = "+neigDistLimit+"; var mass ="+implementVarableMass+"\n");
		sb.append("mass-total = "+massFractionToken+"; user-defined = "+userDefinedMass+"\n");
		sb.append("smooting-passes = "+numSmooth+"\n");
		sb.append("num-of-contours = "+contVal.size()+"\n");
		for(int i=0;i<contVal.size();i++){
			sb.append("  contour = "+contVal.get(i)+"; color = "+contCol.get(i)+"\n");
		}		
		sb.append("num-of-output-formats = "+outMap.size()+"\n");
		Set<String> keys = outMap.keySet();
		for(String key:keys){
			sb.append("  output type = "+key+"; fileName = "+outMap.get(key)+"\n");
		}
		sb.append("meta-active = "+metaactive+"\n");
		sb.append("metafilepath = "+metafilePath+"\n");
		sb.append("num-of-meta-Strings = "+metaStrings.size()+"\n");	
		for(int i=0;i<metaStrings.size();i++){
			sb.append("  string = "+metaStrings.get(i)+"\n");
		}
		return sb.toString();
	}

	public void startDocument(){
		System.out.println("starting document");
	}
	
	public void endDocument(){
		System.out.println("end of document");
	}
	
	public void startElement(String url,String localName,String gname,
			Attributes attr){
		focus = localName;
		// turn on element flags
		if(localName.contentEquals("consoleoutput"))consoleFlag=true;
		if(localName.contentEquals("bnaboundingmap"))mapFlag=true;
		if(localName.contentEquals("splotfile"))splotFlag=true;
		if(localName.contentEquals("neighbordistance"))neigFlag=true;
		if(localName.contentEquals("totalmass"))massFlag=true;
		if(localName.contentEquals("smoothing"))smoothFlag=true;
		if(localName.contentEquals("contours"))contoursFlag=true;
		if(localName.contentEquals("contour"))contourFlag=true;
		if(localName.contentEquals("binarypolys"))binpolFlag=true;
		if(localName.contentEquals("csvcartesianpolys"))csvpolFlag=true;
		if(localName.contentEquals("csvlatlongpolys"))csvLLpolFlag=true;
		if(localName.contentEquals("bnapolys"))bnapolFlag=true;
		if(localName.contentEquals("geojsonpolys"))geojFlag=true;
		if(localName.contentEquals("csvpolyareastat"))areastatFlag=true;
		if(localName.contentEquals("picture"))picFlag=true;
		if(localName.contentEquals("metadata"))metadFlag=true;
		
		int attrCount = attr.getLength();
		if(attrCount>0){
			for(int i=0;i<attrCount;i++){
				if((splotFlag)&&(attr.getQName(i).contentEquals("auto"))){
					if(attr.getValue(i).contentEquals("true")){
						splotAutoFlag=true;
					}else{
						splotAutoFlag=false;
					}
					//System.out.println(">>>>>>>>>>>>>>> splotAuto set = "+splotAutoFlag);
				}
			}
		}
	}
	
	public void endElement(String url,String localName,String gname){
		// turn off element flags
		if(localName.contentEquals("consoleoutput"))consoleFlag=false;
		if(localName.contentEquals("bnaboundingmap"))mapFlag=false;
		if(localName.contentEquals("splotfile"))splotFlag=false;
		if(localName.contentEquals("neighbordistance"))neigFlag=false;
		if(localName.contentEquals("totalmass"))massFlag=false;
		if(localName.contentEquals("smoothing"))smoothFlag=false;
		if(localName.contentEquals("contours"))contoursFlag=false;
		if(localName.contentEquals("contour"))contourFlag=false;
		if(localName.contentEquals("binarypolys"))binpolFlag=false;
		if(localName.contentEquals("csvcartesianpolys"))csvpolFlag=false;
		if(localName.contentEquals("csvlatlongpolys"))csvLLpolFlag=false;
		if(localName.contentEquals("bnapolys"))bnapolFlag=false;
		if(localName.contentEquals("geojsonpolys"))geojFlag=false;
		if(localName.contentEquals("csvpolyareastat"))areastatFlag=false;
		if(localName.contentEquals("picture"))picFlag=false;
		if(localName.contentEquals("metadata"))metadFlag=false;
	}
	
	public void characters(char[] ch,int start,int length){
		
		if((consoleFlag)&&(focus.contentEquals("flag"))){
			String str =new String(ch,start,length);
			//System.out.println(">>>>>>>>>>>>>>> verbose = "+str);
			if(str.contentEquals("true")){verbose=true;}else{verbose=false;}
			consoleFlag=false;
		}
		
		if((mapFlag)&&(focus.contentEquals("name"))){
			String str =new String(ch,start,length);
			mapFilePath = str;
			//System.out.println(">>>>>>>>>>>>>>> mapFilePath = "+str);
			mapFlag=false;
		}
		
		
		if((splotFlag)&&(focus.contentEquals("name"))){
			String str =new String(ch,start,length);
			splotFilePath = str;
			//System.out.println(">>>>>>>>>>>>>>> splotFilePath = "+str);
		}
		if((splotFlag)&&(focus.contentEquals("type"))){
			String str =new String(ch,start,length);
			splotFileToken = str;
			//System.out.println(">>>>>>>>>>>>>>> splotFileType = "+str);
		}
		
		if((neigFlag)&&(focus.contentEquals("value"))){
			String str =new String(ch,start,length);
			neigDistLimit = Double.parseDouble(str);
			//System.out.println(">>>>>>>>>>>>>>> min distance to neighbor = "+str);
		}
		if((neigFlag)&&(focus.contentEquals("implimentvariablemass"))){
			String str =new String(ch,start,length);
			if(str.contentEquals("true")){implementVarableMass=true;}
				else{implementVarableMass=false;}
			//System.out.println(">>>>>>>>>>>>>>> implement Variable Mass = "+str);
		}
		
		if((massFlag)&&(focus.contentEquals("type"))){
			String str =new String(ch,start,length);
			massFractionToken = str;
			//System.out.println(">>>>>>>>>>>>>>> mass fraction token = "+str);
		}
		if((massFlag)&&(focus.contentEquals("value"))){
			String str =new String(ch,start,length);
			userDefinedMass = Double.valueOf(str);
			//System.out.println(">>>>>>>>>>>>>>> user defined noramalized mass = "
			//		+userDefinedMass);
		}
		
		if((smoothFlag)&&(focus.contentEquals("numpasses"))){
			String str =new String(ch,start,length);
			numSmooth = Integer.valueOf(str);
			//System.out.println(">>>>>>>>>>>>>>> number of smooting passes = "
			//		+numSmooth);
		}
		
		if((contoursFlag)&&(contourFlag)&&(focus.contentEquals("value"))){
			String str =new String(ch,start,length);
			contVal.add(Double.valueOf(str));
			//System.out.println(">>>>>>>>>>>>>>> contour val("+(contVal.size()-1)+
			//		") = "+contVal.get(contVal.size()-1));
		}
		if((contoursFlag)&&(contourFlag)&&(focus.contentEquals("red"))){
			String str =new String(ch,start,length);
			tempR = Integer.parseInt(str);
			//System.out.println(">>>>>>>>>>>>>>> contour red("+(contVal.size()-1)+
			//		") = "+tempR);
		}
		if((contoursFlag)&&(contourFlag)&&(focus.contentEquals("green"))){
			String str =new String(ch,start,length);
			tempG = Integer.parseInt(str);
			//System.out.println(">>>>>>>>>>>>>>> contour green("+(contVal.size()-1)+
			//		") = "+tempG);
		}
		if((contoursFlag)&&(contourFlag)&&(focus.contentEquals("blue"))){
			String str =new String(ch,start,length);
			tempB = Integer.parseInt(str);
			//System.out.println(">>>>>>>>>>>>>>> contour blue("+(contVal.size()-1)+
			//		") = "+tempB);
			contCol.add(new Color(tempR,tempG,tempB));
		}
		
		if((binpolFlag)&&(focus.contentEquals("filename"))){
			String str =new String(ch,start,length);
			outMap.put("binpoly",str);
			//System.out.println(">>>>>>>>>>>>>>> key("+"binpoly"+") filename= "+str);
		}
		
		if((csvpolFlag)&&(focus.contentEquals("filename"))){
			String str =new String(ch,start,length);
			outMap.put("cartpolyscsv",str);
			//System.out.println(">>>>>>>>>>>>>>> key("+"cartpolyscsv"+") filename= "+str);
		}
		
		if((csvLLpolFlag)&&(focus.contentEquals("filename"))){
			String str =new String(ch,start,length);
			outMap.put("latlongpolyscsv",str);
			//System.out.println(">>>>>>>>>>>>>>> key("+"latlongpolyscsv"+") filename= "+str);
		}
		
		if((bnapolFlag)&&(focus.contentEquals("filename"))){
			String str =new String(ch,start,length);
			outMap.put("bnapolys",str);
			//System.out.println(">>>>>>>>>>>>>>> key("+"bnapolys"+") filename= "+str);
		}
		
		if((geojFlag)&&(focus.contentEquals("filename"))){
			String str =new String(ch,start,length);
			outMap.put("geojsonpolys",str);
			//System.out.println(">>>>>>>>>>>>>>> key("+"geojsonpolys"+") filename= "+str);
		}
		
		if((areastatFlag)&&(focus.contentEquals("filename"))){
			String str =new String(ch,start,length);
			outMap.put("areastats",str);
			//System.out.println(">>>>>>>>>>>>>>> key("+"areastats"+") filename= "+str);
		}
		
		if((picFlag)&&(focus.contentEquals("filename"))){
			tempFile =new String(ch,start,length);
		}
		if((picFlag)&&(focus.contentEquals("type"))){
			String str =new String(ch,start,length);
			outMap.put(str,tempFile);
			//System.out.println(">>>>>>>>>>>>>>> key("+str+") filename= "+tempFile);
		}
		
		
		if((metadFlag)&&(focus.contentEquals("metaactive"))){
			String str =new String(ch,start,length);
			metaactive = false;
			if(str.contentEquals("true"))metaactive=true;
			//System.out.println(">>>>>>>>>>>>>>> metaactive = "+metaactive);
		}
		if((metadFlag)&&(focus.contentEquals("metasavefile"))){
			String str =new String(ch,start,length);
			metafilePath = str;
			//System.out.println(">>>>>>>>>>>>>>> meta file save name= "+str);
		}
		if((metadFlag)&&(focus.contentEquals("metastring"))){
			String str =new String(ch,start,length);
			metaStrings.add(str);
			//System.out.println(">>>>>>>>>>>>>>> new metaString= "+str);
		}
		
	}
	
	
	public static void main(String[] args) {
		GAxmlHandler myhandler = new GAxmlHandler();
		System.out.println("\n"+myhandler.toString());
		
		myhandler.exportState("/home/jag/Desktop/tempState.bin");
		GAxmlHandler myhandler2 = GAxmlHandler.importState("/home/jag/Desktop/tempState.bin");
		System.out.println("ECHO SAVED");
		System.out.println("\n"+myhandler2.toString());
	}

}