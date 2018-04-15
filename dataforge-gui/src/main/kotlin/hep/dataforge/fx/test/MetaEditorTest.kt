/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.test

import hep.dataforge.description.DescriptorBuilder
import hep.dataforge.fx.meta.ConfigEditor
import hep.dataforge.meta.ConfigChangeListener
import hep.dataforge.meta.Configuration
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.names.Name
import hep.dataforge.values.Value
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import org.slf4j.LoggerFactory
import tornadofx.*
import java.io.IOException

/**
 * @author Alexander Nozik
 */
class MetaEditorTest : App() {

    private val logger = LoggerFactory.getLogger("test")

    @Throws(IOException::class)
    override fun start(stage: Stage) {

        val config = Configuration("test")
                .setValue("testValue", "[1,2,3]")
                .setValue("anotherTestValue", 15)
                .putNode(MetaBuilder("childNode")
                        .setValue("childValue", true)
                        .setValue("anotherChildValue", 18)
                ).putNode(MetaBuilder("childNode")
                .setValue("childValue", true)
                .putNode(MetaBuilder("grandChildNode")
                        .putValue("grandChildValue", "grandChild")
                )
        )

        val descriptor = DescriptorBuilder()
                .setInfo("Configuration editor test node")
                .addValue("testValue", "STRING", "a test value")
                .addValue("defaultValue", "NUMBER", "A value with default", 82.5)
                .addNode(DescriptorBuilder("childNode")
                        .setInfo("A child Node")
                        .addValue("childValue", "BOOLEAN", "A child boolean node"))
                .addNode(DescriptorBuilder("descriptedNode")
                        .setInfo("A descripted node")
                        .addValue("descriptedValue", "BOOLEAN", "described value in described node"))
                .build()

        config.addObserver(object : ConfigChangeListener {
            override fun notifyValueChanged(name: Name, oldItem: Value?, newItem: Value?) {
                logger.info("The value {} changed from {} to {}", name, oldItem, newItem)
            }

            override fun notifyNodeChanged(name: Name, oldItem: List<Meta>, newItem: List<Meta>) {
                logger.info("The node {} changed", name)
            }
        })

        val scene = Scene(ConfigEditor(config, descriptor = descriptor).root, 400.0, 400.0)

        stage.title = "Meta editor test"
        stage.scene = scene
        stage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(MetaEditorTest::class.java, *args)
}