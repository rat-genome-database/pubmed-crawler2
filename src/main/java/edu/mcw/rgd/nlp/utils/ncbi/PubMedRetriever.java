package edu.mcw.rgd.nlp.utils.ncbi;

import edu.mcw.rgd.process.NcbiEutils;
import nu.xom.Builder;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Retrieves PubMed articles from NCBI using eUtils API.
 */
public class PubMedRetriever {

    private static final Logger logger = LogManager.getLogger(PubMedRetriever.class);

    private static final String DB_NAME = "pubmed";
    private static final int BATCH_SIZE = 1000;
    private static final int MAX_RETRIES = 3;

    private NcbiEutils eUtils;

    public void initialize() {
        eUtils = new NcbiEutils();
        eUtils.seteUtils_db(DB_NAME);
        eUtils.seteUtils_email("mtutaj@mcw.edu");
        eUtils.seteUtils_tool("pubmed_crawler");
        eUtils.setNcbiSearchUrl("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi");
        eUtils.setNcbiFetchUrl("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi");
    }

    /**
     * Search for PubMed article IDs by date.
     * Searches multiple date fields: completion, modification, entrez, and MeSH dates.
     */
    public NcbiEutils.ESearchResult getIdSetByDate(String date) throws Exception {
        String query = String.format(
            "((\"%s\"[CDAT] : \"%s\"[CDAT]) " +  // Date - Completion
            "OR (\"%s\"[MDAT] : \"%s\"[MDAT]) " + // Date - Modification
            "OR (\"%s\"[EDAT] : \"%s\"[EDAT]) " + // Date - Entrez
            "OR (\"%s\"[MHDA] : \"%s\"[MHDA]))",  // Date - MeSH
            date, date, date, date, date, date, date, date
        );

        return eUtils.runESearch(query);
    }

    /**
     * Download a batch of articles and save to file.
     * Updates searchResult.totalFetchedCount and searchResult.fetchedCount.
     *
     * @return the output file, or null if download failed
     */
    public File crawlByIdList(NcbiEutils.ESearchResult searchResult, String outFileName) throws Exception {
        File tmpFile = fetchWithRetry(searchResult);
        searchResult.totalFetchedCount += searchResult.fetchedCount;

        // Validate the downloaded file
        if (!validateXmlFile(tmpFile, outFileName)) {
            tmpFile.delete();
            return null;
        }

        // Move to destination
        File outFile = new File(outFileName);
        outFile.getParentFile().mkdirs();
        FileUtils.copyFile(tmpFile, outFile);
        tmpFile.delete();

        return outFile;
    }

    private File fetchWithRetry(NcbiEutils.ESearchResult searchResult) throws Exception {
        Exception lastException = null;

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                return eUtils.runEFetch(searchResult, "xml", BATCH_SIZE, searchResult.totalFetchedCount);
            } catch (IOException e) {
                lastException = e;
                logger.warn("Fetch attempt {} failed: {}", attempt + 1, e.getMessage());
            }
        }

        // Final attempt - let exception propagate
        return eUtils.runEFetch(searchResult, "xml", BATCH_SIZE, searchResult.totalFetchedCount);
    }

    private boolean validateXmlFile(File file, String outFileName) {
        try {
            // Check for PubmedArticleSet tag
            int tagCount = NcbiEutils.getTagCount(file, "<PubmedArticleSet>");
            if (tagCount != 1) {
                String preview = getFilePreview(file);
                logger.error("Malformed file {} - expected 1 <PubmedArticleSet> tag, found {}. Preview: {}",
                        outFileName, tagCount, preview);
                return false;
            }

            // Validate XML structure
            new Builder().build(file);
            return true;

        } catch (Exception e) {
            logger.error("XML validation failed for {}: {}", outFileName, e.getMessage());
            return false;
        }
    }

    private String getFilePreview(File file) {
        try {
            String content = FileUtils.readFileToString(file, "UTF-8");
            return content.length() > 500 ? content.substring(0, 500) + "..." : content;
        } catch (Exception e) {
            return "(unable to read file)";
        }
    }
}
