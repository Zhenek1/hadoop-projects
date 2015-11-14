import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by user on 22.04.15.
 */
public class MyMapValue implements Writable {
    public Text domain;
    public Text page;
    public Text name;
    public Text ref;

    public MyMapValue() {
        this(new Text(""), new Text(""), new Text(""), new Text(""));
    }

    public MyMapValue(Text domain, Text page, Text name, Text ref) {
        this.domain = domain;
        this.page = page;
        this.name = name;
        this.ref = ref;
        this.domain = domain;
    }

    public void write(DataOutput dataOutput) throws IOException {
        domain.write(dataOutput);
        page.write(dataOutput);
        name.write(dataOutput);
        ref.write(dataOutput);
    }

    public void readFields(DataInput dataInput) throws IOException {
        domain.readFields(dataInput);
        page.readFields(dataInput);
        name.readFields(dataInput);
        ref.readFields(dataInput);
    }

    public String toString() {
        return domain + "," + page + "," + name + "," + ref;
    }
}