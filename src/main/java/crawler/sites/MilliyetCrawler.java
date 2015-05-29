/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crawler.sites;

import crawler.CrawlerBase;
import data.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Adem
 */
public class MilliyetCrawler extends CrawlerBase {
    
    public MilliyetCrawler(int id) {
        super(id);
        this.rssurl="http://www.milliyet.com.tr/D/rss/rss/RssSD.xml";
    }

    @Override
    protected void getLinks() {
        Document rssdoc = this.fetchLink(this.rssurl);
        
        Elements items = rssdoc.select("item");
        
        for(Element item:items)
        {
            RssObject obj = new RssObject();
            obj.link = item.select("guid").text();
            this.links.add(obj);

        }
    }

    @Override
    protected void getTitle(Document o, CrawlerObject data) {
        data.title=o.select("h1[itemprop=name]").text();
    }

    @Override
    protected void getDesc(Document o, CrawlerObject data) {
        data.desc=o.select("meta[name=description]").attr("content");
    }

    @Override
    protected void getImage(Document o, CrawlerObject data) {
        data.image=o.select("meta[property=og:image]").attr("content");
    }

    @Override
    protected void getContent(Document o, CrawlerObject data) {
        Elements doc=o.select("div[itemprop=articleBody]");
        for (Element element : doc.select("*")) {
            if (!element.hasText() || element.text().trim().equals(" ")) {
                element.remove();
            }
        }
        data.text=doc.html();
        data.text_clean=doc.text();
    }

    @Override
    protected void getTags(Document o, CrawlerObject data) {
        data.keywords=o.select("meta[name=keywords]").attr("content").split(",");
    }

    @Override
    protected void getCategory(Document o, CrawlerObject data) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
