package clustering;

import data.CrawlerObject;
import lucene.IIndexListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Adem on 10/4/2015.
 */
public class IndexListener implements IIndexListener {

    private static final Logger logger = LogManager.getLogger(IndexListener.class);
    Clustering clustering = Clustering.getInstance();

    public IndexListener() {
    }

    @Override
    public void onIndexed(CrawlerObject data) {
        logger.debug("Emitting: " + data.title);
        clustering.Cluster(data);
    }
}
