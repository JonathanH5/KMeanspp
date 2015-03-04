# KMeans++

This is an implemenation of the KMeans++ algorithm.<br>
The original authors of the algorithm are: https://github.com/TU-Berlin-DIMA/IMPRO-3.SS14 <br>
 * Yuwen Chen <br>
 * Mingliang Qi <br>
 * Mingyuan Wu <br>
<br>

Author of rebuild for Flink 0.8.1: <br>
 * Jonathan Hasenburg <br>

The algorithm can be executed as "double" or "bagOfWords". <br>
The following parameters are necessary: <br>
 * The first parameter must contain whether to start the "double" or the "bagOfWords" variant.
 * The second parameter must contain the dataPath.
 * The third parameter must contain the outputPath.
 * The fourth parameter must contain k.
 * The fifth parameter must contain numIterations.
<br>

The included unit tests use generated data sets. You can generate sample datasets for yourself by executing 
algorithm.util.GenerateDouble or algorithm.util.GenerateBagOfWords. The sample datasets can be found in tmp/double or tmp/bagOfWords.
