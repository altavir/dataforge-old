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
package hep.dataforge.io.log;

import ch.qos.logback.classic.Logger;

/**
 * An object that could handle and store its own log. A purpose of DataForge log
 * is different from standard logging because analysis log is part of the
 * result. Therfore logable objects should be used only when one needs to sore
 * resulting log.
 *
 * @author Alexander Nozik
 */
public interface Logable{

    default Logger getLogger(){
        return getLog().getLogger();
    }

    Log getLog();

    default void log(String str, Object... parameters){
        getLog().log(str, parameters);
    }

    default void log(LogEntry entry){
        getLog().log(entry);
    }
    
    default void logError(String str, Object... parameters){
        getLog().logError(str, parameters);
    }
    
}
