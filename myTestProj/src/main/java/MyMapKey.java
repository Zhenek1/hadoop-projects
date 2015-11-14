import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by user on 09.04.15.
 */
public class MyMapKey implements WritableComparable<MyMapKey> {

    Text url;
    IntWritable w;
    IntWritable h;
    IntWritable pos;

    public MyMapKey() {
        this(new Text(), new IntWritable(), new IntWritable(), new IntWritable());
    }

    public MyMapKey(Text url, IntWritable pos, IntWritable w, IntWritable h) {
        this.url = url;
        this.pos = pos;
        this.w = w;
        this.h = h;
    }

    public int compareTo(MyMapKey other) {

        if (url.compareTo(other.url) != 0) {
            return url.compareTo(other.url);
        } else if (w.compareTo(other.w) != 0) {
            return w.compareTo(other.w);
        } else if (h.compareTo(other.h) != 0) {
            return h.compareTo(other.h);
        } else if (pos.compareTo(other.pos) != 0) {
            return pos.compareTo(other.pos);
        } else {
            return 0;
        }
    }

    public void write(DataOutput dataOutput) throws IOException {
        url.write(dataOutput);
        w.write(dataOutput);
        h.write(dataOutput);
        pos.write(dataOutput);
    }

    public void readFields(DataInput dataInput) throws IOException {
        url.readFields(dataInput);
        w.readFields(dataInput);
        h.readFields(dataInput);
        pos.readFields(dataInput);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyMapKey myMapKey = (MyMapKey) o;

        if (w != myMapKey.w) return false;
        if (h != myMapKey.h) return false;
        if (pos != myMapKey.pos) return false;
        return !(url != null ? !url.equals(myMapKey.url) : myMapKey.url != null);

    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + w.get();
        result = 31 * result + h.get();
        result = 31 * result + pos.get();
        return result;
    }

    public String toString() {
        return url + "\t" + pos + "\t" + w + "x" + h;
    }

}
