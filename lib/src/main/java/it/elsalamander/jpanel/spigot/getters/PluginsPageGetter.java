package it.elsalamander.jpanel.spigot.getters;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import it.elsalamander.jpanel.all.PanelPlugin;
import it.elsalamander.jpanel.all.getters.GetterBase;
import spark.ModelAndView;
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
 *********************************************************************/
public class PluginsPageGetter extends GetterBase {
    public PluginsPageGetter(String path, String template, PanelPlugin plugin) {
        super(path, template, plugin);
    }

    @Override
    protected ModelAndView getPage(Request request, Response response) {
        List<Map<String, Object>> names = new ArrayList<Map<String, Object>>();

        for (Plugin p : ((JavaPlugin)getPlugin().getServer()).getServer().getPluginManager().getPlugins()) {
            Map<String, Object> pluginMap = new HashMap<String, Object>();
            pluginMap.put("name", p.getName());
            pluginMap.put("enabled", p.isEnabled());
            names.add(pluginMap);
        }

        getTemplateMap().put("plugins", names);
        return super.getPage(request, response);
    }

}