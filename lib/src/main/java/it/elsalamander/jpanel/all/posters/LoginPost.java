package it.elsalamander.jpanel.all.posters;

import it.elsalamander.jpanel.all.Utils.PasswordHash;
import spark.Request;
import spark.Response;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/*********************************************************************
 * Schermata di login
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v1.0.0
 * 
 *********************************************************************/
public class LoginPost extends PosterBase {
    private Logger logger;

    public LoginPost(String path, Logger logger) {
        super(path);
        this.logger = logger;
    }

    @Override
	public Object getResponse(Request request, Response response) {
        String username = request.raw().getParameter("username");
        String password = request.raw().getParameter("password");

        try{
        	String user = getSessions().getPasswordForUser(username);
        	if(user == null) {
        		logger.log(Level.INFO, "Accesso fallito con user: " + username + " , password inserita:" + password + " ,IP: " + request.ip());
        	}else {
        		if(PasswordHash.validatePassword(password, user)) {
                    UUID sessionId = UUID.randomUUID();
                    getSessions().addSession(sessionId.toString(), username);
                    logger.log(Level.INFO, "JPanel user " + username + " logged in! IP: " + request.ip());
                    response.cookie("loggedin", sessionId.toString(), 3600);
                } else {
                    logger.log(Level.INFO, "Someone failed to login with the user " + username + "! IP: " + request.ip());
                }
        	}
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        response.redirect("/");
        return 0;
    }
}
