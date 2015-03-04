package algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import algorithm.util.GenerateDouble;

public class TestKMeansppDouble {

	final static String inputPath = "tmp/double/inputData";
	final static String originClustersPath = "tmp/double/originClusters";
	final static String outputPath = "tmp/double/outputData";
	
	final static int k = 4;
	final static int numIterations = 100;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		GenerateDouble gd = new GenerateDouble(inputPath, originClustersPath);
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
		KMeansppDouble kmd = new KMeansppDouble(k, numIterations, inputPath, outputPath);
		kmd.run();
		validate();
	}
	
	public void validate() throws IOException {
		List<String> result = new ArrayList<String>();
		List<String> expectedResult = new ArrayList<String>();
        readAllResultLines(result, outputPath + "/centers");
        for (String centerWrapper : expectedResult) {
            String[] centerExpected = centerWrapper.split("\\|");
            double x = Double.parseDouble(centerExpected[centerExpected.length - 2]);
            double y = Double.parseDouble(centerExpected[centerExpected.length - 1]);
            boolean flag = false;
            for (String centerResultWrapper : result) {
                String[] centerResult = centerResultWrapper.split("\\|");
                double xx = Double.parseDouble(centerResult[centerResult.length - 2]);
                double yy = Double.parseDouble(centerResult[centerResult.length - 1]);
                if (Math.sqrt((x - xx) * (x - xx) + (y - yy) * (y - yy)) < 5) {
                    flag = true;
                }
            }
            Assert.assertTrue("no match for center " + centerWrapper, flag);
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

}
