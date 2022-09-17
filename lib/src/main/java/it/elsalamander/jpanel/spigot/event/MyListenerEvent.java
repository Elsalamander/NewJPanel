package it.elsalamander.jpanel.spigot.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

import it.elsalamander.jpanel.all.PanelNavigation;
import it.elsalamander.jpanel.all.PanelPlugin;
import it.elsalamander.jpanel.all.getters.SimplePageGetter;
import it.elsalamander.jpanel.spigot.posters.PlayerManagerPlus;

public class MyListenerEvent implements Listener{
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginEnable(PluginEnableEvent event){
		String pluginName = event.getPlugin().getName();
		
		if(pluginName.equals("Vault")){
			PanelNavigation.getInstance().registerPath("/perms", "Permissions");
			new SimplePageGetter("/perms", "playersplus.hbs", PanelPlugin.getInstance());
			new PlayerManagerPlus("/permissions", PanelPlugin.getInstance());
		}
		
	}
}
