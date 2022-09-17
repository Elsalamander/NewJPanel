package it.elsalamander.jpanel.spigot.getters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.elsalamander.jpanel.all.PanelPlugin;
import it.elsalamander.jpanel.all.getters.GetterBase;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*********************************************************************
 * 
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v1.0.0
 * 
 ********************************************************************/
public class PlayersGetter extends GetterBase {

    public PlayersGetter(String path, PanelPlugin plugin) {
        super(path, plugin);
        setPlugin(plugin);
    }

    @Override
    protected Object getText(Request request, Response response) throws Exception {
        if (!isLoggedIn(request.cookie("loggedin")))
            return 0;

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        HashMap<String, List<Map<String, Comparable<?>>>> list = new HashMap<String, List<Map<String, Comparable<?>>>>();
        List<Map<String, Comparable<?>>> names = new ArrayList<>();

        for (Player p : ((JavaPlugin)getPlugin().getServer()).getServer().getOnlinePlayers()) {
			Map<String, Comparable<?>> playerMap = new HashMap<String, Comparable<?>>();
			playerMap.put("name", p.getName());
			playerMap.put("health", p.getHealth());
			names.add(playerMap);
        }

        list.put("players", names);
        return gson.toJson(list);

    }
}
