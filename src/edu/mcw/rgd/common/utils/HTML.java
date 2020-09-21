package edu.mcw.rgd.common.utils;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;

public class HTML {

	public static String getText(String text){

		return "";
	}
	//-----------------------------------------------------------
	public static List<String> getSectionNames(Document doc){
		String names=doc.getElementsByTag("h2").toString();
		String[] nameArr=removeTags(names).split("\n");
		List<String> list=new ArrayList<String>();
		for(String n:nameArr)
			list.add(n);
		return list;
	}
	//-----------------------------------------------------------
	public static List<String> getSections(Document doc){
		String text=doc.text();
		String[] sectionName=doc.getElementsByTag("h2").toString().split("\n");
		String[] subSections=doc.getElementsByTag("h3").toString().split("\n");
		String[] sectionBody=doc.getElementsByTag("p").toString().split("\n");
		List<String> list=new ArrayList<String>();
		int fromIndex=0;
		boolean paper=false;
		for(int i=0;i<sectionName.length-1;i++){
			if(removeTags(sectionName[i]).equalsIgnoreCase("abstract"))
				paper=true;
			int start=text.indexOf(" "+removeTags(sectionName[i])+" ", fromIndex);
			int end=text.indexOf(" "+removeTags(sectionName[i+1])+" ", start);

			//			System.out.println(">>>>>>>>>>>>>>>>>>   "+text.length()+"\t"+start+"\t"+end);

			if(start>=fromIndex && end>start){
				String secText=removeTags(sectionName[i])+"\t"+text.substring(start+1, end);
				list.add(secText);
			}
			fromIndex=end;
		}

		if(!paper)
		{
			list.clear();
			list.add(text);
		}

		return list;
	}
	//-----------------------------------------------------------
	public static String getAbstract(Document doc){
		String text=doc.text();
		String[] sectionName=doc.getElementsByTag("h2").toString().split("\n");
		String[] subSections=doc.getElementsByTag("h3").toString().split("\n");
		String[] sectionBody=doc.getElementsByTag("p").toString().split("\n");
		List<String> list=new ArrayList<String>();
		int fromIndex=0;

		for(int i=0;i<sectionName.length-1;i++){
			int start=text.indexOf(" "+removeTags(sectionName[i])+" ", fromIndex);
			int end=text.indexOf(" "+removeTags(sectionName[i+1])+" ", start);
			if(start>=fromIndex && end>start){
				String secText=text.substring(start+1, end);
				if(removeTags(sectionName[i]).equalsIgnoreCase("abstract"))
					return secText;
			}
			fromIndex=end;
		}
		return "";
	}
	//------------------------------------------------------------
	public static String getSectionByName(Document doc, String secName){
		String text=doc.text();
		String[] sectionName=doc.getElementsByTag("h2").toString().split("\n");
		String[] subSections=doc.getElementsByTag("h3").toString().split("\n");
		String[] sectionBody=doc.getElementsByTag("p").toString().split("\n");
		List<String> list=new ArrayList<String>();
		int fromIndex=0;

		for(int i=0;i<sectionName.length-1;i++){
			int start=text.indexOf(" "+removeTags(sectionName[i])+" ", fromIndex);
			int end=text.indexOf(" "+removeTags(sectionName[i+1])+" ", start);
			if(start>=fromIndex && end>start){
				String secText=text.substring(start+1, end);
				if(removeTags(sectionName[i]).equalsIgnoreCase(secName))
					return secText;
			}
			fromIndex=end;
		}
		return "";
	}
	//-------------------------------------------------------------
	public static String html2xml(Document doc, boolean abs){
		List<String> secList=getSections(doc);
		StringBuffer out=new StringBuffer();
		out.append("<body>\n");
		for (int i=0;i<secList.size();i++){
			String secName="";
			if (secList.get(i).split("\t").length>1){
				secName=secList.get(i).split("\t")[0];
				String secBody=secList.get(i).split("\t")[1];
				secBody=secBody.replace(secName, "").trim();
				if(!secName.equalsIgnoreCase("abstract") || abs){
					out.append("<sec sec-type=\""+secName.toLowerCase().trim()+"\">\n");
					out.append("<title>"+secName+"</title>"+"\n");
					out.append("<p>"+secBody+"</p>\n");
					out.append("</sec>"+"\n");
				}
			}
			else
			{
				out.append("<sec sec-type=\""+secName.toLowerCase().trim()+"\">\n");
				out.append("<title>"+""+"</title>"+"\n");
				out.append("<p>"+secList.get(i)+"</p>\n");
				out.append("</sec>"+"\n");
			}
			if(secName.equalsIgnoreCase("references"))
				break;
		}
		out.append("<"+"/"+"body>"+"\n");
		return out.toString().trim();
	}
	//-------------------------------------------------------------
	public static String removeTags(String text){
		String[] tag={"p", "h3", "h2"};
		String[] line=text.split("\n");
		StringBuffer str=new StringBuffer();
		for(int i=0;i<line.length;i++){
			for(int j=0;j<3;j++){
				int start=line[i].indexOf("<"+tag[j]);
				int end=line[i].indexOf(">", start);
				if(start>=0 && end>0)
					line[i]=line[i].replace(line[i].substring(start, end+1), "").replace("</"+tag[j]+">", "").trim();
			}
			str.append(line[i]+"\n");
		}
		return str.toString().trim();
	}
}
