import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

/**
 * Created by user on 09.04.15.
 */
public class MainClass {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        if (args.length != 3) {
            System.err.println("Usage: MyMapReduce <in> <out> <merged_out>");
            System.exit(2);
        }

      //  Job job = new Job(conf, "MyMapReduce");
        Job job = Job.getInstance();
        job.setJarByClass(MainClass.class);
        job.setMapperClass(MyMap.class);
        job.setReducerClass(MyReduce.class);
        job.setMapOutputKeyClass(MyMapKey.class);
        job.setMapOutputValueClass(FloatWritable.class);
        job.setOutputKeyClass(MyMapKey.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

      //  FileInputFormat.setInputDirRecursive(job, true);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        boolean result = job.waitForCompletion(true);

        try { FileSystem hdfs = FileSystem.get(conf);
            FileUtil.copyMerge(hdfs, new Path(args[1]), hdfs, new Path(args[2]), false, conf, null);
        } catch (IOException e) {

        }
        System.exit(result? 0 : 1);
    }
}
