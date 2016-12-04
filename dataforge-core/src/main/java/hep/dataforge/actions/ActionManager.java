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
import hep.dataforge.tables.ReadPointSetAction;
import hep.dataforge.tables.TransformTableAction;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
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

    public ActionManager() {
        register(TransformTableAction.class);
        register(ReadPointSetAction.class);
        register(RunConfigAction.class);
    }

    protected ActionManager getParent() {
        if (getContext() == null || getContext().getParent() == null || !getContext().getParent().provides("actions")) {
            return null;
        } else {
            return getContext().getParent().provide("actions", ActionManager.class);
        }
    }


    /**
     * Build action using its type and default empty constructor
     * @param type
     * @return
     */
    public Action build(Class<Action> type){
        try {
            Constructor<Action> constructor = type.getConstructor();
            Action res = constructor.newInstance();
            if(res instanceof GenericAction){
                return ((GenericAction)res).withContext(getContext());
            } else {
                return res;
            }
            //TODO add logger factory and parent process
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("This action type does not have parameterless constructor");
        } catch (Exception e) {
            throw new RuntimeException("Error while creating an instance of action");
        }
    }

    /**
     * Build action using class name
     * @param type
     * @return
     * @throws ClassNotFoundException
     */
    public Action buildByType(String type) throws ClassNotFoundException {
        return build((Class<Action>) Class.forName(type));
    }

    /**
     *
     * @param name
     * @return
     */
    public Action build(String name) {
        if (actionMap.containsKey(name)) {
            return actionMap.get(name);
        } else {
            ActionManager parent = getParent();
            if (parent == null) {
                //TODO сделать ActionNotFoundException
                throw new NameNotFoundException(name);
            } else {
                return parent.build(name);
            }
        }
    }

    private void put(Action action) {
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
    public final void register(Class<? extends Action> actionClass) {
        try {
            put(actionClass.getDeclaredConstructor().newInstance());
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("Action must have default empty constructor to be registered.");
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException("Error while constructing Action", ex);
        }
    }

    /**
     * <p>
     * unRegister.</p>
     *
     * @param actionType a {@link java.lang.String} object.
     */
    public void unRegister(String actionType) {
        actionMap.remove(actionType);
    }

    /**
     * List all available actions in this context
     *
     * @return
     */
    public List<ActionDescriptor> list() {
        List<ActionDescriptor> list;
        if (getParent() != null) {
            list = getParent().list();
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
