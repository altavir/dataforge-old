package hep.dataforge.io.text;

import hep.dataforge.description.ActionDescriptor;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.exceptions.DescriptorException;
import hep.dataforge.meta.Meta;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.Table;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;

import static hep.dataforge.io.IOUtils.format;
import static hep.dataforge.io.IOUtils.getDefaultTextWidth;

/**
 * Created by darksnake on 05-Jan-17.
 */
public class MarkupUtils {

    public static Markup markupDescriptor(NodeDescriptor descriptor) {
        return applyDescriptorNode(descriptorHead(descriptor), descriptor.meta()).build();
    }

    private static MarkupBuilder descriptorHead(NodeDescriptor descriptor) {
        MarkupBuilder builder = new MarkupBuilder();
        if (descriptor instanceof ActionDescriptor) {
            ActionDescriptor ad = (ActionDescriptor) descriptor;
            builder.addText(ad.getName(), "green")
                    .addText(" {input : ")
                    .addText(ad.inputType(), "cyan")
                    .addText(", output : ")
                    .addText(ad.outputType(), "cyan")
                    .addText(String.format("}: %s", ad.info()));
        } else if (descriptor.getName() != null && !descriptor.getName().isEmpty()) {
            builder.addText(descriptor.getName(), "blue");
        }
        return builder;
    }

    private static MarkupBuilder descriptorValue(Meta valueDef) throws DescriptorException {
        MarkupBuilder builder = new MarkupBuilder();

        if (valueDef.getBoolean("required", false)) {
            builder.addText("(*) ", "cyan");
        }

        builder.addText(valueDef.getString("name"), "red");

        if (valueDef.getBoolean("multiple", false)) {
            builder.addText(" (mult)", "cyan");
        }

        builder.addText(String.format(" (%s)", valueDef.getString("type", "STRING")));

        if (valueDef.hasValue("def")) {
            Value def = valueDef.getValue("default");
            if (def.valueType().equals(ValueType.STRING)) {
                builder.addText(" = \"")
                        .addText(def.stringValue(), "yellow")
                        .addText("\": ");
            } else {
                builder.addText(" = ")
                        .addText(def.stringValue(), "yellow")
                        .addText(": ");
            }
        } else {
            builder.addText(": ");
        }

        builder.addText(String.format("%s", valueDef.getString("info")));
        return builder;
    }

    private static MarkupBuilder applyDescriptorNode(MarkupBuilder builder, Meta nodeDef) throws DescriptorException {
        if (nodeDef.hasMeta("node")) {
            MarkupBuilder elementList = MarkupBuilder.list(-1, "+ ");
            for (Meta elDef : nodeDef.getMetaList("node")) {
                elementList.addContent(descriptorElement(elDef));
            }
            builder.addContent(elementList);
        }

        if (nodeDef.hasMeta("value")) {
            MarkupBuilder valueList = MarkupBuilder.list(-1, "- ");
            for (Meta parDef : nodeDef.getMetaList("value")) {
                valueList.addContent(descriptorValue(parDef));
            }
            builder.addContent(valueList);
        }
        return builder;
    }

    private static MarkupBuilder descriptorElement(Meta elementDef) throws DescriptorException {
        MarkupBuilder builder = new MarkupBuilder();

        if (elementDef.getBoolean("required", false)) {
            builder.addText("(*) ", "cyan");
        }

        builder.addText(elementDef.getString("name"), "magenta");

        if (elementDef.getBoolean("multiple", false)) {
            builder.addText(" (mult)", "cyan");
        }

        builder.addText(String.format(": %s", elementDef.getString("info")));

        applyDescriptorNode(builder, elementDef);
        return builder;
    }


    /**
     * Represent table as a markup
     *
     * @param table
     * @return
     */
    public static MarkupBuilder markupTable(Table table) {
        MarkupBuilder builder = new MarkupBuilder().setType(GenericMarkupRenderer.TABLE_TYPE);

        MarkupBuilder header = new MarkupBuilder().setValue("header", true);
        table.getFormat().getColumns().forEach(c -> {
            header.addColumn(c.getTitle(), getDefaultTextWidth(c.getPrimaryType()));
        });
        builder.addContent(header);

        for (DataPoint dp : table) {
            MarkupBuilder row = new MarkupBuilder();
            table.getFormat().getColumns().forEach(c -> {
                int width = getDefaultTextWidth(c.getPrimaryType());
                row.addColumn(format(dp.getValue(c.getName()), width), width);
            });
            builder.addContent(row);
        }
        return builder;
    }
}
