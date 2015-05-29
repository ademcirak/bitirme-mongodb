package lucene;

import crawler.INewListener;
import data.CrawlerObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Adem on 10/4/2015.
 */

public class CrawlerListener implements INewListener {
    private static final Logger logger = LogManager.getLogger(CrawlerListener.class);
    Lucene lucene = Lucene.getInstance();
    @Override
    public void onNew(CrawlerObject obj) {
        try {
            logger.trace("CrawlerListener received onNew event: " + obj.title);
            lucene.indexCrawlerObject(obj);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }


    }
}
