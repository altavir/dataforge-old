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
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 *
 * @author Alexander Nozik
 */
@PluginDef(name = "actions", group = "hep.dataforge", description = "A list of available actions for given context")
public class ActionManager extends BasicPlugin {

    private final Map<String, Action> actionMap = new HashMap<>();

    public static ActionManager buildFrom(Context context) {
        return context.getPlugin(ActionManager.class);
    }

    protected ActionManager getParent() {
        if (getContext() == null || getContext().getParent() == null || !getContext().getParent().provides("actions")) {
            return null;
        } else {
            return getContext().getParent().provide("actions", ActionManager.class);
        }
    }

    public boolean hasAction(String name) {
        return actionMap.containsKey(name) || (getParent() != null && getParent().hasAction(name));
    }

    public Action getAction(String name) {
        if (actionMap.containsKey(name)) {
            return actionMap.get(name);
        } else {
            ActionManager parent = getParent();
            if (parent == null) {
                //TODO сделать ActionNotFoundException
                throw new NameNotFoundException(name);
            } else {
                return parent.getAction(name);
            }
        }
    }

    private void putAction(Action action) {
        if (actionMap.containsKey(action.getName())) {
            LoggerFactory.getLogger(getClass()).warn("Duplicate action names in ActionManager.");
        } else {
            actionMap.put(action.getName(), action);
        }
    }

    /**
     * Регестрирует действие, используя только его класс. Для этого обязательно
     * должна присутствовать аннотация ActionDescription
     *
     * @param actionClass a {@link java.lang.Class} object.
     */
    public void registerAction(Class<? extends Action> actionClass) {
        try {
            putAction(actionClass.getDeclaredConstructor().newInstance());
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("Action must have default empty constructor to be registered.");
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException("Error while constructing Action", ex);
        }
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
        this.actionMap.values().stream().forEach((action) -> {
            list.add(ActionDescriptor.build(action));
        });

        Collections.sort(list, (ActionDescriptor o1, ActionDescriptor o2) -> o1.getName().compareTo(o2.getName()));
        return list;
    }

 
}
