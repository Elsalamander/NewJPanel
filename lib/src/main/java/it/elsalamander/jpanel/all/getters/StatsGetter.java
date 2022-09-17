package it.elsalamander.jpanel.all.getters;

import com.google.gson.Gson;

import it.elsalamander.jpanel.all.PanelPlugin;
import it.elsalamander.jpanel.all.Utils.Lag;
import oshi.software.os.OSProcess;
import spark.Request;
import spark.Response;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/*********************************************************************
 * metriche visualzizzate affianco alla console
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v1.0.0
 * 
 ********************************************************************/
public class StatsGetter extends GetterBase{
	
	public static final DecimalFormat df = new DecimalFormat("0.00");
	
	private OSProcess data;
	private int pid;
	
	public StatsGetter(String path){
		super(path, null);
		this.pid = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
		this.data = PanelPlugin.getInstance().getOsInfo().getProcess(this.pid);
		
	}
	
	@Override
	protected Object getText(Request request,Response response){
		if(!isLoggedIn(request.cookie("loggedin")))
			return 0;
		
		Gson gson = new Gson();
		
		//Get RAM
		//prima
		Runtime runtime = Runtime.getRuntime();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		//dopo con oshi
		double heap = this.data.getResidentSetSize();
		
		
		//Get CPU
		//prima
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		int processors = os.getAvailableProcessors();
		
		//system load is -1 if you use it with windows
		double usage = os.getSystemLoadAverage() / processors;
		long cpuUsage = Math.round(usage * 100.0D);
		//dopo con oshi
		double myCpuUsage = this.data.getProcessCpuLoadCumulative();
		
		
		// shove in a hashmap
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("total_A", (allocatedMemory / 1024));
		map.put("free_A", (freeMemory / 1024));
		map.put("cpu_A", cpuUsage);
		
		map.put("total_B", (allocatedMemory / 1024));
		map.put("free_B", (heap / 1024));
		map.put("cpu_B", (myCpuUsage));
	
		map.put("tps", Lag.getInstance().getTPS());
		
		return gson.toJson(map);
	}
}
