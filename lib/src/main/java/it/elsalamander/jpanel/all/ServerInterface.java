package it.elsalamander.jpanel.all;

import java.io.File;
import java.util.logging.Logger;

import it.elsalamander.jpanel.ServerType;

public interface ServerInterface{
	
	/**
	 * Logger dove eseguire il log
	 * @return
	 */
	public Logger getLogger();
	
	/**
	 * File data
	 * @return
	 */
	public File getDataFolder();
	
	/**
	 * Avvia un task sincrono
	 * @param runnable
	 */
	public void startTaskSync(Runnable runnable);
	
	/**
	 * Carica i dati
	 */
	public void loadMyConfig();
	
	/**
	 * Salva i dati
	 */
	public void saveMyConfig();

	/**
	 * Versione plugin
	 * @return
	 */
	public String getVersion();
	
	/**
	 * Type server
	 * @return
	 */
	public ServerType getType();

	public void sendCommand(String message);
}
