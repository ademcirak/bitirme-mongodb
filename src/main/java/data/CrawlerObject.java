package data;

import org.bson.types.ObjectId;

/**
 *
 * @author Adem
 */
public class CrawlerObject {

    public ObjectId _id;

    public String title;

    public String text;

    public String text_clean;

    public String image;

    public String desc;

    public String link;

    public String[] keywords;

    public String[] categories;

    public Integer source;


    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();

        sb.append("[CrawlerObject]\n");


        sb.append("\t");
        sb.append(this.title);
        sb.append("\n");

        sb.append("\t");
        sb.append(this.link);
        sb.append("\n");

        sb.append("\t");
        sb.append(this.image);
        sb.append("\n");

        sb.append("\t");
        sb.append(this.desc);
        sb.append("\n");

        if(this.keywords!=null) {
            sb.append("\tKeywords:");
            for(String t: this.keywords)
                sb.append(t + ",");
            sb.append("\n");
        }

        if(this.categories!=null) {
            sb.append("\tCategories:");
            for(String t: this.categories)
                sb.append(t + ",");
            sb.append("\n");
        }
        /*
        sb.append("\t||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n");
        sb.append(this.text);
        sb.append("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n");
        
    */
        return sb.toString();
    }

}