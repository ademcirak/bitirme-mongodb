package config;

/**
 * Created by Adem on 02/3/2015.
 */
public class LocalConfig extends EnvironmentConfig {



    public LocalConfig() {
        luceneIndexPath = "C:/bitirme/data/lucene/index";
        log4j2ConfPath = "file:///C:/bitirme/bitirme_v0.1/src/main/config/log4j2.xml";
        mysqlHost="localhost";
        mysqlPort="3307";
        mysqlUser="root";
        mysqlPass="usbw";

        sampleNewsPath="C:/bitirme/data/news/";
    }
}
