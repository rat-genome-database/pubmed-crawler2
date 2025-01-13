package edu.mcw.rgd.process;

import edu.mcw.rgd.dao.impl.solr.SolrDocsDAO;
import edu.mcw.rgd.datamodel.solr.SolrDoc;

import java.util.List;

public class SolrDBProcessingThread implements Runnable{
    private List<SolrDoc> solrDocs;
    public SolrDBProcessingThread(List<SolrDoc> solrDocs){
        this.solrDocs=solrDocs;
    }
    @Override
    public void run() {
        SolrDocsDAO solrDocsDAO=new SolrDocsDAO();
        try {
            solrDocsDAO.addBatch(solrDocs);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
