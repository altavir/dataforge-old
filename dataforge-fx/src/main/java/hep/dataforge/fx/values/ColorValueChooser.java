package hep.dataforge.fx.values;

import hep.dataforge.values.Value;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import org.slf4j.LoggerFactory;

/**
 * Created by darksnake on 01-May-17.
 */
public class ColorValueChooser extends ValueChooserBase<ColorPicker> {
    @Override
    public void setDisplayValue(Value value) {
        if (!value.stringValue().isEmpty()) {
            try {
                getNode().setValue(Color.valueOf(value.stringValue()));
            } catch (Exception ex) {
                LoggerFactory.getLogger(getClass()).warn("Invalid color field value: " + value.stringValue());
            }
        }
    }

    @Override
    protected ColorPicker buildNode() {
        ColorPicker node = new ColorPicker();
        node.getStyleClass().add("split-button");
        node.setMaxWidth(Double.MAX_VALUE);
        node.setOnAction(event -> valueProperty().setValue(Value.of(node.getValue().toString())));
        return node;
    }
}
