import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.RowCounter;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.TaskCounter;
import org.apache.hadoop.mapreduce.jobhistory.HistoryViewer;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

public class MyRowCounter {

    public static class CounterMapper extends TableMapper<Text, Text> {

        public static enum Counters {
            ROWS
        }

        public void map(ImmutableBytesWritable row, Result value,
                        Context context) throws InterruptedException, IOException {
            context.getCounter(Counters.ROWS).increment(1);
            System.out.println(context.getCounter(Counters.ROWS).getValue());
        }
    }

    public static void main(String[] args) throws Exception {
       /* if (args.length != 2) {
            System.out.println("Usage: HbaseRowCounter <tablename>");
            System.exit(0);
        }*/
        Configuration config = HBaseConfiguration.create();
        Job job = Job.getInstance(config);
        job.setJarByClass(MyRowCounter.class);
        Scan scan = new Scan();
        scan.setCaching(200);
        scan.setCacheBlocks(false);
        //scan.setFilter(new FirstKeyOnlyFilter());

        TableMapReduceUtil.initTableMapperJob(args[0], // input table
                scan, // Scan instance
                CounterMapper.class, // mapper class
                ImmutableBytesWritable.class, // mapper output key
                Result.class, // mapper output value
                job);

        job.setOutputFormatClass(NullOutputFormat.class);
        job.setNumReduceTasks(0); // at least one, adjust as required
        boolean b = job.waitForCompletion(true);
        if (!b) {
            throw new IOException("error with job!");
        }

        long l = job.getCounters().findCounter(CounterMapper.Counters.ROWS).getValue();
        Counters ccc = job.getCounters();
        long rows_count = job.getCounters().findCounter(TaskCounter.MAP_INPUT_RECORDS).getValue();
    }
}