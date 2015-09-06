package hbl.jag.tri.lib;

public class VERSION {

	public static String author = "JAG";
	public static String version = "hbl.jag.lib.3.1.4";
	public static String date = "20150818";
	public static String licence = "GNU-GPL v.3 29June2007";
	
	public static String author(){return author;}
	public static String num(){return version;}
	public static String date(){return date;}
	public static String licence(){return licence;}
	public static void show(){
		System.out.println("author: "+VERSION.author());
		System.out.println("version: "+VERSION.num());
		System.out.println("date: "+VERSION.date());
		System.out.println("licence: "+VERSION.licence());
	}
}
