package algorithm.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import algorithm.KMeansppBagOfWords;

public class GenerateBagOfWords {

    private String inputPath = "";
    
    private String originClustersPath = "";
    
    public static final String fieldDelimiter = " ";
    
    
    public static final long seed = 14839204302832L;
    
    public static final int numPointsPerCluster = 300;

    public static final int numDimensions = 200;

    public static final int k = 5;

    public static final int numIterations = 10;

    public static final int maxClusterRadius = 200;

    public static final int maxFeatureValue = 10000;
    
    
    Random random = new Random(seed);
    
    List<Cluster> originClusters = new ArrayList<Cluster>(k);


    public static void main(String[] args) throws IOException {
		GenerateBagOfWords g = new GenerateBagOfWords("tmp/bagOfWords/inputData", "tmp/bagOfWords/originClusters");
		g.create();
		g.writeOriginClusters();
	}
    
    public GenerateBagOfWords(String inputPath, String originClustersPath) {
    	this.inputPath = inputPath;
    	this.originClustersPath = originClustersPath;
    }
    
    public void create() throws IOException {
    	File f = new File(inputPath);
    	if (f.exists()) {
    		f.delete();
    	}
    	f.getParentFile().mkdirs();
    	f.createNewFile();
    	
        BufferedWriter out = new BufferedWriter(new FileWriter(f));

        // generate initial data set
        for (int c = 0; c < k; ++c) {
            // pick a new center that does not intersect with existing centers
            Cluster cluster = new Cluster();
            cluster.radius = (double) random.nextInt(maxClusterRadius) + 1;
            do {
                for (int w = 0; w < numDimensions; ++w) {
                    cluster.center.wordFreq.put(Integer.toString(w), (double) random.nextInt(maxFeatureValue));
                }
            } while (intersectWithExistingCenter(cluster, originClusters));

            // add points to the new center
            for (int i = 0; i < numPointsPerCluster; ++i) {
                int docId = i + numPointsPerCluster * c;
                KMeansppBagOfWords.Document p = new KMeansppBagOfWords.Document(docId);
                for (int w = 0; w < numDimensions; ++w) {
                    // generate a point whose word counts are within the range cluster.center +/-
                    // radius
                    int freq = (int) (random.nextInt((int) (cluster.radius * 2)) + cluster.center.wordFreq.get(Integer.toString(w)) - cluster.radius);
                    p.wordFreq.put(Integer.toString(w), freq > 0 ? (double) freq : 0);
                }
                cluster.pointIds.add(p.id);

                // write to temp folder as initial data set
                writePointToFile(p, out);
            }
            originClusters.add(cluster);
        }
        out.flush();
        out.close();

    }
    
    public void writePointToFile(KMeansppBagOfWords.Document p, BufferedWriter out) throws IOException {
        for (Map.Entry<String, Double> itr : p.wordFreq.entrySet()) {
            out.write("" + p.id + fieldDelimiter + itr.getKey() + fieldDelimiter + itr.getValue());
            out.newLine();
        }
    }
    
    public void writeOriginClusters() throws IOException {
    	File f = new File(originClustersPath);
    	if (f.exists()) {
    		f.delete();
    	}
    	f.mkdirs();
    	int i = 0;
    	for (Cluster c : originClusters) {
        	ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(originClustersPath + "/cluster_" + i));
        	oos.writeObject(c);
        	oos.close();
        	i++;
    	}

    }
    
    public static class Cluster implements Serializable {

        private static final long serialVersionUID = -2779412714237240261L;

        public KMeansppBagOfWords.Document center = new KMeansppBagOfWords.Document();

        public double radius;

        public HashSet<Integer> pointIds = new HashSet<Integer>();
    }
    
    private boolean intersect(double c1, double r1, double c2, double r2) {
        if (c1 < c2) {
            return c1 + r1 >= c2 - r2;
        } else {
            return c2 + r2 >= c1 - r1;
        }
    }
    
    private boolean intersectWithExistingCenter(Cluster c, Collection<Cluster> exists) {
        if (exists == null || exists.isEmpty()) {
            return false;
        }

        for (Cluster itr : exists) {
            for (Map.Entry<String, Double> attr : itr.center.wordFreq.entrySet()) {
                if (!intersect(c.center.wordFreq.get(attr.getKey()), c.radius, attr.getValue(), itr.radius)) {
                    return false;
                }
            }
        }
        return true;
    }
    
}
