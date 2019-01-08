package ch.so.agi.gretl.util;

import java.sql.*;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestUtilSqlPostgres {

    public static Connection connect(PostgreSQLContainer postgres) throws SQLException {
        String url = postgres.getJdbcUrl();
        String user = postgres.getUsername();
        String password = postgres.getPassword();
        return DriverManager.getConnection(url, user, password);
    }

}
