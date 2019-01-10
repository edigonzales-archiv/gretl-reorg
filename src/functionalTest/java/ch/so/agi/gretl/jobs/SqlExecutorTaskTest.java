package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.TestUtilSqlPostgres;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlExecutorTaskTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    
    Connection con = null;
    
    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername("ddluser")
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    /**
     * Tests that a chain of statements executes properly
     * 1. statement: fill the source table with rows
     * 2. statement: execute the "insert into select from" statement
     */
    @Test
    public void taskChainTest() throws Exception {
        String schemaName = "sqlExecuterTaskChain".toLowerCase();
        try {
            con = TestUtilSqlPostgres.connect(postgres);
            TestUtilSqlPostgres.createOrReplaceSchema(con, schemaName);
            createSqlExecuterTaskChainTables(con, schemaName);

            con.commit();
            con.close();

            // run job
            BuildResult result = GradleRunner.create()
                    .withProjectDir(new File("src/functionalTest/jobs/SqlExecutorTaskChain/"))
                    .withArguments("-i")
                    .withArguments("-Pdb_uri=" + postgres.getJdbcUrl())
                    .withPluginClasspath()
                    .build();

            // check results
            assertEquals(SUCCESS, result.task(":insertInto").getOutcome());

            con = TestUtilSqlPostgres.connect(postgres);

            String countSrcSql = String.format("SELECT count(*) FROM %s.albums_src", schemaName);
            String countDestSql = String.format("SELECT count(*) FROM %s.albums_dest", schemaName);

            int countSrc = TestUtilSqlPostgres.execCountQuery(con, countSrcSql);
            int countDest = TestUtilSqlPostgres.execCountQuery(con, countDestSql);

            Assert.assertEquals("Rowcount in destination table must be equal to rowcount in source table", countSrc, countDest);
            Assert.assertTrue("Rowcount in destination table must be greater than zero", countDest > 0);
        } finally {
            con.close();
        }
    }

    /**
     * Tests if the sql-files can be configured using a relative path.
     *
     * The relative path relates to the location of the build.gradle file
     * of the corresponding gretl job.
     */
    @Test
    public void relPathTest() throws Exception {
        String schemaName = "SqlExecuterRelPath".toLowerCase();
        try {
        	    // prepare postgres
            con = TestUtilSqlPostgres.connect(postgres);
            TestUtilSqlPostgres.createOrReplaceSchema(con, schemaName);
            createSqlExecuterTaskChainTables(con, schemaName);

            con.commit();
            con.close();

            // run job
            BuildResult result = GradleRunner.create()
                    .withProjectDir(new File("src/functionalTest/jobs/sqlExecutorTaskRelPath/"))
                    .withArguments("-i")
                    .withArguments("-Pdb_uri=" + postgres.getJdbcUrl())
                    .withPluginClasspath()
                    .build();

            // check results
            assertEquals(SUCCESS, result.task(":relativePathConfiguration").getOutcome());
        } finally {
            con.close();
        }
    }
    
    private void createSqlExecuterTaskChainTables(Connection con, String schemaName) {
        String ddlBase = "CREATE TABLE %s.albums_%s(" +
                "title text, artist text, release_date text," +
                "publisher text, media_type text)";

        try {
            //source table
            Statement s1 = con.createStatement();
            System.out.println(String.format(ddlBase, schemaName, "src"));
            s1.execute(String.format(ddlBase, schemaName, "src"));
            s1.close();

            //dest table
            Statement s2 = con.createStatement();
            s2.execute(String.format(ddlBase, schemaName,"dest"));
            s2.close();

            TestUtilSqlPostgres.grantDataModsInSchemaToUser(con, schemaName, TestUtilSqlPostgres.CON_DMLUSER);
        } catch(SQLException se) {
            throw new RuntimeException(se);
        }
    }
}
