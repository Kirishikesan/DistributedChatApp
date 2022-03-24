package app.database;

import app.serversState.ServersState;

import java.sql.Connection;
import java.sql.DriverManager;

import java.io.File;
import java.util.Scanner;

public class DatabaseConnection {

    private static DatabaseConnection dbConnectionInstance;
    private static Connection connection = null;
    
    private DatabaseConnection() {
    }

    public void initializeDatabaseConnection() {

        try {
            String url = "jdbc:mysql://localhost:3306/chatApp";
            String user = "root";
            String password = "";

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Info: DB Connect ");

        } catch (Exception e) {
            System.out.println("An error occurred - " + e.getMessage());
        }
    }

    public static DatabaseConnection getInstance() {
        if (dbConnectionInstance == null) {
            synchronized (DatabaseConnection.class) {
                if (dbConnectionInstance == null) {
                    dbConnectionInstance = new DatabaseConnection();
                }
            }
        }
        return dbConnectionInstance;
    }

    public Connection getConnection() {
        return connection;
    }

    
}
