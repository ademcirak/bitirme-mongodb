package crawler;

import data.CrawlerObject;
import data.RssObject;
import main.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.horrabin.horrorss.RssFeed;
import org.horrabin.horrorss.RssParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

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
 * Created by Adem on 22/5/2015.
 */
public abstract class CrawlerBaseMySQL {

/*
    protected static LinkedList<INewListener> listeners = new LinkedList<INewListener>();

    private final Connection _connection;
    protected int id;
    protected String rssurl;

    private static final String userAgent="Mozilla/5.0 (Windows NT 6.3; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0";
    private static final String referrer="https://www.google.com";
    private static final int timeout=10000;
    protected Logger logger;

    protected LinkedList<RssObject> links=new LinkedList<RssObject>();
    protected LinkedList<CrawlerObject> data = new LinkedList<CrawlerObject>();


    public CrawlerBaseMySQL(int id) {

        logger = LogManager.getLogger(this.getClass().getName());
        this._connection= Helper.getInstance().getConnection();
        try {
            this._connection.setAutoCommit((false));
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
        }
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

        Document doc = this.fetchLink(obj.link);
        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
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
            CrawlerObject o = this.extractData(obj);
            this.insert(o);
        }
    }


    public void insert(CrawlerObject obj) {

        if(this.check(obj)) {
            logger.trace("Document already exist " + obj.title);
        } else {
            PreparedStatement state;
            Integer id=null;
            try {

                state = this._connection.prepareStatement("INSERT INTO data(`link`, `source`, `title`, `desc`, `text`, `text_clean`, `image`) VALUES(?,?,?,?,?,?,?);", PreparedStatement.RETURN_GENERATED_KEYS);
                state.setString(1, obj.link);
                state.setInt(2, this.id);
                state.setString(3, obj.title);
                state.setString(4, obj.desc);
                state.setString(5, obj.text);
                state.setString(6, obj.text_clean);
                state.setString(7, obj.image);



                state.executeUpdate();

                ResultSet rs = state.getGeneratedKeys();
                if (rs.next())
                    id=rs.getInt(1);


                if(id==null) {
                    logger.warn("getGeneratedKeys get id failed");
                    this._connection.rollback();
                    return;
                }

                obj.id=id;

                this.insertCategories(id, obj);
                this.insertKeywords(id, obj);

                this._connection.commit();


                for (INewListener listener : listeners)
                    listener.onNew(obj);

                this.data.add(obj);


            } catch (SQLException ex) {
                logger.error(ex.getMessage());
                try {
                    this._connection.rollback();
                } catch (SQLException ex1) {
                    logger.error(ex1.getMessage());
                }
            }
        }
    }


    private void insertKeywords(Integer id, CrawlerObject obj) throws SQLException {
        PreparedStatement state;
        PreparedStatement relation_statement;
        Integer keyword_id=null;

        if(obj.keywords==null)
            return;

        for(String keyword: obj.keywords) {

            state = this._connection.prepareStatement("INSERT IGNORE INTO data_keywords(keyword) VALUES(?) ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id);", PreparedStatement.RETURN_GENERATED_KEYS);
            state.setString(1, keyword);

            state.executeUpdate();
            ResultSet  rs = state.getGeneratedKeys();
            if (rs.next()) {
                keyword_id=rs.getInt(1);
                relation_statement=this._connection.prepareStatement("INSERT IGNORE INTO data_keyword_rel VALUES(?,?)");
                relation_statement.setInt(1, id);
                relation_statement.setInt(2, keyword_id);
                relation_statement.executeUpdate();
                this._connection.commit();
            }

        }



    }

    private void insertCategories(Integer id, CrawlerObject obj) {

        if(obj.categories==null)
            return;
    }


    private boolean check(CrawlerObject obj) {

        PreparedStatement state;
        try {
            state = this._connection.prepareStatement("SELECT count(1) FROM data WHERE link=?");
            state.setString(1,obj.link);

            ResultSet rs = state.executeQuery();
            rs.next();
            return (rs.getInt(1) > 0);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
        }

        return false;
    }

    public void delete(String title) {

        PreparedStatement state;
        try {
            state = this._connection.prepareStatement("DELETE FROM data WHERE title=?");
            state.setString(1,title);
            state.execute();
            _connection.commit();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            logger.error(ex.getMessage(), ex);
        }
    }

    private void saveImage(String url,int id) {

    }

    protected Document fetchLink(String link) {
        try {
            return Jsoup.connect(link).followRedirects(true).referrer(CrawlerBaseMySQL.referrer).userAgent(CrawlerBaseMySQL.userAgent).timeout(CrawlerBaseMySQL.timeout).get();
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
*/
}