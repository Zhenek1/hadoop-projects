import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

/**
 * Created by user on 18.06.15.
 */
public class MyKeyPartitioner extends Partitioner<MyTuple, Text> {

    HashPartitioner<Text, Text> hashPartitioner = new HashPartitioner<Text, Text>();
    Text newKey = new Text();

    @Override
    public int getPartition(MyTuple myTuple, Text value, int numReduceTasks) {
        try {
            newKey.set(myTuple.getUser_id());
            return hashPartitioner.getPartition(newKey, value, numReduceTasks);
        } catch (Exception e) {
            e.printStackTrace();
            return (int) (Math.random() * numReduceTasks);
        }
    }
}
