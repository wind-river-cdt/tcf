/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.cdt.ui.disassembly;

import static org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils.DEBUG;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AbstractDisassemblyBackend;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.ErrorPosition;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.Position;
import org.eclipse.tcf.internal.cdt.ui.Activator;
import org.eclipse.tcf.internal.debug.launch.TCFSourceLookupDirector;
import org.eclipse.tcf.internal.debug.launch.TCFSourceLookupParticipant;
import org.eclipse.tcf.internal.debug.model.TCFContextState;
import org.eclipse.tcf.internal.debug.model.TCFSourceRef;
import org.eclipse.tcf.internal.debug.ui.model.TCFChildrenStackTrace;
import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeStackFrame;
import org.eclipse.tcf.internal.debug.ui.model.TCFNumberFormat;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IDisassembly;
import org.eclipse.tcf.services.IDisassembly.DoneDisassemble;
import org.eclipse.tcf.services.IDisassembly.IDisassemblyLine;
import org.eclipse.tcf.services.IExpressions;
import org.eclipse.tcf.services.IExpressions.DoneCreate;
import org.eclipse.tcf.services.IExpressions.DoneDispose;
import org.eclipse.tcf.services.IExpressions.DoneEvaluate;
import org.eclipse.tcf.services.IExpressions.Expression;
import org.eclipse.tcf.services.IExpressions.Value;
import org.eclipse.tcf.services.ILineNumbers;
import org.eclipse.tcf.services.ILineNumbers.CodeArea;
import org.eclipse.tcf.services.ILineNumbers.DoneMapToSource;
import org.eclipse.tcf.services.IMemory.MemoryError;
import org.eclipse.tcf.services.IMemory;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tcf.services.IRunControl.RunControlListener;
import org.eclipse.tcf.services.ISymbols;
import org.eclipse.tcf.util.TCFDataCache;
import org.eclipse.tcf.util.TCFTask;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class TCFDisassemblyBackend extends AbstractDisassemblyBackend {

    private static class AddressRange {
        BigInteger start;
        BigInteger end;
    }

    private static class FunctionOffset {
        static final FunctionOffset NONE = new FunctionOffset(null, null);
        String name;
        BigInteger offset;
        FunctionOffset(String name, BigInteger offset) {
            this.name = name;
            this.offset = offset;
        }
        @Override
        public String toString() {
            if (name == null || name.length() == 0) return "";
            if (isZeroOffset()) return name;
            return name + '+' + offset.toString();
        }
        boolean isZeroOffset() {
            return offset == null || offset.compareTo(BigInteger.ZERO) == 0;
        }
    }

    private class TCFLaunchListener implements ILaunchesListener {

        public void launchesRemoved(ILaunch[] launches) {
        }

        public void launchesAdded(ILaunch[] launches) {
        }

        public void launchesChanged(ILaunch[] launches) {
            if (fExecContext == null) return;
            for (ILaunch launch : launches) {
                if (launch == fExecContext.getModel().getLaunch()) {
                    if (launch.isTerminated()) {
                        handleSessionEnded();
                    }
                    break;
                }
            }
        }
    }

    private class TCFChannelListener implements IChannelListener {

        public void onChannelOpened() {
        }

        public void onChannelClosed(Throwable error) {
            handleSessionEnded();
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
                handleContextSuspended(pc != null ? new BigInteger(pc) : null);
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
                handleContextSuspended(pc != null ? new BigInteger(pc) : null);
                return;
            }
            for (String contextId : suspended_ids) {
                if (id.equals(contextId)) {
                    handleContextSuspended(null);
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
    private volatile TCFNodeExecContext fMemoryContext;
    private volatile TCFNodeStackFrame fActiveFrame;
    private volatile BigInteger fSuspendAddress;
    private volatile int fSuspendCount;

    private final IRunControl.RunControlListener fRunControlListener = new TCFRunControlListener();
    private final IChannelListener fChannelListener = new TCFChannelListener();
    private final ILaunchesListener fLaunchesListener = new TCFLaunchListener();

    @Override
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
        TCFNodeStackFrame frame = null;
        TCFNodeExecContext thread = null;
        SetDebugContextResult result = new SetDebugContextResult();
        if (context instanceof TCFNodeExecContext) {
            thread = (TCFNodeExecContext)context;
            IChannel channel = thread.getChannel();
            try {
                final TCFNodeExecContext ctx = thread;
                frame = new TCFTask<TCFNodeStackFrame>(channel) {
                    public void run() {
                        TCFChildrenStackTrace stack = ctx.getStackTrace();
                        if (!stack.validate(this)) return;
                        done(stack.getTopFrame());
                    }
                }.getE();
            }
            catch (Error x) {
                if (channel.getState() == IChannel.STATE_OPEN) throw x;
                frame = null;
            }
            if (frame == null) thread = null;
        }
        else if (context instanceof TCFNodeStackFrame) {
            frame = (TCFNodeStackFrame)context;
            thread = (TCFNodeExecContext)frame.getParent();
        }

        if (fExecContext != thread) {
            result.contextChanged = true;
            fSuspendCount++;
            if (fExecContext != null) removeListeners(fExecContext);
            fExecContext = thread;
            if (thread != null) addListeners(thread);
        }
        if (fExecContext != null) {
            fMemoryContext = new TCFTask<TCFNodeExecContext>(fExecContext.getChannel()) {
                public void run() {
                    TCFDataCache<TCFNodeExecContext> cache = fExecContext.getMemoryNode();
                    if (!cache.validate(this)) return;
                    done(cache.getData());
                }
            }.getE();
        }
        else {
            fMemoryContext = null;
        }
        fActiveFrame = frame;
        result.sessionId = thread != null ? thread.getID() : null;

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
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                IChannel channel = context.getChannel();
                IRunControl rctl = channel.getRemoteService(IRunControl.class);
                if (rctl != null) rctl.addListener(fRunControlListener);
                channel.addChannelListener(fChannelListener);
            }
        });
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(fLaunchesListener );
    }

    private void removeListeners(final TCFNodeExecContext context) {
        assert context != null;
        DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(fLaunchesListener);
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                IChannel channel = context.getChannel();
                IRunControl rctl = channel.getRemoteService(IRunControl.class);
                if (rctl != null) rctl.removeListener(fRunControlListener);
                channel.removeChannelListener(fChannelListener);
            }
        });
    }

    private void handleContextSuspended(BigInteger pc) {
        fSuspendCount++;
        fSuspendAddress = pc;
        fCallback.handleTargetSuspended();
    }
    private void handleSessionEnded() {
        clearDebugContext();
        fCallback.handleTargetEnded();
    }

    public void clearDebugContext() {
        if (fExecContext != null) {
            removeListeners(fExecContext);
        }
        fExecContext = null;
        fMemoryContext = null;
        fActiveFrame = null;
    }

    public void retrieveFrameAddress(final int targetFrame) {
        final TCFNodeExecContext execContext = fExecContext;
        if (execContext == null) {
            fCallback.setUpdatePending(false);
            return;
        }
        BigInteger address;
        if (targetFrame == 0 && fSuspendAddress != null) {
            // shortcut for goto frame on suspend
            address = fSuspendAddress;
            fSuspendAddress = null;
        }
        else {
            final int suspendCount = fSuspendCount;
            final TCFChildrenStackTrace stack = execContext.getStackTrace();
            address = new TCFTask<BigInteger>(execContext.getChannel()) {
                public void run() {
                    if (suspendCount != fSuspendCount || execContext != fExecContext) {
                        done(null);
                        return;
                    }
                    if (!stack.validate(this)) return;
                    TCFNodeStackFrame frame = null;
                    if (targetFrame == 0) {
                        frame = stack.getTopFrame();
                    }
                    else {
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
                        if (!addressCache.validate(this)) return;
                        done(addressCache.getData());
                        return;
                    }
                    done(null);
                }
            }.getE();
        }

        if (execContext == fExecContext) {
            fCallback.setUpdatePending(false);
            if (address == null) address = BigInteger.valueOf(-2);
            if (targetFrame == 0) {
                fCallback.updatePC(address);
            }
            else {
                fCallback.gotoFrame(targetFrame, address);
            }
        }
    }

    public int getFrameLevel() {
        if (fActiveFrame == null) return -1;
        Integer level = new TCFTask<Integer>() {
            public void run() {
                done(fActiveFrame != null ? fActiveFrame.getFrameNo() : -1);
            }
        }.getE();
        return level != null ? level.intValue() : -1;
    }

    public boolean isSuspended() {
        if (fExecContext == null) return false;
        Boolean suspended = new TCFTask<Boolean>(fExecContext.getChannel()) {
            public void run() {
                if (fExecContext == null) {
                    done(null);
                    return;
                }
                TCFDataCache<TCFContextState> stateCache = fExecContext.getState();
                if (!stateCache.validate(this)) return;
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
        if (frame == null) return null;
        String file = new TCFTask<String>(frame.getChannel()) {
            public void run() {
                if (frame != fActiveFrame) {
                    done(null);
                    return;
                }
                TCFDataCache<TCFSourceRef> sourceRefCache = frame.getLineInfo();
                if (!sourceRefCache.validate(this)) return;
                TCFSourceRef sourceRef = sourceRefCache.getData();
                if (sourceRef != null && sourceRef.area != null) {
                    done(TCFSourceLookupParticipant.toFileName(sourceRef.area));
                    return;
                }
                done(null);
            }
        }.getE();
        return file;
    }

    public int getFrameLine() {
        final TCFNodeStackFrame frame = fActiveFrame;
        if (frame == null) return -1;
        Integer line = new TCFTask<Integer>(frame.getChannel()) {
            public void run() {
                if (frame != fActiveFrame) {
                    done(null);
                    return;
                }
                TCFDataCache<TCFSourceRef> sourceRefCache = frame.getLineInfo();
                if (!sourceRefCache.validate(this)) return;
                TCFSourceRef sourceRef = sourceRefCache.getData();
                if (sourceRef != null && sourceRef.area != null) {
                    done(sourceRef.area.start_line);
                    return;
                }
                done(null);
            }
        }.getE();
        return line != null ? line.intValue() : -1;
    }

    public void retrieveDisassembly(final BigInteger startAddress,
            BigInteger endAddress, String file, int lineNumber, int lines,
            final boolean mixed, final boolean showSymbols, boolean showDisassembly,
            final int linesHint) {
        final TCFNodeExecContext execContext = fExecContext;
        if (execContext == null || execContext.isDisposed()) {
            fCallback.setUpdatePending(false);
            return;
        }
        final int suspendCount = fSuspendCount;
        final long modCount = getModCount();
        Protocol.invokeLater(new Runnable() {

            IMemory.MemoryContext mem;
            boolean big_endian;
            int addr_bits;
            IDisassemblyLine[] disassembly;
            AddressRange range;
            boolean done_disassembly;
            ISymbols.Symbol[] functionSymbols;
            boolean done_symbols;
            CodeArea[] code_areas;
            boolean done_line_numbers;
            byte[] code;
            boolean done_code;

            public void run() {
                if (execContext != fExecContext)  return;
                if (suspendCount != fSuspendCount || fMemoryContext == null) {
                    fCallback.setUpdatePending(false);
                    return;
                }
                final IChannel channel = execContext.getChannel();
                IDisassembly disass = channel.getRemoteService(IDisassembly.class);
                if (disass == null) {
                    fCallback.setUpdatePending(false);
                    return;
                }
                TCFDataCache<IMemory.MemoryContext> cache = fMemoryContext.getMemoryContext();
                if (!cache.validate(this)) return;
                mem = cache.getData();
                if (mem == null) {
                    fCallback.setUpdatePending(false);
                    return;
                }
                big_endian = mem.isBigEndian();
                addr_bits = mem.getAddressSize() * 8;
                if (!done_disassembly) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    disass.disassemble(mem.getID(), startAddress, linesHint * 4, params, new DoneDisassemble() {
                        @Override
                        public void doneDisassemble(IToken token, final Throwable error, IDisassemblyLine[] res) {
                            if (execContext != fExecContext) return;
                            if (error != null) {
                                fCallback.asyncExec(new Runnable() {
                                    public void run() {
                                        if (execContext != fExecContext) return;
                                        if (modCount == getModCount()) {
                                            fCallback.insertError(startAddress, TCFModel.getErrorMessage(error, false));
                                            fCallback.setUpdatePending(false);
                                            if (fCallback.getAddressSize() < addr_bits) fCallback.addressSizeChanged(addr_bits);
                                        }
                                    }
                                });
                                return;
                            }
                            if (res != null && res.length > 0) {
                                disassembly = res;
                                range = new AddressRange();
                                range.start = JSON.toBigInteger(res[0].getAddress());
                                IDisassemblyLine last = res[res.length - 1];
                                range.end = JSON.toBigInteger(last.getAddress()).add(BigInteger.valueOf(last.getSize()));
                            }
                            done_disassembly = true;
                            run();
                        }
                    });
                    return;
                }
                if (!done_symbols && (range == null || !showSymbols)) {
                    done_symbols = true;
                }
                if (!done_symbols) {
                    final ISymbols symbols = channel.getRemoteService(ISymbols.class);
                    if (symbols == null) {
                        done_symbols = true;
                    }
                    else {
                        final ArrayList<ISymbols.Symbol> symbolList = new ArrayList<ISymbols.Symbol>();
                        IDisassemblyLine line = disassembly[0];
                        symbols.findByAddr(mem.getID(), line.getAddress(), new ISymbols.DoneFind() {
                            int idx = 0;
                            public void doneFind(IToken token, Exception error, String symbol_id) {
                                if (error == null && symbol_id != null) {
                                    symbols.getContext(symbol_id, new ISymbols.DoneGetContext() {
                                        public void doneGetContext(IToken token, Exception error, ISymbols.Symbol context) {
                                            BigInteger nextAddress = null;
                                            if (error == null && context != null) {
                                                if (context.getTypeClass().equals(ISymbols.TypeClass.function)) {
                                                    symbolList.add(context);
                                                    nextAddress = JSON.toBigInteger(context.getAddress()).add(BigInteger.valueOf(context.getSize()));
                                                }
                                            }
                                            findNextSymbol(nextAddress);
                                        }
                                    });
                                    return;
                                }
                                findNextSymbol(null);
                            }
                            private void findNextSymbol(BigInteger nextAddress) {
                                while (++idx < disassembly.length) {
                                    BigInteger instrAddress = JSON.toBigInteger(disassembly[idx].getAddress());
                                    if (nextAddress != null && instrAddress.compareTo(nextAddress) < 0) continue;
                                    symbols.findByAddr(mem.getID(), instrAddress, this);
                                    return;
                                }
                                functionSymbols = symbolList.toArray(new ISymbols.Symbol[symbolList.size()]);
                                done_symbols = true;
                                run();
                            }
                        });
                        return;
                    }
                }
                if (!done_line_numbers && (range == null || !mixed)) {
                    done_line_numbers = true;
                }
                if (!done_line_numbers) {
                    ILineNumbers lineNumbers = channel.getRemoteService(ILineNumbers.class);
                    if (lineNumbers == null) {
                        done_line_numbers = true;
                    }
                    else {
                        lineNumbers.mapToSource(mem.getID(), range.start, range.end, new DoneMapToSource() {
                            public void doneMapToSource(IToken token, Exception error, final CodeArea[] areas) {
                                if (error != null) {
                                    Activator.log(error);
                                }
                                else {
                                    code_areas = areas;
                                }
                                done_line_numbers = true;
                                run();
                            }
                        });
                        return;
                    }
                }
                if (!done_code && range == null) {
                    done_code = true;
                }
                if (!done_code) {
                    code = new byte[range.end.subtract(range.start).intValue()];
                    mem.get(range.start, 1, code, 0, code.length, 0, new IMemory.DoneMemory() {
                        @Override
                        public void doneMemory(IToken token, MemoryError error) {
                            done_code = true;
                            run();
                        }
                    });
                    return;
                }
                fCallback.asyncExec(new Runnable() {
                    public void run() {
                        insertDisassembly(modCount, startAddress, code, range, big_endian,
                                disassembly, functionSymbols, code_areas);
                        if (fCallback.getAddressSize() < addr_bits) fCallback.addressSizeChanged(addr_bits);
                    }
                });
            }
        });
    }

    private long getModCount() {
        return ((IDocumentExtension4) fCallback.getDocument()).getModificationStamp();
    }

    protected final void insertDisassembly(long modCount, BigInteger startAddress, byte[] code, AddressRange range, boolean big_endian,
            IDisassemblyLine[] instructions, ISymbols.Symbol[] symbols, CodeArea[] codeAreas) {
        if (!fCallback.hasViewer() || fExecContext == null) return;
        if (modCount != getModCount()) return;
        if (DEBUG) System.out.println("insertDisassembly "+ DisassemblyUtils.getAddressText(startAddress)); //$NON-NLS-1$
        boolean updatePending = fCallback.getUpdatePending();
        assert updatePending;
        if (!updatePending) {
            // safe-guard in case something weird is going on
            return;
        }

        boolean insertedAnyAddress = false;
        try {
            fCallback.lockScroller();

            AddressRangePosition p = null;
            if (instructions != null) for (IDisassemblyLine instruction : instructions) {
                BigInteger address = JSON.toBigInteger(instruction.getAddress());
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
                }
                else if (p == null /* || address.compareTo(endAddress) > 0 */) {
                    if (DEBUG) System.out.println("Excess disassembly lines at " + DisassemblyUtils.getAddressText(address)); //$NON-NLS-1$
                    return;
                }
                else if (p.fValid) {
                    if (DEBUG) System.out.println("Excess disassembly lines at " + DisassemblyUtils.getAddressText(address)); //$NON-NLS-1$
//                    if (!p.fAddressOffset.equals(address)) {
//                        // override probably unaligned disassembly
//                        p.fValid = false;
//                        fCallback.getDocument().addInvalidAddressRange(p);
//                    }
//                    else {
                        continue;
//                    }
                }

                // insert source
                String sourceFile = null;
                int firstLine = -1;
                int lastLine = -1;
                CodeArea area = findCodeArea(address, codeAreas);
                if (area != null && area.file != null) {
                    IPath filePath = new Path(area.file);
                    if (!filePath.isAbsolute() && area.directory != null) {
                        filePath = new Path(area.directory).append(filePath);
                    }
                    sourceFile = filePath.toString();
                    firstLine = area.start_line - 1;
                    lastLine = area.end_line - 2;
                }
                if (sourceFile != null && firstLine >= 0) {
                    try {
                        p = fCallback.insertSource(p, address, sourceFile, firstLine, lastLine);
                    }
                    catch (NoSuchMethodError nsme) {
                        // use fallback
                        p = fCallback.insertSource(p, address, sourceFile, firstLine);
                    }
                }

                // insert symbol label
                FunctionOffset functionOffset = getFunctionOffset(address, symbols);
                if (functionOffset.name != null && functionOffset.isZeroOffset()) {
                    p = fCallback.getDocument().insertLabel(p, address, functionOffset.name, true);
                }

                // insert instruction
                int instrLength = instruction.getSize();
                Map<String,Object>[] instrAttrs = instruction.getInstruction();
                String instr = formatInstruction(instrAttrs);

                int offs = address.subtract(range.start).intValue();
                if (code != null && offs >= 0 && offs + instrLength <= code.length) {
                    BigInteger opcode = BigInteger.ZERO;
                    for (int i = 0; i < instrLength; i++) {
                        int j = big_endian ? i : instrLength - i - 1;
                        opcode = opcode.shiftLeft(8).add(BigInteger.valueOf(code[offs + j] & 0xff));
                    }
                    p = fCallback.getDocument().insertDisassemblyLine(p,
                            address, instrLength, functionOffset.toString(),
                            opcode, instr, sourceFile, firstLine);
                }
                else {
                    p = fCallback.getDocument().insertDisassemblyLine(p,
                            address, instrLength, functionOffset.toString(),
                            instr, sourceFile, firstLine);
                }
                if (p == null) break;
                insertedAnyAddress = true;
            }
        }
        catch (BadLocationException e) {
            // should not happen
            DisassemblyUtils.internalError(e);
        }
        finally {
            fCallback.setUpdatePending(false);
            if (insertedAnyAddress) {
                fCallback.updateInvalidSource();
                fCallback.unlockScroller();
                fCallback.doPending();
                fCallback.updateVisibleArea();
            }
            else {
                fCallback.unlockScroller();
            }
        }
    }

    private FunctionOffset getFunctionOffset(BigInteger address, ISymbols.Symbol[] symbols) {
        if (symbols != null) {
            for (ISymbols.Symbol symbol : symbols) {
                BigInteger symbolAddress = JSON.toBigInteger(symbol.getAddress());
                BigInteger offset = address.subtract(symbolAddress);
                switch (offset.compareTo(BigInteger.ZERO)) {
                case 0:
                    return new FunctionOffset(symbol.getName(), BigInteger.ZERO);
                case 1:
                    if (offset.compareTo(BigInteger.valueOf(symbol.getSize())) < 0) {
                        return new FunctionOffset(symbol.getName(), offset);
                    }
                    break;
                default:
                    break;
                }
            }
        }
        return FunctionOffset.NONE;
    }

    private CodeArea findCodeArea(BigInteger address, CodeArea[] codeAreas) {
        if (codeAreas != null) {
            for (CodeArea codeArea : codeAreas) {
                if (address.equals(JSON.toBigInteger(codeArea.start_address))) {
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
            if (buf.length() > 0) buf.append(' ');
            Object type = attrs.get(IDisassembly.FIELD_TYPE);
            if (IDisassembly.FTYPE_STRING.equals(type) || IDisassembly.FTYPE_Register.equals(type)) {
                Object text = attrs.get(IDisassembly.FIELD_TEXT);
                buf.append(text);
            }
            else {
                Object value = attrs.get(IDisassembly.FIELD_VALUE);
                BigInteger bigValue = new BigInteger(value.toString());
                // TODO number format
                buf.append("0x").append(bigValue.toString(16));
            }
        }
        return buf.toString();
    }

    public void gotoSymbol(final String symbol) {
        final TCFNodeStackFrame activeFrame = fActiveFrame;
        if (activeFrame == null) return;
        Protocol.invokeLater(new Runnable() {
            public void run() {
                if (activeFrame != fActiveFrame) return;
                IChannel channel = activeFrame.getChannel();
                final IExpressions exprSvc = channel.getRemoteService(IExpressions.class);
                if (exprSvc != null) {
                    TCFNode evalContext = activeFrame.isEmulated() ? activeFrame.getParent() : activeFrame;
                    exprSvc.create(evalContext.getID(), null, symbol, new DoneCreate() {
                        public void doneCreate(IToken token, Exception error, final Expression context) {
                            if (error == null) {
                                exprSvc.evaluate(context.getID(), new DoneEvaluate() {
                                    public void doneEvaluate(IToken token, Exception error, Value value) {
                                        if (error == null && value.getValue() != null) {
                                            final BigInteger address = TCFNumberFormat.toBigInteger(
                                                    value.getValue(), value.isBigEndian(), false);
                                            fCallback.asyncExec(new Runnable() {
                                                public void run() {
                                                    fCallback.gotoAddress(address);
                                                }
                                            });
                                        }
                                        else {
                                            handleError(error);
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
        if (activeFrame == null) return null;
        String value = new TCFTask<String>(activeFrame.getChannel()) {
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
                                        if (error == null && value.getValue() != null) {
                                            BigInteger address = TCFNumberFormat.toBigInteger(
                                                    value.getValue(), value.isBigEndian(), false);
                                            done("0x" + address.toString(16));
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
                }
                else {
                    done(null);
                }
            }
        }.getE();
        return value;
    }

    public void dispose() {
    }

    public Object insertSource(Position pos, BigInteger address, String file, int lineNumber) {
        TCFNodeExecContext ctx = fMemoryContext;
        if (ctx == null) return null;
        return TCFSourceLookupDirector.lookup(ctx.getModel().getLaunch(), ctx.getID(), file);
    }

    /*
     * @see org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AbstractDisassemblyBackend#evaluateAddressExpression(java.lang.String, boolean)
     */
    @Override
    public BigInteger evaluateAddressExpression(String expression, boolean suppressError) {
        String value = evaluateExpression(expression);
        if (value != null) {
            try {
                return DisassemblyUtils.decodeAddress(value);
            }
            catch (NumberFormatException e) {
                if (!suppressError) {
                    MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Error", "Expression does not evaluate to an address");
                }
            }
        }
        return null;
    }
}
