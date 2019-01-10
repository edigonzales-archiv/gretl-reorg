package ch.so.agi.gretl.util;

import java.sql.*;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestUtilSqlPostgres extends AbstractTestUtilSql {
    public static final String CON_DDLUSER = "ddluser";
    public static final String CON_DDLPASS = "ddluser";
    public static final String CON_DMLUSER = "dmluser";

    public static Connection connect(PostgreSQLContainer postgres) {
        try {
            String url = postgres.getJdbcUrl();
            String user = postgres.getUsername();
            String password = postgres.getPassword();
            Connection con = DriverManager.getConnection(url, user, password);
            con.setAutoCommit(false);
            return con;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createOrReplaceSchema(Connection con, String schemaName) {
        try {
            Statement s = con.createStatement();
            s.addBatch(String.format("DROP SCHEMA IF EXISTS %s CASCADE", schemaName));
            s.addBatch("CREATE SCHEMA " + schemaName);
            s.addBatch(String.format("GRANT USAGE ON SCHEMA %s TO dmluser", schemaName));
            s.addBatch(String.format("GRANT USAGE ON SCHEMA %s TO readeruser", schemaName));
            s.executeBatch();
            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Grant data modification rights to all tables in given schema. Data
     * modification includes select, insert, update, delete.
     * 
     * @param con        connection handle to database
     * @param schemaName name of schema in database
     * @param userName   user to give rights to
     */
    public static void grantDataModsInSchemaToUser(Connection con, String schemaName, String userName) {
        String sql = String.format("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA %s TO %s", schemaName,
                userName);
        Statement s = null;
        try {
            s = con.createStatement();
            s.execute(sql);
            s.close();
        } catch (SQLException se) {
            throw new RuntimeException(se);
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
