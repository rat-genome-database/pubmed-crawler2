/*package edu.mcw.rgd.hadoop;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: mtutaj
 * Date: 4/14/15
 * Time: 11:04 AM
 * To change this template use File | Settings | File Templates.
 */
/*public class HdfsTool {

	public static Configuration config = new Configuration();

	public HdfsTool (){
		String hadoopConfDir = "/rgd/conf/hadoop-1.2.1/";
		config.addResource(new Path(hadoopConfDir+"core-site.xml"));
		config.addResource(new Path(hadoopConfDir+"hdfs-site.xml"));
		config.addResource(new Path(hadoopConfDir+"mapred-site.xml"));
	}

	public static void removeHdfsDir(String hdfsDir) throws IOException {
		FileSystem fs = FileSystem.get(config);
		Path dstPath = new Path(hdfsDir);
		fs.delete(dstPath, true);
	}

	public static void createHdfsDir(String hdfsDir) throws IOException {

		FileSystem fs = FileSystem.get(config);
		Path dstPath = new Path(hdfsDir);
		fs.mkdirs(dstPath);
	}

	public static void copyFromLocalToHdfs(String localFiles, String hdfsDir) throws IOException {

		// expand local files if they use wild cards '*' or '?'
		FileSystem fs = FileSystem.get(config);
		if( localFiles.contains("*") || localFiles.contains("?") ) {
			String dirName, fileMask;
			int slashPos = localFiles.lastIndexOf('/');
			if( slashPos<0 ) {
				dirName = ".";
				fileMask = localFiles;
			} else {
				dirName = localFiles.substring(0, slashPos);
				fileMask = localFiles.substring(slashPos+1);
			}
			File dir = new File(dirName);
			FileFilter fileFilter = new WildcardFileFilter(fileMask);
			for( File f: dir.listFiles(fileFilter) ) {
				copyFromLocalToHdfs(f.getAbsolutePath(), hdfsDir);
			}
		}
		else {
			Path dstPath = new Path(hdfsDir);
			Path srcPath = new Path(localFiles);
			System.out.println(" COPY TO HDFS  from ["+localFiles+"] to ["+hdfsDir+"]");
			fs.copyFromLocalFile(srcPath, dstPath);
		}
	}
	/*
    static public void getHostNames (String srcFile) throws IOException {

        DFSClient client = new DFSClient(config);

        FileSystem fs = FileSystem.get(config);
        Path srcPath = new Path(srcFile);
        Path dstPath = new Path("data/pubmed_to_hadoop/1.xml");
        fs.copyFromLocalFile(srcPath, dstPath);

    }
	 */
/*	static void usage() {
		String[] msgs = {
				"HdfsTool v. 1.0, written by Marek Tutaj in 2015",
				"  usage:",
				"HdfsTool <conf-dir> <cmd> <cmd-params>  ",
				"  <conf-dir> directory with hadoop config files: core-site.xml, hdfs-site.xml, mapred-site.xml",
				"  <cmd>        command to be executed",
				"  <cmd-params> command parameters (nr of parameters depends on command)",
				"",
				"available commands",
				"  rmdir <dfs-dir> - remove specified remote directory with all files",
				"  mkdir <dfs-dir> - create specified remote directory",
				"  copyFromLocal <local-files> <dfs-dir> - upload files from local dir to dfs-dir",
				"          wildcards can be used when specifying local-files",
				"          f.e. /rgd/pubmed/2015/2015_04_12*xml data/pubmed_to_hadoop",
		};
		for( String msg: msgs ) {
			System.out.println(msg);
		}
	}
}
*/