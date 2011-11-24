package org.eclipse.tcf.internal.debug.ui.commands;

import org.eclipse.tcf.internal.debug.ui.model.TCFNode;

public class ConsoleCommand extends AbstractActionDelegate {

    @Override
    protected void selectionChanged() {
        getAction().setEnabled(getNode() != null);
    }

    @Override
    protected void run() {
        // TODO Auto-generated method stub

    }

    private TCFNode getNode() {
        return null;
    }
}
