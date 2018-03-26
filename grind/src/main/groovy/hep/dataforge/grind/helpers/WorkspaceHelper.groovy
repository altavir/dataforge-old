package hep.dataforge.grind.helpers

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.grind.workspace.WorkspaceSpec
import hep.dataforge.io.markup.MarkupBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Method

@CompileStatic
class WorkspaceHelper extends AbstractHelper {
    @Delegate private WorkspaceSpec builder;

    WorkspaceHelper(Context context) {
        super(context)
        builder = new WorkspaceSpec(context);
    }

    @Override
    protected Collection<Method> listDescribedMethods() {
        return builder.getClass().getDeclaredMethods()
                .findAll { it.isAnnotationPresent(MethodDescription) }
    }

    @Override
    protected MarkupBuilder getHelperDescription() {
        return new MarkupBuilder().text("The helper for workspace operations");
    }

    Logger getLogger(){
        return LoggerFactory.getLogger(getClass())
    }

}
