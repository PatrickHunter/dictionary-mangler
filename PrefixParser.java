/**
 *Analyze text to detirmine which tokens are used as prefixs to an input.
 **/
public class PrefixParser {

    public static class PrefixMapper extends Mapper<Object, Text, Text, IntWritable>{

	private final static IntWritable one = new IntWritable(1);
	private Text token = new Text();
	private String infix;
	/**
	 *Updates contex with every token in value used as a prefix to infix, 
	 *and the frequnecy of each prefix.
	 **/
	public void map(Object key, Text value, Context context){
	    StringTokenixer itr = new StringTokenizer(value.toString());
	    while (itr.hasMoreTokens()){
		String current = itr.nextToken();
		if(current.contains(infix)){
		    word.set(current.split(infix)[0]);
		    context.write(word, one);
		}
	    }
	}
    }
    
    public static class IntSumReducer extends Reducer<Text, IntWritable, Text,IntWritable> {
	private IntWritable result = new IntWritable();
	
	public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
			   ) throws IOException, InteruptedException {
	    int sum = 0;
	    for(IntWritable val: values){
		sum += val.get();
	    }
	    result.set(sum);
	    context.write(key, result);
	}
    }

    public static void main(String[] args) throws Exception{
	Configuration conf = new Configuration();
	Job job = Job.getInstance(conf, "prefix parser");
	job.setJarByClass(PrefixParser.class);
	job.setMapperClass(TokenizerMapper.class);
	job.setCombinerClass(IntSumReducer.class);
	job.setReducerClass(IntSumReducer.class);
	job.setOutputKeyClass(Text.class);
	job.setOutputValueClass(IntWritable.class);
	FileInputFormat.addInputPath(job, new Path(args[0]));
	FileOutputFormat.setOutputPath(job, new Path(args[1]));
	System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}