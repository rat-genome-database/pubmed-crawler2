///* author: Omid Ghiasvand oghiasavnd@mcw.edu
// * Gets pubmed central articles, indexes, and retrieves them!
// */
//
//package edu.mcw.rgd.indexing;
//
//import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
//
//import java.sql.Date;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.log4j.BasicConfigurator;
//import org.elasticsearch.action.bulk.BulkRequestBuilder;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.search.SearchType;
//import org.elasticsearch.client.Client;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.ImmutableSettings;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
//import org.elasticsearch.common.xcontent.XContentBuilder;
//import org.elasticsearch.index.query.FilterBuilders.*;
//import org.elasticsearch.index.query.QueryBuilders.*;
//
//import edu.mcw.rgd.common.utils.GeneralMethods;
//import edu.mcw.rgd.common.utils.XMLreader;
//
//import org.elasticsearch.node.Node;
//
//import static org.elasticsearch.node.NodeBuilder.*;
//
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.client.Client;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.ImmutableSettings;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
//import org.elasticsearch.index.query.QueryBuilder;
//import org.elasticsearch.search.SearchHit;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;
//
//import edu.mcw.rgd.common.utils.PMCreader;
//
//public class IndexClass {
//	public static Client client;
//
//	public static void start()
//	{
//		try {
//			System.out.println("Indexing...");
//			Node node = nodeBuilder().node();
//			client.admin()
//			.indices()
//			.prepareCreate("pmc_trec_final")
//			.setSettings(
//					ImmutableSettings.settingsBuilder().loadFromSource(
//							jsonBuilder()
//							.startObject("settings")
//							// analysis part
//							.startObject("analysis").startObject("analyzer").startObject("filter")
//							.startObject("test_filter_stopwords_en").field("type", "stop")
//							.field("stopwords_path", "stopwords/stop_en").endObject().endObject()
//							.endObject().toString()));
//
//			//						client.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	public static void setting(){
//
//		try {
//			Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").build();
//			client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(
//					"localhost", 9300));
//
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	//------------------------------------------------------------------------------------
//	public static void indexBulk(List<String> paperList) {
//		String path="";
//		try {
//			BulkRequestBuilder bulk = client.prepareBulk();
//			int added = 1;
//			for (int ind = 0; ind < paperList.size(); ind++)// lstFiles.size()
//			{
//				String p = paperList.get(ind);
//				try {
//					XContentBuilder builder;// = GeneralMethods.indexOneCitaion(p);
//					builder = jsonBuilder().startObject().field("file", p).endObject();
//
//					added++;
//					bulk.add(client.prepareIndex("pmc_trec_final", "pmc_trec_final_type").setSource(
//							builder.string()));
//					if (added % 10 == 0) {
//						bulk.execute().actionGet();
//						bulk = client.prepareBulk();
//						System.out.println("DONE:" + added);
//					}
//				} catch (Exception e) {
//					System.out.println("Error:" + p);
//				}
//			}
//
//			bulk.execute().actionGet();
//			System.out.println("Finish " + path);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static int getTime(){
//		return 0;
//	}
//	//--------------------------------------------------------------------------------
//
//}
