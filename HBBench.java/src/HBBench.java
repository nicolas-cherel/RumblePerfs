import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;



public class HBBench {
	
	String stringTemplate;
	Handlebars hb;
	Template t;
	Object data;
	
	void prepareCompile() throws IOException {
		this.hb = new Handlebars();
		byte[] content = Files.readAllBytes(Paths.get("../data/template.hbs"));
		this.stringTemplate = new String(content, StandardCharsets.UTF_8);
	}
	
	void compile() throws IOException {
		hb.compileInline(this.stringTemplate);
	}

	void prepareContent() throws IOException {

		byte[] content = Files.readAllBytes(Paths.get("../data/template.hbs"));
		String tContent = new String(content, StandardCharsets.UTF_8);
		byte[] dataSource = Files.readAllBytes(Paths.get("../data/template.json"));
		String dataContent = new String(dataSource, StandardCharsets.UTF_8);
		
		this.t = hb.compileInline(tContent);
		this.data = new ObjectMapper().readValue(dataContent, List.class);
	}
	
	public void expansion() throws IOException {
		StringWriter w = new StringWriter();
		this.t.apply(this.data, w);
	}
	
	static class Stats {
		private long cold;
		private long avg;
		private long delta;
		
		long getDelta() {
			return delta;
		}
		
		void setDelta(long delta) {
			this.delta = delta;
		}
		
		long getAvg() {
			return avg;
		}
		
		void setAvg(long avg) {
			this.avg = avg;
		}
		
		long getCold() {
			return cold;
		}
		
		void setCold(long cold) {
			this.cold = cold;
		}
		
		public String toString() {
			return "cold " + this.cold + "ns " + "avg " + this.avg + "ns " + "(± " + this.delta + "ns)";
		}
		
	}
	
    public static void main(String[] args) throws IOException {
    	HBBench b = new HBBench();
    	HashMap<String, Stats> stats = new HashMap<String, Stats>();
    	stats.put("compile", new Stats());
    	stats.put("expansion", new Stats());

    	b.prepareCompile();
    	
    	{
    		long start = System.nanoTime();
    		b.compile();
    		stats.get("compile").setCold(System.nanoTime() - start);    		
    	}
    	
    	
    	for (int i = 0; i < 10; i++) {
    		// warm up
    		b.compile();
    	}
    	
    	{    		
    		long start = System.nanoTime();
    		long max = Long.MIN_VALUE;
    		long min = Long.MAX_VALUE;
    		for (int i = 0; i < 10; i++) {
    			long setpStart  = System.nanoTime();
    			b.compile();
    			max = Math.max(max, System.nanoTime() - setpStart);
    			min = Math.min(min, System.nanoTime() - setpStart);
    		}
    		stats.get("compile").setAvg((System.nanoTime() - start)/10);
    		stats.get("compile").setDelta(max - min);
    	}
    	
    	b.prepareContent();
    	

    	{
    		long start = System.nanoTime();
    		b.expansion();
    		stats.get("expansion").setCold(System.nanoTime() - start);    		
    	}
    	
    	for (int i = 0; i < 10; i++) {
    		// warm up
    		b.expansion();
    	}
    	
    	
    	{
    		long start = System.nanoTime();
    		long max = Long.MIN_VALUE;
    		long min = Long.MAX_VALUE;
    		for (int i = 0; i < 10; i++) {
    			long setpStart  = System.nanoTime();
    			b.expansion();
    			max = Math.max(max, System.nanoTime() - setpStart);
    			min = Math.min(min, System.nanoTime() - setpStart);
    		}
    		stats.get("expansion").setAvg((System.nanoTime() - start)/10);
    		stats.get("expansion").setDelta(max - min);
    	}

    	System.out.println(stats);    	
    }


}
