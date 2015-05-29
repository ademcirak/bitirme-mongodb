/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crawler.sites;

import crawler.CrawlerBase;
import data.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

/**
 *
 * @author Adem
 */
public class HurriyetCrawler extends CrawlerBase {

    public HurriyetCrawler(int id) {
        super(id);
        this.rssurl="http://rss.hurriyet.com.tr/rss.aspx?sectionId=2";
    }

    @Override
    protected void getLinks() {
        Document rssdoc = this.fetchLink(this.rssurl);
        
        
        Elements items = rssdoc.select("item");
        
        for(Element item:items)
        {
            RssObject obj = new RssObject();
            obj.link = item.select("guid").text();
            obj.title = item.select("title").text();
            this.links.add(obj);

        }
    }

    @Override
    protected void getTitle(Document o, CrawlerObject data) {
        data.title = o.select("meta[property=og:title]").attr("content");
                
    }

    @Override
    protected void getDesc(Document o, CrawlerObject data) {
        data.desc = o.select(".detailSpot").text();
    }

    @Override
    protected void getImage(Document o, CrawlerObject data) {
        data.image=o.select("meta[property=og:image]").attr("content");
    }

    @Override
    protected void getContent(Document o, CrawlerObject data) {
        data.text = o.select(".ctx_content").html().replaceAll("(?s)<!--.*?-->", "");
        data.text_clean = Jsoup.clean(data.text, new Whitelist());
    }

    @Override
    protected void getTags(Document o, CrawlerObject data) {
        
    }

    @Override
    protected void getCategory(Document o, CrawlerObject data) {
        
    }
    
}
