import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 14.05.15.
 */
public class MyIdfReduce extends Reducer<Text, Text, Text, Text> {

    private static final DecimalFormat DF = new DecimalFormat("###.########");

    @Override
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        long docsNumber = Long.parseLong(context.getConfiguration().get("rows_count"));
        int numberOfDocumentsInCorpusWhereKeyAppears = 0;

        Map<String, String> tempFrequencies = new HashMap<String, String>();
        for (Text val : values) {
            String[] documentAndFrequencies = val.toString().split("=");
            if (Integer.parseInt(documentAndFrequencies[1].split("/")[0]) > 0) {
                numberOfDocumentsInCorpusWhereKeyAppears++;
            }
            tempFrequencies.put(documentAndFrequencies[0], documentAndFrequencies[1]);
        }

        for (String document : tempFrequencies.keySet()) {
            String[] wordFrequenceAndTotalWords = tempFrequencies.get(document).split("/");
            double tf = Double.valueOf(Double.valueOf(wordFrequenceAndTotalWords[0])
                    / Double.valueOf(wordFrequenceAndTotalWords[1]));

            double idf = Math.log10((double) docsNumber /
                    (double) ((numberOfDocumentsInCorpusWhereKeyAppears == 0 ? 1 : 0) +
                            numberOfDocumentsInCorpusWhereKeyAppears));

            double tfIdf = tf * idf;

            context.write(new Text(key + "@" + document), new Text("[" + numberOfDocumentsInCorpusWhereKeyAppears + "/"
                    + docsNumber + " , " + wordFrequenceAndTotalWords[0] + "/"
                    + wordFrequenceAndTotalWords[1] + " , " + DF.format(tfIdf) + "]"));

        }
    }
}
