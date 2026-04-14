package org.example.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    String url="jdbc:mysql://localhost:3306/ecotrack";
    String username="root";
    String password="";
    Connection connection;
    public static MyConnection instance;

    public static MyConnection getInstance() {
        if(instance==null){
            instance=new MyConnection();
        }
        return instance;
    }

    private MyConnection() {
        try {
            System.out.println("Attempting connection to: " + url);
            connection= DriverManager.getConnection(url,username,password);
            System.out.println("Connected to database successfully");
        }catch(SQLException e){
            System.out.println("Connection Failed!");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public Connection getConnection() {
        return connection;
    }
}
