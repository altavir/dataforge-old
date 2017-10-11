package hep.dataforge.plots.demo;/**
 * Created by darksnake on 29-Apr-17.
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class JFreeChartTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        XYPlot plot = new XYPlot(null,new NumberAxis(),new NumberAxis(),new XYLineAndShapeRenderer());
        JFreeChart chart = new JFreeChart(plot);
        ChartViewer viewer = new ChartViewer(chart);
        root.setCenter(viewer);
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("JFC test");
        stage.setScene(scene);
        stage.show();
    }
}
