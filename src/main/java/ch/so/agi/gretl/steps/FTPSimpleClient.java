package ch.so.agi.gretl.steps;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.gradle.api.GradleException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;

/**
 * Exposes and wraps some methods from Apache FTP library.
 *
 * @author Stefan Ziegler
 */
public class FTPSimpleClient {
    private GretlLogger log;
    
    private FTPClient ftpClient;
    
    public FTPSimpleClient() {
    	log = LogEnvironment.getLogger(FTPSimpleClient.class);
    }
    
    /**
     * Connect and login to a FTP server.
     * 
     * @param host
     * @param username
     * @param password
     * @param port
     */
    public void open(String host, String username, String password, int port) {
    	ftpClient = new FTPClient();
        try {
			ftpClient.connect(host, port);
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				throw new IOException("Exception in connecting to FTP server");
			}
			boolean ret = ftpClient.login(username, password);
			if (!ret) {
				String msg = "could not login to FTP server";
				throw new GradleException(msg);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
		} 
    }

    /**
     * Disconnects from the FTP server.
     */
    public void close() {
    	try {
			ftpClient.disconnect();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
		}
    }
    
    /**
     * Download a file from FTP server to a directory.
     * 
     * @param dataFile Remote file path
     * @param downloadDirectory The local directory where the file is stored
     */
    public void downloadFile(String dataFile, String downloadDirectory, boolean skipOnFailure) {
    	try {
        	// Figure out the directory of the file to get a list of
        	// all files in this directory.
        	String dataFileDirectory = FilenameUtils.getFullPath(dataFile);
			String[] files = ftpClient.listNames(dataFileDirectory);

			// Check if the file that should be downloaded is in the
			// remote directory.
			// This prevents FileOutputStream from creating an empty file.
			String fileName = FilenameUtils.getName(dataFile);
			ArrayList<String> fileList = new ArrayList<String>(Arrays.asList(files));
	        if (!fileList.contains(fileName)) {
				String msg = "file not found on ftp server: " + dataFile;
				log.info(msg);
				if (skipOnFailure) {
					return;
				} else {
					throw new GradleException(msg);
				}
	        }
	        // Download file
	        FileOutputStream out = new FileOutputStream(Paths.get(downloadDirectory, fileName).toAbsolutePath().toString());
	        ftpClient.retrieveFile(dataFile, out);
	        
	        // TODO: Do we need another check? like file size
	        //out.getChannel().size()
		} catch (IOException e) {
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
		}
    }
}
