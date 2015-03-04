package algorithm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;

import algorithm.util.GenericFunctions;


/**
 * KMeans++ usage for bag of words
 */
public class KMeansppBagOfWords {

    private String dataPath;

    private String outputPath;

    private int k;

    private int numIterations;

    public KMeansppBagOfWords(final int k, final int numIterations, final String dataPath, final String outputPath) {
        this.dataPath = dataPath;
        this.outputPath = outputPath;
        this.k = k;
        this.numIterations = numIterations;
    }

    /**
     * User defined class for bag of word.
     */
    public static class Document implements Serializable {

        private static final long serialVersionUID = -8646398807053061675L;

        public Map<String, Double> wordFreq = new HashMap<String, Double>();

        public Integer id;

        public Document() {
            id = -1;
        }

        public Document(Integer id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return Integer.toString(id);
        }
    }

    /**
     * Convert the input data into User defined type - Document.
     */
    public static final class RecordToDocConverter implements GroupReduceFunction<Tuple3<Integer, Integer, Double>, Document> {

        private static final long serialVersionUID = -8476366121490468956L;

        @Override
        public void reduce(Iterable<Tuple3<Integer, Integer, Double>> values, Collector<Document> out) throws Exception {
        	Iterator<Tuple3<Integer, Integer, Double>> i = values.iterator();
            if (i.hasNext()) {
                Tuple3<Integer, Integer, Double> elem = i.next();
                Document doc = new Document(elem.f0);
                doc.wordFreq.put(elem.f1.toString(), elem.f2);

                while (i.hasNext()) {
                    elem = i.next();
                    doc.wordFreq.put(elem.f1.toString(), elem.f2);
                }
                out.collect(doc);
            }
        }
    }

    public static final class FilterFirst3Lines implements FlatMapFunction<String, Tuple3<Integer, Integer, Double>> {

		private static final long serialVersionUID = 7085772419936979083L;

		@Override
        public void flatMap(String value, Collector<Tuple3<Integer, Integer, Double>> out) throws Exception {
            String[] splits = value.split(" ");
            if (splits.length > 2) {
                out.collect(new Tuple3<Integer, Integer, Double>(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]), Double.parseDouble(splits[2])));
            }
        }
    }

    /**
     * User defined function, including input data, average function and distance measure.
     */
    public static class MyFunctions implements GenericFunctions<Document> {

        private static final long serialVersionUID = 5510454279473390773L;

        private String pointsPath;

        public MyFunctions(String filePath) {
            this.pointsPath = filePath;
        }

        @Override
        public DataSet<Document> getDataSet(ExecutionEnvironment env) {
            return env.readTextFile(pointsPath).flatMap(new FilterFirst3Lines()).groupBy(0).reduceGroup(new RecordToDocConverter());
        }

        @Override
        public Document add(Document in1, Document in2) {
            for (Map.Entry<String, Double> itr : in2.wordFreq.entrySet()) {
                Double v1 = in1.wordFreq.get(itr.getKey());
                if (v1 == null) {
                    in1.wordFreq.put(itr.getKey(), itr.getValue());
                } else {
                    in1.wordFreq.put(itr.getKey(), itr.getValue() + v1);
                }
            }
            return in1;
        }

        @Override
        public Document div(Document in1, long val) {
            Document out = new Document(in1.id);
            for (Map.Entry<String, Double> itr : in1.wordFreq.entrySet()) {
                out.wordFreq.put(itr.getKey(), itr.getValue() / val);
            }
            return out;
        }

        @Override
        public double distance(Document in1, Document in2) {
            double sum = 0;
            Set<String> added = new HashSet<String>();

            for (Map.Entry<String, Double> itr : in2.wordFreq.entrySet()) {
                Double v1 = in1.wordFreq.get(itr.getKey());
                double v2 = itr.getValue();
                if (v1 == null) {
                    sum += v2 * v2;
                } else {
                    sum += (v2 - v1) * (v2 - v1);
                    added.add(itr.getKey());
                }
            }

            for (Map.Entry<String, Double> itr : in1.wordFreq.entrySet()) {
                if (!added.contains(itr.getKey())) {
                    sum += itr.getValue() * itr.getValue();
                }
            }
            return Math.sqrt(sum);
        }

    }

    public void run() throws Exception {
        KMeansppGeneric<Document> kmp = new KMeansppGeneric<Document>(this.outputPath, this.k, this.numIterations);
        kmp.run(new MyFunctions(this.dataPath));
    }
}
