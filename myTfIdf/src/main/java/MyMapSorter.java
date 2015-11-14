import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Created by user on 18.06.15.
 */
public class MyMapSorter extends Mapper<LongWritable, Text, MyTuple, Text> {

    private static final Log log = LogFactory.getLog(MyMapSorter.class);

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] wordUserId_Counter = value.toString().split("\t");
        String[] wordAndUserId = wordUserId_Counter[0].split("@");
        String tf_idf_value = wordUserId_Counter[1].
                substring(wordUserId_Counter[1].lastIndexOf(", ") + 2, wordUserId_Counter[1].indexOf("]"));
        context.write(new MyTuple(wordAndUserId[1], Double.valueOf(tf_idf_value)), new Text(wordAndUserId[0]));
        log.debug(wordAndUserId[1] + " => " + tf_idf_value);
    }
}
