package algorithm;

import java.io.Serializable;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;

import algorithm.util.GenericFunctions;

/**
 * KMeans++ usage for 2-Dimensional point data.
 */
public class KMeansppDouble {


    private String dataPath;

    private String outputPath;

    private int k;

    private int numIterations;

    public KMeansppDouble(final int k, final int numIterations, final String dataPath, final String outputPath) {
        this.dataPath = dataPath;
        this.outputPath = outputPath;
        this.k = k;
        this.numIterations = numIterations;
    }

    /**
     * User defined class for 2-dimensional point data.
     */
    public static class MyPoint implements Serializable {

        private static final long serialVersionUID = -8646398807053064759L;

        public double x, y;

        public MyPoint() {}

        public MyPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return x + "|" + y;
        }
    }

    /**
     * Convert the input data to user defined type - MyPoint
     */
    public static final class StringPointConverter implements MapFunction<String, MyPoint> {

        private static final long serialVersionUID = -7253773731266749932L;

        @Override
        public MyPoint map(String value) throws Exception {
            String[] tmp = value.split("\\|");
            return new MyPoint(Double.parseDouble(tmp[1]), Double.parseDouble(tmp[2]));
        }
    }

    /**
     * User defined function, including input data, average function and distance measure.
     */
    public static class MyFunctions implements GenericFunctions<MyPoint> {

        private static final long serialVersionUID = 5510454279473390773L;

        private String pointsPath;

        public MyFunctions(String filePath) {
            this.pointsPath = filePath;
        }

        @Override
        public DataSet<MyPoint> getDataSet(ExecutionEnvironment env) {
            return env.readTextFile(pointsPath).map(new StringPointConverter());
        }

        @Override
        public MyPoint add(MyPoint in1, MyPoint in2) {
            return new MyPoint(in1.x + in2.x, in1.y + in2.y);
        }

        @Override
        public MyPoint div(MyPoint in1, long val) {
            in1.x /= val;
            in1.y /= val;
            return in1;
        }

        @Override
        public double distance(MyPoint in1, MyPoint in2) {
            double xdiff = in1.x - in2.x;
            double ydiff = in1.y - in2.y;
            return Math.sqrt(xdiff * xdiff + ydiff * ydiff);
        }
    }

    public void run() throws Exception {
        KMeansppGeneric<MyPoint> kmp = new KMeansppGeneric<MyPoint>(this.outputPath, this.k, this.numIterations);
        kmp.run(new MyFunctions(this.dataPath));
    }

}
