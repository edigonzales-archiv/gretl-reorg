package ch.so.agi.gretl.steps;

import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import ch.so.agi.gretl.steps.FTPSimpleClient;

public class FTPSimpleClientTest {
	private FakeFtpServer fakeFtpServer;
	
	@Rule
    public TemporaryFolder folder = new TemporaryFolder();
	
	private String REMOTE_PATH = "/data";
	private String REMOTE_FILE = "foobar.txt";
	private String REMOTE_FILE_CONTENT = "abcdef 1234567890";

	@Before
	public void setup() throws IOException {
		fakeFtpServer = new FakeFtpServer();
		fakeFtpServer.addUserAccount(new UserAccount("user", "password", "/data"));

		UnixFakeFileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new DirectoryEntry(REMOTE_PATH));
		fileSystem.add(new FileEntry(REMOTE_PATH + "/" + REMOTE_FILE, REMOTE_FILE_CONTENT));
		fakeFtpServer.setFileSystem(fileSystem);
		fakeFtpServer.setServerControlPort(0);

		fakeFtpServer.start();

        
	}
	
	@After
	public void teardown() throws IOException {
		fakeFtpServer.stop();
	}
	
	@Test
	public void givenRemoteFile_whenDownloading_thenItIsOnTheLocalFilesystem() throws IOException {
		FTPSimpleClient ftpClient = new FTPSimpleClient();//("localhost", fakeFtpServer.getServerControlPort(), "user", "password");
        ftpClient.open("localhost", "user", "password", fakeFtpServer.getServerControlPort());
        String tmpDir = folder.newFolder().getAbsolutePath();
		ftpClient.downloadFile("/data/foobar.txt", tmpDir, true);
		ftpClient.close();
		
		File file = Paths.get(tmpDir, REMOTE_FILE).toFile();
		Assert.assertTrue("file exists", file.exists());
		
		String fileContent = new String(Files.readAllBytes(Paths.get(tmpDir, REMOTE_FILE)));
		Assert.assertTrue("equal file content", fileContent.equals(REMOTE_FILE_CONTENT));
	}
	
	// Tests:
	// - skipOnFailure
	// - could not connect
	// -...

}
