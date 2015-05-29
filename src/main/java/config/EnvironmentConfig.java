package config;

/**
 * Created by Adem on 14/4/2015.
 */
public abstract class EnvironmentConfig {

    private final float similarityThreshold  = 0.45f;
    private final float clusterThreshold = 0.6f;
    String luceneIndexPath;
    String log4j2ConfPath;

    String mysqlHost;
    String mysqlPort;
    String mysqlUser;
    String mysqlPass;

    String sampleNewsPath;

    public EnvironmentConfig() {

    }

    public float getClusterThreshold() { return clusterThreshold; }

    public float getSimilarityThreshold() {
        return similarityThreshold;
    }

    public String getLuceneIndexPath() {
        return luceneIndexPath;
    }

    public String getLog4j2ConfPath() {
        return log4j2ConfPath;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }
    public String getMysqlPort() {
        return mysqlPort;
    }
    public String getMysqlUser() {
        return mysqlUser;
    }
    public String getMysqlPass() {
        return mysqlPass;
    }

    public String getSampleNewsPath() {
        return sampleNewsPath;
    }
}
