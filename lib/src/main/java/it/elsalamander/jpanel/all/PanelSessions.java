package it.elsalamander.jpanel.all;

import java.util.HashMap;

/*********************************************************************
 * Sessioni aperte
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v1.0.0
 * 
 *********************************************************************/
public class PanelSessions{
	static PanelSessions panelSessions = new PanelSessions();
	
	
	//First string is the session UUID, second is the username
	private HashMap<String, String> sessions = new HashMap<>();
	private HashMap<String, PanelUser> users = new HashMap<>(); //userName - sessione
	
	/**
	 * Costruttore di istanza
	 */
	private PanelSessions(){
	}
	
	/**
	 * Istanza
	 * @return
	 */
	public static PanelSessions getInstance(){
		if(panelSessions == null)
			panelSessions = new PanelSessions();
		return panelSessions;
	}
	
	/**
	 * Aggiungi un utente
	 * @param key
	 * @param user
	 */
	public void addUser(String nameUser, PanelUser user){
		users.put(nameUser, user);
	}
	
	/**
	 * Controlla se l'utente è dentro
	 * @param loggedin - UUID sessione
	 * @return
	 */
	public boolean isLoggedIn(String sessionUuid){
		if(sessions == null) {
			return false;
		}
		return sessions.containsKey(sessionUuid);
	}
	
	/**
	 * Ottieni la password dell'utente
	 * @param username
	 * @return
	 */
	public String getPasswordForUser(String nameUser){
		PanelUser user = users.get(nameUser);
		if(user == null) {
			return null;
		}
		return user.password;
	}
	
	/**
	 * Aggiungi una sessione
	 * @param sessionKey - UUID session
	 * @param username
	 */
	public void addSession(String sessionUuid, String nameUser){
		sessions.put(sessionUuid, nameUser);
	}
	
	/**
	 * Rimuovi sessione
	 * @param sessionKey - UUID sessione
	 */
	public void removeSession(String sessionUuid){
		sessions.remove(sessionUuid);
	}
	
	/**
	 * Ritorna il panelUser dall'uuid della sessione
	 * @param loggedin
	 * @return
	 */
	public PanelUser getAuthedUser(String sessionUuid){
		String user = sessions.get(sessionUuid);
		return users.get(user);
	}
	
	/**
	 * Ottieni il panello dato il nome utente
	 * @param nameUser
	 * @return
	 */
	public PanelUser getUser(String nameUser){
		return users.get(nameUser);
	}
	
	/**
	 * Distruggi l'istanza
	 */
	public void destroy(){
		this.sessions = null;
		this.users = null;
		panelSessions = null;
	}
	
	/**
	 * Ottieni il nome dato SessionUuid
	 * @param sessionUuid
	 * @return
	 */
	public String getAuthedUsername(String sessionUuid){
		return sessions.get(sessionUuid);
	}
}
