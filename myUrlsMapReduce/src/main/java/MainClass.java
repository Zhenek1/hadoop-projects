import com.google.protobuf.ServiceException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.mapreduce.IdentityTableReducer;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

/**
 * Created by user on 22.04.15.
 */


public class MainClass {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
        Configuration conf = HBaseConfiguration.create();
        conf.addResource(new Path("/home/user/Downloads/hbase-1.0.0/conf/hbase-site.xml"));
        Connection c = ConnectionFactory.createConnection();
        try {
            HBaseAdmin.checkHBaseAvailable(conf);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        //conf.set("hbase.zookeeper.quorum", "localhost");
        HBaseAdmin adm = (HBaseAdmin) c.getAdmin();
        TableName[] names = adm.listTableNames();

        if (args.length != 2) {
            System.err.println("Usage: MyMapReduce <in> <out>");
            System.exit(2);
        }

        //  Job job = new Job(conf, "MyMapReduce");
        Job job = Job.getInstance(conf);
        job.setJobName("MyMapReduceJob");
        job.setJarByClass(MainClass.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setInputFormatClass(TextInputFormat.class);
        job.setMapperClass(MyUrlsMap.class);
       // job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(Put.class);
      //  job.setOutputKeyClass(Text.class);
      //  job.setOutputValueClass(Text.class);

        //  FileInputFormat.setInputDirRecursive(job, true);
      /*  TableMapReduceUtil.initTableReducerJob(args[1],
                MyUrlsReduce.class, job);*/
        TableMapReduceUtil.initTableReducerJob(args[1],
                null, job);
        boolean result = job.waitForCompletion(true);

        System.exit(result? 0 : 1);
    }
}