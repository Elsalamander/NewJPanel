package it.elsalamander.jpanel.spigot.managerPlayer;

import org.bukkit.plugin.java.JavaPlugin;

import it.elsalamander.jpanel.all.PanelPlugin;

/*********************************************************************
 * 
 * 
 * 
 * @author: Elsalamander
 * @data: 16 set 2022
 * @version: v1.0.0
 * 
 *********************************************************************/
public class ManagePlayer{
	
	private static ManagePlayer instance;
	private PanelPlugin plugin;
	
	/**
	 * @param plugin
	 */
	private ManagePlayer(PanelPlugin plugin){
		super();
		this.plugin = plugin;
	}

	public void managePlayer(String name,String action,String message){
		synchronized(this.plugin) {
			JavaPlugin pl = (JavaPlugin)this.plugin.getServer();
			pl.getServer().getScheduler().runTask(pl, () ->{
				if(action.equalsIgnoreCase("kick"))
					pl.getServer().getPlayer(name).kickPlayer(message);
				
				if(action.equalsIgnoreCase("ban")){
					//this.plugin.getServer().getPlayer(name).ban();
					pl.getServer().getPlayer(name).kickPlayer(message);
				}
			});
		}
	}

	/**
	 * Crea una istanza
	 * @param plugin
	 * @return
	 */
	public static ManagePlayer createInstance(PanelPlugin plugin) {
		instance = new ManagePlayer(plugin);
		return instance;
	}
	
	/**
	 * ottieni l'istanza
	 * @return
	 */
	public static ManagePlayer getInstance(){
		return instance;
	}
}
