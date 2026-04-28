package org.example.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    String url="jdbc:mysql://localhost:3306/ecotrack";
    String username="root";
    String password="";
    Connection connection;
    private String lastError;
    public static MyConnection instance;

    public static MyConnection getInstance() {
        if(instance==null){
            instance=new MyConnection();
        }
        return instance;
    }

    private MyConnection() {
        connect();
    }

    private synchronized void connect() {
        try {
            connection= DriverManager.getConnection(url,username,password);
            lastError = null;
            System.out.println("Connected to database successfully");
        }catch(SQLException e){
            connection = null;
            lastError = e.getMessage();
            System.out.println("Connection Failed! " + e.getMessage());
        }
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connection = null;
            lastError = e.getMessage();
        }
        return connection;
    }

    public String getLastError() {
        return lastError;
    }
}
