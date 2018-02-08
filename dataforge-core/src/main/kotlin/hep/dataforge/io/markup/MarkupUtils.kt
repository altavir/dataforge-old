package hep.dataforge.io.markup

import hep.dataforge.description.Described
import hep.dataforge.description.NodeDescriptor
import hep.dataforge.exceptions.DescriptorException
import hep.dataforge.io.IOUtils.format
import hep.dataforge.io.IOUtils.getDefaultTextWidth
import hep.dataforge.meta.Meta
import hep.dataforge.tables.Table
import hep.dataforge.values.ValueType

/**
 * Created by darksnake on 05-Jan-17.
 */
object MarkupUtils {

    @JvmStatic
    fun markupDescriptor(obj: Described): Markup {
        val builder = MarkupBuilder()
        applyDescriptorHead(builder, obj)
        applyDescriptorNode(obj.descriptor.meta, builder)
        return builder.build()
    }

    @JvmStatic
    fun markupDescriptor(d: NodeDescriptor): Markup {
        val builder = MarkupBuilder()
        applyDescriptorNode(d.meta, builder)
        return builder.build()
    }

    private fun applyDescriptorHead(builder: MarkupBuilder, obj: Described) {
        obj.header?.let { builder.content(it) }

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

    @Throws(DescriptorException::class)
    private fun descriptorValue(valueDef: Meta): MarkupBuilder {
        val builder = MarkupBuilder()

        if (valueDef.getBoolean("required", false)) {
            builder.text("(*) ", "cyan")
        }

        builder.text(valueDef.getString("name"), "red")

        if (valueDef.getBoolean("multiple", false)) {
            builder.text(" (mult)", "cyan")
        }

        builder.text(String.format(" (%s)", valueDef.getString("type", "STRING")))

        if (valueDef.hasValue("def")) {
            val def = valueDef.getValue("default")
            if (def.type == ValueType.STRING) {
                builder.text(" = \"")
                        .text(def.stringValue(), "yellow")
                        .text("\": ")
            } else {
                builder.text(" = ")
                        .text(def.stringValue(), "yellow")
                        .text(": ")
            }
        } else {
            builder.text(": ")
        }

        builder.text(String.format("%s", valueDef.getString("info")))
        return builder
    }

    @Throws(DescriptorException::class)
    private fun applyDescriptorNode(nodeDef: Meta, builder: MarkupBuilder = MarkupBuilder()) {
        if (nodeDef.hasMeta("node")) {
            val elementList = MarkupBuilder.list(-1, "+ ")
            for (elDef in nodeDef.getMetaList("node")) {
                elementList.content(descriptorElement(elDef))
            }
            builder.content(elementList)
        }

        if (nodeDef.hasMeta("value")) {
            val valueList = MarkupBuilder.list(-1, "- ")
            for (parDef in nodeDef.getMetaList("value")) {
                valueList.content(descriptorValue(parDef))
            }
            builder.content(valueList)
        }
    }

    @Throws(DescriptorException::class)
    private fun descriptorElement(elementDef: Meta): MarkupBuilder {
        val builder = MarkupBuilder()

        if (elementDef.getBoolean("required", false)) {
            builder.text("(*) ", "cyan")
        }

        builder.text(elementDef.getString("name"), "magenta")

        if (elementDef.getBoolean("multiple", false)) {
            builder.text(" (mult)", "cyan")
        }

        if (elementDef.hasValue("info")) {
            builder.text(String.format(": %s", elementDef.getString("info")))
        }

        applyDescriptorNode(elementDef, builder)
        return builder
    }


    /**
     * Represent table as a markup
     *
     * @param table
     * @return
     */
    fun markupTable(table: Table): MarkupBuilder {
        val builder = MarkupBuilder().setType(Markup.TABLE_TYPE)

        val header = MarkupBuilder().setValue("header", true)
        table.format.columns.forEach { c -> header.column(c.title, getDefaultTextWidth(c.primaryType)) }
        builder.content(header)

        for (dp in table) {
            val row = MarkupBuilder()
            table.format.columns.forEach { c ->
                val width = getDefaultTextWidth(c.primaryType)
                row.column(format(dp.getValue(c.name), width), width)
            }
            builder.content(row)
        }
        return builder
    }

}
