package brain;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.log4j.Logger;

import algorithm.KMeansppBagOfWords;
import algorithm.KMeansppDouble;

/**
 * You can generate a .jar file that you can submit on your Flink
 * cluster.
 * Just type
 * 		mvn clean package
 * in the projects root directory.
 * You will find the jar in
 * 		target/
 *
 * The first parameter must contain whether to start the "double" or the "bagOfWords" variant.
 * The second parameter must contain the dataPath.
 * The third parameter must contain the outputPath.
 * The fourth parameter must contain k.
 * The fifth parameter must contain numIterations.
 *
 */
public class Job {
	
	static Logger logger = Logger.getLogger(Job.class.getName());

	public static void main(String[] args) throws Exception {
		if (args.length != 5) {
			logger.error("Not enough arguments. Make sure program call fulfills the requirements: \n" +
					"\t* The first parameter must contain whether to start the \"double\" or the \"bagOfWords\" variant. \n" +
					"\t* The second parameter must contain the dataPath. \n" + 
					"\t* The third parameter must contain the outputPath. \n" +
					"\t* The fourth parameter must contain k \n." +
					"\t* The fifth parameter must contain numIterations. \n");
			System.exit(1);
		}
		
		String dataPath = args[1];
		String outputPath = args[2];
		int k = Integer.parseInt(args[3]);
		int numIterations = Integer.parseInt((args[4]));
		
		if ("double".equals(args[0])) {
			logger.info("Running KMeansDouble with k=" + k + ", numIterations=" + numIterations + ", dataPath=" + dataPath + ", outputPath=" + outputPath);
			KMeansppDouble kmd = new KMeansppDouble(k, numIterations, dataPath, outputPath);
			kmd.run();
		} else if ("bagOfWords".equals(args[0])){
			logger.info("Running KMeansBagOfWords with k=" + k + ", numIterations=" + numIterations + ", dataPath=" + dataPath + ", outputPath=" + outputPath);
			KMeansppBagOfWords kmb = new KMeansppBagOfWords(k, numIterations, dataPath, outputPath);
			kmb.run();
		} else {
			logger.error(args[0] + " is no valid parameter");
		}
		
		logger.info("Job finished");
	}

}
