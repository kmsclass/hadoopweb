package dataexpo.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import dataexpo.Airline;

public class MultiDelayMapper 
    extends Mapper<LongWritable, Text,Text,IntWritable>{
	private final static IntWritable ONE = new IntWritable(1);
	private Text outkey = new Text();
	@Override
	public void map(LongWritable key, Text value, Context context) 
			throws IOException, InterruptedException {
		if(key.get() == 0) return;
		Airline al = new Airline(value);
		if(al.isDepartureDelayAvailable()) {
		  if(al.getDepartureDelayTime() > 0) {
			outkey.set("D-"+al.getYear()+"-" + al.getMonth());
			context.write(outkey, ONE);
		  }
		}
		if(al.isArriveDelayAvailable()) {
		  if(al.getArriveDelayTime() > 0) {
		    outkey.set("A-"+al.getYear()+"-" + al.getMonth());
			context.write(outkey,ONE);
		  }
	    }
    }
}