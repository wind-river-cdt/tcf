package org.eclipse.tcf.debug.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.debug.test.util.AggregateCallback;
import org.eclipse.tcf.debug.test.util.Callback;
import org.eclipse.tcf.debug.test.util.DataCallback;
import org.eclipse.tcf.debug.test.util.Query;
import org.eclipse.tcf.debug.test.util.Task;
import org.eclipse.tcf.debug.test.util.Callback.ICanceledListener;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IBreakpoints;
import org.eclipse.tcf.services.IDiagnostics;
import org.eclipse.tcf.services.IDiagnostics.ISymbol;
import org.eclipse.tcf.services.IExpressions;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tcf.services.IStackTrace;
import org.eclipse.tcf.services.ISymbols;
import org.junit.Assert;

/**
 * Base test for validating TCF Debugger UI. 
 */
@SuppressWarnings("restriction")
public abstract class AbstractTcfUITest extends TestCase implements IViewerUpdatesListenerConstants {

    private final static int NUM_CHANNELS = 1;

    protected IChannel[] channels;
    
    private Query<Object> fMonitorChannelQuery;
    private List<Throwable> errors = new ArrayList<Throwable>();
    private IPeer peer;
    protected ILaunch fLaunch;
    
    protected VirtualTreeModelViewer fDebugViewViewer;
    protected TestDebugContextProvider fDebugContextProvider; 
    protected VirtualViewerUpdatesListener fDebugViewListener;
    protected VariablesVirtualTreeModelViewer fVariablesViewViewer;
    protected VirtualViewerUpdatesListener fVariablesViewListener;
    protected VariablesVirtualTreeModelViewer fRegistersViewViewer;
    protected VirtualViewerUpdatesListener fRegistersViewListener;

    protected IDiagnostics diag;
    protected IExpressions expr;
    protected ISymbols syms;
    protected IStackTrace stk;
    protected IRunControl rc;
    protected IBreakpoints bp;

    protected TestRunControlListener fRcListener;

    
    private static class RemotePeer extends TransientPeer {
        private final ArrayList<Map<String,String>> attrs;

        public RemotePeer(ArrayList<Map<String,String>> attrs) {
            super(attrs.get(0));
            this.attrs = attrs;
        }

        public IChannel openChannel() {
            assert Protocol.isDispatchThread();
            IChannel c = super.openChannel();
            for (int i = 1; i < attrs.size(); i++) c.redirect(attrs.get(i));
            return c;
        }
    }

    private static IPeer getPeer(String[] arr) {
        ArrayList<Map<String,String>> l = new ArrayList<Map<String,String>>();
        for (String s : arr) {
            Map<String,String> map = new HashMap<String,String>();
            int len = s.length();
            int i = 0;
            while (i < len) {
                int i0 = i;
                while (i < len && s.charAt(i) != '=' && s.charAt(i) != 0) i++;
                int i1 = i;
                if (i < len && s.charAt(i) == '=') i++;
                int i2 = i;
                while (i < len && s.charAt(i) != ':') i++;
                int i3 = i;
                if (i < len && s.charAt(i) == ':') i++;
                String key = s.substring(i0, i1);
                String val = s.substring(i2, i3);
                map.put(key, val);
            }
            l.add(map);
        }
        return new RemotePeer(l);
    }

    protected void setUp() throws Exception {

        createDebugViewViewer();
        createLaunch();
        
        // Command line should contain peer description string, for example:
        // "ID=Test:TransportName=TCP:Host=127.0.0.1:Port=1534"
        final String[] args = new String[] { "TransportName=TCP:Host=127.0.0.1:Port=1534" };
        if (args.length < 1) {
            System.err.println("Missing command line argument - peer identification string");
            System.exit(4);
        }
    
        peer = new Query<IPeer>() {
            @Override
            protected void execute(DataCallback<IPeer> callback) {
                callback.setData(getPeer(args));
                callback.done();
            }
        }.get();
        
        channels = new IChannel[NUM_CHANNELS];
        
        new Query<Object>() {
            @Override
            protected void execute(DataCallback<Object> callback) {
                try {
                    openChannels(peer, callback);
                }
                catch (Throwable x) {
                    errors.add(x);
                    int cnt = 0;
                    for (int i = 0; i < channels.length; i++) {
                        if (channels[i] == null) continue;
                        if (channels[i].getState() != IChannel.STATE_CLOSED) channels[i].close();
                        cnt++;
                    }
                    if (cnt == 0) {
                        callback.setError(errors.get(0));
                        callback.done();
                    }
                }
            }
        }.get();
        
        getRemoteServices();
        
        validateTestAvailable();
        
        new Task<Object>() {
            @Override
            public Object call() throws Exception {
                setUpServiceListeners();
                return null;
            }
        }.get();
    }

    @Override
    protected void tearDown() throws Exception {
        new Task<Object>() {
            @Override
            public Object call() throws Exception {
                tearDownServiceListeners();
                return null;
            }
        }.get();
        
        terminateLaunch();
        disposeDebugViewViewer();
        
        new Query<Object>() {
            @Override
            protected void execute(DataCallback<Object> callback) {
                closeChannels(callback);
            }
        }.get();

        // Check for listener errors at the end of tearDown.
        fRcListener.checkError();
    }

    protected String getDiagnosticsTestName() {
        return "RCBP1";
    }

    protected void setUpServiceListeners() throws Exception{
        fRcListener = new TestRunControlListener(rc);        
    }
    
    protected void tearDownServiceListeners() throws Exception{
        fRcListener.dispose();
    }
    
    private void createDebugViewViewer() {
        final Display display = Display.getDefault();
        display.syncExec(new Runnable() {
            public void run() {
                fDebugViewViewer = new VirtualTreeModelViewer(display, SWT.NONE, new PresentationContext(IDebugUIConstants.ID_DEBUG_VIEW));
                fDebugViewViewer.setInput(DebugPlugin.getDefault().getLaunchManager());
                fDebugViewViewer.setAutoExpandLevel(-1);
                fDebugViewListener = new VirtualViewerUpdatesListener(fDebugViewViewer);
                fDebugContextProvider = new TestDebugContextProvider(fDebugViewViewer);
                fVariablesViewViewer = new VariablesVirtualTreeModelViewer(IDebugUIConstants.ID_VARIABLE_VIEW, fDebugContextProvider);
                fVariablesViewListener = new VirtualViewerUpdatesListener(fVariablesViewViewer);
                fRegistersViewViewer = new VariablesVirtualTreeModelViewer(IDebugUIConstants.ID_REGISTER_VIEW, fDebugContextProvider);
                fRegistersViewListener = new VirtualViewerUpdatesListener(fRegistersViewViewer);
            }
        });
    }

    private void disposeDebugViewViewer() {
        final Display display = Display.getDefault();
        display.syncExec(new Runnable() {
            public void run() {
                fDebugViewListener.dispose();
                fDebugContextProvider.dispose();
                fDebugViewViewer.dispose();
                fVariablesViewListener.dispose();
                fVariablesViewViewer.dispose();
                fRegistersViewListener.dispose();
                fRegistersViewViewer.dispose();
            }
        });
        
    }
    
    private void createLaunch() throws CoreException {
        ILaunchManager lManager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType lcType = lManager.getLaunchConfigurationType("org.eclipse.tcf.debug.LaunchConfigurationType");
        ILaunchConfigurationWorkingCopy lcWc = lcType.newInstance(null, "test");
        lcWc.doSave();
        fLaunch = lcWc.launch("debug", new NullProgressMonitor());
        Assert.assertTrue( fLaunch instanceof IDisconnect ); 
    }
    
    private void terminateLaunch() throws DebugException, InterruptedException, ExecutionException {
        ((IDisconnect)fLaunch).disconnect();

        new Query<Object>() {
            @Override
            protected void execute(final DataCallback<Object> callback) {
                final ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
                
                final AtomicBoolean callbackDone = new AtomicBoolean(false);
                ILaunchesListener2 disconnectListener = new ILaunchesListener2() {
                    public void launchesAdded(ILaunch[] launches) {}
                    public void launchesChanged(ILaunch[] launches) {}
                    public void launchesRemoved(ILaunch[] launches) {}
                    public void launchesTerminated(ILaunch[] launches) {
                        if (Arrays.asList(launches).contains(fLaunch)) {
                            if (!callbackDone.getAndSet(true)) {
                                lm.removeLaunchListener(this);
                                callback.done();
                            }
                        }
                    }
                };
                lm.addLaunchListener(disconnectListener);
                if (((IDisconnect)fLaunch).isDisconnected() && !callbackDone.getAndSet(true)) {
                    lm.removeLaunchListener(disconnectListener);
                    callback.done();
                    
                }
            }
        }.get();
    }
    
    private void getRemoteServices() {
        assert !Protocol.isDispatchThread();
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                diag = channels[0].getRemoteService(IDiagnostics.class);
                expr = channels[0].getRemoteService(IExpressions.class);
                syms = channels[0].getRemoteService(ISymbols.class);
                stk = channels[0].getRemoteService(IStackTrace.class);
                rc = channels[0].getRemoteService(IRunControl.class);
                bp = channels[0].getRemoteService(IBreakpoints.class);
            };
        });
    }
    
    private void openChannels(IPeer peer, Callback callback) {
        assert Protocol.isDispatchThread();
        
        for (int i = 0; i < channels.length; i++) {
            channels[i] = peer.openChannel();
        }
        monitorChannels(
            new Callback(callback) {
                @Override
                protected void handleSuccess() {
                    fMonitorChannelQuery = new Query<Object>() {
                        protected void execute(org.eclipse.tcf.debug.test.util.DataCallback<Object> callback) {
                            monitorChannels(callback, true);
                        };
                    };
                    fMonitorChannelQuery.invoke();
                    super.handleSuccess();
                }
            }, 
            false);
    }

    private void closeChannels(final Callback callback) {
        assert Protocol.isDispatchThread();
        fMonitorChannelQuery.cancel(false);
        try {
            fMonitorChannelQuery.get();
        } catch (ExecutionException e) {
            callback.setError(e.getCause());
        } catch (CancellationException e) {
            // expected
        } catch (InterruptedException e) {
        }
        
        for (int i = 0; i < channels.length; i++) {
            if (channels[i].getState() != IChannel.STATE_CLOSED) channels[i].close();
        }
        monitorChannels(callback, true);
    }

    private static class ChannelMonitorListener implements IChannel.IChannelListener {

        final IChannel fChannel;
        final boolean fClose;
        final Callback fCallback;
        private boolean fActive = true;
        
        private class CancelListener implements ICanceledListener {
            public void requestCanceled(Callback rm) {
                Protocol.invokeLater(new Runnable() {
                    public void run() {
                        if (deactivate()) { 
                            fCallback.done();
                        }
                    }
                });
            }
        }
        
        private boolean deactivate() {
            if (fActive) {
                fChannel.removeChannelListener(ChannelMonitorListener.this);
                fActive = false;
                return true;
            }
            return false;
        }
        
        ChannelMonitorListener (IChannel channel, Callback cb, boolean close) {
            fCallback = cb;
            fClose = close;
            fChannel = channel;
            fChannel.addChannelListener(this);
            fCallback.addCancelListener(new CancelListener());
        }
        
        public void onChannelOpened() {
            if (!deactivate()) return;
            
            fChannel.removeChannelListener(this);
            fCallback.done();
        }

        public void congestionLevel(int level) {
        }

        public void onChannelClosed(Throwable error) {
            if (!deactivate()) return;

            if (!fClose) {
                fCallback.setError( new IOException("Remote peer closed connection before all tests finished") );
            } else {
                fCallback.setError(error);
            }
            fCallback.done();
        }
    }
    
    protected void monitorChannels(final Callback callback, final boolean close) {
        assert Protocol.isDispatchThread();

        AggregateCallback acb = new AggregateCallback(callback);
        int count = 0;
        for (int i = 0; i < channels.length; i++) {
            if (!checkChannelsState(channels[i], close)) {
                new ChannelMonitorListener(channels[i], new Callback(acb), close);
                count++;
            }
        }
        acb.setDoneCount(count);
    }
    
    // Checks whether all channels have achieved the desired state.
    private boolean checkChannelsState(IChannel channel, boolean close) {
        if (close) {
            if (channel.getState() != IChannel.STATE_CLOSED) {
                return false;
            }
        } else {
            if (channel.getState() != IChannel.STATE_OPEN) {
                return false;
            }
        }
        return true;
    }
    
    private void validateTestAvailable() throws ExecutionException, InterruptedException {
        String[] testList = getDiagnosticsTestList();
        
        int i = 0;
        for (; i < testList.length; i++) {
            if ("RCBP1".equals(testList[i])) break;
        }

        Assert.assertTrue("Required test not supported", i != testList.length);
    }
    
    protected String[] getDiagnosticsTestList() throws ExecutionException, InterruptedException {
        assert !Protocol.isDispatchThread();
        return new Query<String[]>() {
            @Override
            protected void execute(final DataCallback<String[]> callback) {
                diag.getTestList(new IDiagnostics.DoneGetTestList() {
                    public void doneGetTestList(IToken token, Throwable error, String[] list) {
                        callback.setData(list);
                        callback.setError(error);
                        callback.done();
                    }
                });
                
            }
        }.get();
    }
    
    protected void setBreakpoint(final String bp_id, final String location) throws InterruptedException, ExecutionException {
        new Query<Object> () {
            protected void execute(final DataCallback<Object> callback) {
                Map<String,Object> m = new HashMap<String,Object>();
                m.put(IBreakpoints.PROP_ID, bp_id);
                m.put(IBreakpoints.PROP_ENABLED, Boolean.TRUE);
                m.put(IBreakpoints.PROP_LOCATION, location);
                bp.set(new Map[]{ m }, new IBreakpoints.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        callback.setError(error);
                        callback.done();
                    }
                });
            }
        }.get();
    }
    
    protected String startDiagnosticsTest() throws InterruptedException, ExecutionException {
        return new Query<String> () {
            protected void execute(final DataCallback<String> callback) {
                diag.runTest(getDiagnosticsTestName(), new IDiagnostics.DoneRunTest() {
                    public void doneRunTest(IToken token, Throwable error, String id) {
                        callback.setData(id);
                        callback.setError(error);
                        callback.done();
                    }
                });
            }
        }.get();
    }
    
    protected RunControlContext getRunControlContext(final String contextId) throws InterruptedException, ExecutionException {
        return new Query<RunControlContext> () {
            @Override
            protected void execute(final DataCallback<RunControlContext> callback) {
                rc.getContext(contextId, new IRunControl.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, IRunControl.RunControlContext ctx) {
                        callback.setData(ctx);
                        callback.setError(error);
                        callback.done();
                    }
                });
            }
        }.get();
    }
    
    protected String getProcessIdFromRunControlContext(final RunControlContext rcContext) throws InterruptedException, ExecutionException {
        return new Task<String>() {
            @Override
            public String call() throws Exception {
                return rcContext.getProcessID();
            }
        }.get();
    }
    
    protected String getSingleThreadIdFromProcess(final String processId) throws InterruptedException, ExecutionException {
        return new Query<String> () {
            protected void execute(final DataCallback<String> callback) {
                rc.getChildren(processId, new IRunControl.DoneGetChildren() {
                    public void doneGetChildren(IToken token, Exception error, String[] ids) {
                        if (error != null) {
                            callback.setError(error);
                        }
                        else if (ids == null || ids.length == 0) {
                            callback.setError(new Exception("Test process has no threads"));
                        }
                        else if (ids.length != 1) {
                            callback.setError(new Exception("Test process has too many threads"));
                        }
                        else {
                            callback.setData(ids[0]);
                        }
                        callback.done();
                    }
                });
            }
        }.get();
    }

    protected boolean getRunControlContextHasState(final RunControlContext rcContext) throws InterruptedException, ExecutionException {
        return new Task<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return rcContext.hasState();
            }
        }.get();
    }

    protected ISymbol getDiagnosticsSymbol(final String processId, final String testFunction) throws InterruptedException, ExecutionException {
        return new Query<ISymbol>() {
            @Override
            protected void execute(final DataCallback<ISymbol> callback) {
                diag.getSymbol(processId, testFunction, new IDiagnostics.DoneGetSymbol() {
                    public void doneGetSymbol(IToken token, Throwable error, IDiagnostics.ISymbol symbol) {
                        if (error != null) {
                            callback.setError(error);
                        }
                        else if (symbol == null) {
                            callback.setError(new Exception("Symbol must not be null: tcf_test_func3"));
                        }
                        else {
                            callback.setData(symbol);
                        }
                        callback.done();
                    }
                });                
            }
        }.get();
    }
    
    protected Number getSymbolValue(final ISymbol symbol) throws InterruptedException, ExecutionException {
        return new Task<Number>() {
            @Override
            public Number call() throws Exception {
                return symbol.getValue();
            }
        }.get();        
    }

    protected void resumeContext(final RunControlContext rcContext, final int mode) throws InterruptedException, ExecutionException {
        new Query<Object>() {
            @Override
            protected void execute(final DataCallback<Object> callback) {
                rcContext.resume(mode, 1, new IRunControl.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        callback.setError(error);
                        callback.done();
                    }
                });
            }
        }.get();
    }

    protected void resumeAndWaitForSuspend(final RunControlContext rcContext, final int mode) throws InterruptedException, ExecutionException {
        Query<String> suspendQuery = new Query<String> () {
            @Override
            protected void execute(DataCallback<String> callback) {
                fRcListener.addWaitingForSuspend(rcContext.getID(), callback);
            }
        };
        suspendQuery.invoke();
        resumeContext(rcContext, mode);
        suspendQuery.get();
    }

}
