package clustering;

import data.ClusterRelation;
import data.CrawlerObject;
import data.Data;
import lucene.Lucene;
import main.Application;
import mongo.MongoHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.mapdb.*;

import java.io.*;
import java.util.*;

/**
 * Created by Adem on 10/4/2015.
 */
public class Clustering {

    private static final Logger logger = LogManager.getLogger(Clustering.class);
    private static final Clustering instance = new Clustering();

    private LinkedList<IClusterListener> listeners = new LinkedList<IClusterListener>();

    private Lucene lucene = Lucene.getInstance();

    private MongoHelper mongoHelper = MongoHelper.getInstance();


    public Clustering() {

    }

    public static Clustering getInstance() {
        return instance;
    }

    public void addListener(IClusterListener l) {
        listeners.add(l);
    }


    public void Cluster(CrawlerObject doc) {

        try {

            LinkedList<Data> docs = this.lucene.neighbourSearch(doc._id);

            if(docs==null || docs.size()==0) {

                Cluster c = new Cluster(doc);
                invokeOnNewCluster(c);
            }
            else {


                LinkedHashMap<ObjectId,Integer> suitable_clusters = mongoHelper.findSuitableClusters(docs);


                if(suitable_clusters==null || suitable_clusters.size() == 0) {

                    logger.warn("findSuitlableClusters returned empty");

                    Cluster c = new Cluster(doc);
                    invokeOnNewCluster(c);


                }
                else if(suitable_clusters.size() == 1)
                {
                    ObjectId cluster_id = suitable_clusters.entrySet().iterator().next().getKey();
                    mongoHelper.setCluster(doc,cluster_id);
                    mongoHelper.pushDocument(cluster_id, doc);

                }
                else {

                    mongoHelper.setCluster(doc, suitable_clusters.keySet());
                    mongoHelper.pushDocument(suitable_clusters.entrySet().iterator().next().getKey(), doc);

                }



/*
                if(temp == null || temp.getKey()==null) {

                    Cluster c = new Cluster(doc);
                    doc.cluster = c.id;
                    clusters.put(c.id,c);
                    documents.put(doc,c);

                    for(Data d : docs) {
                        Clustering.Link(c, documents.get(d), d.similarity);
                    }

                    db.commit();
                    invokeOnNewCluster(c);

                } else {


                    Cluster temp_c = temp.getKey();
                    temp_c.addDocument(doc);
                    doc.cluster = temp_c.id;
                    documents.put(doc, temp_c);

                    for(Data d : docs) {
                        Clustering.Link(temp_c, documents.get(d), d.similarity);
                    }

                    db.commit();

                    invokeOnClusterChanged(temp_c, doc);

                }*/

            }


        }catch (Exception e) {
            e.printStackTrace();
            logger.error("CLUSTER ERR: " + e.getMessage(), e);
        }

    }

    private void invokeOnNewCluster(Cluster c) {
        for(IClusterListener l : listeners) {
            l.onNewCluster(c);
        }
    }

    private void invokeOnClusterChanged(Cluster c, Data d) {
        for(IClusterListener l : listeners) {
            l.onClusterChanged(c,d);
        }
    }



    public void Cluster(LinkedList<Data> docs) {
        for (Data d : docs) {
            //this.Cluster(d);
        }

    }

    public static void Link(Cluster first, Cluster second, float strength) {

        if(first==null || second==null)
            return;

        return;
/*
        if(first.id == second.id)
            return;

        ClusterRelation rel1 = new ClusterRelation();
        rel1.related_to=second.id;
        rel1.strength= strength;

        ClusterRelation rel2 = new ClusterRelation();
        rel2.related_to=first.id;
        rel2.strength= strength;

        first.related_clusters.add(rel1);
        second.related_clusters.add(rel2);*/


    }

    public JSONArray getJSON() {
        JSONArray arr = new JSONArray();
        /*
        for (Map.Entry<Integer, Cluster> entry : clusters.entrySet())
        {
            arr.put(entry.getValue().toJSON());
        }*/
        return arr;
    }



}