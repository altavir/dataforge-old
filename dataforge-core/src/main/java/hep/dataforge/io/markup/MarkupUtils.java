package hep.dataforge.io.markup;

import hep.dataforge.description.Described;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.exceptions.DescriptorException;
import hep.dataforge.meta.Meta;
import hep.dataforge.tables.Table;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import hep.dataforge.values.Values;

import static hep.dataforge.io.IOUtils.format;
import static hep.dataforge.io.IOUtils.getDefaultTextWidth;

/**
 * Created by darksnake on 05-Jan-17.
 */
public class MarkupUtils {

    public static Markup markupDescriptor(Described obj) {
        MarkupBuilder builder = new MarkupBuilder();
        applyDescriptorHead(builder, obj);
        applyDescriptorNode(builder, obj.getDescriptor().getMeta());
        return builder.build();
    }

    public static Markup markupDescriptor(NodeDescriptor d) {
        MarkupBuilder builder = new MarkupBuilder();
        applyDescriptorNode(builder, d.getMeta());
        return builder.build();
    }

    private static void applyDescriptorHead(MarkupBuilder builder, Described obj) {
        builder.content(obj.getHeader());
//        NodeDescriptor descriptor = obj.getDescriptor();
//        MarkupBuilder builder = new MarkupBuilder();
//        if (descriptor instanceof ActionDescriptor) {
//            ActionDescriptor ad = (ActionDescriptor) descriptor;
//            builder.text(ad.getName(), "green")
//                    .text(" {input : ")
//                    .text(ad.inputType(), "cyan")
//                    .text(", output : ")
//                    .text(ad.outputType(), "cyan")
//                    .text(String.format("}: %s", ad.info()));
//        } else if (descriptor.getName() != null && !descriptor.getName().isEmpty()) {
//            builder.text(descriptor.getName(), "blue");
//        }
//        return builder;
    }

    private static MarkupBuilder descriptorValue(Meta valueDef) throws DescriptorException {
        MarkupBuilder builder = new MarkupBuilder();

        if (valueDef.getBoolean("required", false)) {
            builder.text("(*) ", "cyan");
        }

        builder.text(valueDef.getString("name"), "red");

        if (valueDef.getBoolean("multiple", false)) {
            builder.text(" (mult)", "cyan");
        }

        builder.text(String.format(" (%s)", valueDef.getString("type", "STRING")));

        if (valueDef.hasValue("def")) {
            Value def = valueDef.getValue("default");
            if (def.getType().equals(ValueType.STRING)) {
                builder.text(" = \"")
                        .text(def.stringValue(), "yellow")
                        .text("\": ");
            } else {
                builder.text(" = ")
                        .text(def.stringValue(), "yellow")
                        .text(": ");
            }
        } else {
            builder.text(": ");
        }

        builder.text(String.format("%s", valueDef.getString("info")));
        return builder;
    }

    private static void applyDescriptorNode(MarkupBuilder builder, Meta nodeDef) throws DescriptorException {
        if (builder == null) {
            builder = new MarkupBuilder();
        }
        if (nodeDef.hasMeta("node")) {
            MarkupBuilder elementList = MarkupBuilder.list(-1, "+ ");
            for (Meta elDef : nodeDef.getMetaList("node")) {
                elementList.content(descriptorElement(elDef));
            }
            builder.content(elementList);
        }

        if (nodeDef.hasMeta("value")) {
            MarkupBuilder valueList = MarkupBuilder.list(-1, "- ");
            for (Meta parDef : nodeDef.getMetaList("value")) {
                valueList.content(descriptorValue(parDef));
            }
            builder.content(valueList);
        }
    }

    private static MarkupBuilder descriptorElement(Meta elementDef) throws DescriptorException {
        MarkupBuilder builder = new MarkupBuilder();

        if (elementDef.getBoolean("required", false)) {
            builder.text("(*) ", "cyan");
        }

        builder.text(elementDef.getString("name"), "magenta");

        if (elementDef.getBoolean("multiple", false)) {
            builder.text(" (mult)", "cyan");
        }

        if (elementDef.hasValue("info")) {
            builder.text(String.format(": %s", elementDef.getString("info")));
        }

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
            header.column(c.getTitle(), getDefaultTextWidth(c.getPrimaryType()));
        });
        builder.content(header);

        for (Values dp : table) {
            MarkupBuilder row = new MarkupBuilder();
            table.getFormat().getColumns().forEach(c -> {
                int width = getDefaultTextWidth(c.getPrimaryType());
                row.column(format(dp.getValue(c.getName()), width), width);
            });
            builder.content(row);
        }
        return builder;
    }

}
