import org.jsoup.Jsoup;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by user on 25.06.15.
 */
public class myTextToMatrix {

    private static final DecimalFormat DF = new DecimalFormat("###.########");

    private static String readFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    private static HashMap<String, Double> countWords(List<String> words) {
        HashMap<String, Double> map = new HashMap<String, Double>();
        for (String word : words) {
            if (map.containsKey(word))
                map.put(word, map.get(word) + 1.0);
            else
                map.put(word, 1.0);
        }
        return map;
    }

    public static void main(String[] args) {
        final File input_dir = new File("/home/user/IdeaProjects/myBayesClassifier/input_files_teach");
       // final File input_dir = new File("/home/user/IdeaProjects/myBayesClassifier/input_files_test");
        Set<String> all_words = new HashSet<String>();
        List<Integer> numbers = new ArrayList<Integer>();
        List<List<String>> words_per_text = new ArrayList<List<String>>();
        for (final File file : input_dir.listFiles()) {
            if (file.getAbsolutePath().contains("realty"))
                numbers.add(0);
            else if (file.getAbsolutePath().contains("medicine"))
                numbers.add(1);
            else  if (file.getAbsolutePath().contains("films"))
                numbers.add(2);
            else
                numbers.add(Integer.parseInt(
                        file.getAbsolutePath().substring(file.getAbsolutePath().length() - 2, file.getAbsolutePath().length())));
            String raw_file_data = "";
            try {
                raw_file_data = readFileAsString(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String file_data = "";
            try {
                file_data = Jsoup.parse(raw_file_data).body().text();
            } catch (Exception e) {
                e.printStackTrace();
            }
            file_data = file_data.replaceAll("[^a-zA-Z_0-9А-Яа-я\\s]+", " ");
            String words[] = file_data.split("\\s+");
            ArrayList<String> arr = new ArrayList<String>(Arrays.asList(words));
            words_per_text.add(arr);
            all_words.addAll(arr);
        }
        PrintWriter writer_teach = null;
        PrintWriter writer_test = null;
        try {
            writer_test = new PrintWriter("/home/user/IdeaProjects/myBayesClassifier/input_matrix_test", "UTF-8");
            writer_teach = new PrintWriter("/home/user/IdeaProjects/myBayesClassifier/input_matrix_teach", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < words_per_text.size(); i ++) {
            String output_string = "";
            output_string += numbers.get(i) + ", ";
            for (String word : all_words) {
                HashMap<String, Double> count_map = countWords(words_per_text.get(i));
                if (words_per_text.get(i).contains(word))
                 //   output_string += DF.format(count_map.get(word)) + " ";
                    output_string += count_map.get(word) + " ";
                else
                    output_string += 0.0 + " ";
            }
            if (numbers.get(i) != 0 && numbers.get(i) != 1 && numbers.get(i) != 2)
                writer_test.println(output_string.substring(0, output_string.length() - 1));
            else
                writer_teach.println(output_string.substring(0, output_string.length() - 1));
        }
       /* for (List<String> words : words_per_text) {
            String output_string = "";
            output_string += numbers.get(i) + ", ";
            i ++;
            for (String word : all_words) {
                HashMap<String, Double> count_map = countWords(words);
                if (words.contains(word))
                    output_string += DF.format(count_map.get(word)) + " ";
                else
                    output_string += 0.0 + " ";
            }
            writer_teach.println(output_string.substring(0, output_string.length() - 1));
        }*/
        writer_teach.close();
        writer_test.close();
    }
}
