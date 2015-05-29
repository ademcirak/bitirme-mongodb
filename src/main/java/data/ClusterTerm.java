package data;

import clustering.Cluster;

/**
 * Created by Adem on 26/4/2015.
 */
public class ClusterTerm {
    public String term;
    public Integer doc_freq;
    public Integer total_freq;

    public ClusterTerm() {


    }

    public ClusterTerm(String t, Integer df, Integer tf) {
        term = t;
        doc_freq = df;
        total_freq = tf;
    }
}
