package ch.so.agi.gretl.jobs;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;

import static org.gradle.testkit.runner.TaskOutcome.*;
import static org.junit.Assert.assertEquals;

public class PostgisRasterExportTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
        
    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername("ddluser")
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    @Test
    public void exportTiff() throws Exception {
        String jobPath = "src/functionalTest/jobs/PostgisRasterTiffExport/";
        String exportFileName = "export.tif";
        String targetFileName = "target.tif";

        // Delete existing file from previous test runs.
        File file = new File(jobPath, exportFileName);
        Files.deleteIfExists(file.toPath());

        BuildResult result = GradleRunner.create()
                .withProjectDir(new File(jobPath))
                .withArguments("-i")
                .withArguments("-Pdb_uri=" + postgres.getJdbcUrl())
                .withPluginClasspath()
                .build();

        assertEquals(SUCCESS, result.task(":exportTiff").getOutcome());

        // Compare exported image with existing (=target) image.
        // Since ImageIO cannot deal with TIFF (< Java 9) we just
        // compare the file size. Not sure if this is robust (e.g.
        // different PostgreSQL/PostGIS version).
        long targetFileSize = new File(jobPath, targetFileName).length();
        long exportFileSize = new File(jobPath, exportFileName).length();

        assertEquals(targetFileSize, exportFileSize);
    }

    // At the moment this is more a test if GDAL drivers are enabled
    // in PostGIS.
    @Test
    public void exportGeoTiff() throws Exception {
        String jobPath = "src/functionalTest/jobs/PostgisRasterGeotiffExport/";
        String exportFileName = "export.tif";
        String targetFileName = "target.tif";

        // Delete existing file from previous test runs.
        File file = new File(jobPath, exportFileName);
        Files.deleteIfExists(file.toPath());

        BuildResult result = GradleRunner.create()
                .withProjectDir(new File(jobPath))
                .withArguments("-i")
                .withArguments("-Pdb_uri=" + postgres.getJdbcUrl())
                .withPluginClasspath()
                .build();

        assertEquals(SUCCESS, result.task(":exportGeotiff").getOutcome());

        long targetFileSize = new File(jobPath, targetFileName).length();
        long exportFileSize = new File(jobPath, exportFileName).length();

        assertEquals(targetFileSize, exportFileSize);
    }
}
