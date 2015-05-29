package main;

import config.EnvironmentConfig;
import config.LocalConfig;
import config.ServerConfig;

import java.io.IOException;

/**
 * Created by Adem on 02/3/2015.
 */
public class Run {

    /**
     * Application runner static class for starting application.
     * @param args
     */
    public static void main(String[] args) {

        String env ="";
        try {
            env = System.getenv("ENV").toString();
        }
        catch (Exception e) {
            System.err.println("Please set ENV value!");
            return;
        }


        EnvironmentConfig conf=null;

        if("development".equals(env))
            conf= new LocalConfig();
        else if("test-server".equals(env))
            conf= new ServerConfig();
        else {
            System.err.println("Please set an valid ENV value!");
            return;
        }

        System.setProperty("log4j.configurationFile", conf.getLog4j2ConfPath());
        System.setProperty("DEBUG.MONGO", "true");
        System.setProperty("DB.TRACE", "true");

        final Application app;

        try {
            app = new Application(conf);

            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                @Override
                public void run()
                {
                System.out.println("Shutting down...");
                    try {
                        app.terminate();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true)
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

    }
}
