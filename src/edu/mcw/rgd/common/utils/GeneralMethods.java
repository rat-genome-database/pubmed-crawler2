//package edu.mcw.rgd.common.utils;
//
//import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.elasticsearch.common.xcontent.XContentBuilder;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
//public class GeneralMethods {
//	public static List<String> getLstAllFilesSubFolders(String path) {
//        List<String> lstPaths = new ArrayList<String>();
//        try {
//            File folder = new File(path);
//            File[] listOfFiles = folder.listFiles();
//            for (int i = 0; i < listOfFiles.length; i++) {
//                if (listOfFiles[i].isFile()) {
//                    if (!listOfFiles[i].getName().equalsIgnoreCase(".DS_Store")) {
//                        if (listOfFiles[i].getName().indexOf("~") < 0) {
//                            lstPaths.add(path + listOfFiles[i].getName());
//                        }
//                    }
//                }else{
//                    if(listOfFiles[i].isDirectory()){
//                        lstPaths.addAll(getLstAllFilesSubFolders(listOfFiles[i].getPath() + "/"));
//                    }
//                }
//            }           
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return lstPaths;
//    }
//	//-----------------------------------------------------------------------------------------------
//	public static XContentBuilder indexOneCitaion(String path) {
//        XContentBuilder builder = null;
//        
//        System.out.println("-------------------------------------------------------------------------");
//        System.out.println("in index one citation");
//        System.out.println("-------------------------------------------------------------------------");
//        try {
//            String pmid = "", pmc = "", title = "", abst = "";
//            Document document = XMLreader.getDocument(path);
//            NodeList lstNode = document.getElementsByTagName("article");
//            List<String> lstTitles = new ArrayList<String>();
//            List<String> lstBodies = new ArrayList<String>();
//            for (int i = 0; i < lstNode.getLength(); i++) {
//                Node ndDrug = lstNode.item(i);
//                if (ndDrug.getNodeType() == Node.ELEMENT_NODE) {
//                    Element elemDrug = (Element) ndDrug;
//                    pmid = PMCreader.getPmid(elemDrug, "article-id", "pmid");
//                    pmc = PMCreader.getPmid(elemDrug, "article-id", "pmc");
//                    title = PMCreader.getInsideText(elemDrug, "article-title");
//                    abst = PMCreader.getAbstract(elemDrug, "abstract");
//                    PMCreader.getBody(elemDrug);
//                    lstTitles = PMCreader.lstTitles;
//                    lstBodies = PMCreader.lstBodies;
//                }
//            }
//
//            String body = "";
//            for (int i = 0; i < lstTitles.size() && i < lstBodies.size(); i++) {
//                body += lstTitles.get(i) + "-$STARTST$-" + lstBodies.get(i) + "-$ENDEND$-";
//            }
//
//            if (pmid != null) {
//                builder = jsonBuilder().startObject().field("pmid", pmid).field("pmc", pmc).field("title", title)
//                        .field("abstract", abst).field("body", body).endObject();
//            }
//        } catch (Exception e) {
////            e.printStackTrace();
//        }
//        return builder;
//    }
//    //-------------------------------------------------------------------------
//}
