package org.eclipse.tcf.internal.debug.ui;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContextStatusParameter;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;

public class TCFContextView extends AbstractDebugView implements IDebugContextListener {

    ArrayList<String> filteredKeys;

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "org.eclipse.tcf.debug.ui.views.TCFContextView";

    private Action filterAction;

    private Action resetAction;

    private Action saveAction;

    private Action loadAction;

    // For the System Monitor
    private Label listenerLabel;

    // For the Suspend Table
    private TreeModelViewer fViewer;

    protected IContentProvider fContentProvider;

    protected ITableLabelProvider fTableLabelProvider;

    /**
     * The constructor.
     */
    public TCFContextView() {
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl(Composite parent) {

        parent.setLayout(new GridLayout(1, false));
        listenerLabel = new Label(parent, SWT.BORDER);
        listenerLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        PresentationContext presentation = new PresentationContext("ExecStatusView", this);
        fViewer = new TreeModelViewer(parent, SWT.FULL_SELECTION | SWT.VIRTUAL, presentation);
        // Symbol table title
//        String[] tableTitles = new String[] { "Key", "Value" };
//        for (int ii = 0; ii < tableTitles.length; ii++) {
//            new TableColumn(table, SWT.LEFT).setText(tableTitles[ii]);
//        }

        fViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

        // Create an empty filteredKey
        filteredKeys = new ArrayList<String>();

        // Filter the keys to be displayed
        ViewerFilter myFilter = new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof TCFNodeExecContextStatusParameter) {
                    TCFNodeExecContextStatusParameter myEntry = (TCFNodeExecContextStatusParameter) element;
                    if (filteredKeys.contains(myEntry.getKey())) return false;
                }
                return true;
            }
        };

        ViewerFilter[] viewerFilterTab = new ViewerFilter[1];
        viewerFilterTab[0] = myFilter;
        fViewer.setFilters(viewerFilterTab);

        // Register to monitor the selection

        IDebugContextService contextService = DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow());
        contextService.addDebugContextListener(this);

        // Create the help context id for the viewer's control
        PlatformUI.getWorkbench().getHelpSystem().setHelp(fViewer.getControl(), "org.eclipse.tcf.debug.ui.viewer");
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                TCFContextView.this.fillContextMenu(manager);
            }
        });
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        manager.add(filterAction);
        manager.add(new Separator());
        manager.add(resetAction);
        manager.add(new Separator());
        manager.add(saveAction);
        manager.add(loadAction);
    }

    public void fillContextMenu(IMenuManager manager) {
        manager.add(filterAction);
        manager.add(resetAction);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(filterAction);
        manager.add(resetAction);
    }

    private void makeActions() {
        filterAction = new Action() {
            public void run() {
                // Retrieve the current entry and filter it.
                ISelection selectedItem = fViewer.getSelection();

                if (selectedItem instanceof IStructuredSelection) {
                    IStructuredSelection ss = (IStructuredSelection)selectedItem;
                    if (ss.size() == 1) {
                        Object element = ss.getFirstElement();
                        TCFNodeExecContextStatusParameter myEntry = (TCFNodeExecContextStatusParameter)element;
                        filteredKeys.add(myEntry.getKey());
                        fViewer.refresh();
                    }
                }

            }
        };

        filterAction.setText("Filter this entry");
        filterAction.setToolTipText("Filter this entry");
        filterAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

        resetAction = new Action() {
            public void run() {
                // Clear the filtered
                filteredKeys.clear();
                fViewer.refresh();
            }
        };
        resetAction.setText("Reset filters");
        resetAction.setToolTipText("Reset the filter");
        resetAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

        saveAction = new Action() {
            public void run() {
                // Serialize as XML file

                XMLEncoder e;
                String pathname;
                FileDialog fg = new FileDialog(PlatformUI.getWorkbench().getModalDialogShellProvider().getShell(), SWT.SAVE);
                fg.open();
                pathname = fg.getFilterPath() + System.getProperty("file.separator") + fg.getFileName();

                try {

                    e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(pathname)));
                    e.writeObject(filteredKeys);
                    e.close();
                }
                catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
            };

        };

        saveAction.setText("Store filter as XML file");
        saveAction.setToolTipText("Store in XML file");

        loadAction = new Action() {
            @SuppressWarnings("unchecked")
            public void run() {
                // DeSerialize the XML file

                String pathname;
                FileDialog fg = new FileDialog(PlatformUI.getWorkbench().getModalDialogShellProvider().getShell(), SWT.OPEN);
                fg.open();
                pathname = fg.getFilterPath() + System.getProperty("file.separator") + fg.getFileName();

                try {
                    XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream(pathname)));
                    filteredKeys = (ArrayList<String>)d.readObject();
                    d.close();
                    fViewer.refresh();
                }
                catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
            };
        };
        loadAction.setText("Load filter from XML file");
        loadAction.setToolTipText("Load filter from XML file");

    }

    private void hookDoubleClickAction() {
        /*
         * viewer.addDoubleClickListener(new IDoubleClickListener() { public
         * void doubleClick(DoubleClickEvent event) { doubleClickAction.run(); }
         * });
         */
    }

    public void showMessage(String message) {
        /*
         * MessageDialog.openInformation( viewer.getControl().getShell(),
         * "TCF Context View", message);
         */
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        fViewer.getControl().setFocus();
    }

    // Question : In which thread am I ?
    // Answer : UI thread. \
    // Pawel: not necessarily, it could be any thread though in practice it's usually the UI thread.

    public void debugContextChanged(DebugContextEvent event) {
        if (!fViewer.getControl().isDisposed()) {
            Object element = ((StructuredSelection)event.getContext()).getFirstElement();
            fViewer.setInput(element);
        }
        return;
    }

    @Override
    protected Viewer createViewer(Composite parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void createActions() {
        // TODO Auto-generated method stub

    }

    @Override
    protected String getHelpContextId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void configureToolBar(IToolBarManager tbm) {
        // TODO Auto-generated method stub

    }
}
