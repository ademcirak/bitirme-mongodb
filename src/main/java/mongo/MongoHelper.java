package mongo;

import clustering.Cluster;
import com.mongodb.*;
import data.ClusterRelation;
import data.CrawlerObject;
import data.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import javax.print.Doc;
import java.util.*;

/**
 * Created by Adem on 20/5/2015.
 */
public class MongoHelper {

    private static final Logger logger = LogManager.getLogger(MongoHelper.class);

    private static MongoHelper instance = new MongoHelper();

    MongoClient mongoClient;
    DB database;

    DBCollection  documents;
    DBCollection  clusters;

    public MongoHelper() {

        try {
            mongoClient = new MongoClient("localhost", 27017);
            database = mongoClient.getDB("project");
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }


        documents = database.getCollection("documents");
        clusters = database.getCollection("clusters");
    }


    public boolean checkDocumentExist(String link) {

        DBObject doc = documents.findOne(new BasicDBObject("link", link));

        if(doc != null)
            return true;
        return false;
    }


    public void insertDocument(Data data) {

        DBObject  doc = new BasicDBObject ();

        doc.put("id", data.id);
        // doc.put("cluster_id", this.createReferenceObject("clusters",data.));

        doc.put("title", data.title);
        doc.put("desc", data.desc);
        doc.put("suitable_clusters", data.suitable_clusters);

        documents.insert(doc);
        data._id = (ObjectId) doc.get("_id");
    }


    public void insertDocument(CrawlerObject data) {

        DBObject doc = new BasicDBObject();


        doc.put("title", data.title);
        doc.put("text", data.text);
        doc.put("desc", data.desc);

        doc.put("text_clean", data.text_clean);
        doc.put("image", data.image);
        doc.put("link", data.link);
        doc.put("source", data.source);
/*
        if(data.keywords!=null)
            doc.put("keywords", data.keywords);
        if(data.categories!=null)
            doc.put("categories", data.categories);
*/
        documents.insert(doc);
        data._id = (ObjectId) doc.get("_id");
    }

    public void setCluster(CrawlerObject data, ObjectId cluster_id) {

        DBObject query = new BasicDBObject("_id", data._id);

        DBObject updateObject = new BasicDBObject("$set", new BasicDBObject("cluster_id", new DBRef("clusters", cluster_id)));

        documents.update(query, updateObject);
    }

    public void pushDocument(ObjectId cluster_id, CrawlerObject doc) {

        DBObject query = new BasicDBObject("_id", cluster_id);
        DBObject updateObject = new BasicDBObject("$push", new BasicDBObject("docs", new DBRef("documents", doc._id)));

        clusters.update(query, updateObject);

    }

    public void setCluster(CrawlerObject data,
                           Set<ObjectId> cluster_ids) {

        Iterator<ObjectId> it = cluster_ids.iterator();

        ObjectId first = cluster_ids.iterator().next();


        DBObject query = new BasicDBObject("_id", data._id);

        //  update cluster_id doc
        DBObject updateObject = new BasicDBObject("$set", new BasicDBObject("cluster_id", new DBRef("clusters", first)));
        // put related clusters to doc
        updateObject.put("$push", new BasicDBObject("related_clusters", new BasicDBObject("$each", cluster_ids)));
        documents.update(query, updateObject);



        // TODO update cluster-to-cluster relation

    }




    public void createCluster(Cluster c) {


        DBObject doc = new BasicDBObject();

        doc.put("created_at", c.created_at);
        doc.put("last_update", c.last_update);
        doc.put("vector", c.getVector());


        // reference to related clusters
        List<DBRef> db_cluster_references = new LinkedList<>();
        for(ObjectId d : c.related_clusters)  {
            db_cluster_references.add(new DBRef("documents", d));
        }
        doc.put("docs", db_cluster_references);

        // reference to docs in cluster
        List<DBRef> db_doc_references = new LinkedList<>();
        for(ObjectId d : c.docs)  {
            db_doc_references.add(new DBRef("documents", d));
        }
        doc.put("docs", db_doc_references);

        clusters.insert(doc);
        c._id = (ObjectId) doc.get("_id");

    }


    public void addDocumentToCluster(Cluster c, CrawlerObject d) {

    }


    public LinkedHashMap<ObjectId,Integer> findSuitableClusters(List<Data> docs) {


        LinkedList<ObjectId> ids = new LinkedList<>();

        docs.stream().forEach((Data d) -> {
            ids.add(d._id);
        });

        DBObject match = new BasicDBObject();
        match.put("_id", new BasicDBObject("$in", ids));


        DBObject group = new BasicDBObject();
        group.put("_id", "$cluster_id");
        group.put("count", new BasicDBObject("$sum", 1));


        DBObject sort = new BasicDBObject();
        sort.put("$sort", new BasicDBObject("count", -1));

        List<DBObject> pipeline = new LinkedList<>();
        pipeline.add(new BasicDBObject("$match", match));
        pipeline.add(new BasicDBObject("$group", group));
        pipeline.add(sort);

        Iterable<DBObject> results = this.documents.aggregate(pipeline).results();

        LinkedHashMap<ObjectId,Integer> out_results = new LinkedHashMap<>();

        logger.debug(results);
        results.forEach((DBObject obj) -> {
            logger.debug(obj);
            out_results.put( (ObjectId) ((DBRef) obj.get("_id")).getId(), ((Integer) obj.get("count")) );
        });
        return out_results;
    }




    public static MongoHelper getInstance() { return instance; }

}
