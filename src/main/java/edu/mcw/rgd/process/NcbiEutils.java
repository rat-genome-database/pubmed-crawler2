package edu.mcw.rgd.process;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jaxen.XPath;
import org.jaxen.xom.XOMXPath;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for NCBI eUtils API (eSearch and eFetch).
 */
public class NcbiEutils {

    private static final Logger logger = LogManager.getLogger(NcbiEutils.class);

    private String eUtils_db;
    private String eUtils_tool;
    private String eUtils_email;
    private String ncbiSearchUrl;
    private String ncbiFetchUrl;

    /**
     * Execute eSearch query to find article IDs.
     */
    public ESearchResult runESearch(String term) throws Exception {
        String url = buildSearchUrl(term);

        ESearchResult result = new ESearchResult();
        result.queryOriginal = url;

        Document doc = downloadAndParseXml(url);
        result.xml = doc.toXML();

        // Extract search results
        result.recordCount = xpathString(doc, "/eSearchResult/Count");
        result.queryKey = xpathString(doc, "/eSearchResult/QueryKey");
        result.webEnv = xpathString(doc, "/eSearchResult/WebEnv");
        result.queryTranslation = xpathString(doc, "/eSearchResult/QueryTranslation");

        // Extract IDs
        XPath xpath = new XOMXPath("/eSearchResult/IdList/Id");
        List<Element> idElements = (List<Element>) xpath.selectNodes(doc);
        result.ids = new ArrayList<>(idElements.size());
        for (Element el : idElements) {
            result.ids.add(el.getValue());
        }

        return result;
    }

    /**
     * Execute eFetch to download article records.
     */
    public File runEFetch(ESearchResult search, String retMode, int retMax, int retStart) throws Exception {
        int currentRetMax = retMax;

        while (currentRetMax > 0) {
            try {
                String url = buildFetchUrl(search, retMode, currentRetMax, retStart);
                search.fetchQuery = url;

                File file = downloadToTempFile(url);
                search.fetchedCount = getTagCount(file, "<PubmedArticle>");
                return file;

            } catch (IOException e) {
                throw e;  // IO errors should propagate immediately
            } catch (Exception e) {
                // Reduce batch size and retry
                if (currentRetMax > 5) {
                    currentRetMax /= 5;
                    logger.warn("Reducing batch size to {} due to error: {}", currentRetMax, e.getMessage());
                } else if (currentRetMax > 1) {
                    currentRetMax = 1;
                } else {
                    throw new Exception("Cannot download data from NCBI after retries", e);
                }
            }
        }

        throw new Exception("Cannot download data from NCBI");
    }

    private String buildSearchUrl(String term) throws Exception {
        return ncbiSearchUrl +
                "?db=" + eUtils_db +
                "&tool=" + eUtils_tool +
                "&email=" + eUtils_email +
                "&term=" + URLEncoder.encode(term, StandardCharsets.UTF_8) +
                "&retmax=100000000&usehistory=y";
    }

    private String buildFetchUrl(ESearchResult search, String retMode, int retMax, int retStart) {
        return ncbiFetchUrl +
                "?db=" + eUtils_db +
                "&tool=" + eUtils_tool +
                "&email=" + eUtils_email +
                "&WebEnv=" + search.webEnv +
                "&query_key=" + search.queryKey +
                "&retmode=" + (retMode != null ? retMode : "xml") +
                "&retmax=" + retMax +
                "&retstart=" + retStart;
    }

    private Document downloadAndParseXml(String url) throws Exception {
        File tmpFile = downloadToTempFile(url);

        try {
            removeDocType(tmpFile);

            // Retry parsing with backoff for transient errors
            for (int retry = 0; retry < 10; retry++) {
                try {
                    Document doc = new Builder().build(tmpFile);
                    Thread.sleep((long) (Math.random() * 1000)); // Rate limiting
                    return doc;
                } catch (IOException e) {
                    logger.warn("Parse attempt {} failed: {}", retry + 1, e.getMessage());
                    Thread.sleep(5000L * (retry + 1));
                }
            }
            throw new Exception("Failed to parse XML after retries");
        } finally {
            tmpFile.delete();
        }
    }

    private File downloadToTempFile(String url) throws Exception {
        FileDownloader downloader = new FileDownloader();
        downloader.setMaxRetryCount(8);
        downloader.setDownloadRetryInterval(20);

        File tmpFile = File.createTempFile("ncbi", ".xml");
        downloader.setExternalFile(url);
        downloader.setLocalFile(tmpFile.getAbsolutePath());

        logger.debug("Downloading: {}", url);
        downloader.download();

        return tmpFile;
    }

    private void removeDocType(File file) throws IOException {
        File tmpFile = File.createTempFile("nodoctype", ".xml");

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("<!DOCTYPE")) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }

        // Copy back to original file
        try (BufferedReader reader = new BufferedReader(new FileReader(tmpFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }

        tmpFile.delete();
    }

    private String xpathString(Document doc, String expression) throws Exception {
        return new XOMXPath(expression).stringValueOf(doc);
    }

    public static int getTagCount(File file, String tag) throws Exception {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(tag)) {
                    count++;
                }
            }
        }
        return count;
    }

    // Setters
    public void seteUtils_db(String db) { this.eUtils_db = db; }
    public void seteUtils_tool(String tool) { this.eUtils_tool = tool; }
    public void seteUtils_email(String email) { this.eUtils_email = email; }
    public void setNcbiSearchUrl(String url) { this.ncbiSearchUrl = url; }
    public void setNcbiFetchUrl(String url) { this.ncbiFetchUrl = url; }

    /**
     * Result from eSearch query.
     */
    public class ESearchResult {
        public String recordCount;
        public String queryKey;
        public String webEnv;
        public String queryTranslation;
        public String queryOriginal;
        public String xml;
        public List<String> ids;
        public String fetchQuery;
        public int fetchedCount;
        public int totalFetchedCount;
    }
}
