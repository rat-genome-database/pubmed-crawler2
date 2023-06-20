package edu.mcw.rgd.nlp.utils.ncbi;

import edu.mcw.rgd.process.NcbiPMCEutils;
import nu.xom.Builder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: mtutaj
 * Date: 1/8/15
 * Time: 8:57 AM
 * <p>logic based on PubMedRetriever class from text_mining/Java/TextMiningTools project
 */
public class PMCRetriever2 {
    public static final String DB_NAME = "pmc";

    public static final int ID_TRUNK_SIZE = 100;

    //private String crawlResult;
    //protected static final Logger logger = Logger.getLogger(PubMedRetriever.class);

    NcbiPMCEutils eUtils;

	public void initialize() {

        eUtils = new NcbiPMCEutils();
        eUtils.seteUtils_db(DB_NAME);
        eUtils.seteUtils_email("mtutaj@mcw.edu");
        eUtils.seteUtils_tool("pmc_crawler");
        eUtils.setNcbiSearchUrl("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi");
        eUtils.setNcbiFetchUrl("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi");
    }

	public NcbiPMCEutils.ESearchResult getIdSetByDate(String dateToRequest) throws Exception {

        String query ="("+
                "(\"%DATE%\"[pubdate])"+// 'publication date'
        ")";
        
        NcbiPMCEutils.ESearchResult eS=eUtils.runESearch(query.replace("%DATE%", dateToRequest));
        return eS;
	}

    public File crawlByIdList(NcbiPMCEutils.ESearchResult eSearch, String outFileName) throws Exception {

        // Fetch the results of the previous query as XML records
        File tmpFile = doEFetch(eUtils, eSearch, ID_TRUNK_SIZE, eSearch.totalFetchedCount);
        eSearch.totalFetchedCount += eSearch.fetchedCount;

        // validate if the file is well formed

        int tagCount = NcbiPMCEutils.getTagCount(tmpFile, "<pmc-articleset>");


        if( tagCount!=1 ) {
            throw new Exception("ERROR: crawlByIdList: malformed file "+outFileName);
        }
        //System.out.println("file validated: " + outFileName);

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
    private File doEFetch( NcbiPMCEutils eUtils, NcbiPMCEutils.ESearchResult eSearch, int recCountToFetch, int retstart) throws Exception {

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
