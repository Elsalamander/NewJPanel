package it.elsalamander.jpanel.all.posters;

import it.elsalamander.jpanel.all.Utils.PasswordHash;
import spark.Request;
import spark.Response;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/*********************************************************************
 * 
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v1.0.0
 * 
 *********************************************************************/
public class ClientLoginPost extends PosterBase{
	private Logger logger;
	
	public ClientLoginPost(String path, Logger logger){
		super(path);
		this.logger = logger;
	}
	
	@Override
	public Object getResponse(Request request,Response response){
		String username = request.raw().getParameter("username");
		String password = request.raw().getParameter("password");
		
		try{
			if(PasswordHash.validatePassword(password, getSessions().getPasswordForUser(username))){
				UUID sessionId = UUID.randomUUID();
				getSessions().addSession(sessionId.toString(), username);
				response.cookie("loggedin", sessionId.toString(), 3600);
				logger.log(Level.INFO, "JPanel user " + username + " logged in! IP: " + request.ip());
				return "SUCCESS: " + sessionId.toString();
			}else{
				logger.log(Level.INFO, "Someone failed to login with the user " + username + "! IP: " + request.ip());
				return "FAIL - PASSWORD INCORRECT";
			}
		}catch(Exception e){
			return "FAIL - " + e.getMessage();
		}
	}
}
