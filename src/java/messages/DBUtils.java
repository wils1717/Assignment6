/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

/**
 *
 * @author c0533886
 */

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class DBUtils {

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        String jdbc = "jdbc:mysql://ipro:3306/messagedb";
        String user = "messagedb";
        String pass = "password";
        return DriverManager.getConnection(jdbc, user, pass);
    }

}
