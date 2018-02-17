package hep.dataforge.io.markup

import hep.dataforge.description.Described
import hep.dataforge.description.NodeDescriptor
import hep.dataforge.exceptions.DescriptorException
import hep.dataforge.meta.Meta
import hep.dataforge.values.ValueType

/**
 * Created by darksnake on 05-Jan-17.
 */
object MarkupUtils {

    @JvmStatic
    fun markupDescriptor(obj: Described): Markup {
        val builder = MarkupGroup()
        applyDescriptorHead(builder, obj)
        applyDescriptorNode(obj.descriptor.meta, builder)
        return builder
    }

    @JvmStatic
    fun markupDescriptor(d: NodeDescriptor): Markup {
        val builder = MarkupGroup()
        applyDescriptorNode(d.meta, builder)
        return builder
    }

    private fun applyDescriptorHead(builder: MarkupGroup, obj: Described) {
        obj.header?.let { builder.add(it) }

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
    private fun descriptorValue(valueDef: Meta): Markup {
        val builder = MarkupGroup()

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
                builder.apply {
                    text(" = \"")
                    text(def.stringValue(), "yellow")
                    text("\": ")
                }
            } else {
                builder.apply {
                    text(" = ")
                    text(def.stringValue(), "yellow")
                    text(": ")
                }
            }
        } else {
            builder.text(": ")
        }

        builder.text(String.format("%s", valueDef.getString("info")))
        return builder
    }

    @Throws(DescriptorException::class)
    private fun applyDescriptorNode(nodeDef: Meta, builder: MarkupGroup) {
        if (nodeDef.hasMeta("node")) {
            val elementList = ListMarkup().apply {
                bullet = "+"
            }
            for (elDef in nodeDef.getMetaList("node")) {
                elementList.add(descriptorElement(elDef))
            }
            builder.add(elementList)
        }

        if (nodeDef.hasMeta("value")) {
            val valueList = ListMarkup().apply {
                bullet = "-"
            }
            for (parDef in nodeDef.getMetaList("value")) {
                valueList.add(descriptorValue(parDef))
            }
            builder.add(valueList)
        }
    }

    @Throws(DescriptorException::class)
    private fun descriptorElement(elementDef: Meta): Markup {
        val builder = MarkupGroup()

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


}
