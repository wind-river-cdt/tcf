/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.launch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate;
import org.eclipse.tcf.internal.debug.launch.TCFLocalAgent;
import org.eclipse.tcf.internal.debug.launch.TCFUserDefPeer;
import org.eclipse.tcf.internal.debug.tests.TCFTestSuite;
import org.eclipse.tcf.internal.debug.ui.Activator;
import org.eclipse.tcf.internal.debug.ui.ImageCache;
import org.eclipse.tcf.internal.debug.ui.launch.setup.SetupWizardDialog;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.ILocator;
import org.eclipse.tcf.services.IMemoryMap;
import org.eclipse.tcf.services.IPathMap;
import org.eclipse.tcf.util.TCFTask;


/**
 * Launch configuration dialog tab to specify the Target Communication Framework
 * configuration.
 */
public class TCFTargetTab extends AbstractLaunchConfigurationTab {

    private static final String TAB_ID = "org.eclipse.tcf.launch.targetTab";

    private Button run_local_agent_button;
    private Button use_local_agent_button;
    private Text peer_id_text;
    private Tree peer_tree;
    private Runnable update_peer_buttons;
    private final PeerInfo peer_info = new PeerInfo();
    private Display display;
    private Exception init_error;
    private String mem_map_cfg;

    private static class PeerInfo {
        PeerInfo parent;
        String id;
        Map<String,String> attrs;
        PeerInfo[] children;
        boolean children_pending;
        Throwable children_error;
        IPeer peer;
        IChannel channel;
        ILocator locator;
        LocatorListener listener;
    }

    private class LocatorListener implements ILocator.LocatorListener {

        private final PeerInfo parent;

        LocatorListener(PeerInfo parent) {
            this.parent = parent;
        }

        public void peerAdded(final IPeer peer) {
            if (display == null) return;
            final String id = peer.getID();
            final HashMap<String,String> attrs = new HashMap<String,String>(peer.getAttributes());
            display.asyncExec(new Runnable() {
                public void run() {
                    if (parent.children_error != null) return;
                    PeerInfo[] arr = parent.children;
                    for (PeerInfo p : arr) assert !p.id.equals(id);
                    PeerInfo[] buf = new PeerInfo[arr.length + 1];
                    System.arraycopy(arr, 0, buf, 0, arr.length);
                    PeerInfo info = new PeerInfo();
                    info.parent = parent;
                    info.id = id;
                    info.attrs = attrs;
                    info.peer = peer;
                    buf[arr.length] = info;
                    parent.children = buf;
                    updateItems(parent);
                }
            });
        }

        public void peerChanged(final IPeer peer) {
            if (display == null) return;
            final String id = peer.getID();
            final HashMap<String,String> attrs = new HashMap<String,String>(peer.getAttributes());
            display.asyncExec(new Runnable() {
                public void run() {
                    if (parent.children_error != null) return;
                    PeerInfo[] arr = parent.children;
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].id.equals(id)) {
                            arr[i].attrs = attrs;
                            arr[i].peer = peer;
                            updateItems(parent);
                        }
                    }
                }
            });
        }

        public void peerRemoved(final String id) {
            if (display == null) return;
            display.asyncExec(new Runnable() {
                public void run() {
                    if (parent.children_error != null) return;
                    PeerInfo[] arr = parent.children;
                    PeerInfo[] buf = new PeerInfo[arr.length - 1];
                    int j = 0;
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].id.equals(id)) {
                            final PeerInfo info = arr[i];
                            Protocol.invokeLater(new Runnable() {
                                public void run() {
                                    disconnectPeer(info);
                                }
                            });
                        }
                        else {
                            buf[j++] = arr[i];
                        }
                    }
                    parent.children = buf;
                    updateItems(parent);
                }
            });
        }

        public void peerHeartBeat(final String id) {
            if (display == null) return;
            display.asyncExec(new Runnable() {
                public void run() {
                    if (parent.children_error != null) return;
                    PeerInfo[] arr = parent.children;
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].id.equals(id)) {
                            if (arr[i].children_error != null) {
                                TreeItem item = findItem(arr[i]);
                                boolean visible = item != null;
                                while (visible && item != null) {
                                    if (!item.getExpanded()) visible = false;
                                    item = item.getParentItem();
                                }
                                if (visible) loadChildren(arr[i]);
                            }
                            break;
                        }
                    }
                }
            });
        }
    }

    public void createControl(Composite parent) {
        display = parent.getDisplay();
        assert display != null;

        Font font = parent.getFont();
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        comp.setLayout(layout);
        comp.setFont(font);

        GridData gd = new GridData(GridData.FILL_BOTH);
        comp.setLayoutData(gd);
        setControl(comp);
        createVerticalSpacer(comp, 1);
        createLocalAgentButtons(comp);
        createVerticalSpacer(comp, 1);
        createTargetGroup(comp);
    }

    private void createLocalAgentButtons(Composite parent) {
        Composite local_agent_comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        local_agent_comp.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        local_agent_comp.setLayoutData(gd);

        run_local_agent_button = createCheckButton(local_agent_comp, "Run instance of TCF agent on the local host");
        run_local_agent_button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent evt) {
                updateLaunchConfigurationDialog();
            }
        });
        run_local_agent_button.setEnabled(true);

        use_local_agent_button = createCheckButton(local_agent_comp, "Use local host as the target");
        use_local_agent_button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent evt) {
                updateLaunchConfigurationDialog();
            }
        });
        use_local_agent_button.setEnabled(true);
    }

    private void createTargetGroup(Composite parent) {
        Font font = parent.getFont();

        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 0;
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setFont(font);
        group.setText("Target");

        createVerticalSpacer(group, layout.numColumns);

        Label host_label = new Label(group, SWT.NONE);
        host_label.setText("Target ID:");
        host_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        host_label.setFont(font);

        peer_id_text = new Text(group, SWT.SINGLE | SWT.BORDER);
        peer_id_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        peer_id_text.setFont(font);
        peer_id_text.setEditable(false);

        createVerticalSpacer(group, layout.numColumns);

        Label peer_label = new Label(group, SWT.NONE);
        peer_label.setText("&Available targets:");
        peer_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        peer_label.setFont(font);

        loadChildren(peer_info);
        createPeerListArea(group);
    }

    private void createPeerListArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));

        peer_tree = new Tree(composite, SWT.VIRTUAL | SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.minimumHeight = 150;
        gd.minimumWidth = 470;
        peer_tree.setLayoutData(gd);

        for (int i = 0; i < 6; i++) {
            TreeColumn column = new TreeColumn(peer_tree, SWT.LEAD, i);
            column.setMoveable(true);
            switch (i) {
            case 0:
                column.setText("Name");
                column.setWidth(160);
                break;
            case 1:
                column.setText("OS");
                column.setWidth(100);
                break;
            case 2:
                column.setText("User");
                column.setWidth(100);
                break;
            case 3:
                column.setText("Transport");
                column.setWidth(60);
                break;
            case 4:
                column.setText("Host");
                column.setWidth(100);
                break;
            case 5:
                column.setText("Port");
                column.setWidth(40);
                break;
            }
        }

        peer_tree.setHeaderVisible(true);
        peer_tree.setFont(font);
        peer_tree.addListener(SWT.SetData, new Listener() {
            public void handleEvent(Event event) {
                TreeItem item = (TreeItem)event.item;
                PeerInfo info = findPeerInfo(item);
                if (info == null) {
                    updateItems(item.getParentItem(), false);
                }
                else {
                    fillItem(item, info);
                    updateLaunchConfigurationDialog();
                }
            }
        });
        peer_tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                final PeerInfo info = findPeerInfo(peer_id_text.getText());
                if (info == null) return;
                new PeerPropsDialog(getShell(), getImage(), info.attrs,
                        info.peer instanceof TCFUserDefPeer).open();
                if (!(info.peer instanceof TCFUserDefPeer)) return;
                Protocol.invokeLater(new Runnable() {
                    public void run() {
                        ((TCFUserDefPeer)info.peer).updateAttributes(info.attrs);
                        TCFUserDefPeer.savePeers();
                    }
                });
            }
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] selections = peer_tree.getSelection();
                if (selections.length > 0) {
                    assert selections.length == 1;
                    PeerInfo info = findPeerInfo(selections[0]);
                    if (info != null) peer_id_text.setText(getPath(info));
                }
                updateLaunchConfigurationDialog();
            }
        });
        peer_tree.addTreeListener(new TreeListener() {

            public void treeCollapsed(TreeEvent e) {
                updateItems((TreeItem)e.item, false);
            }

            public void treeExpanded(TreeEvent e) {
                updateItems((TreeItem)e.item, true);
            }
        });

        createPeerButtons(composite);
    }

    private void createPeerButtons(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
        Menu menu = new Menu(peer_tree);
        SelectionAdapter sel_adapter = null;

        final Button button_new = new Button(composite, SWT.PUSH);
        button_new.setText("N&ew...");
        button_new.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button_new.addSelectionListener(sel_adapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final Map<String,String> attrs = new HashMap<String,String>();
                SetupWizardDialog wizard = new SetupWizardDialog(attrs);
                WizardDialog dialog = new WizardDialog(getShell(), wizard);
                dialog.create();
                if (dialog.open() != Window.OK) return;
                if (attrs.isEmpty()) return;
                Protocol.invokeLater(new Runnable() {
                    public void run() {
                        new TCFUserDefPeer(attrs);
                        TCFUserDefPeer.savePeers();
                    }
                });
            }
        });
        final MenuItem item_new = new MenuItem(menu, SWT.PUSH);
        item_new.setText("N&ew...");
        item_new.addSelectionListener(sel_adapter);

        final Button button_edit = new Button(composite, SWT.PUSH);
        button_edit.setText("E&dit...");
        button_edit.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button_edit.addSelectionListener(sel_adapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final PeerInfo info = findPeerInfo(peer_id_text.getText());
                if (info == null) return;
                if (new PeerPropsDialog(getShell(), getImage(), info.attrs,
                        info.peer instanceof TCFUserDefPeer).open() != Window.OK) return;
                if (!(info.peer instanceof TCFUserDefPeer)) return;
                Protocol.invokeLater(new Runnable() {
                    public void run() {
                        ((TCFUserDefPeer)info.peer).updateAttributes(info.attrs);
                        TCFUserDefPeer.savePeers();
                    }
                });
            }
        });
        final MenuItem item_edit = new MenuItem(menu, SWT.PUSH);
        item_edit.setText("E&dit...");
        item_edit.addSelectionListener(sel_adapter);

        final Button button_remove = new Button(composite, SWT.PUSH);
        button_remove.setText("&Remove");
        button_remove.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button_remove.addSelectionListener(sel_adapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final PeerInfo info = findPeerInfo(peer_id_text.getText());
                if (info == null) return;
                if (!(info.peer instanceof TCFUserDefPeer)) return;
                peer_id_text.setText("");
                updateLaunchConfigurationDialog();
                Protocol.invokeAndWait(new Runnable() {
                    public void run() {
                        ((TCFUserDefPeer)info.peer).dispose();
                        TCFUserDefPeer.savePeers();
                    }
                });
            }
        });
        final MenuItem item_remove = new MenuItem(menu, SWT.PUSH);
        item_remove.setText("&Remove");
        item_remove.addSelectionListener(sel_adapter);

        createVerticalSpacer(composite, 20);
        new MenuItem(menu, SWT.SEPARATOR);

        final Button button_test = new Button(composite, SWT.PUSH);
        button_test.setText("Run &Tests");
        button_test.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button_test.addSelectionListener(sel_adapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                runDiagnostics(false);
            }
        });
        final MenuItem item_test = new MenuItem(menu, SWT.PUSH);
        item_test.setText("Run &Tests");
        item_test.addSelectionListener(sel_adapter);

        final Button button_loop = new Button(composite, SWT.PUSH);
        button_loop.setText("Tests &Loop");
        button_loop.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button_loop.addSelectionListener(sel_adapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                runDiagnostics(true);
            }
        });
        final MenuItem item_loop = new MenuItem(menu, SWT.PUSH);
        item_loop.setText("Tests &Loop");
        item_loop.addSelectionListener(sel_adapter);

        peer_tree.setMenu(menu);

        update_peer_buttons = new Runnable() {

            public void run() {
                boolean local = use_local_agent_button.getSelection();
                PeerInfo info = findPeerInfo(peer_id_text.getText());
                button_new.setEnabled(!local);
                button_edit.setEnabled(info != null && !local);
                button_remove.setEnabled(info != null && info.peer instanceof TCFUserDefPeer && !local);
                button_test.setEnabled(local || info != null);
                button_loop.setEnabled(local || info != null);
                item_new.setEnabled(!local);
                item_edit.setEnabled(info != null && !local);
                item_remove.setEnabled(info != null && info.peer instanceof TCFUserDefPeer && !local);
                item_test.setEnabled(info != null);
                item_loop.setEnabled(info != null);
            }
        };
        update_peer_buttons.run();
    }

    @Override
    protected void updateLaunchConfigurationDialog() {
        if (use_local_agent_button.getSelection()) {
            peer_tree.setEnabled(false);
            peer_tree.deselectAll();
            String id = TCFLocalAgent.getLocalAgentID();
            if (id == null) id = "";
            peer_id_text.setText(id);
            peer_id_text.setEnabled(false);
        }
        else {
            peer_tree.setEnabled(true);
            peer_id_text.setEnabled(true);
            String id = peer_id_text.getText();
            TreeItem item = findItem(id);
            if (item != null) peer_tree.setSelection(item);
            else peer_tree.deselectAll();
        }
        update_peer_buttons.run();
        super.updateLaunchConfigurationDialog();
    }

    @Override
    public void dispose() {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                disconnectPeer(peer_info);
                display = null;
            }
        });
        super.dispose();
    }

    public String getName() {
        return "Target";
    }

    @Override
    public Image getImage() {
        return ImageCache.getImage(ImageCache.IMG_TARGET_TAB);
    }

    @Override
    public String getId() {
        return TAB_ID;
    }

    public void initializeFrom(ILaunchConfiguration configuration) {
        setErrorMessage(null);
        setMessage(null);
        try {
            String id = configuration.getAttribute(TCFLaunchDelegate.ATTR_PEER_ID, "");
            TreeItem item = findItem(id);
            if (item != null) peer_tree.setSelection(item);
            peer_id_text.setText(id);
            run_local_agent_button.setSelection(configuration.getAttribute(TCFLaunchDelegate.ATTR_RUN_LOCAL_AGENT, false));
            use_local_agent_button.setSelection(configuration.getAttribute(TCFLaunchDelegate.ATTR_USE_LOCAL_AGENT, true));
            mem_map_cfg = configuration.getAttribute(TCFLaunchDelegate.ATTR_MEMORY_MAP, "null");
        }
        catch (CoreException e) {
            init_error = e;
            setErrorMessage("Cannot read launch configuration: " + e);
            Activator.log(e);
        }
        updateLaunchConfigurationDialog();
    }

    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        if (use_local_agent_button.getSelection()) {
            configuration.removeAttribute(TCFLaunchDelegate.ATTR_PEER_ID);
        }
        else {
            configuration.setAttribute(TCFLaunchDelegate.ATTR_PEER_ID, peer_id_text.getText());
        }
        configuration.setAttribute(TCFLaunchDelegate.ATTR_RUN_LOCAL_AGENT, run_local_agent_button.getSelection());
        configuration.setAttribute(TCFLaunchDelegate.ATTR_USE_LOCAL_AGENT, use_local_agent_button.getSelection());
    }

    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(TCFLaunchDelegate.ATTR_RUN_LOCAL_AGENT, false);
        configuration.setAttribute(TCFLaunchDelegate.ATTR_USE_LOCAL_AGENT, true);
        configuration.removeAttribute(TCFLaunchDelegate.ATTR_PEER_ID);
    }

    @Override
    public boolean isValid(ILaunchConfiguration config) {
        setErrorMessage(null);
        setMessage(null);

        if (init_error != null) {
            setErrorMessage("Cannot read launch configuration: " + init_error);
            return false;
        }

        return true;
    }

    private void disconnectPeer(final PeerInfo info) {
        assert Protocol.isDispatchThread();
        if (info.children != null) {
            for (PeerInfo p : info.children) disconnectPeer(p);
        }
        if (info.listener != null) {
            info.locator.removeListener(info.listener);
            info.listener = null;
            info.locator = null;
        }
        if (info.channel != null) {
            info.channel.close();
        }
    }

    private boolean canHaveChildren(PeerInfo parent) {
        return parent == peer_info || parent.attrs.get(IPeer.ATTR_PROXY) != null;
    }

    private void loadChildren(final PeerInfo parent) {
        assert Thread.currentThread() == display.getThread();
        if (parent.children_pending) return;
        assert parent.children == null;
        parent.children_pending = true;
        parent.children_error = null;
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                assert parent.listener == null;
                assert parent.channel == null;
                if (!canHaveChildren(parent)) {
                    doneLoadChildren(parent, null, new PeerInfo[0]);
                }
                else if (parent == peer_info) {
                    peer_info.locator = Protocol.getLocator();
                    doneLoadChildren(parent, null, createLocatorListener(peer_info));
                }
                else {
                    final IChannel channel = parent.peer.openChannel();
                    parent.channel = channel;
                    parent.channel.addChannelListener(new IChannel.IChannelListener() {
                        boolean opened = false;
                        boolean closed = false;
                        public void congestionLevel(int level) {
                        }
                        public void onChannelClosed(final Throwable error) {
                            assert !closed;
                            if (parent.channel != channel) return;
                            if (!opened) {
                                doneLoadChildren(parent, error, null);
                            }
                            else {
                                if (display != null) {
                                    display.asyncExec(new Runnable() {
                                        public void run() {
                                            if (parent.children_pending) return;
                                            parent.children = null;
                                            parent.children_error = error;
                                            updateItems(parent);
                                        }
                                    });
                                }
                            }
                            closed = true;
                            parent.channel = null;
                            parent.locator = null;
                            parent.listener = null;
                        }
                        public void onChannelOpened() {
                            assert !opened;
                            assert !closed;
                            if (parent.channel != channel) return;
                            opened = true;
                            parent.locator = parent.channel.getRemoteService(ILocator.class);
                            if (parent.locator == null) {
                                parent.channel.terminate(new Exception("Service not supported: " + ILocator.NAME));
                            }
                            else {
                                doneLoadChildren(parent, null, createLocatorListener(parent));
                            }
                        }
                    });
                }
            }
        });
    }

    private PeerInfo[] createLocatorListener(PeerInfo peer) {
        assert Protocol.isDispatchThread();
        Map<String,IPeer> map = peer.locator.getPeers();
        PeerInfo[] buf = new PeerInfo[map.size()];
        int n = 0;
        for (IPeer p : map.values()) {
            PeerInfo info = new PeerInfo();
            info.parent = peer;
            info.id = p.getID();
            info.attrs = new HashMap<String,String>(p.getAttributes());
            info.peer = p;
            buf[n++] = info;
        }
        peer.listener = new LocatorListener(peer);
        peer.locator.addListener(peer.listener);
        return buf;
    }

    private void doneLoadChildren(final PeerInfo parent, final Throwable error, final PeerInfo[] children) {
        assert Protocol.isDispatchThread();
        assert error == null || children == null;
        if (display == null) return;
        display.asyncExec(new Runnable() {
            public void run() {
                assert parent.children_pending;
                assert parent.children == null;
                parent.children_pending = false;
                parent.children = children;
                parent.children_error = error;
                updateItems(parent);
            }
        });
    }

    private ArrayList<PeerInfo> filterPeerList(PeerInfo parent, boolean expanded) {
        ArrayList<PeerInfo> lst = new ArrayList<PeerInfo>();
        HashMap<String,PeerInfo> local_agents = new HashMap<String,PeerInfo>();
        HashSet<String> ids = new HashSet<String>();
        for (PeerInfo p : parent.children) {
            String id = p.attrs.get(IPeer.ATTR_AGENT_ID);
            if (id == null) continue;
            if (!"TCP".equals(p.attrs.get(IPeer.ATTR_TRANSPORT_NAME))) continue;
            if (!"127.0.0.1".equals(p.attrs.get(IPeer.ATTR_IP_HOST))) continue;
            local_agents.put(id, p);
            ids.add(p.id);
        }
        for (PeerInfo p : parent.children) {
            PeerInfo i = local_agents.get(p.attrs.get(IPeer.ATTR_AGENT_ID));
            if (i != null && i != p) continue;
            lst.add(p);
        }
        if (parent != peer_info && expanded) {
            for (PeerInfo p : peer_info.children) {
                if (p.peer instanceof TCFUserDefPeer && !ids.contains(p.id)) {
                    PeerInfo x = new PeerInfo();
                    x.parent = parent;
                    x.id = p.id;
                    x.attrs = p.attrs;
                    x.peer = p.peer;
                    ids.add(x.id);
                    lst.add(x);
                }
            }
        }
        return lst;
    }

    private void updateItems(TreeItem parent_item, boolean reload) {
        final PeerInfo parent_info = findPeerInfo(parent_item);
        if (parent_info == null) {
            parent_item.setText("Invalid");
        }
        else {
            if (reload && parent_info.children_error != null) {
                loadChildren(parent_info);
            }
            display.asyncExec(new Runnable() {
                public void run() {
                    updateItems(parent_info);
                }
            });
        }
    }

    private void updateItems(final PeerInfo parent) {
        if (display == null) return;
        assert Thread.currentThread() == display.getThread();
        TreeItem[] items = null;
        boolean expanded = true;
        if (parent.children == null || parent.children_error != null) {
            if (parent == peer_info) {
                peer_tree.setItemCount(1);
                items = peer_tree.getItems();
            }
            else {
                TreeItem item = findItem(parent);
                if (item == null) return;
                expanded = item.getExpanded();
                item.setItemCount(1);
                items = item.getItems();
            }
            assert items.length == 1;
            if (parent.children_pending) {
                items[0].setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                fillItem(items[0], "Connecting...");
            }
            else if (parent.children_error != null) {
                String msg = parent.children_error.getMessage();
                if (msg == null) msg = parent.children_error.getClass().getName();
                else msg = msg.replace('\n', ' ');
                items[0].setForeground(display.getSystemColor(SWT.COLOR_RED));
                fillItem(items[0], msg);
            }
            else if (expanded) {
                loadChildren(parent);
                items[0].setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                fillItem(items[0], "Connecting...");
            }
            else {
                Protocol.invokeAndWait(new Runnable() {
                    public void run() {
                        disconnectPeer(parent);
                    }
                });
                fillItem(items[0], "");
            }
        }
        else {
            ArrayList<PeerInfo> lst = null;
            if (parent == peer_info) {
                lst = filterPeerList(parent, expanded);
                peer_tree.setItemCount(lst.size());
                items = peer_tree.getItems();
            }
            else {
                TreeItem item = findItem(parent);
                if (item == null) return;
                expanded = item.getExpanded();
                lst = filterPeerList(parent, expanded);
                item.setItemCount(expanded && lst.size() > 0 ? lst.size() : 1);
                items = item.getItems();
            }
            if (expanded && lst.size() > 0) {
                assert items.length == lst.size();
                for (int i = 0; i < items.length; i++) fillItem(items[i], lst.get(i));
            }
            else if (expanded) {
                fillItem(items[0], "No peers");
            }
            else {
                Protocol.invokeAndWait(new Runnable() {
                    public void run() {
                        disconnectPeer(parent);
                    }
                });
                fillItem(items[0], "");
            }
        }
        updateLaunchConfigurationDialog();
    }

    private TreeItem findItem(String path) {
        assert Thread.currentThread() == display.getThread();
        if (path == null) return null;
        int z = path.lastIndexOf('/');
        if (z < 0) {
            int n = peer_tree.getItemCount();
            for (int i = 0; i < n; i++) {
                TreeItem x = peer_tree.getItem(i);
                PeerInfo p = (PeerInfo)x.getData("TCFPeerInfo");
                if (p != null && p.id.equals(path)) return x;
            }
        }
        else {
            TreeItem y = findItem(path.substring(0, z));
            if (y == null) return null;
            String id = path.substring(z + 1);
            int n = y.getItemCount();
            for (int i = 0; i < n; i++) {
                TreeItem x = y.getItem(i);
                PeerInfo p = (PeerInfo)x.getData("TCFPeerInfo");
                if (p != null && p.id.equals(id)) return x;
            }
        }
        return null;
    }

    private TreeItem findItem(PeerInfo info) {
        if (info == null) return null;
        assert info.parent != null;
        if (info.parent == peer_info) {
            int n = peer_tree.getItemCount();
            for (int i = 0; i < n; i++) {
                TreeItem x = peer_tree.getItem(i);
                if (x.getData("TCFPeerInfo") == info) return x;
            }
        }
        else {
            TreeItem y = findItem(info.parent);
            if (y == null) return null;
            int n = y.getItemCount();
            for (int i = 0; i < n; i++) {
                TreeItem x = y.getItem(i);
                if (x.getData("TCFPeerInfo") == info) return x;
            }
        }
        return null;
    }

    private PeerInfo findPeerInfo(String path) {
        TreeItem i = findItem(path);
        if (i == null) return null;
        return (PeerInfo)i.getData("TCFPeerInfo");
    }

    private PeerInfo findPeerInfo(TreeItem item) {
        assert Thread.currentThread() == display.getThread();
        if (item == null) return peer_info;
        return (PeerInfo)item.getData("TCFPeerInfo");
    }

    private void runDiagnostics(boolean loop) {
        IPeer peer = null;
        if (use_local_agent_button.getSelection()) {
            try {
                if (run_local_agent_button.getSelection()) TCFLocalAgent.runLocalAgent();
                final String id = TCFLocalAgent.getLocalAgentID();
                peer = new TCFTask<IPeer>() {
                    public void run() {
                        done(Protocol.getLocator().getPeers().get(id));
                    }
                }.get();
            }
            catch (Throwable err) {
                String msg = err.getLocalizedMessage();
                if (msg == null || msg.length() == 0) msg = err.getClass().getName();
                MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                mb.setText("Error");
                mb.setMessage("Cannot start agent:\n" + msg);
                mb.open();
            }
        }
        else {
            PeerInfo info = findPeerInfo(peer_id_text.getText());
            if (info == null) return;
            peer = info.peer;
        }
        if (peer == null) return;
        final Shell shell = new Shell(getShell(), SWT.TITLE | SWT.PRIMARY_MODAL);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 0;
        layout.numColumns = 2;
        shell.setLayout(layout);
        shell.setText("Running Diagnostics...");
        CLabel label = new CLabel(shell, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        label.setText("Running Diagnostics...");
        final TCFTestSuite[] test = new TCFTestSuite[1];
        Button button_cancel = new Button(shell, SWT.PUSH);
        button_cancel.setText("&Cancel");
        button_cancel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        button_cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Protocol.invokeLater(new Runnable() {
                    public void run() {
                        if (test[0] != null) test[0].cancel();
                    }
                });
            }
        });
        createVerticalSpacer(shell, 0);
        ProgressBar bar = new ProgressBar(shell, SWT.HORIZONTAL);
        bar.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
        shell.setDefaultButton(button_cancel);
        shell.pack();
        shell.setSize(483, shell.getSize().y);
        Rectangle rc0 = getShell().getBounds();
        Rectangle rc1 = shell.getBounds();
        shell.setLocation(rc0.x + (rc0.width - rc1.width) / 2, rc0.y + (rc0.height - rc1.height) / 2);
        shell.setVisible(true);
        runDiagnostics(peer, loop, test, shell, label, bar);
    }

    private void runDiagnostics(final IPeer peer, final boolean loop, final TCFTestSuite[] test,
            final Shell shell, final CLabel label, final ProgressBar bar) {
        final TCFTestSuite.TestListener done = new TCFTestSuite.TestListener() {
            private String last_text = "";
            private int last_count = 0;
            private int last_total = 0;
            public void progress(final String label_text, final int count_done, final int count_total) {
                assert test[0] != null;
                if ((label_text == null || last_text.equals(label_text)) &&
                        last_total == count_total &&
                        (count_done - last_count) / (float)count_total < 0.02f) return;
                if (label_text != null) last_text = label_text;
                last_total = count_total;
                last_count = count_done;
                display.asyncExec(new Runnable() {
                    public void run() {
                        label.setText(last_text);
                        bar.setMinimum(0);
                        bar.setMaximum(last_total);
                        bar.setSelection(last_count);
                    }
                });
            }
            public void done(final Collection<Throwable> errors) {
                assert test[0] != null;
                final boolean b = test[0].isCanceled();
                test[0] = null;
                display.asyncExec(new Runnable() {
                    public void run() {
                        if (errors.size() > 0) {
                            shell.dispose();
                            new TestErrorsDialog(getControl().getShell(),
                                    ImageCache.getImage(ImageCache.IMG_TCF), errors).open();
                        }
                        else if (loop && !b && display != null) {
                            runDiagnostics(peer, true, test, shell, label, bar);
                        }
                        else {
                            shell.dispose();
                        }
                    }
                });
            }
        };
        Protocol.invokeLater(new Runnable() {
            public void run() {
                try {
                    List<IPathMap.PathMapRule> path_map = null;
                    for (ILaunchConfigurationTab t : getLaunchConfigurationDialog().getTabs()) {
                        if (t instanceof TCFPathMapTab) path_map = ((TCFPathMapTab)t).getPathMap();
                    }
                    HashMap<String,ArrayList<IMemoryMap.MemoryRegion>> mem_map = null;
                    if (mem_map_cfg != null) {
                        mem_map = new HashMap<String,ArrayList<IMemoryMap.MemoryRegion>>();
                        TCFLaunchDelegate.parseMemMapsAttribute(mem_map, mem_map_cfg);
                    }
                    boolean enable_tracing =
                            "true".equals(Platform.getDebugOption("org.eclipse.tcf.debug/debug")) &&
                            "true".equals(Platform.getDebugOption("org.eclipse.tcf.debug/debug/tests/runcontrol"));
                    if (enable_tracing) System.setProperty("org.eclipse.tcf.debug.tracing.tests.runcontrol", "true");
                    test[0] = new TCFTestSuite(peer, done, path_map, mem_map);
                }
                catch (Throwable x) {
                    ArrayList<Throwable> errors = new ArrayList<Throwable>();
                    errors.add(x);
                    done.done(errors);
                }
            }
        });
    }

    private void fillItem(TreeItem item, PeerInfo info) {
        assert Thread.currentThread() == display.getThread();
        Object data = item.getData("TCFPeerInfo");
        if (data != null && data != info) item.removeAll();
        item.setData("TCFPeerInfo", info);
        String text[] = new String[6];
        text[0] = info.attrs.get(IPeer.ATTR_NAME);
        text[1] = info.attrs.get(IPeer.ATTR_OS_NAME);
        text[2] = info.attrs.get(IPeer.ATTR_USER_NAME);
        text[3] = info.attrs.get(IPeer.ATTR_TRANSPORT_NAME);
        text[4] = info.attrs.get(IPeer.ATTR_IP_HOST);
        text[5] = info.attrs.get(IPeer.ATTR_IP_PORT);
        for (int i = 0; i < text.length; i++) {
            if (text[i] == null) text[i] = "";
        }
        item.setText(text);
        item.setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
        item.setImage(ImageCache.getImage(getImageName(info)));
        if (!canHaveChildren(info)) item.setItemCount(0);
        else if (info.children == null || info.children_error != null) item.setItemCount(1);
        else item.setItemCount(info.children.length);
    }

    private void fillItem(TreeItem item, String text) {
        item.setText(text);
        int n = peer_tree.getColumnCount();
        for (int i = 1; i < n; i++) item.setText(i, "");
        item.setImage((Image)null);
        item.removeAll();
    }

    private String getPath(PeerInfo info) {
        if (info == peer_info) return "";
        if (info.parent == peer_info) return info.id;
        return getPath(info.parent) + "/" + info.id;
    }

    private String getImageName(PeerInfo info) {
        return ImageCache.IMG_TARGET_TAB;
    }
}
