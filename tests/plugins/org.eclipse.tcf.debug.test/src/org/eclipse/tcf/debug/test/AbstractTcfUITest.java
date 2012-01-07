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
import java.util.regex.Pattern;

import junit.framework.TestCase;

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
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualItem;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.debug.test.services.BreakpointsCM;
import org.eclipse.tcf.debug.test.services.DiagnosticsCM;
import org.eclipse.tcf.debug.test.services.RunControlCM;
import org.eclipse.tcf.debug.test.services.RunControlCM.ContextState;
import org.eclipse.tcf.debug.test.services.StackTraceCM;
import org.eclipse.tcf.debug.test.services.SymbolsCM;
import org.eclipse.tcf.debug.test.util.AggregateCallback;
import org.eclipse.tcf.debug.test.util.Callback;
import org.eclipse.tcf.debug.test.util.Callback.ICanceledListener;
import org.eclipse.tcf.debug.test.util.DataCallback;
import org.eclipse.tcf.debug.test.util.ICache;
import org.eclipse.tcf.debug.test.util.Query;
import org.eclipse.tcf.debug.test.util.Task;
import org.eclipse.tcf.debug.test.util.Transaction;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IBreakpoints;
import org.eclipse.tcf.services.IDiagnostics;
import org.eclipse.tcf.services.IExpressions;
import org.eclipse.tcf.services.IMemoryMap;
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

    protected Object fTestRunKey;
    
    protected IDiagnostics diag;
    protected IExpressions expr;
    protected ISymbols syms;
    protected IStackTrace stk;
    protected IRunControl rc;
    protected IBreakpoints bp;
    protected IMemoryMap fMemoryMap;

    protected RunControlCM fRunControlCM;
    protected DiagnosticsCM fDiagnosticsCM;
    protected BreakpointsCM fBreakpointsCM;
    protected StackTraceCM fStackTraceCM;
    protected SymbolsCM fSymbolsCM;
    
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
        
        fTestRunKey = new Object();
        
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
        
        new Task<Object>() {
            @Override
            public Object call() throws Exception {
                setUpServiceListeners();
                return null;
            }
        }.get();
        
        validateTestAvailable();
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
    }

    protected String getDiagnosticsTestName() {
        return "RCBP1";
    }

    protected void setUpServiceListeners() throws Exception{
        fRunControlCM = new RunControlCM(rc);
        fDiagnosticsCM = new DiagnosticsCM(diag);
        fBreakpointsCM = new BreakpointsCM(bp);
        fStackTraceCM = new StackTraceCM(stk, rc);
        fSymbolsCM = new SymbolsCM(syms, fRunControlCM, fMemoryMap);
    }
    
    protected void tearDownServiceListeners() throws Exception{
        fSymbolsCM.dispose();
        fBreakpointsCM.dispose();
        fStackTraceCM.dispose();
        fRunControlCM.dispose();
        fDiagnosticsCM.dispose();
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
    
    private void createLaunch() throws Exception {
        ILaunchManager lManager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType lcType = lManager.getLaunchConfigurationType("org.eclipse.tcf.debug.LaunchConfigurationType");
        ILaunchConfigurationWorkingCopy lcWc = lcType.newInstance(null, "test");
        lcWc.doSave();
        fDebugViewListener.reset();
        fDebugViewListener.setDelayContentUntilProxyInstall(true);
        fLaunch = lcWc.launch("debug", new NullProgressMonitor());
        fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | MODEL_PROXIES_INSTALLED | CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
        Assert.assertTrue( fLaunch instanceof IDisconnect ); 
        
        VirtualItem launchItem = fDebugViewListener.findElement(new Pattern[] { Pattern.compile(".*" + fLaunch.getLaunchConfiguration().getName() + ".*") }  );
        Assert.assertTrue( launchItem != null && fLaunch.equals(launchItem.getData()) );
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
                fMemoryMap = channels[0].getRemoteService(IMemoryMap.class);
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
        String[] testList = new Transaction<String[]>() {
            protected String[] process() throws InvalidCacheException ,ExecutionException {
                return validate( fDiagnosticsCM.getTestList() );
            }
        }.get();
        
        int i = 0;
        for (; i < testList.length; i++) {
            if ("RCBP1".equals(testList[i])) break;
        }

        Assert.assertTrue("Required test not supported", i != testList.length);
    }
    
    protected ContextState resumeAndWaitForSuspend(final RunControlContext context, final int mode) throws InterruptedException, ExecutionException {
        return new Transaction<ContextState>() {
            @Override
            protected ContextState process() throws InvalidCacheException, ExecutionException {
                ICache<Object> waitCache = fRunControlCM.waitForContextSuspended(context.getID(), this);
                validate( fRunControlCM.resume(context, this, mode, 1) );
                validate(waitCache);
                return validate( fRunControlCM.getState(context.getID()) );
            }
        }.get();
    }
    
}
