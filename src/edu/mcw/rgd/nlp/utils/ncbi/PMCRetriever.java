/* author: Omid ghiasvand oghiasvand@mcw.edu
 *  download and save pmc papers!
 */
package edu.mcw.rgd.nlp.utils.ncbi;

import java.io.FileWriter;
import edu.mcw.rgd.hadoop.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.mcw.rgd.common.utils.ReadWrite;
//import edu.mcw.rgd.indexing.IndexClass;
import edu.mcw.rgd.nlp.utils.ncbi.PMCUtils;

public class PMCRetriever {
	// download and save pmc paper -----
	public static void downloadPMCPapers(List<String> pubmedIdList, String date){
		List<String> paperList=new ArrayList<String>();
		HashMap<String, String> idMap=new HashMap<String, String>();
		HashMap<String, String> idMapNon=new HashMap<String, String>();
		HdfsTool hdfs=new HdfsTool();
		try{
			String pmcFile=ReadWrite.read("download-logs/pmcpapers-full-paper.txt");
			String pmcFileNon=ReadWrite.read("download-logs/pmcpapers-non-full.txt");

			StringBuffer pmcFileBuff=new StringBuffer();
			pmcFileBuff.append(pmcFile);
			StringBuffer pmcFileNonBuff=new StringBuffer();
			pmcFileNonBuff.append(pmcFileNon);

			if(pmcFile!=null){
				String[] pmcIds=pmcFile.split("\n");
				for(int i=0;i<pmcIds.length;i++)
					idMap.put(pmcIds[i], pmcIds[i]);
			}
			//---------- put pmc ids tp hash map (full text)
			if(pmcFileNon!=null){
				String[] pmcIdsNon=pmcFileNon.split("\n");
				for(int i=0;i<pmcIdsNon.length;i++)
					idMapNon.put(pmcIdsNon[i], pmcIdsNon[i]);
			}
			//---------- put pmc ids tp hash map (non-full text)
			System.out.println("Downloading PMC papers...");
			for (int i=0;i<pubmedIdList.size();i++)
			{
				// mapping PMID to PMC ID
				String pmcId=PMCUtils.mapPMID(pubmedIdList.get(i));
				if(!pmcId.isEmpty() && !idMap.containsKey(pmcId) && !idMapNon.containsKey(pmcId))
				{
					// downloading pmc paper
					String paper=PMCUtils.getPaper(pmcId);
					String fileName=date.replace("/", "-")+"-"+pubmedIdList.get(i)+".xml";
					// if there is full text download it! ----

					if (!paper.contains("The publisher of this article does not allow downloading of the full text in XML form"))
					{	
						paperList.add(paper);
						FileWriter fw=new FileWriter("../pmcpapers/"+fileName);
						fw.write(paper);
						fw.close();
						// --- copy to cluster gray01 -->
				    	hdfs.copyFromLocalToHdfs("../pmcpapers/"+fileName, "pmc");
						pmcFileBuff.append("\n"+pmcId);
					}
					else
					{
						pmcFileNonBuff.append("\n"+pmcId);
					}

				}
			}
			
			FileWriter fw=new FileWriter("download-logs/pmcpapers-full-paper.txt");
			fw.write(pmcFileBuff.toString());
			fw.close();
			
			FileWriter fw1=new FileWriter("download-logs/pmcpapers-non-full.txt");
			fw1.write(pmcFileNonBuff.toString());
			fw1.close();
			
			
			
			// indexing downloaded pmc papers
			//			if(paperList.size()>0){
			//				IndexClass.start(); 
			//				IndexClass.indexBulk(paperList);
			//			}
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
		System.out.println("PMC papers for date "+ date +" have been saved!!!");
	}
}
