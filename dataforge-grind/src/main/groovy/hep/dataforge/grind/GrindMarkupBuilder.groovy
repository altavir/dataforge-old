package hep.dataforge.grind

import hep.dataforge.io.text.Markup
import hep.dataforge.io.text.MarkupBuilder
import hep.dataforge.meta.Configuration
import hep.dataforge.meta.Meta

/**
 * Builder for DataForge text markup
 * Created by darksnake on 05-Jan-17.
 */
class GrindMarkupBuilder extends BuilderSupport {

    @Override
    protected void setParent(Object parent, Object child) {
        Configuration parentConfig = ((Markup) parent).getConfig();
        if (child instanceof Markup) {
            Configuration childConfig = ((Markup) child).getConfig().rename(Markup.MARKUP_CONTENT_NODE);
            parentConfig.attachNode(childConfig)
        } else if (child instanceof Meta) {
            parentConfig.setNode((Meta) child);
        } else {
            throw new RuntimeException("unknown child type")
        }
    }

    @Override
    protected Object createNode(Object name, Map attributes, Object value) {
        switch (name) {
            case "style":
            case "meta":
                return new GrindMetaBuilder().createNode(name, attributes, value);
            case "text":
                return MarkupBuilder.text(value).update(attributes).build()
            default:
                MarkupBuilder mb = new MarkupBuilder()
                        .setType(name)
                        .update(attributes);
                if (value != null) {
                    if("row" == name){
                        value.each {mb.addText(it)}
                    }

                    if (value instanceof Markup) {
                        mb.setContent(value.content)
                    }
                }
                return mb.build();
        }
    }

    @Override
    protected Object postNodeCompletion(Object parent, Object node) {
        //remove type for root element
        if (parent == null) {
            ((Markup) node).getConfig().removeValue("type");
        }
        return super.postNodeCompletion(parent, node)
    }

    @Override
    protected Object createNode(Object name, Object value) {
        return createNode(name, [:], value);
    }

    @Override
    protected Object createNode(Object name) {
        return createNode(name, [:]);
    }

    @Override
    protected Object createNode(Object name, Map attributes) {
        return createNode(name, attributes, null);
    }
}
