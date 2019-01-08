package ch.so.agi.gretl.jobs;

import org.junit.Test;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.util.TestUtilSqlPg;

public class TestContainersTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    
    Connection con = null;
    
    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    @BeforeClass
    public static void prepareDatabase() throws Exception {
//        Connection con = createConnection();
//        Statement s = con.createStatement();
//        s.execute("CREATE EXTENSION \"uuid-ossp\";");
//        s.close();
//        con.close();
    }
    
    
//    @AfterClass
//    public static void closeConnection() throws SQLException {
//        con.close();
//    }
    
    @Test
    public void fubar() {
        System.out.println("fubar");
        System.out.println(postgres.getJdbcUrl());
    }
    
    private Connection createConnection() throws Exception {
        String url = postgres.getJdbcUrl();
        String user = postgres.getUsername();
        String password = postgres.getPassword();
        return DriverManager.getConnection(url, user, password);
    }

    @Test
    public void foobar() throws Exception {
        con = createConnection();
        System.out.println(con.isClosed());
        con.close();
    }
    
    @Test
    public void importOk() throws Exception {
        String schemaName = "shpimport".toLowerCase();
        try {
            // prepare postgres
            con = createConnection();
            Statement s = con.createStatement();
            s.execute("CREATE EXTENSION \"uuid-ossp\";");
            s.close();
            
            con.close();
//            TestUtilSqlPg.createOrReplaceSchema(con, schemaName);
//            Statement s1 = con.createStatement();
//            s1.execute("CREATE TABLE "+schemaName+".importdata(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, geometrie geometry(POINT,2056), aextra varchar(40))");
//            s1.close();
//            TestUtilSqlPg.grantDataModsInSchemaToUser(con, schemaName, TestUtilSqlPg.CON_DMLUSER);
//
//            con.commit();
//            TestUtilSqlPg.closeCon(con);
//
//            // run job
//            BuildResult result = GradleRunner.create()
//                    .withProjectDir(new File("src/functionalTest/jobs/ShpImport/"))
//                    .withArguments("-i")
//                    .withPluginClasspath()
//                    .build();
//
//            // check results
//            assertEquals(SUCCESS, result.task(":shpimport").getOutcome());
//
//            con = TestUtilSqlPg.connect();
//
//            Statement s2 = con.createStatement();
//            ResultSet rs=s2.executeQuery("SELECT \"Aint\" , adec, atext, aenum,adate, ST_X(geometrie), ST_Y(geometrie), aextra FROM "+schemaName+".importdata WHERE t_id=1"); 
//            if (!rs.next()) {
//                fail();
//            }
//            assertEquals(2,rs.getInt(1));
//            assertEquals(new BigDecimal("3.4"),rs.getBigDecimal(2));
//            assertEquals("abc",rs.getString(3));
//            assertEquals("rot",rs.getString(4));
//            assertEquals(new java.sql.Date(2013-1900,10-1,21),rs.getDate(5));
//            assertEquals(2638000.0,rs.getFloat(6),0.000001);
//            assertEquals(1175250.0,rs.getFloat(7),0.000001);
//            if (rs.next()) {
//                fail();
//            }
//            rs.close();
//            s1.close();
        } finally {
            TestUtilSqlPg.closeCon(con);
        }
    }


}
