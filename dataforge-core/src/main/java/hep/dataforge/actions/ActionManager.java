/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.actions;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;
import hep.dataforge.description.ActionDescriptor;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import hep.dataforge.context.Encapsulated;

/**
 *
 * @author Alexander Nozik
 */
@PluginDef(name = "actions", group = "hep.dataforge", description = "A list of available actions for given context")
public class ActionManager extends BasicPlugin implements Encapsulated {

    public static ActionManager buildFrom(Context context) {
        return (ActionManager) context.pluginManager().getPlugin("actions");
    }

    private Context context;

    @Override
    public void apply(Context context) {
        this.context = context;
    }

    @Override
    public void clean(Context context) {
        this.context = null;
    }

    private final Map<String, ActionFactory> actionMap = new HashMap<>();

    protected ActionManager getParent() {
        if (getContext() == null || getContext().getParent() == null || !context.getParent().provides("actions")) {
            return null;
        } else {
            return getContext().provide("actions", ActionManager.class);
        }
    }

    public Action buildAction(String name, Context context, Meta a)
            throws NameNotFoundException {
        return getActionFactory(name).build(context, a);
    }

    private boolean hasAction(String name) {
        return actionMap.containsKey(name) || (getParent() != null && getParent().hasAction(name));
    }

    private ActionFactory getActionFactory(String name) {
        if (actionMap.containsKey(name)) {
            return actionMap.get(name);
        } else {
            ActionManager parent = getParent();
            if (parent == null) {
                //TODO сделать ActionNotFoundException
                throw new NameNotFoundException(name);
            } else {
                return parent.getActionFactory(name);
            }
        }
    }

    public void registerAction(String name, ActionFactory actionFactory) {
        if (actionMap.containsKey(name)) {
            LoggerFactory.getLogger(getClass()).warn("Duplicate action names in ActionBuilder.");
        } else {
            actionMap.put(name, actionFactory);
        }
    }

    /**
     * Регестрирует действие, используя только его класс. Для этого обязательно
     * должна присутствовать аннотация ActionDescription
     *
     * @param actionClass a {@link java.lang.Class} object.
     */
    public void registerAction(Class<? extends Action> actionClass) {
        ActionDescriptor descr = ActionDescriptor.build(actionClass);
        String actionName = descr.getName();
        try {
            actionClass.getDeclaredConstructor(Context.class, Meta.class);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("Action must have default constructor (context, annotation) to be registered.");
        }

        ActionFactory factory = (Context factoryContext, Meta annotation) -> {
            try {
                return actionClass.getDeclaredConstructor(Context.class, Meta.class)
                        .newInstance(factoryContext, annotation);
            } catch (Exception ex) {
                throw new Error(ex);
            }
        };
        registerAction(actionName, factory);
    }

    /**
     * <p>
     * unRegisterAction.</p>
     *
     * @param actionType a {@link java.lang.String} object.
     */
    public void unRegisterAction(String actionType) {
        actionMap.remove(actionType);
    }

    /**
     * List all available actions in this context
     *
     * @return
     */
    public List<ActionDescriptor> listActions() {
        List<ActionDescriptor> list;
        if (getParent() != null) {
            list = getParent().listActions();
        } else {
            list = new ArrayList<>();
        }
        for (ActionFactory factory : this.actionMap.values()) {
            list.add(ActionDescriptor.build(factory.build(getContext(), null)));
        }

        Collections.sort(list, (ActionDescriptor o1, ActionDescriptor o2) -> o1.getName().compareTo(o2.getName()));
        return list;
    }

    /**
     * @return the context
     */
    @Override
    public Context getContext() {
        return context;
    }

}
