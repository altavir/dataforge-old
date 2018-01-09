/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.test

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.web.WebView
import javafx.stage.Stage

/**
 *
 * @author Alexander Nozik
 */
class WebViewOutputTest : Application() {

    override fun start(stage: Stage) {

        val webView = WebView()

        webView.engine.loadContent("a\tb\tc\naaaaa\tbbb\tccc")


        val scene = Scene(webView, 400.0, 400.0)

        stage.title = "WebViewOutputTest"
        stage.scene = scene
        stage.show()
    }

}

fun main(args: Array<String>) {
    Application.launch(WebViewOutputTest::class.java, *args)
}