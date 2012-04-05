/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems - Adapted to TCF
 *******************************************************************************/
package org.eclipse.tcf.internal.cdt.ui.breakpoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.internal.cdt.ui.Activator;
import org.eclipse.tcf.internal.cdt.ui.ImageCache;
import org.eclipse.tcf.internal.debug.model.TCFBreakpointsModel;
import org.eclipse.tcf.internal.debug.model.TCFLaunch;
import org.eclipse.tcf.internal.debug.ui.model.TCFChildren;
import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
import org.eclipse.tcf.internal.debug.ui.model.TCFModelManager;
import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IContextQuery;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.util.TCFDataCache;
import org.eclipse.tcf.util.TCFTask;

public class TCFThreadFilterEditor {

    private static class Context {
        private final String fName;
        private final String fId;
        private final String fParentId;
        private final boolean fIsContainer;
        private final String fScopeId;
        private final String fSessionId;

        Context(IRunControl.RunControlContext ctx, Context parent) {
            this(ctx, parent.fSessionId);
        }
        Context(IRunControl.RunControlContext ctx, String sessionId) {
            String name = ctx.getName() != null ? ctx.getName() : ctx.getID();
            fName = name;
            fSessionId = sessionId;
            fScopeId = sessionId != null ? sessionId + '/' + ctx.getID() : ctx.getID();
            fId = ctx.getID();
            fParentId = ctx.getParentID();
            fIsContainer = ctx.isContainer();
        }
    }

    public class CheckHandler implements ICheckStateListener {
        public void checkStateChanged(CheckStateChangedEvent event) {
            Object element = event.getElement();
            boolean checked = event.getChecked();
            if (checked) {
                getThreadViewer().expandToLevel(element, 1);
            }
            if (element instanceof Context) {
                Context ctx = (Context) element;
                checkContext(ctx, checked);
                updateParentCheckState(ctx);
            } else if (element instanceof ILaunch) {
                checkLaunch((ILaunch) element, checked);
            }
        }

        private void checkLaunch(ILaunch launch, boolean checked) {
            getThreadViewer().setChecked(launch, checked);
            getThreadViewer().setGrayed(launch, false);
            Context[] threads = syncGetContainers((TCFLaunch) launch);
            for (int i = 0; i < threads.length; i++) {
                checkContext(threads[i], checked);
            }
        }

        /**
         * Check or uncheck a context in the tree viewer. When a container
         * is checked, attempt to check all of the containers threads by
         * default. When a container is unchecked, uncheck all its threads.
         */
        private void checkContext(Context ctx, boolean checked) {
            if (ctx.fIsContainer) {
                Context[] threads = syncGetThreads(ctx);
                for (int i = 0; i < threads.length; i++) {
                    checkContext(threads[i], checked);
                }
            }
            checkThread(ctx, checked);
        }

        /**
         * Check or uncheck a thread.
         */
        private void checkThread(Context thread, boolean checked) {
            getThreadViewer().setChecked(thread, checked);
            getThreadViewer().setGrayed(thread, false);
        }

        private void updateParentCheckState(Context thread) {
            Context[] threads;
            Object parent = getContainer(thread);
            if (parent == null) {
                parent = getLaunch(thread);
                if (parent == null) return;
                threads = syncGetContainers((TCFLaunch) parent);
            } else {
                threads = syncGetThreads((Context) parent);
            }
            int checkedNumber = 0;
            int grayedNumber = 0;
            for (int i = 0; i < threads.length; i++) {
                if (getThreadViewer().getGrayed(threads[i])) {
                    ++grayedNumber;
                } else if (getThreadViewer().getChecked(threads[i])) {
                    ++checkedNumber;
                }
            }
            if (checkedNumber + grayedNumber == 0) {
                getThreadViewer().setChecked(parent, false);
                getThreadViewer().setGrayed(parent, false);
            } else if (checkedNumber == threads.length) {
                getThreadViewer().setChecked(parent, true);
                getThreadViewer().setGrayed(parent, false);
            } else {
                getThreadViewer().setGrayChecked(parent, true);
            }
            if (parent instanceof Context) {
                updateParentCheckState((Context) parent);
            }
        }
    }

    public class ThreadFilterContentProvider implements ITreeContentProvider {

        public Object[] getChildren(Object parent) {
            if (parent instanceof Context) {
                return syncGetThreads((Context) parent);
            }
            if (parent instanceof ILaunch) {
                return syncGetContainers((TCFLaunch) parent);
            }
            if (parent instanceof ILaunchManager) {
                return getLaunches();
            }
            return new Object[0];
        }

        public Object getParent(Object element) {
            if (element instanceof Context) {
                Context ctx = (Context) element;
                if (ctx.fParentId == null) {
                    return DebugPlugin.getDefault().getLaunchManager();
                } else {
                    return getContainer(ctx);
                }
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    public class ThreadFilterLabelProvider extends LabelProvider  {

        @Override
        public Image getImage(Object element) {
            if (element instanceof Context) {
                Context ctx = (Context) element;
                if (ctx.fIsContainer) {
                    return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET);
                } else {
                    return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
                }
            }
            if (element instanceof ILaunch) {
                ImageDescriptor desc = DebugUITools.getDefaultImageDescriptor(element);
                if (desc != null) return ImageCache.getImage(desc);
            }
            return null;
        }

        @Override
        public String getText(Object element) {
            if (element instanceof Context) {
                Context ctx = (Context) element;
                return ctx.fName;
            }
            if (element instanceof ILaunch) {
                ILaunchConfiguration config = ((ILaunch) element).getLaunchConfiguration();
                if (config != null) return config.getName();
            }
            return "?";
        }
    }

    private TCFBreakpointThreadFilterPage fPage;
    private CheckboxTreeViewer fThreadViewer;
    private final ThreadFilterContentProvider fContentProvider;
    private final CheckHandler fCheckHandler;
    private final List<Context> fContexts = new ArrayList<Context>();
    private final Map<TCFLaunch, Context[]> fContainersPerLaunch = new HashMap<TCFLaunch, Context[]>();
    private final Map<Context, Context[]> fContextsPerContainer = new HashMap<Context, Context[]>();
    private StackLayout stackLayout;
    private Composite basicPage;
    private Composite advancedPage;
    private Combo scopeExprCombo;
    private ControlDecoration scopeExpressionDecoration;
    private Button radioBasic;
    private Button radioAdvanced;
    
    public class ScopingModeListener implements SelectionListener {
        private Composite fParent;

        public ScopingModeListener(Composite parent) {
            fParent = parent;
        }
        public void widgetSelected(SelectionEvent e) {
            if (radioBasic.getSelection()) {
                stackLayout.topControl = basicPage;
            }    
            else {
                stackLayout.topControl = advancedPage;
            }
            fParent.layout();
        }
        public void widgetDefaultSelected(SelectionEvent e) {
        }
    }
 
    /**
     * Returns the dialog settings or <code>null</code> if none
     *
     * @param create
     *            whether to create the settings
     */
    private IDialogSettings getDialogSettings(boolean create) {
        IDialogSettings settings = Activator.getDefault()
                .getDialogSettings();
        IDialogSettings section = settings.getSection(this.getClass()
                .getName());
        if (section == null & create) {
            section = settings.addNewSection(this.getClass().getName());
        }
        return section;
    }
    
    public TCFThreadFilterEditor(Composite parent, TCFBreakpointThreadFilterPage page) {
        fPage = page;
        fContentProvider = new ThreadFilterContentProvider();
        fCheckHandler = new CheckHandler();
        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayout( new GridLayout(3, false));
        buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setFont(parent.getFont());
        stackLayout = new StackLayout();
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainComposite.setLayout(stackLayout);

        radioBasic = new Button(buttonComposite, SWT.RADIO);
        radioBasic.setText(Messages.TCFThreadFilterQueryButtonBasic);
        radioBasic.addSelectionListener(new ScopingModeListener(mainComposite));
        radioAdvanced = new Button(buttonComposite, SWT.RADIO);
        radioAdvanced.setText(Messages.TCFThreadFilterQueryButtonAdvanced);
        radioAdvanced.addSelectionListener(new ScopingModeListener(mainComposite));
        
        IDialogSettings settings= getDialogSettings(false);
        if (settings != null) {
            boolean basicSelected = settings.getBoolean(Messages.TCFThreadFilterQueryModeButtonState);
            if ( basicSelected ) {
                radioBasic.setSelection(true); 
            }
            else {
                radioAdvanced.setSelection(true);
            }
        }
        else {
            radioBasic.setSelection(true);
        }
        createThreadViewer(mainComposite);
    }

    protected TCFBreakpointThreadFilterPage getPage() {
        return fPage;
    }
    
    private String getBPFilterExpression() {
        String expression = null;
        ICBreakpoint bp = (ICBreakpoint)fPage.getElement().getAdapter(ICBreakpoint.class);
        if (bp != null) {
            IMarker marker = bp.getMarker();
            if (marker != null) {
                try {
                    expression = (String)marker.getAttribute(TCFBreakpointsModel.ATTR_CONTEXT_QUERY);
                }
                catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return expression;
    }
    
    private String[] getAvailableAttributes() {
        String[] result = null;
        TCFLaunch launch = (TCFLaunch)getAttributeLaunch();
        if (launch == null) {
            return result;
        }
        final IChannel channel = launch.getChannel();
        if (channel == null) {
            return result;
        }
        result = new TCFTask<String[]>() {
            public void run() {
                IContextQuery service = channel.getRemoteService(IContextQuery.class);                                        
                service.getAttrNames(new IContextQuery.DoneGetAttrNames() {
                    public void doneGetAttrNames(IToken token, Exception error, String[] attributes) {
                        if (error != null) {
                            done(null);
                        }
                        done(attributes);
                    }
                });
                return;
            }
        }.getE();
        return result;
    }

    boolean missingParameterValue(String expression) {
        boolean result = false;
        int lastIndex = expression.length();
        int fromIndex = 0;
        if (lastIndex != 0) {
            fromIndex = expression.indexOf('=', fromIndex);
            if (fromIndex == -1) {
                result = true;
            }
            else {
                fromIndex = 0;
            }
            while (fromIndex != -1) {
                fromIndex = expression.indexOf('=', fromIndex);
                if (fromIndex != -1) {
                    if (fromIndex == 0 || fromIndex == lastIndex-1) {
                        result = true;
                        break;
                    }
                    else {
                        String testChar = expression.substring(fromIndex-1, fromIndex);
                        String testNextChar = expression.substring(fromIndex+1,fromIndex+2);
                        int foundComma = expression.indexOf(',', fromIndex);
                        if (testChar.matches("[,\\s]") || testNextChar.matches("[,\\s]")) {
                            result = true;
                            break;
                        }
                        else if (foundComma != -1 && expression.indexOf('=', fromIndex) != -1) {
                            result = true;
                        }
                        else {
                            result = false;
                        }
                        fromIndex++;
                    }
                }
            }
        }
        return result;
    }

    private class ExpressionModifier implements ModifyListener {
        public void modifyText(ModifyEvent e) {
            String expression = scopeExprCombo.getText();
            if (missingParameterValue(expression)) {
                scopeExpressionDecoration.show();
                fPage.setErrorMessage(Messages.TCFThreadFilterEditorFormatError);
                fPage.setValid(false);
            }
            else {
                scopeExpressionDecoration.hide();
                fPage.setErrorMessage(null);
                fPage.setValid(true);
            }
        }
    }

    private class ExpressionSelectButton implements Listener {
        
        private Shell parentShell;
        
        public ExpressionSelectButton(Shell shell) {
            parentShell = shell;
        }

        public void handleEvent(Event event) {
            String[] attrsList = getAvailableAttributes();
            String result = null;            
            TCFContextQueryExpressionDialog dlg = new TCFContextQueryExpressionDialog(parentShell, attrsList, scopeExprCombo.getText());
            
            if (dlg.open() == Window.OK) {
                result = dlg.getExpression();
            }
            if (result != null) {
            	scopeExprCombo.setText(result);
            }
        }
    }
    
    private void setupScopeExpressionCombo(IDialogSettings settings, String bpContextQuery) {
        String [] expresionList = null;        
        if ( settings != null ) {
            expresionList = settings.getArray(Messages.TCFThreadFilterQueryExpressionStore);
            if ( expresionList != null ) {
                int index;
                // Find if there is a null entry.
                for(index = 0; index < expresionList.length; index++) {
                    String member = expresionList[index];
                    if (member == null || member.length() == 0) {
                        break;
                    }
                }
                String[] copyList = new String[index];
                int found = -1;
                for (int loop = 0; loop < index; loop++) {
                    copyList[loop] = expresionList[loop];  
                    if (bpContextQuery != null && copyList[loop].equals(bpContextQuery)) {
                        found = loop;
                    }
                }
                if (found != -1) {
                    scopeExprCombo.setItems(copyList);
                    scopeExprCombo.select(found);                            
                }
                else {
                    int pad = 0;
                    if (bpContextQuery != null) {
                        pad = 1;
                    }
                    String[] setList = new String[index+pad];
                    if (bpContextQuery != null) {
                        setList[0] = bpContextQuery;
                    }
                    System.arraycopy(copyList, 0, setList, pad, copyList.length);
                    scopeExprCombo.setItems(setList);
                    if (bpContextQuery != null) {
                        scopeExprCombo.select(0);
                    }
                }
            }
            else if (bpContextQuery != null) {
                scopeExprCombo.setItems(new String[]{bpContextQuery});
                scopeExprCombo.select(0);                            
            }
        }
        else if (bpContextQuery != null) {
            scopeExprCombo.setItems(new String[]{bpContextQuery});
            scopeExprCombo.select(0);            
        }          
    }
    
    private void createThreadViewer(Composite parent) {
        GridData twoColumnLayout = new GridData(SWT.FILL,0, true, false);
        twoColumnLayout.horizontalSpan = 2;
        advancedPage = new Composite(parent,SWT.NONE);  
        advancedPage.setLayout(new GridLayout(2,false));
        advancedPage.setFont(parent.getFont());
        Label epressionLabel = new Label(advancedPage, SWT.NONE);
        epressionLabel.setText(Messages.TCFThreadFilterQueryAdvancedLabel);
        epressionLabel.setFont(advancedPage.getFont());
        epressionLabel.setLayoutData(twoColumnLayout);
        scopeExprCombo = new Combo(advancedPage,SWT.DROP_DOWN);
        GridData comboGridData = new GridData(SWT.FILL,0, true, false);
        comboGridData .horizontalIndent = 5;
        scopeExprCombo.setLayoutData(comboGridData);
        scopeExprCombo.addModifyListener(new ExpressionModifier());
        scopeExpressionDecoration = new ControlDecoration(scopeExprCombo, SWT.LEFT, advancedPage);
        scopeExpressionDecoration.hide();
        scopeExpressionDecoration.setDescriptionText(Messages.TCFThreadFilterEditorFormatError); 
        FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR); 
        scopeExpressionDecoration.setImage(fieldDecoration.getImage());
        
        String bpContextQuery = getBPFilterExpression();
        IDialogSettings settings= getDialogSettings(false);
        setupScopeExpressionCombo(settings, bpContextQuery);
        Button selectExpression = new Button(advancedPage, SWT.PUSH);
        selectExpression.setText(Messages.TCFThreadFilterQueryButtonEdit);
        selectExpression.setLayoutData(new GridData(SWT.RIGHT,0, false, false));
        selectExpression.addListener(SWT.Selection, new ExpressionSelectButton(parent.getShell()));
        
        basicPage = new Composite(parent, SWT.NONE);
        basicPage .setLayout(new GridLayout(1,false));
        basicPage .setFont(parent.getFont());
        Label contextTreeLabel = new Label(basicPage, SWT.NONE);
        contextTreeLabel.setText(Messages.TCFThreadFilterQueryTreeViewLabel); //$NON-NLS-1$
        contextTreeLabel.setFont(basicPage.getFont());
        contextTreeLabel.setLayoutData(new GridData(SWT.FILL,0, true, false));
        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 100;
        fThreadViewer = new CheckboxTreeViewer(basicPage, SWT.BORDER);
        fThreadViewer.addCheckStateListener(fCheckHandler);
        fThreadViewer.getTree().setLayoutData(data);
        fThreadViewer.getTree().setFont(basicPage.getFont());
        fThreadViewer.setContentProvider(fContentProvider);
        fThreadViewer.setLabelProvider(new ThreadFilterLabelProvider());
        fThreadViewer.setInput(DebugPlugin.getDefault().getLaunchManager());
        setInitialCheckedState();        

        if (radioBasic.getSelection()) {
            stackLayout.topControl = basicPage;
        }    
        else {
            stackLayout.topControl = advancedPage;
        }
        parent.layout();        
    }

    protected ILaunch getAttributeLaunch() {
        IAdaptable dbgContext = DebugUITools.getDebugContext();
        return (ILaunch)dbgContext.getAdapter(ILaunch.class);
    }

    protected ILaunch[] getLaunches() {
        Object input = fThreadViewer.getInput();
        if (!(input instanceof ILaunchManager)) {
            return new ILaunch[0];
        }
        List<ILaunch> tcfLaunches = new ArrayList<ILaunch>();
        ILaunch[] launches = ((ILaunchManager) input).getLaunches();
        for (int i = 0; i < launches.length; i++) {
            ILaunch launch = launches[i];
            if (launch instanceof TCFLaunch && !launch.isTerminated()) {
                tcfLaunches.add(launch);
            }
        }
        return tcfLaunches.toArray(new ILaunch[tcfLaunches.size()]);
    }

    /**
     * Returns the root contexts that appear in the tree
     */
    protected Context[] getRootContexts() {
        Object input = fThreadViewer.getInput();
        if (!(input instanceof ILaunchManager)) {
            return new Context[0];
        }
        List<Object> targets = new ArrayList<Object>();
        ILaunch[] launches = ((ILaunchManager) input).getLaunches();
        for (int i = 0; i < launches.length; i++) {
            ILaunch launch = launches[i];
            if (launch instanceof TCFLaunch && !launch.isTerminated()) {
                Context[] targetArray = syncGetContainers((TCFLaunch) launch);
                targets.addAll(Arrays.asList(targetArray));
            }
        }
        return targets.toArray(new Context[targets.size()]);
    }

    protected final CheckboxTreeViewer getThreadViewer() {
        return fThreadViewer;
    }
    
    protected final String getScopeExpression() {
        return  scopeExprCombo.getText();
    }

    /**
     * Sets the initial checked state of the tree viewer. The initial state
     * should reflect the current state of the breakpoint. If the breakpoint has
     * a thread filter in a given thread, that thread should be checked.
     */
    protected void setInitialCheckedState() {
        TCFBreakpointScopeExtension filterExtension = fPage.getFilterExtension();
        if (filterExtension == null) {
            return;
        }
        String[] ctxIds = filterExtension.getThreadFilters();

        // expand all to realize tree items
        getThreadViewer().expandAll();

        if (ctxIds == null) {
            ILaunch[] launches = getLaunches();
            for (ILaunch launch : launches) {
                fCheckHandler.checkLaunch(launch, true);
            }
        } else if (ctxIds.length != 0) {
            for (int i = 0; i < ctxIds.length; i++) {
                String id = ctxIds[i];
                Context ctx = getContext(id);
                if (ctx != null) {
                    fCheckHandler.checkContext(ctx, true);
                    fCheckHandler.updateParentCheckState(ctx);
                } else if (id.indexOf('/') < 0) {
                    for (Context context : fContexts) {
                        if (id.equals(context.fId)) {
                            fCheckHandler.checkContext(context, true);
                            fCheckHandler.updateParentCheckState(context);
                        }
                    }
                }
            }
        }
        // expand checked items only
        getThreadViewer().setExpandedElements(getThreadViewer().getCheckedElements());
    }

    private Context getContainer(Context child) {
        String parentId = child.fSessionId != null ? child.fSessionId + '/' + child.fParentId : child.fParentId;
        return getContext(parentId);
    }

    private Context getContext(String id) {
        for (Context ctx : fContexts) {
            if (ctx.fScopeId.equals(id))
                return ctx;
        }
        return null;
    }
    
    void updateExpressionsDialogSettings(IDialogSettings settings, String scopedExpression) {
        String[] list = settings.getArray(Messages.TCFThreadFilterQueryExpressionStore);
        if (list == null) {
            list = new String[20];
        }
        for(int i=0; i < list.length; i++) {
            String member = list[i];
            if (member != null && member.equals(scopedExpression)) {
                return;
            }
            else if (member == null) {
                list[i] = scopedExpression;
                settings.put(Messages.TCFThreadFilterQueryExpressionStore, list);
                return;
            }
        }
        String[] copyList = new String[20];
        copyList[0] = scopedExpression;
        System.arraycopy(list, 0, copyList, 1, list.length-1);
        settings.put(Messages.TCFThreadFilterQueryExpressionStore, copyList);
    }
    
    protected void doStore() {
        IDialogSettings settings= getDialogSettings(true);
        if (settings != null) {
            settings.put(Messages.TCFThreadFilterQueryModeButtonState, radioBasic.getSelection());
        }
        if (radioAdvanced.getSelection()) {
            String scopedExpression = getScopeExpression();
            updateExpressionsDialogSettings(settings, scopedExpression);
            TCFBreakpointScopeExtension filterExtension = fPage.getFilterExtension();
            if (filterExtension == null) return;
            filterExtension.setPropertiesFilter(scopedExpression);
        }
        if (radioBasic.getSelection()) {
            CheckboxTreeViewer viewer = getThreadViewer();
            Object[] elements = viewer.getCheckedElements();
            String[] threadIds;
            List<String> checkedIds = new ArrayList<String>();
            for (int i = 0; i < elements.length; ++i) {
                if (elements[i] instanceof Context) {
                    Context ctx = (Context) elements[i];
                    if (!viewer.getGrayed(ctx)) {
                        checkedIds.add(ctx.fScopeId);
                    }
                }
            }
            if (checkedIds.size() == fContexts.size()) {
                threadIds = null;
            }
            else {
                threadIds = checkedIds.toArray(new String[checkedIds.size()]);
            }
            TCFBreakpointScopeExtension filterExtension = fPage.getFilterExtension();
            if (filterExtension == null) return;
            filterExtension.setThreadFilter(threadIds);        
        }
    }

    private Context[] syncGetContainers(final TCFLaunch launch) {
        Context[] result = fContainersPerLaunch.get(launch);
        if (result != null) {
            return result;
        }
        final String launchCfgName = launch.getLaunchConfiguration().getName();
        result = new TCFTask<Context[]>(launch.getChannel()) {
            public void run() {
                List<Context> containers = new ArrayList<Context>();
                TCFChildren children = TCFModelManager.getModelManager().getRootNode(launch).getChildren();
                if (!children.validate(this)) return;
                Map<String, TCFNode> childMap = children.getData();
                for (TCFNode node : childMap.values()) {
                    if (node instanceof TCFNodeExecContext) {
                        TCFNodeExecContext exeCtx = (TCFNodeExecContext) node;
                        TCFDataCache<IRunControl.RunControlContext> runCtxCache = exeCtx.getRunContext();
                        if (!runCtxCache.validate(this)) return;
                        IRunControl.RunControlContext runCtx = runCtxCache.getData();
                        containers.add(new Context(runCtx, launchCfgName));
                    }
                }
                done(containers.toArray(new Context[containers.size()]));
            }
        }.getE();
        fContexts.addAll(Arrays.asList(result));
        fContainersPerLaunch.put(launch, result);
        return result;
    }

    private Context[] syncGetThreads(final Context container) {
        Context[] result = fContextsPerContainer.get(container);
        if (result != null) {
            return result;
        }
        final TCFLaunch launch = getLaunch(container);
        result = new TCFTask<Context[]>(launch.getChannel()) {
            public void run() {
                List<Context> contexts = new ArrayList<Context>();
                TCFModel model = TCFModelManager.getModelManager().getModel(launch);
                TCFChildren children = ((TCFNodeExecContext) model.getNode(container.fId)).getChildren();
                if (!children.validate(this)) return;
                Collection<TCFNode> childNodes = children.getData().values();
                TCFNode[] nodes = childNodes.toArray(new TCFNode[childNodes.size()]);
                Arrays.sort(nodes);
                for (TCFNode node : nodes) {
                    if (node instanceof TCFNodeExecContext) {
                        TCFNodeExecContext exeCtx = (TCFNodeExecContext) node;
                        TCFDataCache<IRunControl.RunControlContext> runCtxCache = exeCtx.getRunContext();
                        if (!runCtxCache.validate(this)) return;
                        IRunControl.RunControlContext runCtx = runCtxCache.getData();
                        contexts.add(new Context(runCtx, container));
                    }
                }
                done(contexts.toArray(new Context[contexts.size()]));
            }
        }.getE();
        fContextsPerContainer.put(container, result);
        fContexts.addAll(Arrays.asList(result));
        return result;
    }

    private TCFLaunch getLaunch(Context container) {
        Context parent = getContainer(container);
        while (parent != null) {
            container = parent;
            parent = getContainer(container);
        }
        for (TCFLaunch launch : fContainersPerLaunch.keySet()) {
            Context[] containers = fContainersPerLaunch.get(launch);
            for (Context context : containers) {
                if (context.fScopeId.equals(container.fScopeId)) {
                    return launch;
                }
            }
        }
        return null;
    }
}
