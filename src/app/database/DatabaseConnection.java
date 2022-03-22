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

    public void initializeDatabaseConnection(String serverId, String server_config_path) {
        String db_config_path = server_config_path.replace("server_config", "database_config");

        try {
            File file = new File(db_config_path);
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] db_config = data.split(" ");
                
                if (db_config[0].equals(serverId)) {
                    String url = db_config[1];
                    String user = db_config[2];
                    String password = "";

                    Class.forName("com.mysql.cj.jdbc.Driver");
                    connection = DriverManager.getConnection(url, user, password);
                    System.out.println("Info: DB Connect ");
                }

            }

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
