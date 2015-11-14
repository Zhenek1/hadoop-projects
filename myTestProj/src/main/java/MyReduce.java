/**
 * Created by user on 09.04.15.
 */
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;


import java.io.IOException;
import java.util.Iterator;

public class MyReduce extends Reducer<MyMapKey,FloatWritable,MyMapKey,Text> {
    public void reduce(MyMapKey key, Iterable<FloatWritable> values, Context context) throws IOException, InterruptedException {
        float average_bidfloor = 0;
        int amount = 0;
        for (FloatWritable value : values) {
            average_bidfloor += value.get();
            amount ++;
        }
        average_bidfloor = average_bidfloor / amount;
        context.write(key, new Text(average_bidfloor + "\t" + amount));
    }
}

/*public class MyReduce extends TableReducer<MyMapKey,FloatWritable,ImmutableBytesWritable> {
    public void reduce(MyMapKey key, Iterable<FloatWritable> values, Context context) throws IOException, InterruptedException {
        float average_bidfloor = 0;
        int amount = 0;
        for (FloatWritable value : values) {
            average_bidfloor += value.get();
            amount ++;
        }
        average_bidfloor = average_bidfloor / amount;
        context.write(key, new Text(average_bidfloor + "\t" + amount));
    }
}*/

