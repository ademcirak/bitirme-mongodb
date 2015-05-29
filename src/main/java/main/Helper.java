package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Adem on 09/4/2015.
 */
public class Helper {

    private static Helper ourInstance = new Helper();

    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

    public static Helper getInstance() {
        return ourInstance;
    }

    public Helper() {

    }


    public Connection getConnection()
    {
        String host=Application.config.getMysqlHost();
        String port=Application.config.getMysqlPort();
        String user=Application.config.getMysqlUser();
        String pass=Application.config.getMysqlPass();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            return (Connection) DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/project?useUnicode=true&characterEncoding=UTF-8", user, pass);
        } catch (ClassNotFoundException ex) {
            System.out.println("Driver not found: "+ ex.getMessage());
        } catch (SQLException ex) {
            System.out.println("Connection error:  "+ user+"@" + pass + " "+ ex.getMessage() + ex.toString());
        }
        return null;


    }

}
