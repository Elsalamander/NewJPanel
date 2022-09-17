package it.elsalamander.jpanel;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import it.elsalamander.jpanel.all.PanelPlugin;
import it.elsalamander.jpanel.all.PanelUser;
import it.elsalamander.jpanel.all.ServerInterface;
import it.elsalamander.jpanel.bungee.MyConfig;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

public class JPanelBungee extends Plugin implements ServerInterface{
	
private static JPanelBungee instance;
	
	private PanelPlugin generic;
	private MyConfig myConfig;
	
	public void onLoad() {
		instance = this;
		this.generic = new PanelPlugin(this);
		
		this.generic.onLoad();
	}
	
	public void onEnable() {
		this.myConfig = new MyConfig(this);
		
		this.generic.onEnable();
		
		// Creazione pagine sito
		this.createPage();
		
		//crea le navigazioni
		this.createNavigation();
	}
	
	public void onDisable() {
		super.getLogger().log(Level.INFO, "Chiusura JPanel");
		this.generic.onDisable();
	}

	/**
	 * @return the instance
	 */
	public static JPanelBungee getInstance(){
		return instance;
	}

	@Override
	public void startTaskSync(Runnable runnable){
		super.getProxy().getScheduler().schedule(instance, runnable, 100L, 1L, TimeUnit.SECONDS);
	}
	
	private void createPage(){
		this.generic.createPage();
		
		//nulla in più dello spigot
	}
	
	private void createNavigation() {
		//navigazione tra le pagine
		this.generic.createNavigation();

		//nulla in più dello spigot
	}
	
	@Override
	public void loadMyConfig(){
		Configuration config = myConfig.loadConfig();
		
		// carica i profili
		if(config.getSection("users") != null){
			// carica tutti gli utenti
			for(String key : config.getSection("users").getKeys()){
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
		this.myConfig.saveConfig();
	}

	@Override
	public String getVersion(){
		return super.getDescription().getVersion();
	}

	@Override
	public ServerType getType(){
		return ServerType.Bungee;
	}

	@Override
	public void sendCommand(String message){
		super.getProxy().getPluginManager().dispatchCommand(super.getProxy().getConsole(), message);
	}
}
