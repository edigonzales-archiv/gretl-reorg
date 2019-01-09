package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.TestUtilSqlPostgres;
import ch.so.agi.gretl.util.TestUtilSqlSqlite;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

public class Postgres2SqliteTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
        
    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername("ddluser")
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    /**
     * Tests if the "special" datatypes (Date, Time, GUID, Geometry, ..) are transferred
     * faultfree from postgres to sqlite.
     */
    @Test
    public void positivePostgis2SqliteTest() throws Exception {
        String schemaName = "POSTGRES2SQLITE".toLowerCase();
        String geomWkt = "LINESTRING(2600000 1200000,2600001 1200001)";
        String sqliteDbFileName = "src/functionalTest/jobs/postgres2sqliteDatatypes/"+schemaName+".sqlite";

        Connection srcCon = null;
        Connection targetCon = null;
        
        try {
            // prepare postgres
            srcCon = TestUtilSqlPostgres.connect(postgres);
            TestUtilSqlPostgres.createOrReplaceSchema(srcCon, schemaName);

            Statement stmtPg = srcCon.createStatement();
            String createSqlPg = "CREATE TABLE "+schemaName+".source_data(MYINT INTEGER, MYFLOAT REAL, MYTEXT VARCHAR(50), MYDATE DATE, MYTIME TIME, " +
                    "MYUUID UUID, MYGEOM GEOMETRY(LINESTRING,2056))";
            stmtPg.execute(createSqlPg);

            String insertSqlPg = "INSERT INTO "+schemaName+".source_data VALUES(15, 9.99, 'Hello Db2Db', CURRENT_DATE, CURRENT_TIME, '"+UUID.randomUUID()+"', ST_GeomFromText('"+geomWkt+"', 2056))";
            stmtPg.execute(insertSqlPg);

            TestUtilSqlPostgres.grantDataModsInSchemaToUser(srcCon, schemaName, TestUtilSqlPostgres.CON_DMLUSER);

            srcCon.commit();

            // prepare sqlite
            File sqliteDb = new File(sqliteDbFileName);
            Files.deleteIfExists(sqliteDb.toPath());
            sqliteDb = new File(sqliteDbFileName);

            targetCon = TestUtilSqlSqlite.connect(sqliteDb);
            
            String sqlSqlite = "CREATE TABLE target_data(MYINT INTEGER, MYFLOAT REAL, MYTEXT TEXT, MYDATE TEXT, MYTIME TEXT, " +
                    "MYUUID TEXT, MYGEOM_WKT TEXT)";
            Statement stmtSqlite = targetCon.createStatement();
            stmtSqlite.execute(sqlSqlite);

            targetCon.commit();
            targetCon.close();

            // run gradle
            BuildResult result = GradleRunner.create()
                    .withProjectDir(new File("src/functionalTest/jobs/Postgres2SqliteDatatypes/"))
                    .withArguments("-i")
                    .withArguments("-Pdb_uri=" + postgres.getJdbcUrl())
                    .withPluginClasspath()
                    .build();

            // check results
            assertEquals(SUCCESS, result.task(":readFromPostgres").getOutcome());

            String checkSQL = "SELECT COUNT(*) FROM target_data WHERE " +
                    "MYINT IS NOT NULL AND MYFLOAT IS NOT NULL AND MYTEXT IS NOT NULL AND MYDATE IS NOT NULL AND MYTIME IS NOT NULL AND MYUUID IS NOT NULL AND " +
                    "MYGEOM_WKT = '"+geomWkt+"'";

            targetCon = TestUtilSqlSqlite.connect(sqliteDb);            
            int countDest = TestUtilSqlSqlite.execCountQuery(targetCon, "SELECT count(*) FROM target_data;");

            Assert.assertEquals(
                    "Check Statement must return exactly one line.",
                    1,
                    countDest);
        } finally {
        	    srcCon.close();;
        	    targetCon.close();
            Files.deleteIfExists(new File(sqliteDbFileName).toPath());
        }
    }
}
