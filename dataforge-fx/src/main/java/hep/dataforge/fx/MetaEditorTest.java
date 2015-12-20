/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.description.DescriptorBuilder;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.meta.ConfigChangeListener;
import hep.dataforge.meta.Configuration;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.values.Value;
import java.io.IOException;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Nozik
 */
public class MetaEditorTest extends Application {

    private Logger logger = LoggerFactory.getLogger("test");

    @Override
    public void start(Stage primaryStage) throws IOException {


        Configuration config = new Configuration("test")
                .setValue("testValue", "[1,2,3]")
                .setValue("anotherTestValue", 15)
                .putNode(new MetaBuilder("childNode")
                        .setValue("childValue", true)
                        .setValue("anotherChildValue", 18)
                ).putNode(new MetaBuilder("childNode")
                        .setValue("childValue", true)
                        .putNode(new MetaBuilder("grandChildNode")
                                .putValue("grandChildValue", "grandChild")
                        )
                );

        NodeDescriptor descriptor = new DescriptorBuilder()
                .setInfo("Configuration editor test node")
                .addValue("testValue", "STRING", "a test value")
                .addValue("defaultValue", "NUMBER", "A value with default", 82.5)
                .addNode(new DescriptorBuilder("childNode")
                        .setInfo("A child Node")
                        .addValue("childValue", "BOOLEAN", "A child boolean node"))
                .build();

        config.addObserver(new ConfigChangeListener() {
            @Override
            public void notifyValueChanged(String name, Value oldItem, Value newItem) {
                logger.info("The value {} changed from {} to {}", name, oldItem, newItem);
            }

            @Override
            public void notifyElementChanged(String name, List<? extends Meta> oldItem, List<? extends Meta> newItem) {
                logger.info("The node {} changed", name);
            }
        });

        Scene scene = new Scene(MetaEditorComponent.build(config, descriptor), 400, 400);

        primaryStage.setTitle("Meta editor test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}