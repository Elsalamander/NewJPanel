package it.elsalamander.jpanel.all.posters;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import it.elsalamander.jpanel.all.PanelPlugin;
import spark.Request;
import spark.Response;

import java.io.File;
import java.util.*;

/*********************************************************************
 * Gestione file
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v1.0.0
 * 
 *********************************************************************/
public class FileManager extends PosterBase{
	
	/**
	 * Costruttore
	 * 
	 * @param path
	 * @param plugin
	 */
	public FileManager(String path, PanelPlugin plugin){
		super(path);
	}
	
	@Override
	public Object getResponse(Request request,Response response){
		if(!isLoggedIn(request.cookie("loggedin")))
			return 0;
		
		HashMap<String, Comparable<?>> responseMap = new HashMap<String, Comparable<?>>();
		
		JsonObject requestJson = (JsonObject) JsonParser.parseString(request.body());
		File file = null;
		
		if(requestJson.has("target")){
			file = new File(new File(".").getAbsolutePath() + "/" + requestJson.get("target").getAsString());
		}
		
		if(file == null || !file.exists()){
			responseMap.put("success", false);
			responseMap.put("reason", "File " + file.getName() + " does not exist");
		}else if(requestJson.has("action")){
			String action = requestJson.get("action").getAsString();
			if(action.equalsIgnoreCase("remove")){
				if(file.delete()){
					responseMap.put("success", true);
				}else{
					responseMap.put("success", false);
					responseMap.put("reason", "File could not be deleted");
				}
			}else if(action.equalsIgnoreCase("rename")){
				if(!requestJson.has("value")){
					responseMap.put("success", false);
					responseMap.put("reason", "No new name given");
				}else{
					String value = requestJson.get("value").getAsString();
					File newFile = new File(file.getParent() + "/" + value);
					if(file.renameTo(newFile)){
						responseMap.put("success", true);
					}else{
						responseMap.put("success", false);
						responseMap.put("reason", "File could not be renamed");
					}
				}
			}
		}
		
		return new GsonBuilder().setPrettyPrinting().create().toJson(responseMap);
	}
	
}
