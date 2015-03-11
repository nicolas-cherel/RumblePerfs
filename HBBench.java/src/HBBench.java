import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

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
	
	void compile() {
		try {
			hb.compileInline(this.stringTemplate);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void prepareContent() throws IOException {

		byte[] content = Files.readAllBytes(Paths.get("../data/template.hbs"));
		String tContent = new String(content, StandardCharsets.UTF_8);
		byte[] dataSource = Files.readAllBytes(Paths.get("../data/template.json"));
		String dataContent = new String(dataSource, StandardCharsets.UTF_8);
		
		this.t = hb.compileInline(tContent);
		this.data = new ObjectMapper().readValue(dataContent, List.class);
	}
	
	public void expansion() {
		StringWriter w = new StringWriter();
		try {
			this.t.apply(this.data, w);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public static <T> void measure(HashMap<String, Stats> stats, String setName, Function<Void, Void> c)  {
    	{
    		long start = System.nanoTime();
    		c.apply(null);
    		stats.get(setName).setCold(System.nanoTime() - start);    		
    	}
    	
    	
    	for (int i = 0; i < 1000; i++) {
    		// warm up
    		c.apply(null);
    	}
    	
    	{
    		int iterations = 50;
    		long start = System.nanoTime();
    		long max = Long.MIN_VALUE;
    		long min = Long.MAX_VALUE;
    		for (int i = 0; i < iterations; i++) {
    			long setpStart  = System.nanoTime();
        		c.apply(null);
    			max = Math.max(max, System.nanoTime() - setpStart);
    			min = Math.min(min, System.nanoTime() - setpStart);
    		}
    		stats.get(setName).setAvg((System.nanoTime() - start)/iterations);
    		stats.get(setName).setDelta(max - min);
    	}
	}
	
    public static void main(String[] args) throws IOException {
    	HBBench b = new HBBench();
    	HashMap<String, Stats> stats = new HashMap<String, Stats>();
    	stats.put("compile", new Stats());
    	stats.put("expansion", new Stats());

    	b.prepareCompile();
    	measure(stats, "compile", (Void) -> { b.compile(); return null; });
    	
    	b.prepareContent();
    	measure(stats, "expansion", (Void) -> { b.expansion(); return null; });


    	System.out.println(stats);    	
    }


}
