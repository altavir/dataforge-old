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
import hep.dataforge.io.output.Output;
import hep.dataforge.io.output.SelfRendered;
import hep.dataforge.io.output.TextOutput;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * ActionDescriptor class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ActionDescriptor extends NodeDescriptor implements SelfRendered {

    public ActionDescriptor(Meta meta) {
        super(meta);
    }

    public static ActionDescriptor build(Action action) {
        MetaBuilder builder = Descriptors.buildDescriptorMeta(action.getClass());

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
        MetaBuilder builder = Descriptors.buildDescriptorMeta(actionClass);

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

    @NotNull
    @Override
    public String getInfo() {
        return getMeta().getString("actionDef.description", "");
    }

    public String getInputType() {
        return getMeta().getString("actionDef.inputType", "");
    }

    public String getOutputType() {
        return getMeta().getString("actionDef.outputType", "");
    }

    @NotNull
    @Override
    public String getName() {
        return getMeta().getString("actionDef.name", super.getName());
    }

    @Override
    public void render(@NotNull Output output, @NotNull Meta meta) {
        if(output instanceof TextOutput){
            TextOutput textOutput = ((TextOutput) output);
            textOutput.renderText(getName(), Color.GREEN);
            textOutput.renderText(" {input : ");
            textOutput.renderText(getInputType(), Color.CYAN);
            textOutput.renderText(", output : ");
            textOutput.renderText(getOutputType(), Color.CYAN);
            textOutput.renderText(String.format("}: %s", getInfo()));

        } else {
            output.render(String.format("Action %s (input: %s, output: %s): %s%n",getName(), getInputType(), getOutputType(), getInfo()),meta);
        }
    }
}
