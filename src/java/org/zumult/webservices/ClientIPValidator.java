/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author Frick
 */
public class ClientIPValidator {    

    private static final Logger log = Logger.getLogger(ClientIPValidator.class.getName());
    static Connection connection;
    static Statement statement;
    public static final String TABLE_ZUMULT_CLIENTS = "TOMCAT_USER.ZUMULT_REST_API_CLIENTS";
    public static final String COLUMN_CLIENT_INFO = "CO_USER";
    public static final String COLUMN_CLIENT_IP = "CO_IP";
    
    static {
        try {
            Context initContext = new InitialContext();
            Context envContext  = (Context)initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource)envContext.lookup("jdbc/TestDB");
            connection = ds.getConnection();
            statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        } catch (SQLException | NamingException ex) {
            Logger.getLogger(ClientIPValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static boolean validateClientIP(String client_ip) {
        if (client_ip==null || client_ip.isEmpty()){
            return false;
        }
        
        log.log(Level.INFO, ": {0} is trying to connect the ZUMULT REST-API... ", client_ip);
        try {
            String query = "SELECT "+ COLUMN_CLIENT_INFO +" FROM "+TABLE_ZUMULT_CLIENTS+" WHERE "+ COLUMN_CLIENT_IP +"='" + client_ip + "'";
            System.out.println(query);
            ResultSet srs = statement.executeQuery(query);
            
            if (srs.next()){
                String info =": " + srs.getString(1) + " (IP address: " + client_ip + ") is getting access...";
                log.info(info);
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClientIPValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    
}
