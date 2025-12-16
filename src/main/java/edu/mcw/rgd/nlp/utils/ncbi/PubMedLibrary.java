package edu.mcw.rgd.nlp.utils.ncbi;

import edu.mcw.rgd.process.NcbiEutils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * PubMed Abstract Crawler - Downloads PubMed abstracts daily from NCBI.
 *
 * Usage: java -jar pubmed-crawler.jar --crawlByDate &lt;outputDir&gt; &lt;startDate&gt; &lt;endDate&gt;
 *
 * Date format: yyyy/MM/dd
 *
 * Output structure:
 *   outputDir/
 *     YYYY/                    (year directories)
 *       YYYY_MM_DD_NNN.xml     (article XML files, NNN = chunk number)
 *     pubmed_ids/
 *       YYYY-MM-DD.txt         (list of PubMed IDs per day)
 */
public class PubMedLibrary {

    private static final Logger logger = LogManager.getLogger(PubMedLibrary.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy_MM_dd");

    private final Path outputDir;
    private final PubMedRetriever retriever;

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        crawlByDate(args);

        printUsage();
    }

    private static void printUsage() {
        System.out.println("PubMed Abstract Crawler");
        System.out.println("Usage: --crawlByDate <outputDir> <startDate> <endDate>");
        System.out.println("  Date format: yyyy/MM/dd");
        System.out.println("  Example: --crawlByDate ./data 2025/01/01 2025/01/31");
    }

    public static void crawlByDate(String[] args) {
        if (args.length < 4) {
            logger.error("Insufficient arguments. Usage: --crawlByDate <outputDir> <startDate> <endDate>");
            return;
        }

        String outputPath = args[1];
        LocalDate startDate;
        LocalDate endDate;

        try {
            startDate = LocalDate.parse(args[2], DATE_FORMAT);
        } catch (DateTimeParseException e) {
            logger.error("Invalid start date: {}. Expected format: yyyy/MM/dd", args[2]);
            return;
        }

        try {
            endDate = LocalDate.parse(args[3], DATE_FORMAT);
        } catch (DateTimeParseException e) {
            logger.error("Invalid end date: {}. Expected format: yyyy/MM/dd", args[3]);
            return;
        }

        try {
            PubMedLibrary crawler = new PubMedLibrary(outputPath);
            crawler.crawlDateRange(startDate, endDate);
        } catch (Exception e) {
            logger.error("Crawl failed", e);
        }
    }

    public PubMedLibrary(String outputPath) throws IOException {
        this.outputDir = Paths.get(outputPath);
        Files.createDirectories(outputDir);

        this.retriever = new PubMedRetriever();
        retriever.initialize();
    }

    /**
     * Crawl PubMed abstracts for a date range.
     */
    public void crawlDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Starting crawl from {} to {}", startDate, endDate);

        LocalDate current = startDate;
        int successCount = 0;
        int failCount = 0;

        while (!current.isAfter(endDate)) {
            try {
                crawlDate(current);
                successCount++;
            } catch (Exception e) {
                logger.error("Failed to crawl date: {}", current, e);
                failCount++;
            }
            current = current.plusDays(1);
        }

        logger.info("Crawl completed. Success: {}, Failed: {}", successCount, failCount);
    }

    /**
     * Crawl all PubMed abstracts for a single date.
     */
    public void crawlDate(LocalDate date) throws Exception {
        String dateStr = date.format(DATE_FORMAT);
        logger.info("Crawling date: {}", dateStr);

        // Search for article IDs
        NcbiEutils.ESearchResult searchResult = retriever.getIdSetByDate(dateStr);
        List<String> ids = searchResult.ids;

        if (ids == null || ids.isEmpty()) {
            logger.info("No articles found for date: {}", dateStr);
            return;
        }

        logger.info("Found {} articles for {}", ids.size(), dateStr);

        // Save article IDs
        saveArticleIds(date, ids);

        // Download articles in chunks
        downloadArticles(date, searchResult);

        logger.info("Completed crawl for date: {}", dateStr);
    }

    private void saveArticleIds(LocalDate date, List<String> ids) throws IOException {
        Path idDir = outputDir.resolve("pubmed_ids");
        Files.createDirectories(idDir);

        String fileName = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".txt";
        Path idFile = idDir.resolve(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(idFile)) {
            for (String id : ids) {
                writer.write(id);
                writer.newLine();
            }
        }

        logger.info("Saved {} IDs to {}", ids.size(), idFile);
    }

    private void downloadArticles(LocalDate date, NcbiEutils.ESearchResult searchResult) throws Exception {
        Path yearDir = outputDir.resolve(String.valueOf(date.getYear()));
        Files.createDirectories(yearDir);

        String fileBase = date.format(FILE_DATE_FORMAT);
        int chunkNum = 0;

        while (searchResult.totalFetchedCount < searchResult.ids.size()) {
            String fileName = String.format("%s_%03d.xml", fileBase, chunkNum);
            Path outFile = yearDir.resolve(fileName);

            File downloaded = retriever.crawlByIdList(searchResult, outFile.toString());

            if (downloaded == null) {
                logger.warn("Failed to download chunk {} for date {}", chunkNum, date);
                break;
            }

            logger.info("Downloaded chunk {}: {} articles (total: {}/{})",
                    chunkNum, searchResult.fetchedCount,
                    searchResult.totalFetchedCount, searchResult.ids.size());

            if (searchResult.fetchedCount == 0) {
                break;
            }

            chunkNum++;
        }
    }
}
