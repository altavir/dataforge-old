package hep.dataforge.osgi;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;
import hep.dataforge.meta.Meta;
import org.apache.felix.framework.Felix;
import org.osgi.framework.BundleException;

import java.util.HashMap;
import java.util.Map;

/**
 * An OSGI framework based on Apache Felix implementation
 * Created by darksnake on 02-Dec-16.
 */
@PluginDef(name = "osgi", group = "hep.dataforge", description = "OSGI platform support")
public class OSGIPlugin extends BasicPlugin {
    Felix felix;

    @Override
    public void attach(Context context) {
        super.attach(context);
        Map<String, String> properties = new HashMap<>();
        felix = new Felix(properties);
        try {
            felix.start();
        } catch (BundleException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void detach() {
        super.detach();
    }

    @Override
    protected void applyConfig(Meta config) {
        super.applyConfig(config);
    }


}
