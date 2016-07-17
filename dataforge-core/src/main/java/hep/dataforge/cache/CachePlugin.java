/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.cache;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;
import hep.dataforge.meta.Meta;
import java.io.File;

/**
 *
 * @author Alexander Nozik
 */
@PluginDef(name = "dataCache", group = "hep.dataforge", description = "Data caching plugin")
public class CachePlugin extends BasicPlugin {

    public static CachePlugin buildFrom(Context ctx) {
        if (ctx.pluginManager().hasPlugin("dataCache")) {
            return ctx.provide("dataCache", CachePlugin.class);
        } else {
            CachePlugin plugin = new CachePlugin();
            ctx.pluginManager().loadPlugin(plugin);
            return plugin;
        }
    }

    private DataCache cache;

    public DataCache getCache() {
        if (cache == null) {
            cache = buildDefaultCache();
        }
        return cache;
    }

    private DataCache buildDefaultCache() {
        return new LocalFileDataCache(new File(getContext().io().getTmpDirectory(), ".cache"));
    }

    @Override
    protected void applyConfig(Meta config) {
        super.applyConfig(config);
        //TODO create custom cache here
    }

}
