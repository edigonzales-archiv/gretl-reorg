package ch.so.agi.gretl.jobs;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.gradle.testkit.runner.TaskOutcome.*;
import static org.junit.Assert.assertEquals;

public class PostgisRasterExportTest {
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
                .withPluginClasspath()
                .build();

        assertEquals(SUCCESS, result.task(":exportGeotiff").getOutcome());

        long targetFileSize = new File(jobPath, targetFileName).length();
        long exportFileSize = new File(jobPath, exportFileName).length();

        assertEquals(targetFileSize, exportFileSize);
    }
}
