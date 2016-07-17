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
package hep.dataforge.datafitter;

import hep.dataforge.actions.ActionManager;
import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;

/**
 *
 * @author Alexander Nozik
 */
@PluginDef(group = "hep.dataforge", name = "fitting", description = "Basic dataforge fitting plugin")
public class FitPlugin extends BasicPlugin {
    
    private FitManager fitManager;
    
    @Override
    public void attach(Context context) {
        if(getFitManager() == null){
            fitManager = new FitManager(context);
        } else {
            throw new RuntimeException("This FitPlugin is alredy atached");
        }
        
        FitEngineBuilder.addEngine("QOW", new QOWFitEngine());
        FitEngineBuilder.addEngine("CM", new CMFitEngine());

        ActionManager.buildFrom(context).registerAction(FitAction.class);
    }

    @Override
    public void detach() {
        fitManager = null;
        ActionManager.buildFrom(getContext()).unRegisterAction(FitAction.FIT_ACTION_NAME);
        super.detach();
    }

    /**
     * @return the fitManager
     */
    public FitManager getFitManager() {
        return fitManager;
    }
    
}
