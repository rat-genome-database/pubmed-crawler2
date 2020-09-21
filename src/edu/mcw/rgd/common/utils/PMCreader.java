package edu.mcw.rgd.common.utils;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PMCreader {

	public static String getPmid(Element el, String name, String feature) {
		String text = "";
		try {
			NodeList nodeCon = el.getElementsByTagName(name);
			for (int i = 0; i < nodeCon.getLength(); i++) {
				Node ndDrug = nodeCon.item(i);
				if (ndDrug.getNodeType() == Node.ELEMENT_NODE) {
					Element elmCon = (Element) ndDrug;
					String att = elmCon.getAttribute("pub-id-type");
					if (att.equalsIgnoreCase(feature)) {
						NodeList childElemNameCon = elmCon.getChildNodes();
						if (childElemNameCon.getLength() > 0) {
							text = ((Node) (childElemNameCon.item(0))).getNodeValue();
							if (!text.isEmpty())
								text = text.trim();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (text.length() > 0)
			text = text.trim();
		return text;
	}

	public static String getInsideText(Element el, String name) {
		String text = "";
		try {
			NodeList nodeCon = el.getElementsByTagName(name);
			if (nodeCon.getLength() > 0) {
				Element elmCon = (Element) nodeCon.item(0);
				text = elmCon.getTextContent();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	public static List<String> lstBodies;
	public static List<String> lstTitles;

	public static void getBody(Element el) {
		lstBodies = new ArrayList<String>();
		lstTitles = new ArrayList<String>();
		try {
			NodeList lstNodeInter = el.getElementsByTagName("body");
			for (int k = 0; k < 1; k++) {
				Node ndInter = lstNodeInter.item(k);
				Element elem = (Element) ndInter;
				NodeList lstNdTitles = elem.getElementsByTagName("title");
				NodeList lstParas = elem.getElementsByTagName("p");
				for (int q = 0; q < lstNdTitles.getLength(); q++) {
					Node nTitle = lstNdTitles.item(q);
					if (nTitle.getNodeType() == Node.ELEMENT_NODE) {
						String title = nTitle.getTextContent();
						String para = "";
						for (int qP = 0; qP < lstParas.getLength(); qP++) {
							Node nPara = lstParas.item(qP);
							if (nPara.getParentNode().equals(nTitle.getParentNode())) {
								para += nPara.getTextContent() + " ";
							}
						}
						lstTitles.add(title);
						lstBodies.add(para);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getAbstract(Element el, String name) {
		String text = "";
		try {
			NodeList nodeCon = el.getElementsByTagName(name);
			if (nodeCon.getLength() > 0) {
				Element elm = (Element) nodeCon.item(0);
				text = getInsideText(elm, "p");
				if (text == null || text.length() < 5) {
					Element elmCon = (Element) nodeCon.item(0);
					text = elmCon.getTextContent();

				}
			}
			return text;
		}
			catch(Exception e){
				System.out.println(e);
				return null;
			}
	}
}

