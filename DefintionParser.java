import java.io.IOException;
import java.util.StringTokenizer;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Tokenizes primary input on all caps words and then outputs each all cap word paired with
 * each word in the secondary input that apears in its token.
 * For the Websters input this is equivelent to pairing each word in the dictionary with each * keyword that apears in its definition.
 **/ 
public class DefintionParser {
    /**
     * Splits input texts on all caps words, returns the all caps words as keys with all the
     * text between them and the next all caps word as values 
     **/
    public static class AllCapsTokenizer extends Mapper <Object, Text, Text, Text>{

	private Text key = new Text();
	private Text value = new Text();
	private String allCaps = "((?<=\\s[A-Z]+\\s)|(?=\\s[A-Z]+\\s))";

	public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
	    String[] tokenized = value.toString().split(allCaps);
	    int current;
	    if(tokenized[0].matches("\\s[A-Z]+\\s")){
		current = 0;
	    }else{
		current = 1;
	    }
	    for(; current < (tokenized.length + 1); current += 2){
		this.key.set(tokenized[current]);
		value.set(tokenized[current + 1]);
		context.write(this.key,value);
	    }
	}
	    
    }

    public static void main(String[] args) throws Exception{
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "defintion parser");
        job.setJarByClass(DefintionParser.class);
        job.setMapperClass(AllCapsTokenizer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}