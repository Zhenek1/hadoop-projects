import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * Created by user on 18.06.15.
 */
public class MyGroupingComparator extends WritableComparator {

    protected MyGroupingComparator() {
        super(MyTuple.class, true);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int compare(WritableComparable w1, WritableComparable w2) {
        MyTuple key1 = (MyTuple) w1;
        MyTuple key2 = (MyTuple) w2;
        return key1.getUser_id().compareTo(key2.getUser_id());
    }
}
