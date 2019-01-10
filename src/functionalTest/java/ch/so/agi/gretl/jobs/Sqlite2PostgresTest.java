package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.TestUtilSqlPostgres;
import ch.so.agi.gretl.util.TestUtilSqlSqlite;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Random;
import java.util.UUID;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

public class Sqlite2PostgresTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";

    @ClassRule
    public static PostgreSQLContainer postgres = (PostgreSQLContainer) new PostgisContainerProvider().newInstance()
            .withDatabaseName("gretl").withUsername("ddluser").withInitScript("init_postgresql.sql")
            .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Tests loading several hundred thousand rows from sqlite to postgres. Loading
     * 300'000 rows should take about 15 seconds
     */
    @Test
    public void positiveBulkLoadPostgisTest() throws Exception {
        int numRows = 300000;
        String schemaName = "BULKLOAD2POSTGIS".toLowerCase();
        String geomWkt = "LINESTRING(2600000 1200000,2600001 1200001)";
        String sqliteDbFileName = "src/functionalTest/jobs/sqlite2postgresBulk/" + schemaName + ".sqlite";

        Connection srcCon = null;
        Connection targetCon = null;

        try {
            // prepare sqlite source database
            File sqliteDb = new File(sqliteDbFileName);
            Files.deleteIfExists(sqliteDb.toPath());
            sqliteDb = new File(sqliteDbFileName);

            srcCon = TestUtilSqlSqlite.connect(sqliteDb);

            String sqlSqlite = "CREATE TABLE source_data(MYINT INTEGER, MYFLOAT REAL, MYTEXT TEXT, MYWKT TEXT)";
            Statement stmtSqlite = srcCon.createStatement();
            stmtSqlite.execute(sqlSqlite);

            Random random = new Random();

            PreparedStatement ps = srcCon.prepareStatement("INSERT INTO source_data VALUES(?, ?, ?, ?)");
            for (int i = 0; i < numRows; i++) {
                ps.setInt(1, random.nextInt());
                ps.setDouble(2, random.nextDouble());
                ps.setString(3, UUID.randomUUID().toString());
                ps.setString(4, geomWkt);
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();

            srcCon.commit();
            srcCon.close();

            // prepare postgres target database
            targetCon = TestUtilSqlPostgres.connect(postgres);
            TestUtilSqlPostgres.createOrReplaceSchema(targetCon, schemaName);

            String sqlPg = "CREATE TABLE " + schemaName
                    + ".target_data(MYINT INTEGER, MYFLOAT REAL, MYTEXT VARCHAR(50), MYGEOM GEOMETRY(LINESTRING,2056))";

            Statement stmtPg = targetCon.createStatement();
            stmtPg.execute(sqlPg);

            TestUtilSqlPostgres.grantDataModsInSchemaToUser(targetCon, schemaName, TestUtilSqlPostgres.CON_DMLUSER);

            targetCon.commit();
            targetCon.close();

            // run gradle job
            BuildResult result = GradleRunner.create()
                    .withProjectDir(new File("src/functionalTest/jobs/Sqlite2PostgresBulk/"))
                    .withArguments("-i")
                    .withArguments("-Pdb_uri=" + postgres.getJdbcUrl())
                    .withPluginClasspath().build();

            // check results
            assertEquals(SUCCESS, result.task(":readFromSqlite").getOutcome());

            targetCon = TestUtilSqlPostgres.connect(postgres);
            int countDest = TestUtilSqlPostgres.execCountQuery(targetCon,
                    "SELECT count(*) FROM " + schemaName + ".target_data;");

            Assert.assertEquals("Check Statement must return exactly " + numRows, numRows, countDest);
        } finally {
            srcCon.close();
            ;
            targetCon.close();
            Files.deleteIfExists(new File(sqliteDbFileName).toPath());
        }
    }

    /**
     * Tests if the sqlite datatypes and geometry as wkt are transferred faultfree
     * from sqlite to postgis
     */
    @Test
    public void sqlite2postgresDatatypes() throws Exception {
        int numRows = 1;
        String schemaName = "DATATYPES2POSTGIS".toLowerCase();
        String geomWkt = "LINESTRING(2600000 1200000,2600001 1200001)";
        String sqliteDbFileName = "src/functionalTest/jobs/sqlite2postgresDatatypes/" + schemaName + ".sqlite";

        Connection srcCon = null;
        Connection targetCon = null;

        try {
            // prepare sqlite source database
            File sqliteDb = new File(sqliteDbFileName);
            Files.deleteIfExists(sqliteDb.toPath());
            sqliteDb = new File(sqliteDbFileName);

            srcCon = TestUtilSqlSqlite.connect(sqliteDb);

            String sqlSqlite = "CREATE TABLE source_data(MYINT INTEGER, MYFLOAT REAL, MYTEXT TEXT, MYWKT TEXT)";
            Statement stmtSqlite = srcCon.createStatement();
            stmtSqlite.execute(sqlSqlite);

            Random random = new Random();

            PreparedStatement ps = srcCon.prepareStatement("INSERT INTO source_data VALUES(?, ?, ?, ?)");
            for (int i = 0; i < numRows; i++) {
                ps.setInt(1, random.nextInt());
                ps.setDouble(2, random.nextDouble());
                ps.setString(3, UUID.randomUUID().toString());
                ps.setString(4, geomWkt);
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();

            srcCon.commit();
            srcCon.close();

            // prepare postgis target database
            targetCon = TestUtilSqlPostgres.connect(postgres);
            TestUtilSqlPostgres.createOrReplaceSchema(targetCon, schemaName);

            String sqlPg = "CREATE TABLE " + schemaName
                    + ".target_data(MYINT INTEGER, MYFLOAT REAL, MYTEXT VARCHAR(50), MYGEOM GEOMETRY(LINESTRING,2056))";

            Statement stmtPg = targetCon.createStatement();
            stmtPg.execute(sqlPg);

            TestUtilSqlPostgres.grantDataModsInSchemaToUser(targetCon, schemaName, TestUtilSqlPostgres.CON_DMLUSER);

            targetCon.commit();
            targetCon.close();

            // run gradle job
            BuildResult result = GradleRunner.create()
                    .withProjectDir(new File("src/functionalTest/jobs/Sqlite2PostgresDatatypes/"))
                    .withArguments("-i")
                    .withArguments("-Pdb_uri=" + postgres.getJdbcUrl())
                    .withPluginClasspath().build();

            // check results
            assertEquals(SUCCESS, result.task(":readFromSqlite").getOutcome());

            String checkSql = "SELECT COUNT(*) FROM " + schemaName + ".target_data "
                    + "WHERE MYINT IS NOT NULL AND MYFLOAT IS NOT NULL AND MYTEXT IS NOT NULL AND "
                    + "ST_Equals(MYGEOM, ST_GeomFromText('" + geomWkt + "', 2056)) = True;";

            targetCon = TestUtilSqlPostgres.connect(postgres);
            int countDest = TestUtilSqlPostgres.execCountQuery(targetCon, checkSql);

            Assert.assertEquals("Check Statement must return exactly " + numRows, numRows, countDest);
        } finally {
            srcCon.close();
            targetCon.close();
            Files.deleteIfExists(new File(sqliteDbFileName).toPath());
        }
    }
}
