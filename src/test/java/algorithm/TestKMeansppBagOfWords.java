package algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import algorithm.util.GenerateBagOfWords;
import algorithm.util.GenerateBagOfWords.Cluster;

public class TestKMeansppBagOfWords {

	final static String inputPath = "tmp/bagOfWords/inputData";
	final static String originClustersPath = "tmp/bagOfWords/originClusters";
	final static String outputPath = "tmp/bagOfWords/outputData";
	
	final static int k = 5;
	final static int numIterations = 100;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		GenerateBagOfWords gd = new GenerateBagOfWords(inputPath, originClustersPath);
		gd.create();
		gd.writeOriginClusters();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		File f = new File(inputPath);
		File f2 = new File(originClustersPath);
		File f3 = new File(outputPath);
		
		FileUtils.forceDelete(f);
		FileUtils.forceDelete(f2);
		FileUtils.forceDelete(f3);
	}

	@Test
	public void test() throws Exception {
		KMeansppBagOfWords kmb = new KMeansppBagOfWords(k, numIterations, inputPath, outputPath);
	    kmb.run();
		validate();
	}
	
	public void validate() throws IOException, ClassNotFoundException {
		List<String> result = new ArrayList<String>();
		List<Cluster> expectedResult = new ArrayList<Cluster>();
		
        readAllResultLines(result, outputPath + "/clusters");
        
    	for (int i = 0; i < GenerateBagOfWords.k; i++) {
    		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(originClustersPath + "/cluster_" + i));
			Cluster c = (Cluster) ois.readObject();
			ois.close();
        	expectedResult.add(c);
    	}

        // read clusters formed by K-Means++ from result file
        List<HashSet<Integer>> clusters = new ArrayList<HashSet<Integer>>(k);
        for (int i = 0; i <= k; ++i) {
            clusters.add(new HashSet<Integer>());
        }
        for (String line : result) {
            String[] fields = line.split("\\|");

            int clusterId = Integer.parseInt(fields[0]);
            int pointId = Integer.parseInt(fields[1]);

            clusters.get(clusterId).add(pointId);
        }

        // compare the original clusters with the formed ones
        for (HashSet<Integer> cluster : clusters) {
            if (cluster.isEmpty())
                continue;
            boolean match = false;
            for (Cluster c : expectedResult) {
                match = match || hashsetEquals(cluster, c.pointIds);
            }
            Assert.assertTrue("The cluster formed by K-Means++ does not match the original cluster!", match);
        }
	}
	
	private void readAllResultLines(List<String> target, String resultPath) throws IOException {
		for (BufferedReader reader : getResultReader(resultPath)) {
			String s = null;
			while ((s = reader.readLine()) != null) {
				target.add(s);
			}
		}
	}

	private BufferedReader[] getResultReader(String resultPath) throws FileNotFoundException {
		File[] files = new File(resultPath).listFiles();
		BufferedReader[] readers = new BufferedReader[files.length];
		for (int i = 0; i < files.length; i++) {
			readers[i] = new BufferedReader(new FileReader(files[i]));
		}
		return readers;
	}
	
    private boolean hashsetEquals(HashSet<Integer> s1, HashSet<Integer> s2) {
        if (s1.size() != s2.size()) {
            return false;
        }
        for (int itr : s1) {
            if (!s2.contains(itr))
                return false;
        }
        return true;
    }

}
