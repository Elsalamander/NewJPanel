package it.elsalamander.jpanel.all.getters;

import it.elsalamander.jpanel.all.PanelSessions;
import spark.Request;
import spark.Response;

/*********************************************************************
 * Panello di logout
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v1.0.0
 * 
 ********************************************************************/
public class LogoutPath extends GetterBase {
	
    public LogoutPath(String path) {
        super(path, null);
    }

    @Override
    protected Object getText(Request request, Response response) {
        if (!isLoggedIn(request.cookie("loggedin")))
            return 0;

        PanelSessions.getInstance().removeSession(request.cookie("loggedin"));

        response.redirect("/");
        return 0;
    }
}
