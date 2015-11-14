import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Created by user on 14.05.15.
 */
public class MyIdfMap extends Mapper<LongWritable, Text, Text, Text> {

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] wordDocAndCounter = value.toString().split("\t");
        String[] wordAndDoc = wordDocAndCounter[0].split("@");
        context.write(new Text(wordAndDoc[0]), new Text(wordAndDoc[1] + "=" + wordDocAndCounter[1]));
    }
}
