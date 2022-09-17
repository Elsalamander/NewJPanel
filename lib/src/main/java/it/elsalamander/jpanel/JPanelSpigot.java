package it.elsalamander.jpanel;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import it.elsalamander.jpanel.all.PanelNavigation;
import it.elsalamander.jpanel.all.PanelPlugin;
import it.elsalamander.jpanel.all.PanelUser;
import it.elsalamander.jpanel.all.ServerInterface;
import it.elsalamander.jpanel.all.getters.SimplePageGetter;
import it.elsalamander.jpanel.spigot.command.MyCommand;
import it.elsalamander.jpanel.spigot.event.MyListenerEvent;
import it.elsalamander.jpanel.spigot.getters.PlayerManagerPath;
import it.elsalamander.jpanel.spigot.getters.PlayersGetter;
import it.elsalamander.jpanel.spigot.getters.PlayersPageGetter;
import it.elsalamander.jpanel.spigot.getters.PluginsPageGetter;
import it.elsalamander.jpanel.spigot.managerPlayer.ManagePlayer;
import it.elsalamander.jpanel.spigot.posters.PlayerManagerPlus;

/*********************************************************************
 * Da capire meglio alcune cose.
 * - [x]: Trova i messaggio che non passa per il log.
 * - [x]: Fixa errore di compilazione.
 * - [x]: Fixa errore di chiusura. (stop -> awaitstop)
 * - [x]: Risolvi errori e warnig dovuti alla api del server e lagacy material
 * - [x]: Implementa oshii aggiungi la sua telemetria.
 * - [ ]: migliora i permessi presenti per l'utente della board.
 * - [ ]: migliora i commandi
 * 
 * - [ ]: aggiungi implementazione/classe astratta per avere un layout 
 *        di settings come pagina, alla pagina plugin.
 * - [ ]: aggiugin implementazione/classe astratta per avere un layout
 *        di informazione per gli utenti.
 *        
 * - [-]: fix vari.
 *   
 * - [x]: astrai un po tutto per avere questo plugin anche per il bungeecord
 * 		[x]: crea file bungee.yml
 * 		[x]: crea classe per spigot
 * 		[x]: crea classe per bungee
 * 		[x]: crea classe per tutte le cose in comune
 * 			 la parte in comune sara:
 * 			 - maggior parte delle pagine
 *           - compilazione
 *           - oshii
 *           - tutti i file che riguardano il sito
 *           
 * 
 * 
 * @author: Elsalamander
 * @data: 16 set 2022
 * @version: v2.0.1
 * 
 *********************************************************************/
public class JPanelSpigot extends JavaPlugin implements ServerInterface{
	
	private static JPanelSpigot instance;
	
	private PanelPlugin generic;
	private ManagePlayer managerPlayer;
	private FileConfiguration config;
	
	public void onLoad() {
		instance = this;
		this.generic = new PanelPlugin(this);
		this.managerPlayer = ManagePlayer.getInstance(); 
		
		this.generic.onLoad();
	}
	
	public void onEnable() {
		this.generic.onEnable();
		
		// Creazione pagine sito
		this.createPage();
		
		//crea le navigazioni
		this.createNavigation();
				
		//registra eventi
		this.registerEvents();
		
		//registra commandi
		this.registerCommand();
	}
	
	public void onDisable() {
		this.generic.onDisable();
	}

	/**
	 * @return the instance
	 */
	public static JPanelSpigot getInstance(){
		return instance;
	}

	@Override
	public void startTaskSync(Runnable runnable){
		getServer().getScheduler().scheduleSyncRepeatingTask(this, runnable, 100L, 1L);
	}
	
	private void createPage(){
		this.generic.createPage();
		
		new PluginsPageGetter("/plugins", "plugins.hbs", this.generic);
		
		// se è presente la Vault aggiungi anche:
		if(getServer().getPluginManager().isPluginEnabled("Vault")){
			new SimplePageGetter("/players", "playersplus.hbs", this.generic);
			new PlayerManagerPlus("/players", this.generic);
		}else{
			new PlayersPageGetter("/players", "players.hbs", this.generic);
			new PlayersGetter("/players", this.generic);
			new PlayerManagerPath("/player/:name/:action", this.generic);
		}
	}
	
	private void createNavigation() {
		//navigazione tra le pagine
		this.generic.createNavigation();
		
		PanelNavigation nav = this.generic.getNav();
		nav.registerPath("/players", "Players");
		nav.registerPath("/plugins", "Plugins");
	}
	
	public ManagePlayer getManagerPlayer() {
		return this.managerPlayer;
	}
	
	/**
	 * Registra eventi
	 */
	private void registerEvents() {
		PluginManager manager = this.getServer().getPluginManager();
		manager.registerEvents(new MyListenerEvent(), this);
	}
	
	/**
	 * Registra i commandi
	 */
	private void registerCommand(){
		MyCommand exec = new MyCommand();
		this.getCommand("jpanel").setExecutor(exec);
		this.getCommand("addlogin").setExecutor(exec);
		this.getCommand("passwd").setExecutor(exec);
	}

	@Override
	public void loadMyConfig(){
		config = getConfig();
		
		// carica i profili
		if(config.isConfigurationSection("users")){
			// carica tutti gli utenti
			for(String key : config.getConfigurationSection("users").getKeys(false)){
				String password = config.getString("users." + key + ".password");
				boolean canEditFiles = config.getBoolean("users." + key + ".canEditFiles", false);
				boolean canChangeGroups = config.getBoolean("users." + key + ".canChangeGroups", false);
				boolean canSendCommands = config.getBoolean("users." + key + ".canSendCommands", false);
				PanelUser user = new PanelUser(password, canEditFiles, canChangeGroups, canSendCommands);
				this.generic.getSession().addUser(key, user);
			}
		}
		
		// impostazioni http-port
		config.set("http-port", config.get("http-port", this.generic.getHttpPort()));
		
		config.set("debug-mode", config.get("debug-mode", this.generic.getDebugMode()));
		
		config.set("use-ssl", config.get("use-ssl", this.generic.isUseSsl()));
		config.set("keystore-name", config.get("keystore-name", this.generic.getKeystorePath()));
		config.set("keystore-password", config.get("keystore-password", this.generic.getKeystorePassword()));
		
		this.generic.setHttpPort(config.getInt("http-port"));
		this.generic.setDebugMode(config.getBoolean("debug-mode"));
		this.generic.setUseSsl(config.getBoolean("use-ssl"));
		
		//path
		this.generic.setKeystorePath(getDataFolder() + "/" + config.getString("keystore-name"));
		this.generic.setKeystorePassword(config.getString("keystore-password"));
	}

	@Override
	public void saveMyConfig(){
		super.saveConfig();
	}

	@Override
	public String getVersion(){
		return super.getDescription().getVersion();
	}

	@Override
	public ServerType getType(){
		return ServerType.Spigot;
	}

	@Override
	public void sendCommand(String message){
		final JavaPlugin pl = (JavaPlugin) this.generic.getServer();
		new BukkitRunnable(){
			@Override
			public void run(){
				pl.getServer().dispatchCommand(pl.getServer().getConsoleSender(), message);
			}
		}.runTask(pl);
	}
}
