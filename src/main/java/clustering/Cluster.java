package clustering;

import com.sun.xml.internal.ws.developer.Serialization;
import data.ClusterRelation;
import data.CrawlerObject;
import data.Data;
import lucene.Lucene;
import mongo.MongoHelper;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Adem on 10/4/2015.
 */
public class Cluster  implements  java.io.Serializable {

    private static Lucene lucene = Lucene.getInstance();

    @Serialization
    public HashSet<ObjectId> docs = new HashSet<ObjectId>();

    @Serialization
    public ObjectId _id;

    @Serialization
    public Date created_at = new Date();

    @Serialization
    public Date last_update;

    @Serialization
    public LinkedHashMap<String, Integer> vector;

    @Serialization
    public HashSet<ObjectId> related_clusters = new HashSet<>();

    @Serialization
    protected HashMap<String,Integer> significant_vector = new HashMap<String,Integer>();


    protected MongoHelper mongoHelper = MongoHelper.getInstance();



    public Cluster() {
    }

    public Cluster(CrawlerObject doc) {
        docs.add(doc._id);
        updateVector(doc);

        mongoHelper.createCluster(this);
        mongoHelper.setCluster(doc, this._id);
    }

    public void addDocument(CrawlerObject doc) {
        docs.add(doc._id);
        updateVector(doc);

        MongoHelper.getInstance().addDocumentToCluster(this, doc);
    }


    protected void updateVector(CrawlerObject doc) {
        last_update = new Date();

        Map<String,Integer> map = lucene.getTerms(doc._id);
        if(map!=null) {
            for (Map.Entry<String, Integer> entry : map.entrySet())
            {
                String encoded_key = entry.getKey();
                Integer val = significant_vector.get(encoded_key);

                if(val==null)
                    significant_vector.put(encoded_key,entry.getValue());
                else
                    significant_vector.put(encoded_key, (val + entry.getValue()) );
            }
        } else {
            System.err.println("Empty map");
        }


    }

    String encodeKey(String key) {
        return key.replace(".", "\\uff0E");
    }

    String decodeKey(String key) {
        return key.replace("\\uff0E", ".");
    }


    public LinkedHashMap<String, Integer> getVector() {

        vector = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry  : EntiriesSorted.entriesSortedByValues(this.significant_vector)) {
            if(entry.getValue() < 3)
                break;

            vector.put(encodeKey(entry.getKey()), entry.getValue());

        }
        return vector;
    }


    public JSONObject toJSON() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("cluster_id",this._id.toString());
            obj.put("doc_count", docs.size());
            obj.put("last_update",last_update.toString());
            obj.put("vector", new JSONObject(this.getVector()));

            obj.put("related_clusters", new JSONArray(this.related_clusters));

            JSONArray arr = new JSONArray();
            for(ObjectId d : this.docs) {
                arr.put(d.toHexString());
            }
            obj.put("docs",arr);
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("[Cluster] id:")
                .append(this._id).append("\n");
        for(ObjectId d : this.docs)
            sb.append("\t").append(d.toHexString()).append("\n");
        return sb.toString();
    }


    @Override
    public int hashCode() {
        return this._id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Cluster))
            return false;
        if (((Cluster)obj)._id == this._id)
            return true;
        return false;
    }

}
