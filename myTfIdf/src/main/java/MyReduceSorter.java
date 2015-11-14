import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * Created by user on 18.06.15.
 */
public class MyReduceSorter extends Reducer<MyTuple, Text, Text, Text> {

    private static final DecimalFormat DF = new DecimalFormat("###.########");

    @Override
    public void reduce(MyTuple key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        Iterator<Text> it = values.iterator();
        int count = 0;
        while (it.hasNext() && count <= 10) {
            context.write(new Text(key.getUser_id() + "@" + it.next().toString()), new Text(DF.format(key.getTf_Idf())));
            count ++;
        }
    }
}

