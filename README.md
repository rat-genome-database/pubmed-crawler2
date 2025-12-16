# pubmed-crawler2

The project has been refactored and simplified. Here's a summary:

Refactored Project Structure

Before: 13 Java files with complex interdependencies
After: 3 clean Java files

Files:

1. PubMedLibrary.java (200 lines) - Main entry point
   - Clean command-line interface with usage help
   - Uses Java 8+ LocalDate instead of legacy Date
   - Uses java.nio.file.Path for cleaner file handling
   - Simple date range iteration
   - Structured output: outputDir/YYYY/YYYY_MM_DD_NNN.xml and outputDir/pubmed_ids/YYYY-MM-DD.txt
2. PubMedRetriever.java (120 lines) - PubMed API interface
   - Searches for article IDs by date
   - Downloads articles in batches with retry logic
   - Validates XML before saving
3. NcbiEutils.java (217 lines) - Low-level NCBI eUtils wrapper
   - eSearch for finding IDs
   - eFetch for downloading records
   - Proper error handling and rate limiting

Removed:

- FileList.java, FileEntry.java - Complex tracking replaced with simple file operations
- ReadWrite.java, GeneralMethods.java - Unused utilities
- HTML.java, HTML2XML.java, XMLreader.java, PMCreader.java - Unused parsers
- PMCRetriever.java, PMCUtils.java - PMC functionality (was disabled)

Usage:

java -jar pubmed-crawler.jar --crawlByDate ./data 2025/01/01 2025/01/31