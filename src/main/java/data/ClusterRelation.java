package data;

import clustering.Cluster;
import com.sun.xml.internal.ws.developer.Serialization;

/**
 * Created by Adem on 14/5/2015.
 */
public class ClusterRelation implements java.io.Serializable {

    @Serialization
    public Integer related_to;

    @Serialization
    public float strength;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClusterRelation))
            return false;
        if (((ClusterRelation)obj).related_to == this.related_to)
            return true;
        return false;
    }

}
