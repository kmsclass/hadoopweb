package dataexpo.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import dataexpo.Airline;
//출발지연건수 맵퍼
public class DepartureDelayMapper extends Mapper<LongWritable, Text,Text, IntWritable>{
	private final static IntWritable one = new IntWritable(1);
	private Text outkey = new Text();
	@Override
	protected void map(LongWritable key, Text value,Context context)
			throws IOException, InterruptedException {
		if(key.get() == 0) return;
		Airline al = new Airline(value);
		if(al.isDepartureDelayAvailable() && al.getDepartureDelayTime() > 0) {
			outkey.set(al.getYear() + "-" + al.getMonth());
			context.write(outkey, one);
		}
	}
}