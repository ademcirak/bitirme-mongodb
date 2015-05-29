package crawler;


import data.CrawlerObject;
import data.RssObject;
import main.Helper;
import mongo.MongoHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.horrabin.horrorss.RssFeed;
import org.horrabin.horrorss.RssParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Adem
 */
public abstract class CrawlerBase {

    protected Logger logger;

    protected static LinkedList<INewListener> listeners = new LinkedList<INewListener>();
    protected MongoHelper mongoHelper = MongoHelper.getInstance();

    private static final String userAgent="Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36";
    private static final String referrer="https://www.google.com.tr";
    private static final int timeout=10000;


    protected int id;
    protected String rssurl;
    protected LinkedList<RssObject> links=new LinkedList<RssObject>();


    /**
     *
     * @param id
     */
    public CrawlerBase(int id) {

        logger = LogManager.getLogger(this.getClass().getName());
        this.id=id;
    }

    protected abstract void getLinks();
    protected abstract void getTitle(Document o, CrawlerObject data);
    protected abstract void getDesc(Document o, CrawlerObject data);
    protected abstract void getImage(Document o, CrawlerObject data);
    protected abstract void getContent(Document o, CrawlerObject data);
    protected abstract void getTags(Document o, CrawlerObject data);
    protected abstract void getCategory(Document o, CrawlerObject data);


    static void addListener(INewListener listener) {
        listeners.add(listener);
    }


    private CrawlerObject extractData(RssObject obj) {
        CrawlerObject data= new CrawlerObject();
        data.source = this.id;

        Document doc = this.fetchLink(obj.link);
        doc.outputSettings().escapeMode(EscapeMode.xhtml);
        data.link=obj.link;


        if(obj.title==null)
            this.getTitle(doc,data);
        else
            data.title=obj.title;

        if(obj.desc==null)
            this.getDesc(doc,data);
        else
            data.desc=obj.desc;

        if(obj.image==null)
            this.getImage(doc,data);
        else
            data.image=obj.image;

        this.getContent(doc,data);
        this.getTags(doc,data);
        this.getCategory(doc,data);

        return data;
    }

    public void execute() {
        this.getLinks();
        for(RssObject obj: this.links) {
            try {
                CrawlerObject o = this.extractData(obj);
                this.insert(o);
            } catch (Exception e) {
                logger.debug(e);
            }

        }
    }


    public void insert(CrawlerObject obj) {


        if(mongoHelper.checkDocumentExist(obj.link)) {
            logger.trace("Document already exist " + obj.title);
        } else {

            mongoHelper.insertDocument(obj);

            for (INewListener listener : listeners)
                listener.onNew(obj);

        }
    }


    private void saveImage(String url,int id) {

    }

    protected Document fetchLink(String link) {
        try {
            return Jsoup.connect(link).followRedirects(true).referrer(CrawlerBase.referrer).userAgent(CrawlerBase.userAgent).timeout(CrawlerBase.timeout).get();
        } catch (IOException ex) {
            logger.error(ex.getMessage(),ex);
            return null;
        }
    }

    protected RssFeed fetchRss(String link) {
        RssParser rss = new RssParser();
        rss.setDateParser(new nowDateParser());
        try {
            return rss.load(link);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     *avoid console error in date parse horrorss just returns new date for each 
     */
    public class nowDateParser implements org.horrabin.horrorss.util.DateParser {

        @Override
        public Date getDate(String string, int i) throws Exception {
            return new Date();
        }
    }

    public String findImage(String content) {
        Pattern p = Pattern.compile(".*<img[^>]*src=\"([^\"]*)");
        Matcher m = p.matcher(content);

        while (m.find()) {
            return m.group(1);
        }
        return null;
    }

}