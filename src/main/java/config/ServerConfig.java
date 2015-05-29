package config;

/**
 * Created by Adem on 14/4/2015.
 */
public class ServerConfig extends EnvironmentConfig {



    public ServerConfig() {
        this.luceneIndexPath = "/home/data/lucene/index";
        this.log4j2ConfPath = "/home/bitirme/src/main/config/log4j2.xml";
        mysqlHost="localhost";
        mysqlPort="3306";
        mysqlUser="root";
        mysqlPass="815a.d.14733";


        sampleNewsPath="/home/adem/bitirme/data/news/";
    }

}
