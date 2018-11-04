package ch.so.agi.gretl.tasks;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.FTPSimpleClient;

public class FTPDownload extends DefaultTask {
    private GretlLogger log;

    @Input
    public String host;

    @Input
    public String username;
    
    @Input
    public String password;
    
    @Input
    @Optional
    public int port = 21;

    @InputFile
    public String dataFile;
    
    @Input
    public String downloadDirectory;

    /**
     * Download a file from a FTP server. Task will finish withou
     * errors if the file does not exist on the FTP server.
     */
    @TaskAction
    public void downloadFile() {
        log = LogEnvironment.getLogger(ShpExport.class);
        
        if (host == null) {
            throw new IllegalArgumentException("host must not be null");
        }
        if (username == null) {
            throw new IllegalArgumentException("username must not be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("password must not be null");
        }
        if (dataFile == null) {
            throw new IllegalArgumentException("dataFile must not be null");
        }
        if (downloadDirectory == null) {
            throw new IllegalArgumentException("downloadDirectory must not be null");
        }
        
        FTPSimpleClient ftpClient = new FTPSimpleClient();
        ftpClient.open(host, username, password, port);
        ftpClient.downloadFile(dataFile, downloadDirectory);
        ftpClient.close();
    }
}
