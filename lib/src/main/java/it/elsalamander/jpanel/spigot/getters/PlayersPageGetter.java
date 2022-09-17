package it.elsalamander.jpanel.spigot.getters;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import it.elsalamander.jpanel.all.PanelPlugin;
import it.elsalamander.jpanel.all.getters.GetterBase;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*********************************************************************
 * 
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v1.0.0
 * 
 *********************************************************************/
public class PlayersPageGetter extends GetterBase{
	
	public PlayersPageGetter(String path, String template, PanelPlugin plugin){
		super(path, template, plugin);
	}
	
	@Override
	protected ModelAndView getPage(Request request,Response response){
		List<Map<String, Comparable<?>>> names = new ArrayList<Map<String, Comparable<?>>>();
		
		for(Player p : ((JavaPlugin)getPlugin().getServer()).getServer().getOnlinePlayers()){
			Map<String, Comparable<?>> playerMap = new HashMap<String, Comparable<?>>();
			playerMap.put("name", p.getName());
			playerMap.put("health", p.getHealth());
			names.add(playerMap);
		}
		getTemplateMap().put("players", names);
		return super.getPage(request, response);
	}
}
