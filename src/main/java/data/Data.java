package data;

import com.sun.xml.internal.ws.developer.Serialization;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Adem on 09/4/2015.
 */
public class Data  implements  java.io.Serializable {

    public ObjectId _id;

    public Integer id;
    public Integer cluster;
    public String title;
    public String desc;
    public float similarity;

    @Serialization
    public Set<Integer> suitable_clusters = new HashSet<Integer>();


    public JSONObject toJSON() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("_id", _id);
            obj.put("cluster", cluster);
            obj.put("title", title);
            obj.put("desc", desc);
            obj.put("suitable_clusters", new JSONArray(this.suitable_clusters));
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[Data] id:").append(this.id).append(" _id:").append(this._id).append("\n\t").append(this.title);
        return sb.toString();
    }


    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Data))
            return false;
        if (((Data)obj).id == this.id)
            return true;
        return false;
    }
}
