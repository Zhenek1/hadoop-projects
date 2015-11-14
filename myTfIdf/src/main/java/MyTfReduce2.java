import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 14.05.15.
 */
public class MyTfReduce2 extends Reducer<Text, Text, Text, Text> {

    @Override
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        int wordsSumInDoc = 0;
        Map<String, String> tempCounter = new HashMap<String, String>();
        for (Text value : values) {
            String[] wordAndCounter = value.toString().split("=");
            tempCounter.put(wordAndCounter[0], wordAndCounter[1]);
            wordsSumInDoc += Integer.parseInt(wordAndCounter[1]);
        }
        for (String wordKey : tempCounter.keySet()) {
            context.write(new Text(wordKey + "@" + key.toString()),
                    new Text(tempCounter.get(wordKey) + "/" + wordsSumInDoc));
        }
    }
}
