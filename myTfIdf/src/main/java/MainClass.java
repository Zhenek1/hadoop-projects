import com.google.protobuf.ServiceException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.hbase.mapreduce.RowCounter;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import sun.applet.Main;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by user on 13.05.15.
 */


public class MainClass {

   /* private static final String output1 = "/home/user/IdeaProjects/myTfIdf/input_ouput/outputTf1";
    private static final String output2 = "/home/user/IdeaProjects/myTfIdf/input_ouput/outputTf2";
    private static final String output3 = "/home/user/IdeaProjects/myTfIdf/input_ouput/outputTfIdf";
    private static final String output4 = "/home/user/IdeaProjects/myTfIdf/input_ouput/outputTfIdf_sorted";

    private static final String output1 = "/user/emistukov/myTfIdf/input_ouput/outputTf1";
    private static final String output2 = "/user/emistukov/myTfIdf/input_ouput/outputTf2";
    private static final String output3 = "/user/emistukov/myTfIdf/input_ouput/outputTfIdf";
    private static final String output4 = "/user/emistukov/myTfIdf/input_ouput/outputTfIdf_sorted";*/

    /*private static final String outputs[] = {
            "/home/user/IdeaProjects/myTfIdf/input_ouput/outputTf1",
            "/home/user/IdeaProjects/myTfIdf/input_ouput/outputTf2",
            "/home/user/IdeaProjects/myTfIdf/input_ouput/outputTfIdf",
            "/home/user/IdeaProjects/myTfIdf/input_ouput/outputTfIdf_sorted"};*/


    private static final String outputs[] = {
            "/user/emistukov/myTfIdf/input_ouput/outputTf1",
            "/user/emistukov/myTfIdf/input_ouput/outputTf2",
            "/user/emistukov/myTfIdf/input_ouput/outputTfIdf",
            "/user/emistukov/myTfIdf/input_ouput/outputTfIdf_sorted"};

    public static long rows_count;
    public static int column_max_versions;

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        if (args.length != 1) {
            System.err.println("Usage: MyMapReduce <in>");
            System.exit(2);
        }
        Configuration conf = HBaseConfiguration.create();
        conf.set("fs.hdfs.impl",
                org.apache.hadoop .hdfs.DistributedFileSystem.
                        class.getName()
        );
        conf.set("fs.file.impl",
                org.apache.hadoop.fs.LocalFileSystem.
                        class.getName()
        );

        conf.setBoolean(MRJobConfig.MAPREDUCE_JOB_USER_CLASSPATH_FIRST, true);

      //  conf.set("hbase.client.scanner.caching", "1000");

        System.setProperty("HADOOP_USER_NAME", "emistukov");

        Connection conn = ConnectionFactory.createConnection(conf);
        column_max_versions = conn.getTable(TableName.valueOf(args[0])).
                getTableDescriptor().getFamily("cf".getBytes()).getMaxVersions();

      /*  Connection c = ConnectionFactory.createConnection();

        try {
            HBaseAdmin.checkHBaseAvailable(conf);
        } catch (ServiceException e) {
            System.err.println("Something wrong with HBase");
            e.printStackTrace();
        }
        //conf.set("hbase.zookeeper.quorum", "localhost");
        HBaseAdmin adm = (HBaseAdmin) c.getAdmin();
        HColumnDescriptor desc = adm.getTableDescriptor(args[0].getBytes()).getFamily("url".getBytes());
        column_max_versions = desc.getMaxVersions();*/
        Job job = Job.getInstance(conf);
        job.setJobName("myTfIdfJob");
        job.setJarByClass(MainClass.class);

        Scan scan = new Scan();
        scan.setCaching(1000);
        scan.setCacheBlocks(false);
        scan.setMaxVersions(column_max_versions);

       /* long rows_count = j.getCounters().findCounter(TaskCounter.MAP_INPUT_RECORDS).getValue();*/
       /* Iterator<String> iter = j.getCounters().getGroupNames().iterator();
        while (iter.hasNext()) {
            CounterGroup g = j.getCounters().getGroup(iter.next());
            int k = 5;
        }*/

      /*  String[] rowCounter_args = ("-Dhbase.client.scanner.caching=12345 -Dmapreduce.map.speculative=false " + args[0]).split(" ");
        try {
            RowCounter.main(rowCounter_args);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
      //  Configuration counter_conf = HBaseConfiguration.create();
        Job j = RowCounter.createSubmittableJob(conf, args);
        j.waitForCompletion(true);
        Class enum_class = j.getMapperClass().getDeclaredClasses()[0];
        Enum en = (Enum)enum_class.getEnumConstants()[0];
        rows_count = j.getCounters().findCounter(en).getValue();
        System.out.println("Rows count: " + rows_count);


        TableMapReduceUtil.initTableMapperJob(
                args[0],        // input HBase table name
                scan,             // Scan instance to control CF and attribute selection
                MyTfMap.class,   // mapper
                Text.class,             // mapper output key
                IntWritable.class,             // mapper output value
                job);

        job.setReducerClass(MyTfReduce.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileOutputFormat.setOutputPath(job, new Path(outputs[0]));

        boolean result = job.waitForCompletion(true);

        Configuration conf2 = new Configuration();
        conf2.setBoolean(MRJobConfig.MAPREDUCE_JOB_USER_CLASSPATH_FIRST, true);

        Job job2 = Job.getInstance(conf2);
        job2.setJobName("myTfJob2");
        job2.setJarByClass(MainClass.class);
        job2.setMapperClass(MyTfMap2.class);
        job2.setReducerClass(MyTfReduce2.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);
        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(TextOutputFormat.class);
        TextInputFormat.addInputPath(job2, new Path(outputs[0]));
        TextOutputFormat.setOutputPath(job2, new Path(outputs[1]));

        result = job2.waitForCompletion(true);

      /*  Job j = RowCounter.createSubmittableJob(conf, new String[]{"myHBaseTable"});

        result = j.waitForCompletion(true);

        rows_count = j.getCounters().findCounter(TaskCounter.MAP_INPUT_RECORDS).getValue();*/

        Configuration conf3 = new Configuration();
        conf3.setBoolean(MRJobConfig.MAPREDUCE_JOB_USER_CLASSPATH_FIRST, true);
        conf3.set("rows_count", String.valueOf(rows_count));

        Job job3 = Job.getInstance(conf3);
        job3.setJobName("myTfIdfJob");
        job3.setJarByClass(MainClass.class);
        job3.setMapperClass(MyIdfMap.class);
        job3.setReducerClass(MyIdfReduce.class);
        job3.setOutputKeyClass(Text.class);
        job3.setOutputValueClass(Text.class);
        job3.setInputFormatClass(TextInputFormat.class);
        job3.setOutputFormatClass(TextOutputFormat.class);
        TextInputFormat.addInputPath(job3, new Path(outputs[1]));
        TextOutputFormat.setOutputPath(job3, new Path(outputs[2]));

        result = job3.waitForCompletion(true);

        Job job4 = Job.getInstance(conf2);
        job4.setJobName("myTfIdfSorter");
        job4.setJarByClass(MainClass.class);
        job4.setPartitionerClass(MyKeyPartitioner.class);
        job4.setGroupingComparatorClass(MyGroupingComparator.class);

        job4.setMapOutputKeyClass(MyTuple.class);
        job4.setMapOutputValueClass(Text.class);

        job4.setOutputKeyClass(Text.class);
        job4.setOutputValueClass(Text.class);

        job4.setInputFormatClass(TextInputFormat.class);
        job4.setOutputFormatClass(TextOutputFormat.class);

        TextInputFormat.addInputPath(job4, new Path(outputs[2]));
        TextOutputFormat.setOutputPath(job4, new Path(outputs[3]));

        job4.setMapperClass(MyMapSorter.class);
        job4.setReducerClass(MyReduceSorter.class);

        result = job4.waitForCompletion(true);

        System.exit(result? 0 : 1);
    }
}
