package main;

import clustering.Cluster;
import config.LocalConfig;
import data.Data;
import mongo.MongoHelper;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import lucene.Lucene;

/**
 * Created by Adem on 14/5/2015.
 */
public class JsonGenerator {

    private static DB db;
    private static File databaseFile;




    HTreeMap<Integer,Cluster> clusters;
    HTreeMap<Data,Cluster> documents;

    public JsonGenerator() {

        Application.config = new LocalConfig();
        Lucene lucene = Lucene.getInstance();


        LinkedList<Data> docs  = new LinkedList<>();

        Data d = new Data();
        d._id = new ObjectId("55634301b8c7d723f42d978d");
        d.title = "Naz Elmas hastanelik oldu!";

        docs.add(d);

        Data d2 = new Data();
        d2._id = new ObjectId("556342dbb8c7d723f42d9724");
        d2.title = "Çağatay Ulusoy hakkında şaşırtıcı iddia";

        docs.add(d2);

        LinkedHashMap<ObjectId,Integer> results = MongoHelper.getInstance().findSuitableClusters(docs);


        /*
            databaseFile = new File("objects.db");
            db = DBMaker.newFileDB(databaseFile).closeOnJvmShutdown().make();

            clusters = db.getHashMap("clusters");
            documents = db.getHashMap("documents");

            MongoHelper mongoHelper = MongoHelper.getInstance();


            System.out.println(mongoHelper.checkDocumentExist("deneme2"));
*/

            /*
            for (Map.Entry<Data,Cluster> entry : documents.entrySet()) {


                mongoHelper.insertDocument(entry.getKey());

                System.out.println(entry.getKey());

            }


            for (Map.Entry<Integer, Cluster> entry : clusters.entrySet())
            {

                Cluster c = entry.getValue();

                mongoHelper.insertCluster(c);


                System.out.println(c);
            }
*/
            /*
            JSONArray arr = new JSONArray();
            for (Map.Entry<Integer, Cluster> entry : clusters.entrySet())
            {

                arr.put(entry.getValue().toJSON());
            }

            FileWriter f = new FileWriter("output.json");

            f.write(arr.toString());
            f.close();
            */


    }
}
