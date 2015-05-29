package lucene;

import data.CrawlerObject;


/**
 * Created by Adem on 10/4/2015.
 */
public interface IIndexListener {

    public void onIndexed(CrawlerObject data);
}
