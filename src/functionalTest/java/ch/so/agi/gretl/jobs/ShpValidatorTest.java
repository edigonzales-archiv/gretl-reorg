package ch.so.agi.gretl.jobs;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.File;

import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.*;

public class ShpValidatorTest {
    @Test
    public void validationOk() throws Exception {
        // run job
        BuildResult result = GradleRunner.create()
                .withProjectDir(new File("src/functionalTest/jobs/ShpValidator/"))
                .withArguments("-i")
                .withPluginClasspath()
                .build();

        // check result
        assertTrue(result.getOutput().contains("...validation done"));
        assertEquals(SUCCESS, result.task(":validate").getOutcome());
    }
    
    @Test
    public void validationFail() throws Exception {
        // run job
        BuildResult result = GradleRunner.create()
                .withProjectDir(new File("src/functionalTest/jobs/ShpValidatorFail/"))
                .withArguments("-i")
                .withPluginClasspath()
                .buildAndFail();

        // check result
        assertTrue(result.getOutput().contains("...validation failed"));
        assertEquals(FAILED, result.task(":validate").getOutcome());
    }
}
