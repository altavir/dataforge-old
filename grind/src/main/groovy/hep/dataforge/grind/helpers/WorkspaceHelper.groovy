package hep.dataforge.grind.helpers

import hep.dataforge.context.Context
import hep.dataforge.io.markup.MarkupBuilder
import hep.dataforge.workspace.Workspace

class WorkspaceHelper extends AbstractHelper {
    private Map<String,Workspace> wsps;

    WorkspaceHelper(Context context) {
        super(context)
    }

    @Override
    protected MarkupBuilder getHelperDescription() {
        return new MarkupBuilder().text("The helper for workspace operations");
    }
}
