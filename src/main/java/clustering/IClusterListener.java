package clustering;

import data.Data;
import org.json.JSONException;

/**
 * Created by Adem on 12/4/2015.
 */
public interface IClusterListener {

    public void onNewCluster(Cluster c);
    public void onClusterChanged(Cluster c, Data d);
    public void onClusterMerged(Cluster new_cluster, Cluster first, Cluster second);
}
