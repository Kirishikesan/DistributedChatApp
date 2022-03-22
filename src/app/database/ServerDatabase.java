package app.database;

import app.serversState.ServersState;

import java.sql.*;

import java.util.Set;

public class ServerDatabase {

    static Connection connection = DatabaseConnection.getInstance().getConnection();

    public static void saveView(Set<Integer> view){

        String query = "";
        PreparedStatement ps = null;
        String server_id = String.valueOf(ServersState.getInstance().getSelfServerId());
        String view_list = view.toString();

        try {

            ResultSet isViewExit = isViewExit();
            if(isViewExit.next()){
                System.out.println(isViewExit.getInt(1));
                if( isViewExit.getInt(1) == 1){
                    query = "update activeViews set view_list=? where server_id = ?";
                    ps = connection.prepareStatement(query);
                    ps.setString(1, view_list);
                    ps.setString(2, server_id);
                    ps.executeUpdate();
                }else{
                    query = "insert into activeViews(server_id,view_list) VALUES (?, ?)";
                    ps = connection.prepareStatement(query);
                    ps.setString(1, server_id);
                    ps.setString(2, view_list);
                    ps.executeUpdate();
                }
            }

        } catch (SQLException e) {
            System.out.println("WARN: SQL saveView Error - " + e.getMessage());
        }

    }

    public static ResultSet isViewExit(){
        ResultSet result = null;
        String server_id = String.valueOf(ServersState.getInstance().getSelfServerId());
        String query = "SELECT if (exists(SELECT server_id from activeViews where server_id = ?), 1, 0)";

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, server_id);
            result = ps.executeQuery();

        } catch (SQLException e) {
            System.out.println("WARN: SQL isViewExit Error - " + e.getMessage());
        }
        return result;

    }

}
