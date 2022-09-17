package it.elsalamander.jpanel.all;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.net.HttpCookie;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/*********************************************************************
 * 
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v1.0.0
 * 
 ********************************************************************/
@WebSocket
public class ConsoleSocket{
	private static final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
	
	private final PanelSessions sessions;
	private PanelPlugin plugin;
	private ArrayList<String> oldMsgs = new ArrayList<>();
	
	private ConcurrentHashMap<Session, String> sockets = new ConcurrentHashMap<>();
	
	private static ConsoleSocket instance;
	public static ConsoleSocket getInstance() {
		return instance;
	}
	
	/**
	 * Creazione socket
	 * @throws UnknownHostException
	 */
	public ConsoleSocket() throws UnknownHostException{
		this.plugin = PanelPlugin.getInstance();
		sessions = PanelSessions.getInstance();
		
		logger.addAppender(new LogHandlerLog4J());
		
		Handler handler = new LogHandler();
		plugin.getServerLogger().addHandler(handler);
		
		plugin.getServerLogger().info("[JPanel] WebSocket started");
		instance = this;
	}
	
	@OnWebSocketConnect
	public void connected(Session session) throws IOException{
		List<HttpCookie> cookies = session.getUpgradeRequest().getCookies();
		String token = "";
		
		for(HttpCookie cookie : cookies){
			if(cookie.getName().equalsIgnoreCase("loggedin")){
				token = cookie.getValue();
			}
		}
		
		if(sessions.isLoggedIn(token)){
			sockets.put(session, sessions.getAuthedUsername(token));
			session.getRemote().sendString("SCROLLBACK " + oldMsgs.size());
			for(String msg : oldMsgs){
				session.getRemote().sendString(msg);
			}
			session.getRemote().sendString("Connected!");
		}else{
			session.getRemote().sendString("Failed to authenticate with the web socket!");
			session.close();
		}
	}
	
	@OnWebSocketClose
	public void onClose(Session session,int statusCode,String reason){
		sockets.remove(session);
	}
	
	@OnWebSocketMessage
	public void message(Session session,String message) throws IOException{
		String username = sockets.get(session);
		
		if(!sessions.getUser(username).canSendCommands){
			session.getRemote()
					.sendString("You're not allowed to send commands! Contact the server admin if this is in error.");
			return;
		}
		
		this.plugin.getServer().sendCommand(message);
		
		if(!message.contains("passwd"))
			plugin.getServerLogger().log(Level.INFO, "Console user " + username + " ran the command " + message);
	}
	
	public void appendMessage(String message) throws IOException{
		oldMsgs.add(message);
		if(oldMsgs.size() > 1000){
			oldMsgs.remove(0);
			oldMsgs.trimToSize();
		}
		for(Session socket : sockets.keySet()){
			socket.getRemote().sendString(message);
		}
	}
	
	/*********************************************************************
	 * 
	 * 
	 * 
	 * @author: Elsalamander
	 * @data: 15 set 2022
	 * @version: v2.0.0
	 * 
	 *********************************************************************/
	private class LogHandler extends Handler{

		@Override
		public void publish(LogRecord record){
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			Date date = new Date();
			String message = record.getMessage();
			try{
				appendMessage(dateFormat.format(date) + " [" + record.getLevel().toString() + "] " + message);
			}catch(IOException e){
				e.printStackTrace();
			}
		}

		@Override
		public void flush(){
		}

		@Override
		public void close() throws SecurityException{
		}
	}
	
	private class LogHandlerLog4J extends AbstractAppender {

        public LogHandlerLog4J() {
            super("RemoteController", null, null, false, Property.EMPTY_ARRAY );
            start();
        }

        @Override
        public void append(LogEvent event) {
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            String message = event.getMessage().getFormattedMessage();
			try{
				appendMessage(dateFormat.format(date) + " [" + event.getLevel().toString() + "] " + message);
			}catch (IOException e){
				e.printStackTrace();
			}
		}
    }
}
