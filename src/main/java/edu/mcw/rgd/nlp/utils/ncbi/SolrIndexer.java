package edu.mcw.rgd.nlp.utils.ncbi;


import edu.mcw.rgd.dao.impl.solr.SolrDocsDAO;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import java.util.List;

public class SolrIndexer {
    private static final String SOLR_URL = "http://hansen.rgd.mcw.edu:8983/solr/"; // Solr URL
    private static final int BATCH_SIZE = 1000; // Size of each batch
    private static final int COMMIT_INTERVAL = 10000; // Commit after every 10,000 docs

    public static void batchIndexer(int year, String solrCollectionName) throws Exception {
        SolrDocsDAO solrDocsDAO=new SolrDocsDAO();
        SolrClient solrClient= new HttpSolrClient.Builder(SOLR_URL+solrCollectionName)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
        solrClient.deleteByQuery("*");
        solrClient.commit();

        try{
            int batchSize=1000;
            int offset=0;
            int count=0;
            int commitInterval=0;
            while (true){
                List<SolrInputDocument> documents=solrDocsDAO.getSolrDocs(year,batchSize, offset);
                if(documents==null || documents.size()==0){
                    break;
                }
                try {
                    addDocumentsToSolr(solrClient, documents, count);
                }catch (Exception e){e.printStackTrace();}
                offset+=batchSize;
                count++;
                commitInterval++;
                if(commitInterval==10){
                    solrClient.commit();
                    commitInterval=0;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                solrClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static void addDocumentsToSolr(SolrClient solrClient, List<SolrInputDocument> batch, int batchNumber) throws Exception {
        UpdateResponse response = solrClient.add(batch);
        System.out.println("Indexed batch "+batchNumber+" of " + batch.size() + " documents, response status: " + response.getStatus());
    }

    public static void main(String[] args) throws Exception {
        batchIndexer(2025, "ai1");
    }
}
