package it.elsalamander.jpanel.all.posters;

import spark.Request;
import spark.Response;
import static spark.Spark.post;

import it.elsalamander.jpanel.all.PanelSessions;

/*********************************************************************
 * Base
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v1.0.0
 * 
 *********************************************************************/
public abstract class PosterBase{
	private PanelSessions sessions;
	private String template;
	
	public PosterBase(String path){
		sessions = PanelSessions.getInstance();
		post(path, this::getResponse);
	}
	
	public abstract Object getResponse(Request request,Response response);
	
	public String getTemplate(){
		return template;
	}
	
	public boolean isLoggedIn(String token){
		return sessions.isLoggedIn(token);
	}
	
	public PanelSessions getSessions(){
		return sessions;
	}
}
