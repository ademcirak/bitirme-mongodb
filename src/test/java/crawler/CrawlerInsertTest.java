package crawler;


import crawler.sites.HurriyetCrawler;
import data.CrawlerObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 * Created by Adem on 10/4/2015.
 */
public class CrawlerInsertTest {



    @Test public void insert() {

        CrawlerObject obj = getObject();
        DummyListener listener = new DummyListener();
        CrawlerBase c = new HurriyetCrawler(1);

        try {

            c.addListener(listener);
            c.insert(obj);

            assertEquals(obj.title, listener.get_title);
        }
        catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    @After public void delete() {
        CrawlerBase c = new HurriyetCrawler(1);
    }




    protected CrawlerObject getObject() {
        CrawlerObject obj = new CrawlerObject();

        obj.title = "test";
        obj.text = "test";
        obj.text_clean = "test";
        obj.image = "test";
        obj.desc = "test";
        obj.link = "test";

        return obj;
    }

    class DummyListener implements INewListener {
        public String get_title;
        @Override
        public void onNew(CrawlerObject obj) {
            get_title = obj.title;
        }
    }

}
