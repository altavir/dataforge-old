package hep.dataforge.grind

import hep.dataforge.io.text.Markup
import hep.dataforge.io.text.MarkupBuilder
import hep.dataforge.meta.Configuration
import hep.dataforge.meta.Meta
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * Builder for DataForge text markup
 * Created by darksnake on 05-Jan-17.
 */
class GrindMarkupBuilder extends BuilderSupport {

    @Override
    protected void setParent(Object parent, Object child) {
        Configuration parentConfig = ((Markup) parent).getConfig();
        Configuration childConfig = ((Markup) child).getConfig().rename(Markup.MARKUP_CONTENT_NODE);
        parentConfig.attachNode(childConfig)
    }

    @Override
    Object invokeMethod(String methodName, Object args) {
        if (methodName == "style" || methodName == "meta") {
            def mb = new GrindMetaBuilder();
            List list = InvokerHelper.asList(args);
            //Change environment of passed closure
            if (list.last() instanceof Closure) {
                Closure closure = list.last()
                closure.setDelegate(mb);
                //closure still can look up global builder scope, but uses delegate first
                closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            }
            Meta meta = new GrindMetaBuilder().invokeMethod(methodName, args);
            ((Markup) getCurrent()).getConfig().setNode(meta)
            return current
        }
        return super.invokeMethod(methodName, args)
    }

    @Override
    protected Markup createNode(Object name, Map attributes, Object value) {
        switch (name) {
            case "text":
                return MarkupBuilder.text(value).update(attributes).build()
            default:
                MarkupBuilder mb = new MarkupBuilder()
                        .setType(name)
                        .update(attributes);
                if (value != null) {
                    if ("row" == name) {
                        value.each { mb.addText(it) }
                    }

                    if (value instanceof Markup) {
                        mb.setContent(value.content)
                    }
                }
                return mb.build();
        }
    }

    @Override
    protected Markup postNodeCompletion(Object parent, Object node) {
        //remove type for root element
        if (parent == null) {
            ((Markup) node).getConfig().removeValue("type");
        }
        return super.postNodeCompletion(parent, node)
    }

    @Override
    protected Markup createNode(Object name, Object value) {
        return createNode(name, [:], value);
    }

    @Override
    protected Markup createNode(Object name) {
        return createNode(name, [:]);
    }

    @Override
    protected Markup createNode(Object name, Map attributes) {
        return createNode(name, attributes, null);
    }
}
