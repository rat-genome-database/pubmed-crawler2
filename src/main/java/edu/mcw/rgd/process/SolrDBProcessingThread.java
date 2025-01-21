package edu.mcw.rgd.process;

import edu.mcw.rgd.dao.impl.solr.SolrDocsDAO;
import edu.mcw.rgd.datamodel.solr.SolrDoc;

import java.util.List;
import java.util.Set;

public class SolrDBProcessingThread implements Runnable{
    private List<SolrDoc> solrDocs;
    private List<Integer> chunkDataCounts;
    private Set<String> pmidsMissed;
    public SolrDBProcessingThread(List<SolrDoc> solrDocs, List<Integer> chunkDataCounts, Set<String> pmidsMissed){
        this.solrDocs=solrDocs;
        this.chunkDataCounts=chunkDataCounts;
        this.pmidsMissed=pmidsMissed;
    }
    @Override
    public void run() {
        SolrDocsDAO solrDocsDAO=new SolrDocsDAO();
        try {
           int chunkedDataCount= solrDocsDAO.addBatch(solrDocs, pmidsMissed);
           chunkDataCounts.add(chunkedDataCount);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
