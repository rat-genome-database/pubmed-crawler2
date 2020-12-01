package edu.mcw.rgd.nlp.utils.ncbi;

import edu.mcw.rgd.common.utils.FileEntry;
import edu.mcw.rgd.hadoop.*;
import edu.mcw.rgd.common.utils.FileList;
import edu.mcw.rgd.common.utils.HTML2XML;
import edu.mcw.rgd.common.utils.ReadWrite;
import edu.mcw.rgd.process.NcbiEutils;
import edu.mcw.rgd.util.StringUtils;
// newly added ---
import edu.mcw.rgd.nlp.utils.ncbi.PMCRetriever;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONObject;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

//import edu.mcw.rgd.indexing.IndexClass;

/**
 * Created by IntelliJ IDEA.
 * User: mtutaj
 * Date: 1/8/15
 * Time: 8:23 AM
 * <p>logic based on PubMedLibrary class from text_mining/Java/TextMiningTools project
 */
public class PubMedLibrary {

	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

	protected static final Logger logger = Logger.getLogger(PubMedLibrary.class);
	protected String pathDoc;
	protected FileList fileList = new FileList();
	protected FileList failedList = new FileList();
	protected FileList emptyList = new FileList();

	protected static String DATE_FILE_DIR = "/date_id_maps/";
	protected static DateFormat FILE_NAME_DF = new SimpleDateFormat("yyyy_MM_dd");

	public static void main(String[] args) throws Exception {
	/*	args=new String[4];
	args[0]="crawlByDate";
		args[1]="src/..";
	args[2]="2015/10/16";
		args[3]="2015/10/17";
	*/
		//crawlByDate(args);
indexer();
	}

	public static void crawlByDate(String[] args) throws Exception {

		String path = args[1];

		Date startDate = new SimpleDateFormat("yyyy/MM/dd").parse(args[2]), endDate = new SimpleDateFormat("yyyy/MM/dd").parse(args[3]);
		getDates(args, 2, startDate, endDate);

		// configure log4j logger for pubmed crawler
		
//		PropertyConfigurator.configure(path + "/PubMedCrawler2/properties/crawler.cnf");  // modified to the next line!!!
		
		PropertyConfigurator.configure("properties/crawler.cnf");
		
		// Crawl by dates
		logger.info("Start crawling: [" + path + "] from [" + startDate	+ "] to [" + endDate + "]");

		PubMedLibrary library = new PubMedLibrary();
		library.setPathDoc(path);
		//library.tryFailedDates(); --------------------- it's disabled
		library.batchDownload(startDate, endDate, true);

		logger.info("Crawling finished [" + path + "] from [" + startDate + "] to [" + endDate + "]");
	}

	public void tryFailedDates() throws Exception {
		ArrayList<FileEntry> list_copy = failedList.cloneList();
		for (FileEntry fe: list_copy) {
			Date start_date = DATE_FORMAT.parse(fe.getFileName());
			Date end_date = new Date(start_date.getTime() + 23 * 3600 * 1000 + 59 * 60 * 1000 + 59 * 1000);

			batchDownload(start_date, end_date, true);
		}
	}

	/**
	 * @param start_date start date
	 * @param end_date end date
	 * @parem force_update force update
	 * @return
	 */
	public int batchDownload(Date start_date, Date end_date, boolean force_update) throws IOException {
		String start_date_str = DATE_FORMAT.format(start_date);
		String end_date_str = DATE_FORMAT.format(end_date);
		logger.info("Start downloading from " + start_date_str + " to "	+ end_date_str);

		PubMedRetriever retriever = new PubMedRetriever();
		retriever.initialize();

		boolean forward_crawling = start_date.before(end_date);

		while ((forward_crawling && !start_date.after(end_date))
				|| (!forward_crawling && !start_date.before(end_date))) {

			String startdateStr = DATE_FORMAT.format(start_date);
			if (force_update) {
				fileList.removeFile(startdateStr);
				fileList.save();
				failedList.removeFile(startdateStr);
				failedList.save();
				emptyList.removeFile(startdateStr);
				emptyList.save();
			}

			if (fileList.findFile(startdateStr, 0) < 0) {
				
				
				downloadForDate(start_date, startdateStr, retriever);

			} else {
				logger.info("Date already crawled " + startdateStr);
			}

			start_date = new Date(start_date.getTime() + (forward_crawling ? 1 : -1) * 24 * 3600 * 1000);
		}

		logger.info("Finished downloading from " + start_date_str + " to " + end_date_str);

		return 0;
	}

	void downloadForDate(Date date, String dateStr, PubMedRetriever retriever) {
		try {
			
			
			
			logger.info("Crawling " + dateStr);
			fileList.removeFile(dateStr);
			fileList.save();
			failedList.addFile(dateStr);
			failedList.save();
			NcbiEutils.ESearchResult eSearch = retriever.getIdSetByDate(dateStr);
			logger.info("  count of articles to be downloaded: "+eSearch.ids.size());
			saveDateIdMap(date, eSearch.ids);
			crawlFilesByDate(date, eSearch, retriever);

			
			System.out.println("File for "+dateStr+" is downloaded");
			String fileName=dateStr.replace("/", "-")+".txt";
			
			

			// saving pubmed IDs into files, for each day there is one file containing IDs 
		
			FileWriter fw=new FileWriter("download/PubMedIDs/"+fileName);
			fw.write(eSearch.ids.toString().replace(", ", "\n").replace("[", "").replace("]", ""));
			fw.close();

			// Sending Pubmed IDs to PMCRetriever to download those IDs that have PMC papers.

//			PMCRetriever.downloadPMCPapers(eSearch.ids, dateStr);   //Disabled now!

			// write hdfs copy commands -----------------------------------------

			failedList.removeFile(dateStr);
			failedList.save();
			fileList.addFile(dateStr);
			fileList.save();
		}
		catch(Exception e) {
			logger.error("Error downloading for date "+dateStr);
			e.printStackTrace();
		}
	}

	public void saveDateIdMap(Date date, Collection<String> ids) throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(getDateFilePath(date)));
		if (ids != null) {
			bw.write(String.format("%d", ids.size()));
			bw.newLine();
			for (String id: ids) {
				bw.write(id);
				bw.newLine();
			}
		} else {
			bw.write("0");
			bw.newLine();
		}
		bw.close();
	}

	public String getDateFilePath(Date date) throws Exception {
		File file = new File(getDocPath() + DATE_FILE_DIR);
		try {
			if (!file.exists())
				file.mkdir();
		} catch (Exception e) {
			logger.error("Error getting date file directory", e);
			throw e;
		}
		return getDocPath() + DATE_FILE_DIR + FILE_NAME_DF.format(date)	+ ".txt";
	}

	private static void getDates(String[] args, int startPos, Date startDateIn, Date endDateIn) {

		Date startDate, endDate;
		try {
			startDate = DATE_FORMAT.parse(args[startPos]);
			if (args[startPos + 1] != null && args[startPos + 1].length() > 0) {
				try {
					endDate = DATE_FORMAT.parse(args[startPos + 1]);
				} catch (Exception e) {
					System.out.println("Can't get End date!");
					return;
				}
			} else {
				endDate = startDate;
			}
		} catch (Exception e) {
			System.out.println("Can't get Start date!");
			return;
		}

		if (endDate.after(endDateIn)) {
			endDate.setTime(endDateIn.getTime());
		} else if (!startDate.after(endDate))
			endDate = new Date(endDate.getTime() + 23 * 3600 * 1000 + 59 * 60 * 1000 + 59 * 1000);
		startDateIn.setTime(startDate.getTime());
		endDateIn.setTime(endDate.getTime());
	}

	public void crawlFilesByDate(Date date, NcbiEutils.ESearchResult eSearch, PubMedRetriever retriever) throws Exception {
		if (eSearch.ids==null || eSearch.ids.size()==0 )
			return;

		int cur_file_no = 0;
		String file_name_base = getFileNameByDate(date);
		String file_path = getFilePathByDate(date);

		if(false) {
			// delete existing files for this date
			int staleFilesDeleted = 0;
			for( File f: new File(file_path).listFiles() ) {
				if( f.getName().contains(file_name_base) ) {
					f.delete();
					staleFilesDeleted++;
				}
			}
			if( staleFilesDeleted>0 ) {
				logger.info(staleFilesDeleted+" stale files deleted for "+file_name_base);
			}
		}

		String cur_file_no_str = String.format("%03d", cur_file_no);

		do {
			String file_name = file_name_base + "_" + cur_file_no_str;
			try {
				crawlDateChunk(file_name, file_path, eSearch, retriever);
			} catch (Exception e) {
				logger.error("Error in crawling a date chunk [" + cur_file_no_str + "]", e);
				throw e;
			}
			cur_file_no++;
			cur_file_no_str = String.format("%03d", cur_file_no);
		}
		while(eSearch.fetchedCount>0 && eSearch.totalFetchedCount<eSearch.ids.size());
	}

	private void crawlDateChunk(String file_name, String file_path, NcbiEutils.ESearchResult eSearch, PubMedRetriever retriever) throws Exception {
		
	/*	System.out.println("waiting for 5 seconds ...");
		
		try {
		    Thread.sleep(5000);                 //1000 milliseconds is one second.
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}

		System.out.println("finish waiting ");
	*/
		String outFileName = file_path + "/" + file_name+ ".xml";
		File file = retriever.crawlByIdList(eSearch, outFileName);
		if( file == null ) {
			logger.warn(" failed to download to "+outFileName+" fetched:"+eSearch.totalFetchedCount);
			emptyList.addFile(file_name);
			emptyList.save();
		}
		else {
			logger.info(eSearch.fetchedCount + " articles downloaded to " + outFileName + ", total:" + eSearch.totalFetchedCount);
			
			BufferedReader br = new BufferedReader(new FileReader(outFileName));
			String line="";
			StringBuffer out=new StringBuffer();
			
			while ((line = br.readLine()) != null) {
				line=line.replace("<", "<ns1:").replace("<ns1:/", "</ns1:").trim();
				if(!line.contains("!DOCTYPE"))
					out.append(line);
			}
			
			ReadWrite.write(out.toString(), outFileName);
			
			logger.info("File format changed to the old file format");
//			ReadWrite.write(out, outFileName);
		}
	}

	public String getFileNameByDate(Date date) {
		return FILE_NAME_DF.format(date);
	}

	public String getFilePathByDate(Date date) {
		DateFormat df = new SimpleDateFormat("yyyy");
		String path = getDocPath() + "/" + df.format(date);
		File file = new File(path);
		if (!file.exists())
			file.mkdir();
		return path;
	}

	public String getPathDoc() {
		return pathDoc;
	}

	public void setPathDoc(String pathDoc) throws Exception {

		this.pathDoc = pathDoc + ((pathDoc.charAt(pathDoc.length() - 1) == '/') ?  "" : "/");

		fileList.setFilePath(this.pathDoc + "file_list.txt", true);
		failedList.setFilePath(this.pathDoc + "file_list_failed.txt", true);
		emptyList.setFilePath(this.pathDoc + "file_list_empty.txt", true);
	}

	public String getDocPath() {
		return getPathDoc();
	}

	public static void indexer() throws SolrServerException, IOException {
		//Preparing the Solr client


		SolrServer Solr = new HttpSolrServer("http://localhost:8983/solr/test");

		try {
//            SolrPingResponse pingResponse = Solr.ping();
			//           System.out.println("Response "+ pingResponse.getResponse() + "," + pingResponse.getStatus());
			UpdateRequest updateRequest = new UpdateRequest();
			updateRequest.setAction( UpdateRequest.ACTION.COMMIT, false, false);


			File folder = new File("data/");
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
