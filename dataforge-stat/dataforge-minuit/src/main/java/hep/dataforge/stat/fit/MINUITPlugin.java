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
package hep.dataforge.stat.fit;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;
import hep.dataforge.io.history.Chronicle;
import org.slf4j.LoggerFactory;

/**
 * Мэнеджер для MINUITа. Пока не играет никакой активной роли кроме ведения
 * внутреннего лога.
 *
 * @author Darksnake
 * @version $Id: $Id
 */
@PluginDef(group = "hep.dataforge", name = "MINUIT",
        dependsOn = {"hep.dataforge:fitting"},
        description = "The MINUIT fitter engine for DataForge fitting")
public class MINUITPlugin extends BasicPlugin {

    /**
     * Constant <code>staticLog</code>
     */
    private static final Chronicle staticLog = new Chronicle("MINUIT-STATIC");

    /**
     * <p>
     * clearStaticLog.</p>
     */
    public static void clearStaticLog() {
        staticLog.clear();
    }

    /**
     * <p>
     * logStatic.</p>
     *
     * @param str  a {@link java.lang.String} object.
     * @param pars a {@link java.lang.Object} object.
     */
    public static void logStatic(String str, Object... pars) {
        if (staticLog == null) {
            throw new IllegalStateException("MINUIT log is not initialized.");
        }
        staticLog.report(str, pars);
        LoggerFactory.getLogger("MINUIT").info(String.format(str, pars));
//        Out.out.printf(str,pars);
//        Out.out.println();
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
        context.getFeature(FitManager.class).addEngine(MINUITFitEngine.MINUIT_ENGINE_NAME, new MINUITFitEngine());
        clearStaticLog();
    }

    @Override
    public void detach() {
        clearStaticLog();
        super.detach();
    }

}
