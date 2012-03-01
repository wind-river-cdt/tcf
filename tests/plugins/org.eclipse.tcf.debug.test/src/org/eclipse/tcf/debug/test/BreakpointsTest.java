package org.eclipse.tcf.debug.test;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.debug.test.BreakpointsListener.EventTester;
import org.eclipse.tcf.debug.test.BreakpointsListener.EventType;
import org.eclipse.tcf.debug.test.services.RunControlCM.ContextState;
import org.eclipse.tcf.debug.test.util.Transaction;
import org.eclipse.tcf.internal.debug.launch.TCFSourceLookupDirector;
import org.eclipse.tcf.internal.debug.model.TCFLaunch;
import org.eclipse.tcf.internal.debug.ui.launch.TCFLaunchContext;
import org.eclipse.tcf.internal.debug.ui.model.TCFModelPresentation;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.ILineNumbers;
import org.eclipse.tcf.services.ILineNumbers.CodeArea;
import org.eclipse.tcf.services.ISymbols.Symbol;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Assert;

public class BreakpointsTest extends AbstractTcfUITest 
{
    private BreakpointsListener fBpListener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fBpListener = new BreakpointsListener();
        
        // CDT Breakpoint integration depends on the TCF-CDT breakpoint 
        // integration to be active.  This is normally triggered by selecting
        // a stack frame in the UI.  Here force activation of the plugin 
        // artificially.  None of the cdt integration packages are exported, so
        // use the TCF Launch Context extension point indirectly to force the 
        // plugin to load.
        TCFLaunchContext.getLaunchContext(null);
    }
    
    @Override
    protected void tearDown() throws Exception {
        fBpListener.dispose();
        super.tearDown();
    }
    
    private CodeArea getFunctionCodeArea(String functionName) throws Exception {
        return new Transaction<CodeArea>() {
            @Override
            protected CodeArea process() throws InvalidCacheException, ExecutionException {
            	ContextState state = validate ( fRunControlCM.getState(fThreadId) );
                String symId = validate ( fSymbolsCM.find(fProcessId, new BigInteger(state.pc), "tcf_test_func0") );
                Symbol sym = validate ( fSymbolsCM.getContext(symId) );
                CodeArea[] area = validate ( fLineNumbersCM.mapToSource(
                		fProcessId,
                		sym.getAddress(),
                		new BigInteger(sym.getAddress().toString()).add(BigInteger.valueOf(1))) );
                return area[0];
            }
        }.get();
    }
    
    private ICLineBreakpoint createLineBreakpoint(String file, int line) throws CoreException, ExecutionException, InterruptedException {
        // Initiate wait for the context changed event.
        final Object contextChangedWaitKey = new Object();
        Protocol.invokeAndWait(new Runnable() { public void run() {
            fBreakpointsCM.waitContextAdded(contextChangedWaitKey);               
        }});
        
        final ICLineBreakpoint bp = CDIDebugModel.createLineBreakpoint(file, ResourcesPlugin.getWorkspace().getRoot(), ICBreakpointType.REGULAR, line, true, 0, "", true);
        
        Map<String, Object>[] addedBps = new Transaction<Map<String, Object>[]>() {
            protected Map<String, Object>[] process() throws InvalidCacheException ,ExecutionException {
                return validate(fBreakpointsCM.waitContextAdded(contextChangedWaitKey));
            }
            
        }.get();

        fBpListener.setTester(new EventTester() {
            public boolean checkEvent(EventType type, IBreakpoint testBp, Map<String, Object> deltaAttributes) {
                return (type == EventType.CHANGED && bp == testBp); 
            }
        });
        
        fBpListener.waitForEvent();
        
        Assert.assertEquals(1, addedBps.length);
        Assert.assertEquals(1, bp.getMarker().getAttribute(ICBreakpoint.INSTALL_COUNT, -1));    
        
        return bp;
    }
    
    public void testContextAddedOnLineBrakpointCreate() throws Exception {
        initProcessModel("tcf_test_func0");
        
        CodeArea bpCodeArea = getFunctionCodeArea("tcf_test_func0");
        ICLineBreakpoint bp = createLineBreakpoint(bpCodeArea.file, bpCodeArea.start_line);
    }

    public void testToggleLineBreakpointCreate() throws Exception {
        initProcessModel("tcf_test_func0");
        
        CodeArea bpCodeArea = getFunctionCodeArea("tcf_test_func0");
        
        ITextEditor editor = openEditor(fProcessId, bpCodeArea);
        ITextSelection selection = selectLine(editor, bpCodeArea);
        
        
        IToggleBreakpointsTarget toggleTarget = DebugUITools.getToggleBreakpointsTargetManager().
            getToggleBreakpointsTarget(editor, selection);
        if (toggleTarget != null) {
            toggleTarget.toggleLineBreakpoints(editor, selection);
        }
        //ICLineBreakpoint bp = createLineBreakpoint(bpCodeArea.file, bpCodeArea.start_line);
    }

    private ITextEditor openEditor(final String mem_id, final ILineNumbers.CodeArea area)  throws ExecutionException
    {
        final ITextEditor[] textEditor = new ITextEditor[1];
        final Throwable[] error = new Throwable[1];
        Display.getDefault().syncExec(new Runnable() { 
            public void run() {
                try {
                    IWorkbenchPage page = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage();
                    
                    String editor_id = null;
                    IEditorInput editor_input = null;
                    int line = 0;
                    if (area != null) {
                        Object source_element = TCFSourceLookupDirector.lookup((TCFLaunch)fLaunch, mem_id, area);
                        if (source_element != null) {
                            ISourcePresentation presentation = TCFModelPresentation.getDefault();
                            editor_input = presentation.getEditorInput(source_element);
                            if (editor_input != null) editor_id = presentation.getEditorId(editor_input, source_element);
                        }
                    }
                    if (page != null && editor_input != null && editor_id != null) {
                        IEditorPart editor = openEditor(editor_input, editor_id, page);
                        if (editor instanceof ITextEditor) {
                            textEditor[0] = (ITextEditor)editor;
                        }
                        else {
                            textEditor[0] = (ITextEditor)editor.getAdapter(ITextEditor.class);
                        }
                    }
                }
                catch (Throwable t) {
                    error[0] = t;
                }
            }
        });
        if (error[0] != null) {
            throw new ExecutionException("Error trying to display source", error[0]);
        }
        Assert.assertNotNull(textEditor[0]);
        return textEditor[0];
    }
    
    private ITextSelection selectLine(final ITextEditor editor, final ILineNumbers.CodeArea area)  throws ExecutionException
    {
        final Throwable[] error = new Throwable[1];
        final ISelection[] selection = new ISelection[1];
        Display.getDefault().syncExec(new Runnable() { 
            public void run() {
                try {
                    IWorkbenchPage page = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage();
                    IRegion region = null;
                    region = getLineInformation(editor, area.start_line);
                    if (region != null) editor.selectAndReveal(region.getOffset(), 0);
                    selection[0] = editor.getSelectionProvider().getSelection();
                }
                catch (Throwable t) {
                    error[0] = t;
                }
            }
        });
        if (error[0] != null) {
            throw new ExecutionException("Error trying to display source", error[0]);
        }
        Assert.assertTrue(selection[0] instanceof ITextSelection);
        return (ITextSelection)selection[0];
    }

    /*
     * Open an editor for given editor input.
     * @param input - IEditorInput representing a source file to be shown in the editor
     * @param id - editor type ID
     * @param page - workbench page that will contain the editor
     * @return - IEditorPart if the editor was opened successfully, or null otherwise.
     */
    private IEditorPart openEditor(final IEditorInput input, final String id, final IWorkbenchPage page) throws PartInitException {
        assert Display.getDefault().getThread() == Thread.currentThread();

        if (page.getWorkbenchWindow().getWorkbench().isClosing()) {
            Assert.fail("Workbench closing");
        }
        IEditorPart editor = page.openEditor(input, id, false, IWorkbenchPage.MATCH_ID|IWorkbenchPage.MATCH_INPUT);
        Assert.assertNotNull("Editor open failed", editor);
        
        return editor;
    }

    /*
     * Returns the line information for the given line in the given editor
     */
    private IRegion getLineInformation(ITextEditor editor, int line) {
        IDocumentProvider provider = editor.getDocumentProvider();
        IEditorInput input = editor.getEditorInput();
        try {
            provider.connect(input);
        }
        catch (CoreException e) {
            return null;
        }
        try {
            IDocument document = provider.getDocument(input);
            if (document != null) return document.getLineInformation(line - 1);
        }
        catch (BadLocationException e) {
        }
        finally {
            provider.disconnect(input);
        }
        return null;
    }


}
