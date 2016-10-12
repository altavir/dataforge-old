package hep.dataforge.control;

import hep.dataforge.meta.Meta;

/**
 * Created by darksnake on 11-Oct-16.
 */
public class ControlUtils {
    public static String getDeviceType(Meta meta){
        return meta.getString("type");
    }

    public static String getDeviceName(Meta meta){
        //TODO add default
        return meta.getString("name");
    }
}
