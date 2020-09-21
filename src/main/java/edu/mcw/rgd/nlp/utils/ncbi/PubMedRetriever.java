package edu.mcw.rgd.nlp.utils.ncbi;

import edu.mcw.rgd.process.NcbiEutils;
import nu.xom.Builder;
import org.apache.commons.io.FileUtils;
//import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: mtutaj
 * Date: 1/8/15
 * Time: 8:57 AM
 * <p>logic based on PubMedRetriever class from text_mining/Java/TextMiningTools project
 */
public class PubMedRetriever {

    public static final String DB_NAME = "pubmed";
    public static final int ID_TRUNK_SIZE = 1000;

    //private String crawlResult;
    //protected static final Logger logger = Logger.getLogger(PubMedRetriever.class);

    NcbiEutils eUtils;

	public void initialize() {

        eUtils = new NcbiEutils();
        eUtils.seteUtils_db(DB_NAME);
        eUtils.seteUtils_email("mtutaj@mcw.edu");
        eUtils.seteUtils_tool("pubmed_crawler");
        eUtils.setNcbiSearchUrl("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi");
        eUtils.setNcbiFetchUrl("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi");
    }

	public NcbiEutils.ESearchResult getIdSetByDate(String dateToRequest) throws Exception {

        String query ="("+
                "(\"%DATE%\"[CDAT] : \"%DATE%\"[CDAT]) "+ // 'Date - Completion'
             "OR (\"%DATE%\"[MDAT] : \"%DATE%\"[MDAT]) "+// 'Date - Modification'
             "OR (\"%DATE%\"[EDAT] : \"%DATE%\"[EDAT]) "+// 'Date - Entrez'
             "OR (\"%DATE%\"[MHDA] : \"%DATE%\"[MHDA]) "+// 'Date - MeSH'
        ")";
        
        NcbiEutils.ESearchResult eS=eUtils.runESearch(query.replace("%DATE%", dateToRequest));
        return eS;
	}

    public File crawlByIdList(NcbiEutils.ESearchResult eSearch, String outFileName) throws Exception {

        // Fetch the results of the previous query as XML records
        File tmpFile = doEFetch(eUtils, eSearch, ID_TRUNK_SIZE, eSearch.totalFetchedCount);
        eSearch.totalFetchedCount += eSearch.fetchedCount;

        // validate if the file is well formed
        int tagCount = NcbiEutils.getTagCount(tmpFile, "<PubmedArticleSet>");
        if( tagCount!=1 ) {
            throw new Exception("ERROR: crawlByIdList: malformed file "+outFileName);
        }

        // to verify if the downloaded file is a valid XML document, try to load it through XML parser
        Builder builder = new Builder();
        builder.build(tmpFile);

        // move temp file to the destination location
        File outFile = new File(outFileName);
        FileUtils.copyFile(tmpFile, outFile);
        // delete temporary file after it was copied to destination file
        tmpFile.delete();

        return outFile;
	}

    // try to download the file through eFetch, 3 times
    private File doEFetch( NcbiEutils eUtils, NcbiEutils.ESearchResult eSearch, int recCountToFetch, int retstart) throws Exception {

        // try 3 times; retrying if IOException happens
        for( int i=0; i<3; i++ ) {
            try {
                return eUtils.runEFetch(eSearch, "xml", recCountToFetch, retstart);
            }
            catch(IOException io) {
                io.printStackTrace(System.out);
            }
        }

        // no mercy now: any exception will terminate the processing
        return eUtils.runEFetch(eSearch, "xml", recCountToFetch, retstart);
    }
    /*
    public String getCrawlResult() {
        return crawlResult;
    }

    public void setCrawlResult(String crawlResult) {
        this.crawlResult = crawlResult;
    }
    */
}
