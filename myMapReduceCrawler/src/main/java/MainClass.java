import com.google.protobuf.ServiceException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import java.io.IOException;

/**
 * Created by user on 30.04.15.
 */
public class MainClass {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
        Configuration conf = HBaseConfiguration.create();
    //    conf.addResource(new Path("/home/user/Downloads/hbase-1.0.0/conf/hbase-site.xml"));
        conf.set("fs.hdfs.impl",
                org.apache.hadoop.hdfs.DistributedFileSystem.class.getName()
        );
        conf.set("fs.file.impl",
                org.apache.hadoop.fs.LocalFileSystem.class.getName()
        );
        conf.setBoolean(MRJobConfig.MAPREDUCE_JOB_USER_CLASSPATH_FIRST, true);
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


        Job job = Job.getInstance(conf);
        job.setJobName("myCrawlerJob");
        job.setJarByClass(MainClass.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
       // job.setInputFormatClass(TextInputFormat.class);
        job.setInputFormatClass(MyMapInputFormat.class);
        job.setMapperClass(MyCrawlerMap.class);
        job.setMapOutputValueClass(Put.class);
        TableMapReduceUtil.initTableReducerJob(args[1],
                null, job);
        job.setNumReduceTasks(0);
        boolean result = job.waitForCompletion(true);

        System.exit(result? 0 : 1);
    }
}
