package dictionary.core;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Tokenizes primary input on all caps words and then outputs each all cap word paired with
 * each word in the secondary input that apears in its token.
 * For the Websters input this is equivelent to pairing each word in the dictionary with each * keyword that apears in its definition.
 */
public class DefinitionParser {
    /**
     * Splits input texts on all caps words, returns the all caps words as keys with all the
     * text between them and the next all caps word as values
     */
    public static class AllCapsTokenizer extends Mapper<Object, Text, Text, Text> {

        private Text key = new Text();
        private Text value = new Text();
        private String allCaps = "((?<=\\s[A-Z]{2,20}\\s)|(?=\\s[A-Z]{2,20}\\s))";

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] tokenized = value.toString().split(allCaps);
            int current;
            if (tokenized[0].matches("\\s[A-Z]+\\s")) {
                current = 0;
            } else {
                current = 1;
            }
            for (; current < (tokenized.length - 1); current += 2) {
                this.key.set(tokenized[current]);
                value.set(tokenized[current + 1]);
                context.write(this.key, value);
            }
        }

    }

    /**
     * Replace the value with just the parts of speech markers retains
     * key value
     */
    public static class DefinitionReducer extends Reducer<Text, Text, Text, Text> {

        private Path[] localFiles;
        private List<String> partsOfSpeech = new ArrayList<String>();

        /**
         * Populates this.partsOfSpeech from the distributed cache.
         * @param context the hadoop supplied reducer context
         */
        @Override
        protected void setup(Context context) {
            Scanner sc = null;
            try {
                localFiles = context.getLocalCacheFiles();
                sc = new Scanner(new File(localFiles[0].toUri()));
            } catch (IOException e) {
                System.out.println("IOException");
                System.out.println(e);
                return;
            }

            while (sc.hasNext()) {
                partsOfSpeech.add(sc.next());
            }
        }

        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            StringBuffer value = new StringBuffer();
            for (Text current : values) {
                String defintion = current.toString();
                for (String part : partsOfSpeech) {
                    if (defintion.contains(part)) {
                        value.append(part);
                    }
                }
            }
            Text output = new Text(value.toString());
            context.write(key, output);
        }
    }


    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "definition parser");
        job.setJarByClass(DefinitionParser.class);
        job.setMapperClass(AllCapsTokenizer.class);
        job.setReducerClass(DefinitionReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(NLineInputFormatWithNLineReader.class);
        NLineInputFormatWithNLineReader.addInputPath(job, new Path(args[0]));
        job.getConfiguration().setInt(
                "mapreduce.input.lineinputformat.linespermap", 500);
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.addCacheFile(new URI(args[2]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}