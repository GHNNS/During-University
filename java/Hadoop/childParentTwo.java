import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class childParentTwo {
    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, Text> {
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

            StringTokenizer itr = new StringTokenizer(value.toString()," ");
            while(itr.hasMoreTokens()){
                String child = itr.nextToken();
                String parent = itr.nextToken();
                if(child.equals("C") && parent.equals("p")){
                    continue;
                }
                context.write(new Text(child), new Text(child + "-" +parent));
                context.write(new Text(parent), new Text( child + "-" +parent));
            }
        }
    }

    public static class IntSumReducer
            extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            ArrayList<String> grandchild = new ArrayList<String>();
            ArrayList<String> grandparent = new ArrayList<String>();
//            System.out.println(key.toString());
            Iterator<Text> iterator = values.iterator();
            while (iterator.hasNext()) {
                String value = iterator.next().toString();
                String[] childParent = value.split("-");
//                System.out.println(childParent[0]+childParent[1]);
                if(childParent[0].equals(key.toString())){
                    grandparent.add(childParent[1]);
                } else if(childParent[1].equals(key.toString())) {
                    grandchild.add(childParent[0]);

                }
            }
            if(grandchild.size() != 0 && grandparent.size() != 0) {
                for (int i = 0; i < grandchild.size(); i++) {
                    for (int j = 0; j < grandparent.size(); j++) {
                        context.write(new Text(grandchild.get(i)), new Text(grandparent.get(j)));
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        Configuration conf = new Configuration();
        FileSystem hdfs=FileSystem.get(conf);
        Path deletefile=new Path("/output");
        boolean isDeleted=hdfs.delete(deletefile,true);
        Job job = Job.getInstance(conf, "childParentTwo");
        job.setJarByClass(childParentTwo.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.out.println(job.waitForCompletion(true) ? 0 : 1);
        long end = System.currentTimeMillis();
        System.out.println("One Time : " + (end-start) + "ms");
    }
}