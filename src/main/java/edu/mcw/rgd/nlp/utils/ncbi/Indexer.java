package edu.mcw.rgd.nlp.utils.ncbi;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Indexer {
    public static void main(String args[]) throws SolrServerException, IOException {
        //Preparing the Solr client


       SolrServer Solr = new HttpSolrServer("http://travis.rgd.mcw.edu:8983/solr/");

        try {
//            SolrPingResponse pingResponse = Solr.ping();
 //           System.out.println("Response "+ pingResponse.getResponse() + "," + pingResponse.getStatus());
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.setAction( UpdateRequest.ACTION.COMMIT, false, false);


            File folder = new File("/data/");
            String json = "";
            for (final File fileEntry : folder.listFiles()) {
                try {
                    System.out.println(fileEntry.getAbsolutePath());
                    String strCurrentLine;

                    BufferedReader objReader = new BufferedReader(new FileReader(fileEntry));
int counter = 0;
                    List<SolrInputDocument> solr_docs = new ArrayList<>();
                    while ((strCurrentLine = objReader.readLine()) != null) {
                        json = strCurrentLine;
                        JSONObject data = new JSONObject(json);
                        SolrInputDocument solr_doc = new SolrInputDocument();
                        Iterator<String> keys = data.keys();

                        while (keys.hasNext()) {
                            String key = keys.next();
                            solr_doc.addField(key, data.get(key).toString());
                           // System.out.println(key + "," + data.get(key).toString());
                        }
                       solr_docs.add(solr_doc);
                       // System.out.println("Documents Updated "+ counter);
                        counter ++;
                    }
                    System.out.println("Documents Updated "+ counter);
                    updateRequest.add( solr_docs);
                    UpdateResponse rsp = updateRequest.process(Solr);
                    objReader.close();
                } catch (FileNotFoundException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }
            }
        }catch(Exception e){
                e.printStackTrace();
            }

    }
}