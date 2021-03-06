package dataexpo.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileAlreadyExistsException;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import dataexpo.mapreduce.ArrivalDelayMapper;
import dataexpo.mapreduce.DelayCountReducer;
import dataexpo.mapreduce.DepartureDelayMapper;
// http://localhost:8080/hadoopweb/MonDelayCntServlet 요청시 호출되는 서블릿
public class MonDelayCntServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public MonDelayCntServlet() {
        super();
    }
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		String year = request.getParameter("year");
		String kbn = request.getParameter("kbn");
		String input = "C:/kms/20200914/hadoop/workspace/hadoopstudy1/infile/" + year + ".csv";
		String output = request.getSession().getServletContext().getRealPath("/") 
				                                       + "output/mondelay/" + year+"_"+kbn;
		Configuration conf = new Configuration();
		try {
			Job job = new Job(conf,"MonDelayCntServlet");
			
			job.setJarByClass(this.getClass());  //서블릿 클래스를 작업클래스로 설정
			
			if(kbn.equals("a")) { //도착지연정보
				job.setMapperClass(ArrivalDelayMapper.class);
			} else {   //출발지연정보
				job.setMapperClass(DepartureDelayMapper.class);
			}
			job.setReducerClass(DelayCountReducer.class);

			job.setInputFormatClass(TextInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(IntWritable.class);
			FileInputFormat.addInputPath(job, new Path(input));
			FileOutputFormat.setOutputPath(job, new Path(output));
			job.waitForCompletion(true); //하둡 실행
		} catch(FileAlreadyExistsException e) { //
			System.out.println("기존 파일 존재 :" + output);
		} catch(Exception e) {
			e.printStackTrace();
		}
		String file = "part-r-00000";
		request.setAttribute("file", year);
		Path outFile = new Path(output + "/" + file); //출력폴더/part-r-00000 => 하둡이 생성한 출력파일
		FileSystem fs = FileSystem.get(conf);
		BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(outFile)));
		//람다식 코딩 : 함수적인터페이스(Functional Interface)=> 추상메서드가 한개만있는 인터페이스
		//Comparator 인터페이스 : int compare(Object o1, Object o2)
		//정렬방식 : 월순으로 지정
		Map<String ,Integer> map = new TreeMap<String,Integer>
		((o1,o2)-> Integer.parseInt(o1.split("-")[1])-Integer.parseInt(o2.split("-")[1]));
		String line = null;
		while((line = br.readLine()) != null) {
			String[] v = line.split("\t"); //1987-10 121313
			map.put(v[0].trim(), Integer.parseInt(v[1].trim()));									
		}
		request.setAttribute("map", map);
		String view = request.getParameter("view");
		if(view == null) view="1";
		RequestDispatcher v=request.getRequestDispatcher("/dataexpo/dataexpo"+view+".jsp");
		v.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
