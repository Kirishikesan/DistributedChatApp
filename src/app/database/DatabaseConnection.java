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
            String url = "jdbc:mysql://database-1.cekawtkg31nl.us-east-1.rds.amazonaws.com:3306/chatApp";
            String user = "admin";
            String password = "DistributedChatApp2022";

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Info: DB Connect ");

        } catch (Exception e) {
            System.out.println("An error occurred - " + e.toString());
            e.printStackTrace();
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
