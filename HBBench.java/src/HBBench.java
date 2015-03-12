import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;



public class HBBench {
	
	String warmupCompile = "{{#this}} will not show {{missingContent}}{{! and comment}} "
			+ "but can expand this {{text}} {{else}}{{that}}{{/this}}"
			+ "{{#list}}found {{element}} in list {{/list}}";
	Template warmupTemplate = null;
	Object warmUpData = null;
	
	HashMap<String, String> stringTemplate;
	Handlebars hb;
	
	void prepareCompile() throws IOException {
		this.hb = new Handlebars();
		
		this.stringTemplate = new HashMap<String, String>();
		for(String f: Arrays.asList("template", "helper")) {
			byte[] content;
			content = Files.readAllBytes(Paths.get("../data/" + f +".hbs"));
			this.stringTemplate.put(f, new String(content, StandardCharsets.UTF_8));
		}

	}
	
	void warmupCompile() {
		try {
			hb.compileInline(this.warmupCompile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void compile(String s) {
		try {
			hb.compileInline(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	Template t;
	Object data;

	void prepareContent() throws IOException {
		this.warmUpData = new ObjectMapper().readTree("{\"text\": \"some string\", \"elemet\": [1, 2, 3, 4]}");
		this.warmupTemplate = hb.compileInline(this.warmupCompile);
		
		byte[] content = Files.readAllBytes(Paths.get("../data/template.hbs"));
		String tContent = new String(content, StandardCharsets.UTF_8);
		byte[] dataSource = Files.readAllBytes(Paths.get("../data/template.json"));
		String dataContent = new String(dataSource, StandardCharsets.UTF_8);
		
		this.t = hb.compileInline(tContent);
		this.data = new ObjectMapper().readValue(dataContent, List.class);
	}
	
	public void warmupExpansion() {
		StringWriter w = new StringWriter();
		try {
			this.warmupTemplate.apply(this.warmUpData, w);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public static <T> void measure(HashMap<String, Stats> stats, String setName, Consumer<Void> warmup, Consumer<Void> c)  {
    	{
    		long start = System.nanoTime();
    		c.accept(null);
    		stats.get(setName).setCold(System.nanoTime() - start);    		
    	}
    	
    	
    	for (int i = 0; i < 1000; i++) {
    		// warm up
    		warmup.accept(null);
    	}
    	
    	{
    		int iterations = 20;
    		long start = System.nanoTime();
    		long max = Long.MIN_VALUE;
    		long min = Long.MAX_VALUE;
    		for (int i = 0; i < iterations; i++) {
    			long setpStart  = System.nanoTime();
    			c.accept(null);
    			max = Math.max(max, System.nanoTime() - setpStart);
    			min = Math.min(min, System.nanoTime() - setpStart);
    		}
    		stats.get(setName).setAvg((System.nanoTime() - start)/iterations);
    		stats.get(setName).setDelta(max - min);
    	}
	}
	
    public static void main(String[] args) throws IOException {
    	HBBench b = new HBBench();
    	b.prepareCompile();
    	
    	for (Entry<String, String> t: b.stringTemplate.entrySet()) {
        	HashMap<String, Stats> stats = new HashMap<String, Stats>();
        	stats.put("compile", new Stats());
        	stats.put("expansion", new Stats());

        	measure(stats, "compile", (Void) -> b.warmupCompile(), (Void) -> b.compile(t.getValue()));
        	
        	b.prepareContent();
        	measure(stats, "expansion", (Void) -> b.warmupExpansion(), (Void) -> b.expansion());


        	System.out.println(t.getKey() + " " + stats);
    	}    	
    }


}
