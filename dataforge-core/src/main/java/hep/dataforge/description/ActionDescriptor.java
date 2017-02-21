/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.description;

import hep.dataforge.actions.Action;
import hep.dataforge.actions.GenericAction;
import hep.dataforge.actions.ManyToOneAction;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;

/**
 * <p>
 * ActionDescriptor class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ActionDescriptor extends NodeDescriptor {

    public ActionDescriptor(String name) {
        super(name);
    }

    public ActionDescriptor(Meta meta) {
        super(meta);
    }

    public static ActionDescriptor build(Action action) {
        MetaBuilder builder = DescriptorUtils.buildDescriptorMeta(action.getClass());

        MetaBuilder actionDef = new MetaBuilder("actionDef")
                .putValue("name", action.getName());
        if (action instanceof GenericAction) {
            actionDef
                    .putValue("inputType", ((GenericAction) action).getInputType().getSimpleName())
                    .putValue("outputType", ((GenericAction) action).getOutputType().getSimpleName());
            if (action instanceof ManyToOneAction) {
                actionDef.setValue("inputType", ((GenericAction) action).getOutputType().getSimpleName() + "[]");
            }
        }

        TypedActionDef def = action.getClass().getAnnotation(TypedActionDef.class);

        if (def != null) {
            actionDef.putValue("description", def.info());
        }
        builder.putNode(actionDef);
        return new ActionDescriptor(builder);
    }

    public static ActionDescriptor build(Class<? extends Action> actionClass) {
        MetaBuilder builder = DescriptorUtils.buildDescriptorMeta(actionClass);

        TypedActionDef def = actionClass.getAnnotation(TypedActionDef.class);
        if (def != null) {
            MetaBuilder actionDef = new MetaBuilder("actionDef")
                    .putValue("name", def.name())
                    .putValue("inputType", def.inputType().getSimpleName())
                    .putValue("outputType", def.outputType().getSimpleName())
                    .putValue("description", def.info());

            if (actionClass.isAssignableFrom(ManyToOneAction.class)) {
                actionDef.setValue("inputType", def.inputType().getSimpleName() + "[]");
            }

            builder.putNode(actionDef);

        }
        return new ActionDescriptor(builder);
    }

    @Override
    public String info() {
        return meta().getString("actionDef.description", "");
    }

    public String inputType() {
        return meta().getString("actionDef.inputType", "");
    }

    public String outputType() {
        return meta().getString("actionDef.outputType", "");
    }

    @Override
    public String getName() {
        return meta().getString("actionDef.name", super.getName());
    }


}
