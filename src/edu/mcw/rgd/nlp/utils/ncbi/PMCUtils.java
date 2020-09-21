/* 
 * author: Omid Ghiasvand (oghiasvand@mcw.edu)
 *  This class includes methods to extract abstract and full papers from pubmed central. 
 *  There is a method to map pubmed ID to PMC id.
 */
package edu.mcw.rgd.nlp.utils.ncbi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import edu.mcw.rgd.common.utils.HTML;
import edu.mcw.rgd.common.utils.HTML2XML;

public class PMCUtils {


	//--------------- fetch abstract -----------------------------
	/*
	 *  This method gets the abstract of paper with ID pmid
	 *  
	 */
	public String getAbstract(String pmid)
	{
		System.out.println("\nEntrez.getAbstract(" + pmid + ")\n");

		String output = "";

		try
		{
			String base = "https://www.ncbi.nlm.nih.gov/pubmed/";   // --- PubMed URL
			String url  = base + pmid + "?dopt=XML";

			// --- download abstract page
			URL            u = new URL(url);
			InputStream    is  = u.openStream();
			BufferedReader dis = new BufferedReader(new InputStreamReader(is));
			String         s, download = "";
			while ( (s = dis.readLine()) != null ){
				download += s;
			}

			// --- extract article TITLE ---
			String articleTitle = "";
			int b = 0, e = 0, offset = 0;
			String anchor = "&lt;ArticleTitle&gt;";
			String anchor2="&gt;";
			if ( (b = download.indexOf(anchor)) >= 0)
			{
				b += anchor.length();
				anchor = "&lt;/ArticleTitle&gt;";
				e = download.indexOf(anchor);
				articleTitle = download.substring(b, e);
			}

			// --- extract abstract TEXT ---
			String abstractText = "";
			//	      anchor = "&lt;AbstractText&gt;<font class=\"val\">";
			anchor = "&lt;AbstractText";
			if ( (b = download.indexOf(anchor)) >= 0)
			{
				b += anchor.length();
				b=download.indexOf(anchor2, b)+anchor2.length();
				anchor = "&lt;/AbstractText&gt;";
				e = download.indexOf(anchor);
				abstractText = download.substring(b, e);
			}
			output = articleTitle + "\n\n" + abstractText;
		}
		catch (Exception e) {System.out.println(e.toString());}

		return output;
	}
	// -----------------------------------------------------------------------------

	// --- fetch full-text article from PubMed Central
	/*
	 *  This method gets the abstract of paper with ID pmid
	 *  
	 */
	// -----------------------------------------------------------------------------
	static int counter=0, counter1=0;
	public static String getPaper(String id)
	{
		System.out.println("PMCUtils.getPaper(" + id + "): fetch paper from PubMed Central");
		StringBuffer doc = new StringBuffer();
		String text="";
		try
		{
			java.net.URL   u;
			InputStream    ins = null;
			BufferedReader dis;
			String         s;
			u   = new java.net.URL("https://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pmc&id=" + id);
			ins = u.openStream();  // --- throws an IOException
			dis = new BufferedReader(new InputStreamReader(ins));
			while ( (s = dis.readLine()) != null ){
				doc.append(s+"\n");
//				if(s.contains("The publisher of this article does not allow downloading of the full text in XML form")){
//					doc=null;
//					break;
//				}
			}
			if(doc==null)
				text="";
			else
				text=doc.toString();
//			if(text!=null)
//				if (text.contains("publisher of this article does not allow downloading of the full text")){
////					text=readHTML(text, "http://www.ncbi.nlm.nih.gov/pmc/articles/PMC"+id+"/");
//				}
//			counter=0;
			if (text.compareTo("") == 0) text = null;
		}
		catch (Exception e) {
			System.out.println(e.toString());
//			counter++;
//			if(counter<5)
//				getPaper(id);

		}

		return text;
	}
	// -----------------------------------------------------------------------------
//	public static String readHTML(String text, String url){
//		try{
//			System.out.println("HTML downloading...");
//			System.out.println(url);
//			Document doc= Jsoup.connect(url).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
//					.get();
//
//			String out=HTML.html2xml(doc, false);
//			int endFront=text.indexOf("</front>")+8;
//			String front=text.substring(0, endFront);
//			out=front+"\n"+out;
//			out+="\n"+"</article>\n</pmc-articleset>";
//			String rep="<!--The publisher of this article does not allow downloading of the full text in XML form.-->";
//			out=out.replace(rep, "");
//			counter1=0;
//			return out;
//		}
//		catch(Exception e){
//			System.out.println("Problem in downloading HTML version of the paper");
//			counter1++;
//			if(counter1<5){
//				try{
//					Thread.sleep(2000);
//				}
//				catch (InterruptedException ex){
//					Thread.currentThread().interrupt();
//				}
//				readHTML(text,url);
//			}
//			throw new RuntimeException(e);
//		}
//	}
	// -----------------------------------------------------------------------------
	public static String mapPMID(String pmid){
		String pmcId = "";
		try
		{
			// --- download XML page ---
			String base = "https://www.ncbi.nlm.nih.gov/sites/entrez?db=pubmed&cmd=DetailsSearch&term=";
			String url  = base + pmid + "[uid]&dopt=XML";
			URL            u = new URL(url);
			InputStream    is  = u.openStream();
			BufferedReader dis = new BufferedReader(new InputStreamReader(is));
			String         s, anchor = "&gt;PMC";
			int            i, len = anchor.length();

			while ( (s = dis.readLine()) != null )
			{
				if ( (i = s.indexOf(anchor)) >= 0 )
				{
					s = s.substring(i+len);
					i = s.indexOf("&lt;/OtherID");
					if(i >-1)
						pmcId = s.substring(0, i).split(" ")[0].trim();

					break;
				}
			}
		}
		catch (Exception e) {System.out.println(e.toString());}

		return pmcId;
	}
	//----------------------------------------------------------------------------------------------------
	public static String readHTML(String url)
	{
		try{
			URL            u = new URL(url);
			InputStream    is  = u.openStream();
			BufferedReader dis = new BufferedReader(new InputStreamReader(is));
			String line="";
			StringBuffer out=new StringBuffer();
			while ( (line = dis.readLine()) != null )
			{
				out.append(line+"\n");
			}
		}
		catch (Exception e) {System.out.println(e.toString());}
		return "";
	}
	//----------------------------------------------------------------------------------------------------
	public String cleanPMC_XMLtags(String text)
	{
		String clean = text;

		clean = clean.replaceAll("</abstract>", "");
		clean = clean.replaceAll("<abstract[^>]*>", "");
		clean = clean.replaceAll("</ack>", "");
		clean = clean.replaceAll("<ack[^>]*>", "");
		clean = clean.replaceAll("</addr-line>", "");
		clean = clean.replaceAll("<addr-line[^>]*>", "");
		clean = clean.replaceAll("</aff>", "");
		clean = clean.replaceAll("<aff[^>]*>", "");
		clean = clean.replaceAll("</alt-title>", "");
		clean = clean.replaceAll("<alt-title[^>]*>", "");
		clean = clean.replaceAll("</article>", "");
		clean = clean.replaceAll("<article[^>]*>", "");
		clean = clean.replaceAll("</article-categories>", "");
		clean = clean.replaceAll("<article-categories[^>]*>", "");
		clean = clean.replaceAll("</article-id>", "");
		clean = clean.replaceAll("<article-id[^>]*>", "");
		clean = clean.replaceAll("</article-meta>", "");
		clean = clean.replaceAll("<article-meta[^>]*>", "");
		clean = clean.replaceAll("</article-title>", "");
		clean = clean.replaceAll("<article-title[^>]*>", "");
		clean = clean.replaceAll("</author-notes>", "");
		clean = clean.replaceAll("<author-notes[^>]*>", "");
		clean = clean.replaceAll("</back>", "");
		clean = clean.replaceAll("<back[^>]*>", "");
		clean = clean.replaceAll("</body>", "");
		clean = clean.replaceAll("<body[^>]*>", "");
		clean = clean.replaceAll("</bold>", "");
		clean = clean.replaceAll("<bold[^>]*>", "");
		clean = clean.replaceAll("</caption>", "");
		clean = clean.replaceAll("<caption[^>]*>", "");
		clean = clean.replaceAll("</citation>", "");
		clean = clean.replaceAll("<citation[^>]*>", "");
		clean = clean.replaceAll("</contract-num>", "");
		clean = clean.replaceAll("<contract-num[^>]*>", "");
		clean = clean.replaceAll("</contract-sponsor>", "");
		clean = clean.replaceAll("<contract-sponsor[^>]*>", "");
		clean = clean.replaceAll("</contrib>", "");
		clean = clean.replaceAll("<contrib[^>]*>", "");
		clean = clean.replaceAll("</contrib-group>", "");
		clean = clean.replaceAll("<contrib-group[^>]*>", "");
		clean = clean.replaceAll("</copyright-holder>", "");
		clean = clean.replaceAll("<copyright-holder[^>]*>", "");
		clean = clean.replaceAll("</copyright-statement>", "");
		clean = clean.replaceAll("<copyright-statement[^>]*>", "");
		clean = clean.replaceAll("</copyright-year>", "");
		clean = clean.replaceAll("<copyright-year[^>]*>", "");
		clean = clean.replaceAll("</corresp>", "");
		clean = clean.replaceAll("<corresp[^>]*>", "");
		clean = clean.replaceAll("</counts>", "");
		clean = clean.replaceAll("<counts[^>]*>", "");
		clean = clean.replaceAll("</custom-meta>", "");
		clean = clean.replaceAll("<custom-meta[^>]*>", "");
		clean = clean.replaceAll("</custom-meta-wrap>", "");
		clean = clean.replaceAll("<custom-meta-wrap[^>]*>", "");
		clean = clean.replaceAll("</date>", "");
		clean = clean.replaceAll("<date[^>]*>", "");
		clean = clean.replaceAll("</day>", "");
		clean = clean.replaceAll("<day[^>]*>", "");
		clean = clean.replaceAll("</dc:author>", "");
		clean = clean.replaceAll("<dc:author[^>]*>", "");
		clean = clean.replaceAll("</dc:date>", "");
		clean = clean.replaceAll("<dc:date[^>]*>", "");
		clean = clean.replaceAll("</dc:identifier>", "");
		clean = clean.replaceAll("<dc:identifier[^>]*>", "");
		clean = clean.replaceAll("</dcterms:bibliographicCitation>", "");
		clean = clean.replaceAll("<dcterms:bibliographicCitation[^>]*>", "");
		clean = clean.replaceAll("</dcterms:isPartOf>", "");
		clean = clean.replaceAll("<dcterms:isPartOf[^>]*>", "");
		clean = clean.replaceAll("</dc:title>", "");
		clean = clean.replaceAll("<dc:title[^>]*>", "");
		clean = clean.replaceAll("</dc:type>", "");
		clean = clean.replaceAll("<dc:type[^>]*>", "");
		clean = clean.replaceAll("</degrees>", "");
		clean = clean.replaceAll("<degrees[^>]*>", "");
		clean = clean.replaceAll("</edition>", "");
		clean = clean.replaceAll("<edition[^>]*>", "");
		clean = clean.replaceAll("</email>", "");
		clean = clean.replaceAll("<email[^>]*>", "");
		clean = clean.replaceAll("</ext-link>", "");
		clean = clean.replaceAll("<ext-link[^>]*>", "");
		clean = clean.replaceAll("</fig>", "");
		clean = clean.replaceAll("<fig[^>]*>", "");
		clean = clean.replaceAll("</fig-count>", "");
		clean = clean.replaceAll("<fig-count[^>]*>", "");
		clean = clean.replaceAll("</fn>", "");
		clean = clean.replaceAll("<fn[^>]*>", "");
		clean = clean.replaceAll("</fn-group>", "");
		clean = clean.replaceAll("<fn-group[^>]*>", "");
		clean = clean.replaceAll("</fpage>", "");
		clean = clean.replaceAll("<fpage[^>]*>", "");
		clean = clean.replaceAll("</front>", "");
		clean = clean.replaceAll("<front[^>]*>", "");
		clean = clean.replaceAll("</given-names>", "");
		clean = clean.replaceAll("<given-names[^>]*>", "");
		clean = clean.replaceAll("</graphic>", "");
		clean = clean.replaceAll("<graphic[^>]*>", "");
		clean = clean.replaceAll("</history>", "");
		clean = clean.replaceAll("<history[^>]*>", "");
		clean = clean.replaceAll("</issn>", "");
		clean = clean.replaceAll("<issn[^>]*>", "");
		clean = clean.replaceAll("</issue>", "");
		clean = clean.replaceAll("<issue[^>]*>", "");
		clean = clean.replaceAll("</italic>", "");
		clean = clean.replaceAll("<italic[^>]*>", "");
		clean = clean.replaceAll("</journal-id>", "");
		clean = clean.replaceAll("<journal-id[^>]*>", "");
		clean = clean.replaceAll("</journal-meta>", "");
		clean = clean.replaceAll("<journal-meta[^>]*>", "");
		clean = clean.replaceAll("</journal-title>", "");
		clean = clean.replaceAll("<journal-title[^>]*>", "");
		clean = clean.replaceAll("</kwd>", "");
		clean = clean.replaceAll("<kwd[^>]*>", "");
		clean = clean.replaceAll("</kwd-group>", "");
		clean = clean.replaceAll("<kwd-group[^>]*>", "");
		clean = clean.replaceAll("</label>", "");
		clean = clean.replaceAll("<label[^>]*>", "");
		clean = clean.replaceAll("</license>", "");
		clean = clean.replaceAll("<license[^>]*>", "");
		clean = clean.replaceAll("</License>", "");
		clean = clean.replaceAll("<License[^>]*>", "");
		clean = clean.replaceAll("</lpage>", "");
		clean = clean.replaceAll("<lpage[^>]*>", "");
		clean = clean.replaceAll("</meta-name>", "");
		clean = clean.replaceAll("<meta-name[^>]*>", "");
		clean = clean.replaceAll("</meta-value>", "");
		clean = clean.replaceAll("<meta-value[^>]*>", "");
		clean = clean.replaceAll("</month>", "");
		clean = clean.replaceAll("<month[^>]*>", "");
		clean = clean.replaceAll("</name>", "");
		clean = clean.replaceAll("<name[^>]*>", "");
		clean = clean.replaceAll("</notes>", "");
		clean = clean.replaceAll("<notes[^>]*>", "");
		clean = clean.replaceAll("</p>", "");
		clean = clean.replaceAll("<p[^>]*>", "");
		clean = clean.replaceAll("</page-count>", "");
		clean = clean.replaceAll("<page-count[^>]*>", "");
		clean = clean.replaceAll("</permissions>", "");
		clean = clean.replaceAll("<permissions[^>]*>", "");
		clean = clean.replaceAll("</permits>", "");
		clean = clean.replaceAll("<permits[^>]*>", "");
		clean = clean.replaceAll("</person-group>", "");

		clean = clean.replaceAll("<person-group[^>]*>", "");
		clean = clean.replaceAll("</pmc-articleset>", "");
		clean = clean.replaceAll("<pmc-articleset[^>]*>", "");
		clean = clean.replaceAll("</pub-date>", "");
		clean = clean.replaceAll("<pub-date[^>]*>", "");
		clean = clean.replaceAll("</pub-id>", "");
		clean = clean.replaceAll("<pub-id[^>]*>", "");
		clean = clean.replaceAll("</publisher>", "");
		clean = clean.replaceAll("<publisher[^>]*>", "");
		clean = clean.replaceAll("</publisher-loc>", "");
		clean = clean.replaceAll("<publisher-loc[^>]*>", "");
		clean = clean.replaceAll("</publisher-name>", "");
		clean = clean.replaceAll("<publisher-name[^>]*>", "");
		clean = clean.replaceAll("</ref>", "");
		clean = clean.replaceAll("<ref[^>]*>", "");
		clean = clean.replaceAll("</ref-count>", "");
		clean = clean.replaceAll("<ref-count[^>]*>", "");
		clean = clean.replaceAll("</ref-list>", "");
		clean = clean.replaceAll("<ref-list[^>]*>", "");
		clean = clean.replaceAll("</requires>", "");
		clean = clean.replaceAll("<requires[^>]*>", "");
		clean = clean.replaceAll("</role>", "");
		clean = clean.replaceAll("<role[^>]*>", "");
		clean = clean.replaceAll("</sc>", "");
		clean = clean.replaceAll("<sc[^>]*>", "");
		clean = clean.replaceAll("</sec>", "");
		clean = clean.replaceAll("<sec[^>]*>", "");
		clean = clean.replaceAll("</self-uri>", "");
		clean = clean.replaceAll("<self-uri[^>]*>", "");
		clean = clean.replaceAll("</series-title>", "");
		clean = clean.replaceAll("<series-title[^>]*>", "");
		clean = clean.replaceAll("</source>", "");
		clean = clean.replaceAll("<source[^>]*>", "");
		clean = clean.replaceAll("</sub>", "");
		clean = clean.replaceAll("<sub[^>]*>", "");
		clean = clean.replaceAll("</subject>", "");
		clean = clean.replaceAll("<subject[^>]*>", "");
		clean = clean.replaceAll("</subj-group>", "");
		clean = clean.replaceAll("<subj-group[^>]*>", "");
		clean = clean.replaceAll("</suffix>", "");
		clean = clean.replaceAll("<suffix[^>]*>", "");

		clean = clean.replaceAll("</sup>", "");
		clean = clean.replaceAll("<sup[^>]*>", "");
		clean = clean.replaceAll("</surname>", "");
		clean = clean.replaceAll("<surname[^>]*>", "");
		clean = clean.replaceAll("</table>", "");
		clean = clean.replaceAll("<table[^>]*>", "");
		clean = clean.replaceAll("</table-count>", "");
		clean = clean.replaceAll("<table-count[^>]*>", "");
		clean = clean.replaceAll("</table-wrap>", "");
		clean = clean.replaceAll("<table-wrap[^>]*>", "");
		clean = clean.replaceAll("</table-wrap-foot>", "");
		clean = clean.replaceAll("<table-wrap-foot[^>]*>", "");
		clean = clean.replaceAll("</tbody>", "");
		clean = clean.replaceAll("<tbody[^>]*>", "");
		clean = clean.replaceAll("</td>", "");
		clean = clean.replaceAll("<td[^>]*>", "");
		clean = clean.replaceAll("</thead>", "");
		clean = clean.replaceAll("<thead[^>]*>", "");

		clean = clean.replaceAll("</title>", ". ");
		clean = clean.replaceAll("<title[^>]*>", "");
		clean = clean.replaceAll("</title-group>", "");
		clean = clean.replaceAll("<title-group[^>]*>", "");
		clean = clean.replaceAll("</tr>", "");
		clean = clean.replaceAll("<tr[^>]*>", "");
		clean = clean.replaceAll("</uri>", "");
		clean = clean.replaceAll("<uri[^>]*>", "");
		clean = clean.replaceAll("</volume>", "");
		clean = clean.replaceAll("<volume[^>]*>", "");
		clean = clean.replaceAll("</Work>", "");
		clean = clean.replaceAll("<Work[^>]*>", "");
		clean = clean.replaceAll("</xref>", "");
		clean = clean.replaceAll("<xref[^>]*>", "");
		clean = clean.replaceAll("</year>", "");
		clean = clean.replaceAll("<year[^>]*>", "");
		clean = clean.replaceAll("<inline-graphic[^>]*>", "");
		clean = clean.replaceAll("&#x[^;]*;", "");

		return clean;
	}
}

