/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.commands;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.internal.debug.ui.Activator;
import org.eclipse.tcf.internal.debug.ui.ImageCache;
import org.eclipse.tcf.internal.debug.ui.model.ICastToType;
import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.ISymbols;
import org.eclipse.tcf.util.TCFDataCache;
import org.eclipse.ui.IWorkbenchWindow;

public class CastToArrayCommand extends AbstractActionDelegate {

    private static class CastToTypeInputValidator implements IInputValidator {

        public CastToTypeInputValidator() {
        }

        public String isValid(String new_text) {
            try {
                if (new_text.length() == 0) return "";
                int i = Integer.parseInt(new_text);
                if (i < 1) return "Array length must be >= 1";
            }
            catch (Exception x) {
                return "Invalid number";
            }
            return null;
        }
    }

    private static class CastToTypeDialog extends InputDialog {

        public CastToTypeDialog(Shell shell, String initial_value) {
            super(shell, "Cast To Array", "Enter array length",
                    initial_value, new CastToTypeInputValidator() );
        }

        @Override
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.setImage(ImageCache.getImage(ImageCache.IMG_TCF));
        }
    }

    private static class GetBaseTypeName implements Runnable {
        final TCFNode node;
        String base_type_name;
        String cur_length;

        GetBaseTypeName(TCFNode n) {
            node = n instanceof ICastToType ? n : null;
            if (node == null) done();
            else Protocol.invokeLater(this);
        }

        public void run() {
            cur_length = null;
            String cast = node.getModel().getCastToType(node.getID());
            if (cast != null) {
                if (cast.endsWith("]")) {
                    int i = cast.lastIndexOf('[');
                    if (i > 0) {
                        cur_length = cast.substring(i + 1, cast.length() - 1);
                        done(cast.substring(0, i));
                        return;
                    }
                }
                done(null);
            }
            TCFDataCache<ISymbols.Symbol> type_cache = ((ICastToType)node).getType();
            if (!type_cache.validate(this)) return;
            ISymbols.Symbol type_data = type_cache.getData();
            if (type_data == null || type_data.getTypeClass() != ISymbols.TypeClass.pointer) {
                done(null);
            }
            else {
                for (int i = 0;; i++) {
                    TCFDataCache<ISymbols.Symbol> base_type_cache = node.getModel().getSymbolInfoCache(type_data.getBaseTypeID());
                    if (!base_type_cache.validate(this)) return;
                    ISymbols.Symbol base_type_data = base_type_cache.getData();
                    if (base_type_data == null) {
                        done(null);
                        return;
                    }
                    else {
                        if (base_type_data.getName() != null) {
                            done(makePtrTypeName(base_type_data.getName(), i));
                            return;
                        }
                        else if (base_type_data.getTypeClass() == ISymbols.TypeClass.pointer) {
                            type_data = base_type_data;
                        }
                        else if (!base_type_data.getID().equals(base_type_data.getTypeID())) {
                            // modified type without name, like "volatile int"
                            base_type_cache = node.getModel().getSymbolInfoCache(base_type_data.getTypeID());
                            if (!base_type_cache.validate(this)) return;
                            base_type_data = base_type_cache.getData();
                            if (base_type_data != null && base_type_data.getName() != null) {
                                done(makePtrTypeName(base_type_data.getName(), i));
                            }
                            else {
                                done(null);
                            }
                            return;
                        }
                        else {
                            done(null);
                            return;
                        }
                    }
                }
            }
        }

        private String makePtrTypeName(String base, int cnt) {
            StringBuffer bf = new StringBuffer();
            bf.append(base);
            if (cnt > 0) {
                bf.append(' ');
                while (cnt > 0) {
                    bf.append('*');
                    cnt--;
                }
            }
            return bf.toString();
        }

        private void done(String base_type_name) {
            this.base_type_name = base_type_name;
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    done();
                }
            });
        }

        void done() {
        }
    }

    @Override
    protected void run() {
        new GetBaseTypeName(getSelectedNode()) {
            void done() {
                if (base_type_name == null) return;
                final IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
                if (window == null) return;
                CastToTypeDialog dialog = new CastToTypeDialog(window.getShell(), cur_length);
                if (dialog.open() != Window.OK) return;
                final String new_type = dialog.getValue().trim();
                Protocol.invokeLater(new Runnable() {
                    public void run() {
                        node.getModel().setCastToType(node.getID(), base_type_name + "[" + new_type + "]");
                    }
                });
            }
        };
    }

    @Override
    protected void selectionChanged() {
        new GetBaseTypeName(getSelectedNode()) {
            void done() {
                getAction().setEnabled(base_type_name != null);
            }
        };
    }
}
