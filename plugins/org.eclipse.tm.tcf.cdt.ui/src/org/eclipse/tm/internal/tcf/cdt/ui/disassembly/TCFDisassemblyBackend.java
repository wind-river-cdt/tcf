/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui.disassembly;

import static org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils.DEBUG;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.ErrorPosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyPartCallback;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.tm.internal.tcf.cdt.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFChildrenStackTrace;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeStackFrame;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IDisassembly;
import org.eclipse.tm.tcf.services.IDisassembly.DoneDisassemble;
import org.eclipse.tm.tcf.services.IDisassembly.IDisassemblyLine;
import org.eclipse.tm.tcf.services.IExpressions.DoneCreate;
import org.eclipse.tm.tcf.services.IExpressions.DoneDispose;
import org.eclipse.tm.tcf.services.IExpressions.DoneEvaluate;
import org.eclipse.tm.tcf.services.IExpressions.Expression;
import org.eclipse.tm.tcf.services.IExpressions.Value;
import org.eclipse.tm.tcf.services.ILineNumbers.CodeArea;
import org.eclipse.tm.tcf.services.ILineNumbers.DoneMapToSource;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tm.tcf.services.IRunControl.RunControlListener;
import org.eclipse.tm.tcf.services.IExpressions;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;

@SuppressWarnings("restriction")
public class TCFDisassemblyBackend implements IDisassemblyBackend {

    private class AddressRange {
        BigInteger start;
        BigInteger end;
    }

    private class TCFLaunchListener implements ILaunchesListener {

        public void launchesRemoved(ILaunch[] launches) {
        }

        public void launchesAdded(ILaunch[] launches) {
        }

        public void launchesChanged(ILaunch[] launches) {
            if(fExecContext == null) {
                return;
            }
            for (ILaunch launch : launches) {
                if (launch == fExecContext.getModel().getLaunch()) {
                    if (launch.isTerminated()) {
                        clearDebugContext();
                    }
                }
            }
        }
    }

    private class TCFChannelListener implements IChannelListener {

        public void onChannelOpened() {
        }

        public void onChannelClosed(Throwable error) {
            clearDebugContext();
        }

        public void congestionLevel(int level) {
        }
    }

    private class TCFRunControlListener implements RunControlListener {

        public void contextAdded(RunControlContext[] contexts) {
        }

        public void contextChanged(RunControlContext[] contexts) {
        }

        public void contextRemoved(String[] context_ids) {
            String id = fExecContext.getID();
            for (String contextId : context_ids) {
                if (id.equals(contextId)) {
                    fCallback.handleTargetEnded();
                    return;
                }
            }
        }

        public void contextSuspended(String context, String pc, String reason,
                Map<String, Object> params) {
            if (fExecContext.getID().equals(context)) {
                fSuspendAddress = pc != null ? new BigInteger(pc) : null;
                fCallback.handleTargetSuspended();
            }
        }

        public void contextResumed(String context) {
            if (fExecContext.getID().equals(context)) {
                fCallback.handleTargetResumed();
            }
        }

        public void containerSuspended(String context, String pc,
                String reason, Map<String, Object> params,
                String[] suspended_ids) {
            String id = fExecContext.getID();
            if (id.equals(context)) {
                fSuspendAddress = pc != null ? new BigInteger(pc) : null;
                fCallback.handleTargetSuspended();
                return;
            }
            for (String contextId : suspended_ids) {
                if (id.equals(contextId)) {
                    fSuspendAddress = null;
                    fCallback.handleTargetSuspended();
                    return;
                }
            }
        }

        public void containerResumed(String[] context_ids) {
            String id = fExecContext.getID();
            for (String contextId : context_ids) {
                if (id.equals(contextId)) {
                    fCallback.handleTargetResumed();
                    return;
                }
            }
        }

        public void contextException(String context, String msg) {
        }

    }

    private IDisassemblyPartCallback fCallback;
    private volatile TCFNodeExecContext fExecContext;
    private volatile TCFNodeStackFrame fActiveFrame;
    private volatile BigInteger fSuspendAddress;

    private final IRunControl.RunControlListener fRunControlListener = new TCFRunControlListener();
    private final IChannelListener fChannelListener = new TCFChannelListener();
    private final ILaunchesListener fLaunchesListener = new TCFLaunchListener();
    
    public void init(IDisassemblyPartCallback callback) {
        fCallback = callback;
    }

    public boolean supportsDebugContext(IAdaptable context) {
        return (context instanceof TCFNodeExecContext || context instanceof TCFNodeStackFrame)
            && hasDisassemblyService((TCFNode) context);
    }

    private boolean hasDisassemblyService(final TCFNode context) {
        Boolean hasService = new TCFTask<Boolean>() {
            public void run() {
                IDisassembly disass = null;
                IChannel channel = context.getChannel();
                if (channel != null && channel.getState() != IChannel.STATE_CLOSED) {
                    disass = channel.getRemoteService(IDisassembly.class);
                }
                done(disass != null);
            }
        }.getE();
        return hasService != null && hasService.booleanValue();
    }

    public boolean hasDebugContext() {
        return fExecContext != null;
    }

    public SetDebugContextResult setDebugContext(IAdaptable context) {
        TCFNodeExecContext newContext = null;
        TCFNodeStackFrame frame = null;
        SetDebugContextResult result = new SetDebugContextResult();
        if (context instanceof TCFNodeExecContext) {
            newContext = (TCFNodeExecContext) context;
            final TCFNodeExecContext _execContext = newContext;
            frame = new TCFTask<TCFNodeStackFrame>() {
                public void run() {
                    TCFChildrenStackTrace stack = _execContext.getStackTrace();
                    if (!stack.validate(this)) {
                        return;
                    }
                    done(stack.getTopFrame());
                }
            }.getE();
            if (frame == null) {
                newContext = null;
            }
        } else if (context instanceof TCFNodeStackFrame) {
            final TCFNodeStackFrame _frame = frame = (TCFNodeStackFrame) context;
            newContext = new TCFTask<TCFNodeExecContext>() {
                public void run() {
                    TCFNode parent = _frame.getParent();
                    if (parent instanceof TCFNodeExecContext) {
                        done((TCFNodeExecContext) parent);
                    } else {
                        done(null);
                    }
                }
            }.getE();
        }
        TCFNodeExecContext oldContext = fExecContext;
        if (oldContext == null || newContext == null || oldContext.compareTo(newContext) != 0) {
            result.contextChanged = true;
            if (oldContext != null) {
                removeListeners(oldContext);
            }
        }
        fExecContext = newContext;
        if (newContext != null && result.contextChanged) {
            addListeners(newContext);
        }
        fActiveFrame = frame;
        result.sessionId = newContext != null ? newContext.getID() : null;

        if (!result.contextChanged && fActiveFrame != null) {
            fCallback.asyncExec(new Runnable() {
                public void run() {
                    fCallback.gotoFrameIfActive(getFrameLevel());
                }
            });
        }

        return result;
    }

    private void addListeners(final TCFNodeExecContext context) {
        assert context != null;
        Protocol.invokeLater(new Runnable() {
            public void run() {
                IChannel channel = context.getChannel();
                IRunControl rctl = channel.getRemoteService(IRunControl.class);
                if (rctl != null) {
                    rctl.addListener(fRunControlListener);
                }
                channel.addChannelListener(fChannelListener);
            }
        });
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(fLaunchesListener );
    }

    private void removeListeners(final TCFNodeExecContext context) {
        assert context != null;
        DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(fLaunchesListener);
        Protocol.invokeLater(new Runnable() {
            public void run() {
                IChannel channel = context.getChannel();
                IRunControl rctl = channel.getRemoteService(IRunControl.class);
                if (rctl != null) {
                    rctl.removeListener(fRunControlListener);
                }
                channel.removeChannelListener(fChannelListener);
            }
        });
    }

    public void clearDebugContext() {
        if (fExecContext != null) {
            removeListeners(fExecContext);
        }
        fExecContext = null;
        fActiveFrame = null;
    }

    public void retrieveFrameAddress(final int targetFrame) {
        if (fExecContext == null) {
            fCallback.setUpdatePending(false);
            return;
        }
        BigInteger address;
        if (targetFrame == 0 && fSuspendAddress != null) {
            // shortcut for goto frame on suspend
            address = fSuspendAddress;
            fSuspendAddress = null;
        } else {
            final TCFChildrenStackTrace stack = fExecContext.getStackTrace();
            address = new TCFTask<BigInteger>() {
                public void run() {
                    if (!stack.validate(this)) {
                        return;
                    }
                    TCFNodeStackFrame frame = null;
                    if (targetFrame == 0) {
                        frame = stack.getTopFrame();
                    } else {
                        Map<String,TCFNode> frameData = stack.getData();
                        for (TCFNode node : frameData.values()) {
                            if (node instanceof TCFNodeStackFrame) {
                                TCFNodeStackFrame cand = (TCFNodeStackFrame) node;
                                if (cand.getFrameNo() == targetFrame) {
                                    frame = cand;
                                    break;
                                }
                            }
                        }
                    }
                    if (frame != null) {
                        TCFDataCache<BigInteger> addressCache = frame.getAddress();
                        if (!addressCache.validate(this)) {
                            return;
                        }
                        done(addressCache.getData());
                        return;
                    }
                    done(null);
                }
            }.getE();
        }
        
        fCallback.setUpdatePending(false);
        if (address != null) {
            if (targetFrame == 0) {
                fCallback.updatePC(address);
            } else {
                fCallback.gotoFrame(targetFrame, address);
            }
        }
    }

    public int getFrameLevel() {
        if (fActiveFrame == null) {
            return -1;
        }
        Integer level = new TCFTask<Integer>() {
            public void run() {
                done(fActiveFrame != null ? fActiveFrame.getFrameNo() : -1);
            }
        }.getE();
        return level != null ? level.intValue() : -1;
    }

    public boolean isSuspended() {
        if (fExecContext == null) {
            return false;
        }
        Boolean suspended = new TCFTask<Boolean>() {
            public void run() {
                if (fExecContext == null) {
                    done(null);
                    return;
                }
                TCFDataCache<TCFContextState> stateCache = fExecContext.getState();
                if (!stateCache.validate(this)) {
                    return;
                }
                TCFContextState state = stateCache.getData();
                if (state != null) {
                    done(state.is_suspended);
                    return;
                }
                done(null);
            }
        }.getE();
        return suspended != null ? suspended.booleanValue() : false;
    }

    public boolean hasFrameContext() {
        return fActiveFrame != null;
    }

    public String getFrameFile() {
        final TCFNodeStackFrame frame = fActiveFrame;
        if (frame == null) {
            return null;
        }
        String file = new TCFTask<String>() {
            public void run() {
                if (frame != fActiveFrame) {
                    done(null);
                    return;
                }
                TCFDataCache<TCFSourceRef> sourceRefCache = frame.getLineInfo();
                if (!sourceRefCache.validate(this)) {
                    return;
                }
                TCFSourceRef sourceRef = sourceRefCache.getData();
                String file = sourceRef.area.file;
                if (file != null) {
                    IPath filePath = new Path(file);
                    if (!filePath.isAbsolute()) {
                        String dir = sourceRef.area.directory;
                        if (dir != null) {
                            filePath = new Path(dir).append(filePath);
                        }
                    }
                    done(filePath.toString());
                }
                done(null);
            }
        }.getE();
        return file;
    }

    public int getFrameLine() {
        final TCFNodeStackFrame frame = fActiveFrame;
        if (frame == null) {
            return -1;
        }
        Integer line = new TCFTask<Integer>() {
            public void run() {
                if (frame != fActiveFrame) {
                    done(null);
                    return;
                }
                TCFDataCache<TCFSourceRef> sourceRefCache = frame.getLineInfo();
                if (!sourceRefCache.validate(this)) {
                    return;
                }
                TCFSourceRef sourceRef = sourceRefCache.getData();
                done(sourceRef.area.start_line);
            }
        }.getE();
        return line != null ? line.intValue() : -1;
    }

    public void retrieveDisassembly(final BigInteger startAddress,
            BigInteger endAddress, String file, int lineNumber, int lines,
            final boolean mixed, boolean showSymbols, boolean showDisassembly,
            final int linesHint) {
        final TCFNodeExecContext execContext = fExecContext;
        if (execContext == null || execContext.isDisposed()) {
            fCallback.setUpdatePending(false);
            return;
        }
        Protocol.invokeLater(new Runnable() {
            public void run() {
                if (execContext != fExecContext) {
                    return;
                }
                final IChannel channel = execContext.getChannel();
                IDisassembly disass = channel.getRemoteService(IDisassembly.class);
                if (disass != null) {
                    TCFDataCache<IMemory.MemoryContext> cache = execContext.getMemoryContext();
                    if (!cache.validate(this)) return;
                    final IMemory.MemoryContext mem = cache.getData();
                    if (mem == null) {
                        fCallback.setUpdatePending(false);
                        return;
                    }
                    final String contextId = mem.getID();
                    Map<String, Object> params = new HashMap<String, Object>();
                    disass.disassemble(contextId, startAddress, linesHint*4, params, new DoneDisassemble() {
                        public void doneDisassemble(IToken token, final Throwable error,
                                final IDisassemblyLine[] disassembly) {
                            if (error != null) {
                                fCallback.asyncExec(new Runnable() {
                                    public void run() {
                                        fCallback.insertError(startAddress, error.toString());
                                    }
                                });
                                fCallback.setUpdatePending(false);
                            } else {
                                ILineNumbers lineNumbers = null;
                                if (mixed) {
                                    lineNumbers = channel.getRemoteService(ILineNumbers.class);
                                }
                                if (lineNumbers == null) {
                                    fCallback.asyncExec(new Runnable() {
                                        public void run() {
                                            insertDisassembly(startAddress, disassembly, null);
                                        }
                                    });
                                } else {
                                    AddressRange range = getAddressRange(disassembly);
                                    lineNumbers.mapToSource(contextId, range.start, range.end, new DoneMapToSource() {
                                        public void doneMapToSource(IToken token, Exception error, final CodeArea[] areas) {
                                            if (error != null) {
                                                Activator.log(error);
                                                fCallback.asyncExec(new Runnable() {
                                                    public void run() {
                                                        insertDisassembly(startAddress, disassembly, null);
                                                    }
                                                });
                                            } else {
                                                fCallback.asyncExec(new Runnable() {
                                                    public void run() {
                                                        insertDisassembly(startAddress, disassembly, areas);
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        }

                        private AddressRange getAddressRange(IDisassemblyLine[] lines) {
                            AddressRange range = new AddressRange();
                            range.start = toBigInteger(lines[0].getAddress());
                            IDisassemblyLine lastLine = lines[lines.length-1];
                            range.end = toBigInteger(lastLine.getAddress()).add(BigInteger.valueOf(lastLine.getSize()));
                            return range;
                        }
                    });
                } else {
                    fCallback.setUpdatePending(false);
                }
            }
        });
    }

    protected final boolean insertDisassembly(BigInteger startAddress, IDisassemblyLine[] instructions, CodeArea[] codeAreas) {
        if (!fCallback.hasViewer() || fExecContext == null) {
            // return true to avoid a retry
            return true;
        }
        if (DEBUG) System.out.println("insertDisassembly "+ DisassemblyUtils.getAddressText(startAddress)); //$NON-NLS-1$
        boolean updatePending = fCallback.getUpdatePending();
        assert updatePending;
        if (!updatePending) {
            // safe-guard in case something weird is going on
            // return true to avoid a retry
            return true;
        }

        boolean insertedAnyAddress = false;
        try {
            fCallback.lockScroller();

            AddressRangePosition p= null;
            for (IDisassemblyLine instruction : instructions) {
                BigInteger address;
                if (instruction.getAddress() instanceof BigInteger) {
                    address = (BigInteger) instruction.getAddress();
                } else {
                    address = BigInteger.valueOf(instruction.getAddress().longValue());
                }
                if (startAddress == null) {
                    startAddress = address;
                    fCallback.setGotoAddressPending(address);
                }
                if (p == null || !p.containsAddress(address)) {
                    p = fCallback.getPositionOfAddress(address);
                }
                if (p instanceof ErrorPosition && p.fValid) {
                    p.fValid = false;
                    fCallback.getDocument().addInvalidAddressRange(p);
                } else if (p == null /* || address.compareTo(endAddress) > 0 */) {
                    if (DEBUG) System.out.println("Excess disassembly lines at " + DisassemblyUtils.getAddressText(address)); //$NON-NLS-1$
                    return insertedAnyAddress;
                } else if (p.fValid) {
                    if (DEBUG) System.out.println("Excess disassembly lines at " + DisassemblyUtils.getAddressText(address)); //$NON-NLS-1$
//                    if (!p.fAddressOffset.equals(address)) {
//                        // override probably unaligned disassembly
//                        p.fValid = false;
//                        fCallback.getDocument().addInvalidAddressRange(p);
//                    } else {
                        continue;
//                    }
                }
                // determine instruction byte length
                int instrLength= instruction.getSize();
                Map<String, Object>[] instrAttrs = instruction.getInstruction();
                String instr = formatInstruction(instrAttrs);
                
                String sourceFile = null;
                int sourceLine = -1;
                CodeArea area = findCodeArea(address, codeAreas);
                if (area != null) {
                    if (area.file != null) {
                        IPath filePath = new Path(area.file);
                        if (!filePath.isAbsolute() && area.directory != null) {
                            filePath = new Path(area.directory).append(filePath);
                        }
                        sourceFile = filePath.toString();
                        sourceLine = area.start_line - 1;
                    }
                }
                if (sourceFile != null && sourceLine >= 0) {
                    p = fCallback.insertSource(p, address, sourceFile, sourceLine);
                }

                p = fCallback.getDocument().insertDisassemblyLine(p, address, instrLength, "", instr, sourceFile, sourceLine);
                if (p == null) {
                    break;
                }
                insertedAnyAddress = true;
            }
        } catch (BadLocationException e) {
            // should not happen
            DisassemblyUtils.internalError(e);
        } finally {
            fCallback.setUpdatePending(false);
            if (insertedAnyAddress) {
                fCallback.updateInvalidSource();
                fCallback.unlockScroller();
                fCallback.doPending();
                fCallback.updateVisibleArea();
            } else {
                fCallback.unlockScroller();
            }
        }
        return insertedAnyAddress;
    }

    private CodeArea findCodeArea(BigInteger address, CodeArea[] codeAreas) {
        if (codeAreas != null) {
            for (CodeArea codeArea : codeAreas) {
                if (address.equals(toBigInteger(codeArea.start_address))) {
                    return codeArea;
                }
            }
        }
        return null;
    }

    /**
     * Format an instruction.
     * 
     * @param instrAttrs
     * @return string representation
     */
    private String formatInstruction(Map<String, Object>[] instrAttrs) {
        StringBuilder buf = new StringBuilder(20);
        for (Map<String, Object> attrs : instrAttrs) {
            Object type = attrs.get(IDisassembly.FIELD_TYPE);
            if (IDisassembly.FTYPE_STRING.equals(type) || IDisassembly.FTYPE_Register.equals(type)) {
                Object text = attrs.get(IDisassembly.FIELD_TEXT);
                buf.append(text);
            } else {
                Object value = attrs.get(IDisassembly.FIELD_VALUE);
                BigInteger bigValue = new BigInteger(value.toString());
                // TODO number format
                buf.append("0x").append(bigValue.toString(16)).append(' ');
            }
        }
        return buf.toString();
    }

    public void gotoSymbol(final String symbol) {
        final TCFNodeStackFrame activeFrame = fActiveFrame;
        if (activeFrame == null) {
            return;
        }
        Protocol.invokeLater(new Runnable() {
            public void run() {
                if (activeFrame != fActiveFrame) {
                    return;
                }
                IChannel channel = activeFrame.getChannel();
                final IExpressions exprSvc = channel.getRemoteService(IExpressions.class);
                if (exprSvc != null) {
                    TCFNode evalContext = activeFrame.isEmulated() ? activeFrame.getParent() : activeFrame;
                    exprSvc.create(evalContext.getID(), null, symbol, new DoneCreate() {
                        public void doneCreate(IToken token, Exception error, final Expression context) {
                            if (error == null) {
                                exprSvc.evaluate(context.getID(), new DoneEvaluate() {
                                    public void doneEvaluate(IToken token, Exception error, Value value) {
                                        if (error == null) {
                                            final BigInteger address = toBigInteger(value.getValue(), value.isBigEndian(), false);
                                            fCallback.asyncExec(new Runnable() {
                                                public void run() {
                                                    fCallback.gotoAddress(address);
                                                }
                                            });
                                        } else {
                                            handleError(error);
                                        }
                                        exprSvc.dispose(context.getID(), new DoneDispose() {
                                            public void doneDispose(IToken token, Exception error) {
                                                // no-op
                                            }
                                        });
                                    }
                                });
                            } else {
                                handleError(error);
                            }
                        }
                    });
                }
            }
            protected void handleError(final Exception error) {
                fCallback.asyncExec(new Runnable() {
                    public void run() {
                        Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, error.getLocalizedMessage(), error);
                        ErrorDialog.openError(fCallback.getSite().getShell(), "Error", null, status); //$NON-NLS-1$
                    }
                });
            }
        });
    }

    public void retrieveDisassembly(String file, int lines,
            BigInteger endAddress, boolean mixed, boolean showSymbols,
            boolean showDisassembly) {
        // TODO disassembly for source file
        fCallback.setUpdatePending(false);
    }

    public String evaluateExpression(final String expression) {
        final TCFNodeStackFrame activeFrame = fActiveFrame;
        if (activeFrame == null) {
            return null;
        }
        String value = new TCFTask<String>() {
            public void run() {
                if (activeFrame != fActiveFrame) {
                    done(null);
                    return;
                }
                IChannel channel = activeFrame.getChannel();
                final IExpressions exprSvc = channel.getRemoteService(IExpressions.class);
                if (exprSvc != null) {
                    TCFNode evalContext = activeFrame.isEmulated() ? activeFrame.getParent() : activeFrame;
                    exprSvc.create(evalContext.getID(), null, expression, new DoneCreate() {
                        public void doneCreate(IToken token, Exception error, final Expression context) {
                            if (error == null) {
                                exprSvc.evaluate(context.getID(), new DoneEvaluate() {
                                    public void doneEvaluate(IToken token, Exception error, Value value) {
                                        if (error == null) {
                                            final BigInteger address = toBigInteger(value.getValue(), value.isBigEndian(), false);
                                            done("0x"+address.toString(16));
                                        }
                                        else {
                                            done(null);
                                        }
                                        exprSvc.dispose(context.getID(), new DoneDispose() {
                                            public void doneDispose(IToken token, Exception error) {
                                                // no-op
                                            }
                                        });
                                    }
                                });
                            }
                            else {
                                done(null);
                            }
                        }
                    });
                } else {
                    done(null);
                }
            }
        }.getE();
        return value;
    }

    public void dispose() {
    }

    public Object insertSource(Position pos, BigInteger address, String file, int lineNumber) {
        // TODO map disassembly to source
        return null;
    }

    private static BigInteger toBigInteger(byte[] data, boolean big_endian, boolean sign_extension) {
        byte[] temp = null;
        if (sign_extension) {
            temp = new byte[data.length];
        }
        else {
            temp = new byte[data.length + 1];
            temp[0] = 0; // Extra byte to avoid sign extension by BigInteger
        }
        if (big_endian) {
            System.arraycopy(data, 0, temp, sign_extension ? 0 : 1, data.length);
        }
        else {
            for (int i = 0; i < data.length; i++) {
                temp[temp.length - i - 1] = data[i];
            }
        }
        return new BigInteger(temp);
    }

    private static BigInteger toBigInteger(Number address) {
        if (address instanceof BigInteger) {
            return (BigInteger) address;
        }
        return new BigInteger(address.toString());
    }

}