package socket;

import clustering.Cluster;
import clustering.Clustering;
import clustering.IClusterListener;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import data.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * Created by Adem on 06/4/2015.
 */
public class SocketHelper implements IClusterListener {
    private static final Logger logger = LogManager.getLogger(SocketHelper.class);
    private static SocketHelper ourInstance = new SocketHelper();

    protected Socket socket;
    protected LinkedBlockingDeque<Job> incoming = new LinkedBlockingDeque<Job>();

    public static SocketHelper getInstance() {
        return ourInstance;
    }


    public SocketHelper() {

        final SocketConsumer consumer = new SocketConsumer(this);
        Clustering.getInstance().addListener(this);

        try {

            IO.Options opts = new IO.Options();
            socket = IO.socket("http://localhost:3000");
            socket.connect();
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    logger.debug("connected");
                    socket.emit("server-connected");
                    consumer.start();
                }

            });
            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    logger.debug("disconnected");
                    consumer.pause();

                }
            });

            socket.on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    logger.debug("reconnected");
                    consumer.resume();
                }
            });


        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public void emit(Job j) {
        this.incoming.add(j);
    }

    @Override
    public void onNewCluster(Cluster c) {

        this.incoming.add(new Job("server-cluster", c.toJSON()));
    }

    @Override
    public void onClusterChanged(Cluster c, Data d) {

        try {
            JSONObject clusterJson = new JSONObject();
            clusterJson.put("cluster",c.toJSON());
            clusterJson.put("new_data", d.toJSON());
            this.incoming.add(new Job("server-new", clusterJson));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClusterMerged(Cluster new_cluster, Cluster first, Cluster second) {

    }
}
