package ch.so.agi.gretl.jobs;

import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.util.TestUtilSqlPostgres;

public class Ili2pgImportSchemaTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    
    Connection con = null;
    
    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername("ddluser")
        .withPassword("ddluser")
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    @Test
    public void importOk() throws Exception {
        String schemaName = "schemaimport".toLowerCase();
        try {
            // prepare postgres
            con = TestUtilSqlPostgres.connect(postgres);

            // run job
            BuildResult result = GradleRunner.create()
                    .withProjectDir(new File("src/functionalTest/jobs/Ili2pgImportSchema/"))
                    .withArguments("-i")
                    .withArguments("-Pdb_uri=" + postgres.getJdbcUrl())
                    .withPluginClasspath()
                    .build();

            // check results
            assertEquals(SUCCESS, result.task(":schemaimport").getOutcome());

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = '" + schemaName + "' AND table_name = 'eigentumsbeschraenkung'"); 
            
            if(!rs.next()) {
                fail();
            }
            assertEquals("eigentumsbeschraenkung",rs.getString(1));
            if(rs.next()) {
                fail();
            }
            rs.close();
            stmt.close();        
        } finally {
            con.close();
        }
    }
    
    @Test
    // Fails b/c user has no permissions.
    public void importFail() throws Exception {
        String schemaName = "schemaimportfail".toLowerCase();
        try {
            // prepare postgres
            con = TestUtilSqlPostgres.connect(postgres);

            // run job
            BuildResult result = GradleRunner.create()
                    .withProjectDir(new File("src/functionalTest/jobs/Ili2pgImportSchemaFail/"))
                    .withArguments("-i")
                    .withArguments("-Pdb_uri=" + postgres.getJdbcUrl())
                    .withPluginClasspath()
                    .buildAndFail();

            // check results
            assertEquals(FAILED, result.task(":schemaimport").getOutcome());

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = '" + schemaName + "' AND table_name = 'eigentumsbeschraenkung'"); 
            
            if(rs.next()) {
                fail();
            }
            
            rs.close();
            stmt.close();        
        } finally {
            con.close();
        }
    }

}
