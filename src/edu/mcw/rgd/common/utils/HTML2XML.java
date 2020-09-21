package edu.mcw.rgd.common.utils;

import java.io.File;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class HTML2XML {
	
	public static String html2xml(String html) throws Exception{
		
		html=ReadWrite.read("file.html");
		File f=new File("file.html");
//		html="http://www.ncbi.nlm.nih.gov/pmc/articles/PMC3190858/";
//		Document doc= Jsoup.connect(html).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
//			     .get();
		
		Document doc=Jsoup.parse(html);
//		Elements newsHeadlines = doc.select("#mp-itn b a");
		
//		System.out.println(newsHeadlines.toString());
		
		
//		for(int i=0;i<HTML.getSections(doc).size();i++)
//			System.out.println(HTML.getSections(doc).get(i));//.html2xml(doc));
		
		System.out.println(HTML.html2xml(doc, false));
		
//		System.out.println(doc.getElementsByTag("p"));
//		textAnalysis(doc.toString());
//		ReadWrite.write(doc.toString(),"file2.txt");
		System.out.println("**********************************************************");
//		System.out.println(html);
		return "";
	}
	//-------------------------------------------------------------------
	public static String textAnalysis(String text){
	     StringTokenizer st = new StringTokenizer(text);
	     String st1=st.nextToken();
	     int fromIndex=0;
	     while (st.hasMoreTokens()) {
	    	 String st2=st.nextToken();
	    	 if(st2.contains("</h2>")){
	    		 System.out.println(st2);
	    	 }
	    	 fromIndex+=st2.length();
	    	 st1=st2;
	     }
	     
		return "";
	}
}
