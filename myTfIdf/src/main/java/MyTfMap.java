import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by user on 13.05.15.
 */
public class MyTfMap extends TableMapper<Text, IntWritable> {

    static Connection hBaseConnection;

    private static final Set<String> stopwords = new HashSet<String>();
    static {
        stopwords.add("I"); stopwords.add("a"); stopwords.add("about");
        stopwords.add("an"); stopwords.add("are"); stopwords.add("as");
        stopwords.add("at"); stopwords.add("be"); stopwords.add("by");
        stopwords.add("com"); stopwords.add("de"); stopwords.add("en");
        stopwords.add("for"); stopwords.add("from"); stopwords.add("how");
        stopwords.add("in"); stopwords.add("is"); stopwords.add("it");
        stopwords.add("la"); stopwords.add("of"); stopwords.add("on");
        stopwords.add("or"); stopwords.add("that"); stopwords.add("the");
        stopwords.add("this"); stopwords.add("to"); stopwords.add("was");
        stopwords.add("what"); stopwords.add("when"); stopwords.add("where");
        stopwords.add("who"); stopwords.add("will"); stopwords.add("with");
        stopwords.add("and"); stopwords.add("the"); stopwords.add("www");
        stopwords.add("while");

        stopwords.add("а"); stopwords.add("но"); stopwords.add("на");
        stopwords.add("под"); stopwords.add("для"); stopwords.add("над");
        stopwords.add("как"); stopwords.add("все"); stopwords.add("тут");
        stopwords.add("там"); stopwords.add("здесь"); stopwords.add("чем");
        stopwords.add("это"); stopwords.add("эту");
    };

  //  private static final Pattern PATTERN = Pattern.compile("\\w+");
 // private static final Pattern PATTERN = Pattern.compile("[a-zA-Z_0-9а-я]+");

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    public static String removeDigits(String s) {

        while (!s.isEmpty() && Character.digit(s.charAt(0), 10) >= 0) {
            s = s.substring(1);
        }

        while (!s.isEmpty() && Character.digit(s.charAt(s.length() - 1), 10) >= 0) {
            s = s.substring(0, s.length() - 1);
        }

        return s;

    }

    public String getAllWords(Result value) {
        String allWords = "";
        try (Table table = hBaseConnection.getTable(TableName.valueOf("myUrlContentTable"))) {
            for (Cell c : value.getColumnCells("cf".getBytes(), "url".getBytes())) {
                Get get = new Get(CellUtil.cloneValue(c));
                Result result = table.get(get);
                Cell content = result.getColumnLatestCell("cf".getBytes(), "content".getBytes());
                String s = "";
                try {
                    s = Jsoup.parse(new String(CellUtil.cloneValue(content))).body().text() + " ";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                allWords += s;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allWords;
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();
        hBaseConnection = ConnectionFactory.createConnection(conf);
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        hBaseConnection.close();
    }

    @Override
    public void map(ImmutableBytesWritable row, Result value, Context context) throws IOException, InterruptedException {
        String user_id = new String(row.get());
      /*  String text = "";
        for (Cell c : value.getColumnCells("url".getBytes(), "CONTENT".getBytes()))
            text += new String(CellUtil.cloneValue(c));*/
        String text = getAllWords(value);

        text = text.replaceAll("[^a-zA-Z_0-9А-Яа-я\\s]+", " "); //Добавить дефис
        text = text.replaceAll("[^a-zA-Z_0-9А-Яа-я\\s]+", " ");
        String[] words = text.split("\\s+");
      //      Matcher m = PATTERN.matcher(text);
            // String[] words = text.split("[?!.,:()«\\d'\"\\s]+");
            StringBuilder valueBuilder = new StringBuilder();
        for (String word : words) {
            String matchedValue  = word.toLowerCase();
            if (matchedValue.length() < 3 || matchedValue.length() > 15 || isInteger(matchedValue) || stopwords.contains(matchedValue)) //|| !Character.isLetter(matchedValue.charAt(0)))
                continue;
            valueBuilder.append(matchedValue);
            valueBuilder.append("@");
            valueBuilder.append(user_id);
            context.write(new Text(valueBuilder.toString()), new IntWritable(1));
            valueBuilder.setLength(0);
        }
        /*    while (m.find()) {
                String matchedValue = m.group().toLowerCase();
                if (matchedValue.length() < 3 || isInteger(matchedValue))
                    continue;
                valueBuilder.append(matchedValue);
                valueBuilder.append("@");
                valueBuilder.append(user_id);
                context.write(new Text(valueBuilder.toString()), new IntWritable(1));
                valueBuilder.setLength(0);
            }*/
        }
}
