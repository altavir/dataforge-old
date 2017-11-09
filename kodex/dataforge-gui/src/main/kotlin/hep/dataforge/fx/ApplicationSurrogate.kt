package hep.dataforge.fx

import javafx.application.Application
import javafx.stage.Stage
import tornadofx.*

import java.util.concurrent.CompletableFuture

/**
 * A surrogate JavaFX application to be used in case there is no global application present.
 * Created by darksnake on 23-Jan-17.
 */
class ApplicationSurrogate : App() {

    @Throws(Exception::class)
    override fun start(stage: Stage) {
        stageGenerator!!.complete(stage)
    }

    @Throws(Exception::class)
    override fun stop() {
        stageGenerator = null
        super.stop()
    }

    companion object {
        /**
         * The primary stage
         */
        private var stageGenerator: CompletableFuture<Stage>? = null

        val stage: Stage
            get() {
                if (!isStarted) {
                    throw RuntimeException("FX application surrogate not initialized")
                }
                try {
                    return stageGenerator!!.get()
                } catch (e: Exception) {
                    throw RuntimeException("Failed to load FX surrogate primary stage", e)
                }

            }

        private val isStarted: Boolean
            get() = stageGenerator != null

        fun start() {
            if (!isStarted) {
                stageGenerator = CompletableFuture()
                Thread { Application.launch(ApplicationSurrogate::class.java) }.start()
            }
        }
    }


}
