package org.eclipse.tcf.debug.ui.views;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.swt.SWT;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tcf.protocol.Protocol;

public class TCFContextView extends AbstractDebugView implements IDebugContextListener {

    ArrayList<String> filteredKeys;

    Map<String, Object> local_copy;
    
    private class MyTableEntry {
        // Used in the table viewer...
        String key;

        String value;

        public MyTableEntry(String pKey, String pValue) {
            key = pKey;
            value = pValue;
        }
    }

    class MapEntryContentProvider implements IStructuredContentProvider {
        ArrayList<MyTableEntry> localInputElement;

        public MapEntryContentProvider() {
        }

        public Object[] getElements(Object inputElement) {
            return localInputElement.toArray();
        }

        public void dispose() {
            localInputElement.clear();
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            localInputElement = new ArrayList<MyTableEntry>();
            // Convert the map as ArrayList.
            TCFNodeExecContext node = (TCFNodeExecContext)newInput;

            // Do proper validation
            if (node == null) {
                listenerLabel.setText("Wrong TCFNodeExecContext. Try again !");
                return;
            }

            // Sometimes, the suspended_datas can be empty
            if (local_copy == null) {
                listenerLabel.setText("No suspended datas. Try again !");
                return;
            }

            // Convert the local copy as ArrayList for the table viewer.
            for (Map.Entry<String, Object> e : local_copy.entrySet()) {
                System.out.println(e.getKey() + " : " + e.getValue());
                MyTableEntry mt = new MyTableEntry(e.getKey(), e.getValue().toString());
                localInputElement.add(mt);
            }
        }
    }

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
    private TableViewer fTableView;

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

        fTableView = new TableViewer(parent, SWT.FULL_SELECTION | SWT.VIRTUAL);

        // The label provider
        fTableLabelProvider = new MyTableEntryLabelProvider();
        fTableView.setLabelProvider(fTableLabelProvider);

        // The content provider
        fContentProvider = new MapEntryContentProvider();
        fTableView.setContentProvider(fContentProvider);

        Table table = fTableView.getTable();

        // Symbol table title
        String[] tableTitles = new String[] { "Key", "Value" };
        for (int ii = 0; ii < tableTitles.length; ii++) {
            new TableColumn(table, SWT.LEFT).setText(tableTitles[ii]);
        }

        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        // Prepare the table position
        table.getColumn(0).setWidth(200);
        table.getColumn(1).setWidth(400);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Create an empty filteredKey
        filteredKeys = new ArrayList<String>();

        // Filter the keys to be displayed
        ViewerFilter myFilter = new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                MyTableEntry myEntry = (MyTableEntry)element;
                if (filteredKeys.contains(myEntry.key)) return false;
                return true;
            }
        };

        ViewerFilter[] viewerFilterTab = new ViewerFilter[1];
        viewerFilterTab[0] = myFilter;
        fTableView.setFilters(viewerFilterTab);

        // Register to monitor the selection

        IDebugContextService contextService = DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow());
        contextService.addDebugContextListener(this);

        // Create the help context id for the viewer's control
        PlatformUI.getWorkbench().getHelpSystem().setHelp(fTableView.getControl(), "org.eclipse.tcf.debug.ui.viewer");
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
                ISelection selectedItem = fTableView.getSelection();

                if (selectedItem instanceof IStructuredSelection) {
                    IStructuredSelection ss = (IStructuredSelection)selectedItem;
                    if (ss.size() == 1) {
                        Object element = ss.getFirstElement();
                        MyTableEntry myEntry = (MyTableEntry)element;
                        filteredKeys.add(myEntry.key);
                        fTableView.refresh();
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
                fTableView.refresh();
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
                    fTableView.refresh();
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
        fTableView.getControl().setFocus();
    }

    static class MyTableEntryLabelProvider implements ITableLabelProvider {
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            MyTableEntry currentEntry = (MyTableEntry)element;
            if (currentEntry == null) return "";

            switch (columnIndex) {
            case 0:
                return currentEntry.key;
            case 1:
                return currentEntry.value;
            default:
                return "";
            }
        }

        public void addListener(ILabelProviderListener listener) {
        }

        public void dispose() {
        }

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {
        }
    }

    // Question : In which thread are we running ?
    // Answer : In the dispatcher thread.

    private class MyNodeRetrieved implements Runnable {

        TCFNodeExecContext l_node;

        public MyNodeRetrieved(TCFNodeExecContext node) {
            l_node = node;
        }

        public void run() {

            // Validate the cache for the State too.
            if (!l_node.getState().validate()) {
                // Question : How can I tell the UI that something went wrong ??
                return;
            }
            
            // Get the suspend map and copy it locally
            if (l_node.getState().getData().suspend_params == null) {
                local_copy = null;
            } else {
                local_copy = new HashMap<String,Object> (l_node.getState().getData().suspend_params);
            }
            
            // We can ask the UI to refresh now !
            synchronized (Device.class) {
                Display display = Display.getDefault();
                if (!display.isDisposed()) {
                    // This will be executed in the UI thread.
                    display.asyncExec(new Runnable() {
                        public void run() {
                            // Now we have select a TCFNodeExecContext
                            // String result = "Suspended states for target pid  " + node.getSystemMonitor().getData().pid;
                            String result = "Cache retrieved !";
                            listenerLabel.setText(result);

                            // Both caches are available, we can refresh the UI.
                            fTableView.setInput(l_node);
                        }
                    });
                }
            }
            
        }
    }

    // Question : In which thread am I ?
    // Answer : UI thread.

    public void debugContextChanged(DebugContextEvent event) {
        Object element = ((StructuredSelection)event.getContext()).getFirstElement();
        if (element instanceof TCFNodeExecContext) {
            TCFNodeExecContext node = (TCFNodeExecContext)element;

            listenerLabel.setText("Retrieving the cache...");
            // Retrieve the datas from the caches
            try {
                Protocol.invokeAndWait(new MyNodeRetrieved(node));
            }
            catch (Exception e) {
                listenerLabel.setText("Can't get data from caches. Try again..");
            }
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
