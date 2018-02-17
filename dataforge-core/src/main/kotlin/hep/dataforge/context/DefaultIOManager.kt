/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hep.dataforge.context

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.UnsynchronizedAppenderBase
import hep.dataforge.io.output.Output
import hep.dataforge.meta.Meta

/**
 *
 *
 * DefaultIOManager class.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@PluginDef(name = "io", group = "hep.dataforge", info = "Basic input and output plugin")
open class DefaultIOManager(meta: Meta = Meta.empty()) : IOManager(meta) {

    override val output: Output
        get() = Global.console


    override fun output(meta: Meta): Output {
        return output
    }

    /**
     * Create logger appender for this manager
     *
     * @return
     */
    open fun createLoggerAppender(): Appender<ILoggingEvent> {
        return object : UnsynchronizedAppenderBase<ILoggingEvent>() {
            override fun append(eventObject: ILoggingEvent) {
                output.push(eventObject)
            }
        }
    }

    private fun addLoggerAppender(logger: Logger) {
        val loggerContext = logger.loggerContext
        val appender = createLoggerAppender()
        appender.name = IOManager.LOGGER_APPENDER_NAME
        appender.context = loggerContext
        appender.start()
        logger.addAppender(appender)
    }

    private fun removeLoggerAppender(logger: Logger) {
        val app = logger.getAppender(IOManager.LOGGER_APPENDER_NAME)
        if (app != null) {
            logger.detachAppender(app)
            app.stop()
        }
    }

    override fun attach(context: Context) {
        super.attach(context)
        if (context.logger is ch.qos.logback.classic.Logger) {
            addLoggerAppender(context.logger as ch.qos.logback.classic.Logger)
        }
    }

    override fun detach() {
        if (logger is ch.qos.logback.classic.Logger) {
            removeLoggerAppender(logger as ch.qos.logback.classic.Logger)
        }
        super.detach()
    }
}
