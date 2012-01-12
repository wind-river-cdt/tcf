/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.commands;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeRegister;
import org.eclipse.tcf.services.IRegisters;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartSite;


public class CustomPropertiesCommand extends AbstractActionDelegate {
    
    protected void selectionChanged() {
        getAction().setEnabled(getRootNode() != null);
    }

    protected void run() {

        IDebugView view = (IDebugView)getView();

        Viewer viewer = view.getViewer();

        if (viewer instanceof ITreeModelViewer) {

            // update the register columns to display
            ITreeModelViewer It = ((ITreeModelViewer)viewer);

            TreeModelViewer t = (TreeModelViewer)It;

            // update the value of the "custom" column with the information
            // coming from the input windows

            TreeSelection fSelection = (TreeSelection)this.getSelection();
            TreePath[] selectedObjPaths = fSelection.getPaths();
            
            // get the max reg size
            int regSize = 0;
            for (int ix = 0; ix < selectedObjPaths.length; ix++) {
                TreeItem treeItem = (TreeItem)t.findItem(selectedObjPaths[ix]);
                if (treeItem != null) {
                    TCFNodeRegister curRegister = (TCFNodeRegister)treeItem.getData();
                    IRegisters.RegistersContext ctx = curRegister.getContext().getData();
                    if (regSize < ctx.getSize())
                        regSize = ctx.getSize();
                }
            }
            
            CustomPropertiesDialog dialog = new CustomPropertiesDialog(getView().getSite().getShell(), regSize);

            if (dialog.open() == Window.OK) {
                
                String nbElementStr = "2";
                String selectedType = dialog.getSelectedType();

                if (selectedType == "short")
                    nbElementStr = "2";
                else if (selectedType == "integer")
                    nbElementStr = "4";
                else if (selectedType == "double")
                    nbElementStr = "8";
                else if (selectedType == "long double")
                    nbElementStr = "16";

                for (int ix = 0; ix < selectedObjPaths.length; ix++) {
                    TreeItem treeItem = (TreeItem)t.findItem(selectedObjPaths[ix]);
                    if (treeItem != null) {
                        TCFNodeRegister curRegister = (TCFNodeRegister)treeItem.getData();
                        curRegister.SetVectorListSize(Integer.parseInt(nbElementStr));
                        t.refresh();
                    }
                }
            }
        }
    }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#update()
         */
//        protected void update() {
//                IAction action = getAction();
//                if ( action != null ) {
//                        ICDebugTarget target = getDebugTarget();
//                        //action.setEnabled( ( target != null ) ? target.isSuspended() : false );
//                        action.setEnabled(true);
//                }
//        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#doHandleDebugEvent(org.eclipse.debug.core.DebugEvent)
         */
        protected void doHandleDebugEvent( DebugEvent event ) {
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#init(org.eclipse.jface.action.IAction)
         */
//        public void init( IAction action ) {
//                super.init( action );
//                Object element = DebugUITools.getDebugContext();
//                setSelection( (element != null) ? new StructuredSelection( element ) : new StructuredSelection() );
//                update();
//        }

//        private ICDebugTarget getDebugTarget() {
//                Object element = getSelection().getFirstElement();
//                if ( element instanceof IDebugElement ) {
//                        return (ICDebugTarget)((IDebugElement)element).getDebugTarget().getAdapter( ICDebugTarget.class );
//                }
//                return null;
//        }
        
        private TCFNode getRootNode() {
            IViewPart view = getView();
            if (view == null) return null;
            IWorkbenchPartSite site = view.getSite();
            if (site != null && IDebugUIConstants.ID_DEBUG_VIEW.equals(site.getId())) {
                TCFNode n = getSelectedNode();
                if (n == null) return null;
                return n.getModel().getRootNode();
            }
            if (site != null && IDebugUIConstants.ID_MEMORY_VIEW.equals(site.getId())) {
                ISelection selection = DebugUITools.getDebugContextManager().getContextService(
                        site.getWorkbenchWindow()).getActiveContext();
                if (selection instanceof IStructuredSelection) {
                    Object obj = ((IStructuredSelection)selection).getFirstElement();
                    if (obj instanceof TCFNode) return (TCFNode)obj;
                }
            }
            if (view instanceof IDebugView) {
                Object input = ((IDebugView)view).getViewer().getInput();
                if (input instanceof TCFNode) return (TCFNode)input;
            }
            return null;
            
        }
}


