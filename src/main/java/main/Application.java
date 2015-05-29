package main;

import clustering.Clustering;
import clustering.IndexListener;
import config.EnvironmentConfig;
import crawler.CrawlerManager;
import crawler.sites.AaCrawler;
import crawler.sites.MilliyetCrawler;
import loader.MysqlNewsLoader;
import loader.SampleNewsLoader;
import lucene.CrawlerListener;
import lucene.Lucene;
import nanohttp.RestServer;
import nanohttp.ServerRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.FileUtils;
import socket.Job;
import socket.SocketHelper;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Adem on 09/4/2015.
 */
public class Application {

    private static final Logger logger = LogManager.getLogger(Application.class);
    public static EnvironmentConfig config;

    Lucene lucene;
    CrawlerManager crawlerManager;
    Clustering clustering;


    public Application(EnvironmentConfig config) {

        // load config
        Application.config = config;

        /*
        * connection test
        */
        /*
        try {
            Connection con = Helper.getInstance().getConnection();
            // this.clear(con);

            // (new SampleNewsLoader()).load();
            // (new MysqlNewsLoader()).load();
            con.close();

        } catch (Exception e ) {
            e.printStackTrace();
            System.err.println("Connection error: " + e.getMessage());
            return;
        }*/

        // get lucene instance
        lucene = Lucene.getInstance();

        // get clustering instance
        clustering = Clustering.getInstance();

        // starts and manages crawlers
        crawlerManager = new CrawlerManager();

        // listens new data from crawlers
        CrawlerListener crawlerListener = new CrawlerListener();

        // listens new indexed data from lucene
        IndexListener indexListener = new IndexListener();


        lucene.addListener(indexListener);
        crawlerManager.addListener(crawlerListener);


        try {
            crawlerManager.startSync();
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
        }


        /*

        clustering = Clustering.getInstance();
        SocketHelper socketHelper = SocketHelper.getInstance();





        logger.debug("starting");


        ServerRunner.run(RestServer.class);

/*
        Map<String,Integer> map = lucene.getTerms(10);
        if(map!=null) {
            for (Map.Entry<String, Integer> entry : map.entrySet())
            {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }*/



/*
        crawlerManager.addListener(crawlerListener);
        lucene.addListener(indexListener);

        crawlerManager.start();
        */

    }




    public void terminate() throws IOException {

        crawlerManager.shutdown();
        lucene.close();


    }


    public void clear(Connection con) {

        File file = new File(config.getLuceneIndexPath());
        File db_file = new File("objects.db");
        db_file.delete();
        db_file = null;

        this.delete(file);
        file= null;
        try {
            Statement st = con.createStatement();
            st.executeUpdate("TRUNCATE TABLE  data");
            st.close();
        } catch (Exception e) {
            System.err.println("Db truncate failed! " + e.getMessage());
        }

    }


    private boolean delete(File pFile) {
        boolean bResult = false;

        if(pFile.exists()) {
            if(pFile.isDirectory()) {
                if(pFile.list().length == 0) {
                    pFile.delete();
                } else {
                    String[] strFiles = pFile.list();

                    for(String strFilename: strFiles) {
                        File fileToDelete = new File(pFile, strFilename);

                        delete(fileToDelete);
                    }
                }
            } else {
                pFile.delete();
            }
        }

        return bResult;
    }
}
