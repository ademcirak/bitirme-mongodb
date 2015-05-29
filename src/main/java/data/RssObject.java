package data;
/**
 *
 * @author Adem
 */
public class RssObject {
    public String link=null;
    public String title=null;
    public String desc=null;
    public String image=null;


    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();

        sb.append("[RssObject]\n");
        sb.append("\t");
        sb.append(this.link+"\n");
        if(title!=null) {
            sb.append("\t");
            sb.append(this.title+"\n");
        }
        if(image!=null) {
            sb.append("\t");
            sb.append(this.image+"\n");
        }
        if(desc!=null) {
            sb.append("\t");
            sb.append(this.desc+"\n");
        }
        return sb.toString();
    }
}