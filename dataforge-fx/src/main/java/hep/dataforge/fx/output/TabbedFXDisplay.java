package hep.dataforge.fx.output;

import hep.dataforge.fx.FXPlugin;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.names.Named;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * A display where each stage corresponds to the tab pane
 */
public class TabbedFXDisplay extends SimpleConfigurable implements FXDisplay {
    private final FXPlugin fx;

    private Map<String, TabbedStage> stages = new HashMap<>();

    public TabbedFXDisplay(FXPlugin fx) {
        this.fx = fx;
    }

    private synchronized TabbedStage getStage(String stageName) {
        return stages.computeIfAbsent(stageName, TabbedStage::new);
    }

    private Meta getStageConfig(String name) {
        if (name.isEmpty()) {
            return getConfig();
        } else {
            return new Laminate((getConfig().getMetaOrEmpty(name)), getConfig());
        }
    }

    private void applyStageConfig(TabbedStage stage) {
        Meta config = getStageConfig(stage.name);

        stage.stage.setTitle(config.getString("title", stage.name));
    }

    @Override
    public BorderPane getContainer(String stage, String name) {
        return getStage(stage).getTab(name).pane;
    }

    @Override
    protected void applyConfig(Meta config) {
        super.applyConfig(config);
        stages.values().forEach(this::applyStageConfig);
    }

    private class TabbedStage implements Named {
        String name;
        Stage stage;
        TabPane tabPane;
        Map<String, DisplayTab> tabs = new HashMap<>();

        public TabbedStage(String stageName) {
            this.name = stageName;
            tabPane = new TabPane();
            stage = fx.show(stage -> {
                Meta config = getStageConfig(name);
                double width = config.getDouble("width", 800);
                double height = config.getDouble("height", 600);
                stage.setScene(new Scene(tabPane, width, height));
            });
            applyStageConfig(this);
        }

        public DisplayTab getTab(String tabName) {
            return tabs.computeIfAbsent(tabName, DisplayTab::new);
        }

        @Override
        public String getName() {
            return name;
        }

        private class DisplayTab implements Named {
            String name;
            Tab tab;
            BorderPane pane;

            public DisplayTab(String name) {
                this.name = name;
                tab = new Tab(name);
                pane = new BorderPane();
                tab.setContent(pane);
                tabPane.getTabs().add(tab);
            }

            @Override
            public String getName() {
                return name;
            }
        }
    }
}
