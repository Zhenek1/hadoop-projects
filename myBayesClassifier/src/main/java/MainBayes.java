/**
 * Created by user on 24.06.15.
 */
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class MainBayes {

    private static JavaRDD<LabeledPoint> parseData(JavaSparkContext sc, String filename) {
        return sc.textFile(filename)
                .map(new Function<String, LabeledPoint>() {
                    @Override
                    public LabeledPoint call(String line) {
                        String parts[] = line.split(", ");
                        String value_parts[] = parts[1].split(" ");
                        double value_parts_double[] = new double[value_parts.length];
                        for (int i = 0; i < value_parts_double.length; i++)
                            value_parts_double[i] = Double.valueOf(value_parts[i]);
                        return new LabeledPoint(Double.valueOf(parts[0]), Vectors.dense(value_parts_double));
                    }
                });
    }

    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Simple Application").setMaster("local[4]");
        JavaSparkContext sc = new JavaSparkContext(conf);
      //  sc.addJar("/home/user/IdeaProjects/myBayesClassifier/out/artifacts/myBayesClassifier_jar/myBayesClassifier.jar");
      /*  JavaRDD<LabeledPoint> parsedData = sc.textFile("/home/user/IdeaProjects/myBayesClassifier/myTest")
                .map(new Function<String, LabeledPoint>() {
                    @Override
                    public LabeledPoint call(String line) {
                        String parts[] = line.split(", ");
                        String value_parts[] = parts[1].split(" ");
                        double value_parts_double[] = new double[value_parts.length];
                        for (int i = 0; i < value_parts_double.length; i++)
                            value_parts_double[i] = Double.valueOf(value_parts[i]);
                        return new LabeledPoint(Double.valueOf(parts[0]), Vectors.dense(value_parts_double));
                    }
                });
        parsedData.cache();
        JavaRDD<LabeledPoint> splits[] = parsedData.randomSplit(new double[]{0.6, 0.4});
        JavaRDD<LabeledPoint> training = splits[0];
        JavaRDD<LabeledPoint> test = splits[1];*/

        JavaRDD<LabeledPoint> trainingData = parseData(sc, "/home/user/IdeaProjects/myBayesClassifier/input_matrix_teach");
        JavaRDD<LabeledPoint> testData = parseData(sc, "/home/user/IdeaProjects/myBayesClassifier/input_matrix_test");

        final NaiveBayesModel model = NaiveBayes.train(trainingData.rdd(), 1.0);

        JavaPairRDD<Double, Double> predictionAndLabel =
                testData.mapToPair(new PairFunction<LabeledPoint, Double, Double>() {
                    @Override
                    public Tuple2<Double, Double> call(LabeledPoint p) {
                        return new Tuple2<Double, Double>(model.predict(p.features()), p.label());
                    }
                });
       /* JavaRDD<Double> label =
                testData.map(new Function<LabeledPoint, Double>() {
                    @Override
                    public Double call(LabeledPoint p) {
                        return new Double (model.predict(p.features()));
                    }
                });*/
        for (Tuple2<Double, Double> d : predictionAndLabel.collect())
            if (d._1 == 0)
                System.out.println("test" + d._2 + " realty");
            else if (d._1 == 1)
                System.out.println("test" + d._2 + " medicine");
            else
                System.out.println("test" + d._2 + " films");
       /* double accuracy = predictionAndLabel.filter(new Function<Tuple2<Double, Double>, Boolean>() {
            @Override
            public Boolean call(Tuple2<Double, Double> pl) {
                return pl._1().equals(pl._2());
            }
        }).count() / (double) testData.count();*/
        model.save(sc.sc(), "myModelPath");
        NaiveBayesModel sameModel = NaiveBayesModel.load(sc.sc(), "myModelPath");
    }
}
