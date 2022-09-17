package it.elsalamander.jpanel.spigot.getters;

import it.elsalamander.jpanel.JPanelSpigot;
import it.elsalamander.jpanel.all.PanelPlugin;
import it.elsalamander.jpanel.all.getters.GetterBase;
import spark.Request;
import spark.Response;

/*********************************************************************
 * 
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v1.0.0
 * 
 *********************************************************************/
public class PlayerManagerPath extends GetterBase{
	public PlayerManagerPath(String path, PanelPlugin plugin){
		super(path, plugin);
		setPlugin(plugin);
	}
	
	@Override
	protected Object getText(Request request,Response response) throws Exception{
		if(!isLoggedIn(request.cookie("loggedin")))
			return 0;
		
		String msg = (request.params(":action").equals("kick")) ? "Kicked!" : "Banned!";
		
		((JPanelSpigot) getPlugin().getServer()).getManagerPlayer().managePlayer(request.params(":name"), request.params(":action"),msg);
		
		return "OK";
	}
}
