package it.elsalamander.jpanel.all.getters;

import it.elsalamander.jpanel.all.PanelNavigation;
import it.elsalamander.jpanel.all.PanelPlugin;
import it.elsalamander.jpanel.all.PanelSessions;
import it.elsalamander.jpanel.all.Utils.HandlebarsTemplateEngine;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import java.io.File;
import java.util.HashMap;
import static spark.Spark.*;

/*********************************************************************
 * Base per i getter
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v1.0.0
 * 
 *********************************************************************/
public abstract class GetterBase{
	private PanelPlugin plugin;
	private PanelSessions sessions;
	private String template;
	private HashMap<String, Object> templateMap = new HashMap<String, Object>();
	
	public GetterBase(String path, PanelPlugin plugin){
		sessions = PanelSessions.getInstance();
		get(path, (request,response) -> getText(request, response));
	}
	
	public GetterBase(String path, String template, PanelPlugin plugin){
		sessions = PanelSessions.getInstance();
		this.plugin = plugin;
		this.template = template;
		get(path, (request,response) -> getPage(request, response), new HandlebarsTemplateEngine());
	}
	
	public GetterBase(String path, File template, PanelPlugin plugin){
		sessions = PanelSessions.getInstance();
		this.plugin = plugin;
		this.template = template.getName();
		get(path, (request,response) -> getPage(request, response), new HandlebarsTemplateEngine(template));
	}
	
	protected Object getText(Request request,Response response) throws Exception{
		throw new Exception("Not Implemented");
	}
	
	protected ModelAndView getPage(Request request,Response response){
		String version = getPlugin().getServer().getVersion();
		getTemplateMap().put("version", version);
		
		if(request.cookie("theme") != null){
			boolean dark = request.cookie("theme").equals("dark");
			getTemplateMap().put("dark", dark);
		}
		
		getTemplateMap().put("header", PanelNavigation.getInstance().generate());
		
		if(sessions.isLoggedIn(request.cookie("loggedin"))){
			return new ModelAndView(getTemplateMap(), getTemplate());
		}else{
			return new ModelAndView(getTemplateMap(), "login.hbs");
		}
	}
	
	public String getTemplate(){
		return template;
	}
	
	public HashMap<String, Object> getTemplateMap(){
		return templateMap;
	}
	
	public boolean isLoggedIn(String token){
		return sessions.isLoggedIn(token);
	}
	
	public PanelPlugin getPlugin(){
		return plugin;
	}
	
	public void setPlugin(PanelPlugin plugin){
		this.plugin = plugin;
	}
}
