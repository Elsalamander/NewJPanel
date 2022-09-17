package it.elsalamander.jpanel.all;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*********************************************************************
 * Navigazione fra i pannelli
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v2.0.1
 * 
 ********************************************************************/
public class PanelNavigation{
	static PanelNavigation panelNavigation = new PanelNavigation();
	
	private List<Map<String, String>> paths = new ArrayList<>();
	private List<Map<String, String>> externalPaths = new ArrayList<>();
	
	private PanelNavigation(){
		
	}
	
	/**
	 * Ottieni istanza per la navigazione
	 * 
	 * @return
	 */
	public static PanelNavigation getInstance(){
		if(panelNavigation == null)
			panelNavigation = new PanelNavigation();
		return panelNavigation;
	}
	
	/**
	 * Registra un path di navigazione
	 * 
	 * @param path
	 * @param name
	 */
	public void registerPath(String path,String name){
		HashMap<String, String> pathMap = new HashMap<>();
		pathMap.put("path", path);
		pathMap.put("name", name);
		
		paths.add(pathMap);
	}
	
	/**
	 * registra un path esterno
	 * 
	 * @param path
	 * @param name
	 */
	public void registerExternalPath(String path,String name){
		HashMap<String, String> pathMap = new HashMap<>();
		pathMap.put("path", path);
		pathMap.put("name", name);
		
		externalPaths.add(pathMap);
	}
	
	/**
	 * Genera la stringa rappresentativa
	 * 
	 * @return
	 */
	public String generate(){
		try{
			TemplateLoader loader = new ClassPathTemplateLoader();
			loader.setPrefix("/templates");
			loader.setSuffix(".hbs");
			
			Handlebars handlebars = new Handlebars(loader);
			Template template = handlebars.compile("header");
			
			HashMap<String, Object> pathsMap = new HashMap<>();
			pathsMap.put("paths", paths);
			pathsMap.put("extras", externalPaths);
			pathsMap.put("isExtras", !externalPaths.isEmpty());
			String header = template.apply(pathsMap);
			return header;
		}catch(Exception e){
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			return "Error - see console";
		}
	}
	
}
