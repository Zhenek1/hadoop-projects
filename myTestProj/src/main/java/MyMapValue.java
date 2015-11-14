import org.apache.hadoop.io.Writable;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by user on 09.04.15.
 */
public class MyMapValue implements Writable {
    public int pos;
    public int w;
    public int h;
    public float bidfloor;

    public MyMapValue() {
        this(0, 0, 0, 0);
    }

    public MyMapValue(int pos, int w, int h, float bidfloor) {
        this.pos = pos;
        this.w = w;
        this.h = h;
        this.bidfloor = bidfloor;
    }

    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(pos);
        dataOutput.writeInt(w);
        dataOutput.writeInt(h);
        dataOutput.writeFloat(bidfloor);
    }

    public void readFields(DataInput dataInput) throws IOException {
        pos = dataInput.readInt();
        w = dataInput.readInt();
        h = dataInput.readInt();
        bidfloor = dataInput.readFloat();
    }

    public String toString() {
        return pos + ", " + w + ", " + h + ", " + bidfloor;
    }
}

