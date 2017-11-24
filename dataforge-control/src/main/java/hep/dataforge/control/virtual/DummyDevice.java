package hep.dataforge.control.virtual;

import hep.dataforge.context.Context;
import hep.dataforge.control.devices.AbstractDevice;
import hep.dataforge.exceptions.ControlException;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;

public class DummyDevice extends AbstractDevice {
    public DummyDevice(Context context, Meta meta) {
        super(context, meta);
    }

    @Override
    protected void requestStateChange(String stateName, Value value) throws ControlException {

    }

    @Override
    protected Object computeState(String stateName) throws ControlException {
        return null;
    }
}
