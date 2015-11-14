import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by user on 18.06.15.
 */
public class MyTuple implements WritableComparable<MyTuple> {

    private String user_id;
    private double tf_Idf;

    public MyTuple() {

    }

    public MyTuple(String user_id, double tf_Idf) {
        this.user_id = user_id;
        this.tf_Idf = tf_Idf;
    }

    @Override
    public int compareTo(MyTuple o) {
        int result = user_id.compareTo(o.user_id);
        if (result == 0)
            result = -1 * Double.valueOf(tf_Idf).compareTo(Double.valueOf(o.tf_Idf));
        return result;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        WritableUtils.writeString(dataOutput, user_id);
        WritableUtils.writeString(dataOutput, String.valueOf(tf_Idf));
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        user_id = WritableUtils.readString(dataInput);
        tf_Idf = Double.valueOf(WritableUtils.readString(dataInput));
    }

    public String getUser_id() {
        return user_id;
    }

    public double getTf_Idf() {
        return tf_Idf;
    }
}
