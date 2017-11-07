/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.test

import hep.dataforge.fx.output.FXOutputPane
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

/**
 *
 * @author Alexander Nozik
 */
class FXOutputPaneTest : Application() {

    override fun start(stage: Stage) {

        val out = FXOutputPane()
        out.setMaxLines(5)

        for (i in 0..11) {
            out.appendLine("my text number " + i)
        }

        //        onComplete.appendLine("a\tb\tc");
        //        onComplete.appendLine("aaaaa\tbbb\tccc");

        val scene = Scene(out.root, 400.0, 400.0)

        stage.title = "FXOutputPaneTest"
        stage.scene = scene
        stage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(FXOutputPaneTest::class.java, *args)
}
