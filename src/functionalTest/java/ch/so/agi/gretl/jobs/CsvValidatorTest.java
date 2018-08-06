package ch.so.agi.gretl.jobs;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.File;

import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.*;

public class CsvValidatorTest {
    @Test
    public void validationOk() throws Exception {
        BuildResult result = GradleRunner.create()
                .withProjectDir(new File("src/functionalTest/jobs/CsvValidator/"))
                .withArguments("-i")
                .withPluginClasspath()
                .build();

        assertTrue(result.getOutput().contains("...validation done"));
        assertEquals(SUCCESS, result.task(":validate").getOutcome());
    }
    
    @Test
    public void validationFail() throws Exception {
        BuildResult result = GradleRunner.create()
                .withProjectDir(new File("src/functionalTest/jobs/CsvValidatorFail/"))
                .withArguments("-i")
                .withPluginClasspath()
                .buildAndFail();

        assertTrue(result.getOutput().contains("...validation failed"));
        assertEquals(FAILED, result.task(":validate").getOutcome());
    }
}
