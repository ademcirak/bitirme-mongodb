/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crawler.sites;

import crawler.CrawlerBase;
import data.*;
import org.horrabin.horrorss.RssFeed;
import org.horrabin.horrorss.RssItemBean;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

/**
 *
 * @author Adem
 * http://www.aa.com.tr/rss/ajansguncel.xml
 */
public class AaCrawler extends CrawlerBase {

    public AaCrawler(int id) {
        super(id);
        this.rssurl="http://www.aa.com.tr/rss/ajansguncel.xml";
    }

    @Override
    protected void getLinks() {
        RssFeed  feed = this.fetchRss(this.rssurl);

        if(feed==null) {
            logger.error("feed is null");
            return;
        }

        
        List<RssItemBean> items = feed.getItems();
        for (int i=0; i<items.size(); i++){
             RssItemBean item = items.get(i);
             
              RssObject obj = new RssObject();
              obj.title = item.getTitle();
              obj.link = item.getLink();
              obj.image = this.findImage(item.getDescription());
              obj.desc = item.getDescription().split("<br/>")[1];
             
             // System.out.println(obj);
             this.links.add(obj);
        }
    }

    @Override
    protected void getTitle(Document o, CrawlerObject data) {
        data.title = o.select(".news-kunye h3").text();
    }

    @Override
    protected void getDesc(Document o, CrawlerObject data) {
        data.desc = o.select(".news-content .news-spot").text();
    }

    @Override
    protected void getImage(Document o, CrawlerObject data) {
        // <meta property="og:image" content="" 
        data.image=o.select("meta[property=og:image]").attr("content");
    }

    @Override
    protected void getContent(Document o, CrawlerObject data) {
        Elements doc=o.select("#news-maincontent");
        for (Element element : doc.select("*")) {
            if (!element.hasText() || element.text().trim().equals(" ")) {
                element.remove();
            }
        }
        data.text=doc.html();
        data.text_clean = doc.text();
    }

    @Override
    protected void getTags(Document o, CrawlerObject data) {

    }

    @Override
    protected void getCategory(Document o, CrawlerObject data) {

    }
    
}
