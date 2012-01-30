# *******************************************************************************
# * Copyright (c) 2011 Wind River Systems, Inc. and others.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *     Wind River Systems - initial API and implementation
# *******************************************************************************

import sys, time, threading, atexit
import tcf
from tcf import protocol, channel, errors
from tcf.util import sync

__TRACE = False
class TraceListener(channel.TraceListener):
    def onMessageReceived(self, type, token, service, name, data):
        print "<<<", type, token, service, name, data
    def onMessageSent(self, type, token, service, name, data):
        print ">>>", type, token, service, name, data
    def onChannelClosed(self, error):
        print>>sys.stderr, "*** closed ***", error

_suspended = []
_memory = []

def test():
    protocol.startEventQueue()
    atexit.register(protocol.getEventQueue().shutdown)
    #testTimer()
    try:
        c = tcf.connect("TCP:127.0.0.1:1534")
    except Exception as e:
        protocol.log(e)
        sys.exit()
    assert c.state == channel.STATE_OPEN
    if __TRACE: protocol.invokeLater(c.addTraceListener, TraceListener())
    def printServices():
        print "services=", c.getRemoteServices()
    protocol.invokeLater(printServices)

    try:
        testRunControl(c)
        testStackTrace(c)
        testDisassembly(c)
        testBreakpoints(c)
        testSymbols(c)
        testRegisters(c)
        testExpressions(c)
        testLineNumbers(c)
        testSyncCommands(c)
        testTasks(c)
        testEvents(c)
        testDataCache(c)
        testProcesses(c)
        testFileSystem(c)
        testMemory(c)
        testMemoryMap(c)
        testPathMap(c)
        testSysMonitor(c)
    except Exception as e:
        protocol.log(e)

    if c.state == channel.STATE_OPEN:
        time.sleep(5)
        protocol.invokeLater(c.close)
    time.sleep(2)


def testTimer():
    cond = threading.Condition()
    def countdown(left):
        if left == 0:
            print "Ignition sequence started!"
            with cond:
                cond.notify()
            return
        print "%d seconds to go" % left
        sys.stdout.flush()
        protocol.invokeLaterWithDelay(1000, countdown, left - 1)
    with cond:
        protocol.invokeLaterWithDelay(0, countdown, 10)
        cond.wait(15)

def testRunControl(c):
    lock = threading.Condition()
    from tcf.services import runcontrol
    def getContexts():
        rctrl = c.getRemoteService(runcontrol.NAME)
        pending = []
        class DoneGetContext(runcontrol.DoneGetContext):
            def doneGetContext(self, token, error, context):
                pending.remove(token)
                if error:
                    protocol.log("Error from RunControl.getContext", error)
                else:
                    print context
                class DoneGetState(runcontrol.DoneGetState):
                    def doneGetState(self, token, error, suspended, pc, reason, params):
                        pending.remove(token)
                        if error:
                            protocol.log("Error from RunControl.getState", error)
                        else:
                            print "suspended: ", suspended
                            print "pc:        ", pc
                            print "reason:    ", reason
                            print "params:    ", params
                        if suspended:
                            _suspended.append(context.getID())
                        if len(pending) == 0:
                            with lock:
                                lock.notify()
                if context and context.hasState(): pending.append(context.getState(DoneGetState()))
                if len(pending) == 0:
                    with lock:
                        lock.notify()
        class DoneGetChildren(runcontrol.DoneGetChildren):
            def doneGetChildren(self, token, error, context_ids):
                pending.remove(token)
                if error:
                    protocol.log("Error from RunControl.GetChildren", error)
                else:
                    for c in context_ids:
                        pending.append(rctrl.getContext(c, DoneGetContext()))
                        pending.append(rctrl.getChildren(c, self))
                if len(pending) == 0:
                    with lock:
                        lock.notify()
        pending.append(rctrl.getChildren(None, DoneGetChildren()))
    with lock:
        protocol.invokeLater(getContexts)
        lock.wait(5)
    def listenerTest():
        rc = c.getRemoteService(runcontrol.NAME)
        class RCListener(runcontrol.RunControlListener):
            def contextSuspended(self, *args):
                print "context suspended: ", args
                rc.removeListener(self)
            def contextResumed(self, *args):
                print "context resumed: ", args
            def containerSuspended(self, *args):
                print "container suspended:", args
                rc.removeListener(self)
            def containerResumed(self, *args):
                print "container resumed:", args
        rc.addListener(RCListener())
        class DoneGetContext(runcontrol.DoneGetContext):
            def doneGetContext(self, token, error, context):
                if error:
                    protocol.log("Error from RunControl.getContext", error)
                    with lock: lock.notify()
                    return
                class DoneResume(runcontrol.DoneCommand):
                    def doneCommand(self, token, error):
                        if error:
                            protocol.log("Error from RunControl.resume", error)
                        else:
                            context.suspend(runcontrol.DoneCommand())
                        with lock: lock.notify()
                context.resume(runcontrol.RM_RESUME, 1, None, DoneResume())
        rc.getContext(_suspended[0], DoneGetContext())

    if _suspended:
        with lock:
            protocol.invokeLater(listenerTest)
            lock.wait(5)

def testBreakpoints(c):
    from tcf.services import breakpoints
    def testBPQuery():
        bps = c.getRemoteService(breakpoints.NAME)
        def doneGetIDs(token, error, ids):
            if error:
                protocol.log("Error from Breakpoints.getIDs", error)
                return
            print "Breakpoints :", ids
            def doneGetProperties(token, error, props):
                if error:
                    protocol.log("Error from Breakpoints.getProperties", error)
                    return
                print "Breakpoint Properties: ", props
            def doneGetStatus(token, error, props):
                if error:
                    protocol.log("Error from Breakpoints.getStatus", error)
                    return
                print "Breakpoint Status: ", props
            for id in ids:
                bps.getProperties(id, doneGetProperties)
                bps.getStatus(id, doneGetStatus)
        bps.getIDs(doneGetIDs)
    protocol.invokeLater(testBPQuery)
    def testBPSet():
        bpsvc = c.getRemoteService(breakpoints.NAME)
        class BPListener(breakpoints.BreakpointsListener):
            def breakpointStatusChanged(self, id, status):
                print "breakpointStatusChanged", id, status
            def contextAdded(self, bps):
                print "breakpointAdded", bps
                bpsvc.removeListener(self)
            def contextChanged(self, bps):
                print "breakpointChanged", bps
            def contextRemoved(self, ids):
                print "breakpointRemoved", ids
        bpsvc.addListener(BPListener())
        def doneSet(token, error):
            if error:
                protocol.log("Error from Breakpoints.set", error)
                return
        bp = {
            breakpoints.PROP_ID : "python:1",
            breakpoints.PROP_ENABLED : True,
            breakpoints.PROP_LOCATION : "sysClkRateGet"
        }
        bpsvc.set([bp], doneSet)
    protocol.invokeLater(testBPSet)

def testStackTrace(c):
    from tcf.services import stacktrace
    def stackTest(ctx_id):
        stack = c.getRemoteService(stacktrace.NAME)
        class DoneGetChildren(stacktrace.DoneGetChildren):
            def doneGetChildren(self, token, error, ctx_ids):
                if error:
                    protocol.log("Error from StackTrace.getChildren", error)
                    return
                class DoneGetContext(stacktrace.DoneGetContext):
                    def doneGetContext(self, token, error, ctxs):
                        if error:
                            protocol.log("Error from StackTrace.getContext", error)
                            return
                        if ctxs:
                            for ctx in ctxs:
                                print ctx
                stack.getContext(ctx_ids, DoneGetContext())
        stack.getChildren(ctx_id, DoneGetChildren())
    for ctx_id in _suspended:
        protocol.invokeLater(stackTest, ctx_id)

def testDisassembly(c):
    if not _suspended: return
    ctl = sync.CommandControl(c)
    try:
        dis = ctl.Disassembly
    except AttributeError:
        # no Disassembly service
        return
    for ctx_id in _suspended:
        frames = ctl.StackTrace.getChildren(ctx_id).get()
        if frames:
            frameData = ctl.StackTrace.getContext(frames).get()
            if frameData:
                addr = frameData[0].get("IP")
                if addr:
                    print "Disassemble context %s from 0x%x" % (ctx_id, addr)
                    lines = dis.disassemble(ctx_id, addr, 256, None).get()
                    if lines:
                        for line in lines:
                            print line

def testSymbols(c):
    from tcf.services import symbols
    def symTest(ctx_id):
        syms = c.getRemoteService(symbols.NAME)
        class DoneList(symbols.DoneList):
            def doneList(self, token, error, ctx_ids):
                if error:
                    protocol.log("Error from Symbols.list", error)
                    return
                class DoneGetContext(symbols.DoneGetContext):
                    def doneGetContext(self, token, error, ctx):
                        if error:
                            protocol.log("Error from Symbols.getContext", error)
                            return
                        print ctx
                if ctx_ids:
                    for ctx_id in ctx_ids:
                        syms.getContext(ctx_id, DoneGetContext())
        syms.list(ctx_id, DoneList())
    for ctx_id in _suspended:
        protocol.invokeLater(symTest, ctx_id)

def testRegisters(c):
    if not _suspended: return
    from tcf.services import registers
    lock = threading.Condition()
    def regTest(ctx_id):
        regs = c.getRemoteService(registers.NAME)
        pending = []
        def onDone():
            with lock: lock.notify()
        class DoneGetChildren(registers.DoneGetChildren):
            def doneGetChildren(self, token, error, ctx_ids):
                pending.remove(token)
                if error:
                    protocol.log("Error from Registers.getChildren", error)
                if not pending:
                    onDone()
                class DoneGetContext(registers.DoneGetContext):
                    def doneGetContext(self, token, error, ctx):
                        pending.remove(token)
                        if error:
                            protocol.log("Error from Registers.getContext", error)
                        else:
                            print ctx
                            if ctx.isReadable() and not ctx.isReadOnce() and ctx.getSize() >= 2:
                                locs = []
                                locs.append(registers.Location(ctx.getID(), 0, 1))
                                locs.append(registers.Location(ctx.getID(), 1, 1))
                                class DoneGetM(registers.DoneGet):
                                    def doneGet(self, token, error, value):
                                        pending.remove(token)
                                        if error:
                                            protocol.log("Error from Registers.getm", error)
                                        else:
                                            print "getm", ctx.getID(), map(ord, value)
                                        if not pending:
                                            onDone()
                                pending.append(regs.getm(locs, DoneGetM()))
                            if ctx.isWriteable() and not ctx.isWriteOnce() and ctx.getSize() >= 2:
                                locs = []
                                locs.append(registers.Location(ctx.getID(), 0, 1))
                                locs.append(registers.Location(ctx.getID(), 1, 1))
                                class DoneSetM(registers.DoneSet):
                                    def doneGet(self, token, error):
                                        pending.remove(token)
                                        if error:
                                            protocol.log("Error from Registers.setm", error)
                                        if not pending:
                                            onDone()
                                pending.append(regs.setm(locs, (255, 255), DoneSetM()))
                        if not pending:
                            onDone()
                if ctx_ids:
                    for ctx_id in ctx_ids:
                        pending.append(regs.getContext(ctx_id, DoneGetContext()))
        pending.append(regs.getChildren(ctx_id, DoneGetChildren()))
    with lock:
        for ctx_id in _suspended:
            protocol.invokeLater(regTest, ctx_id)
        lock.wait(5)

def testExpressions(c):
    if not _suspended: return
    from tcf.services import expressions
    ctl = sync.CommandControl(c)
    exprs = ctl.Expressions
    e = exprs.create(_suspended[0], None, "1+2*(3-4/2)").getE()
    id = e.get(expressions.PROP_ID)
    val, cls = exprs.evaluate(id).getE()
    print e.get(expressions.PROP_EXPRESSION), "=", val
    exprs.dispose(id)

def testLineNumbers(c):
    if not _suspended: return
    from tcf.services import stacktrace
    ctl = sync.CommandControl(c)
    stack = ctl.StackTrace
    lineNumbers = ctl.LineNumbers
    for ctx_id in _suspended:
        bt = stack.getChildren(ctx_id).get()
        if bt:
            bt = stack.getContext(bt).get()
            for frame in bt:
                addr = frame.get(stacktrace.PROP_INSTRUCTION_ADDRESS)
                area = lineNumbers.mapToSource(ctx_id, addr, addr+1).get()
                print "Frame %d - CodeArea: %s" % (frame.get(stacktrace.PROP_LEVEL), area)

def testSyncCommands(c):
    # simplified command execution
    ctl = sync.CommandControl(c)
    try:
        diag = ctl.Diagnostics
    except AttributeError:
        # no Diagnostics service
        return
    s = "Hello TCF World"
    r = diag.echo(s).getE()
    assert s == r
    pi = 3.141592654
    r = diag.echoFP(pi).getE()
    assert pi == r
    e = errors.ErrorReport("Test", errors.TCF_ERROR_OTHER)
    r = diag.echoERR(e.getAttributes()).getE()
    assert e.getAttributes() == r
    print "Diagnostic tests:", diag.getTestList().getE()

    for ctx_id in _suspended:
        print "Symbols:", ctl.Symbols.list(ctx_id)
    for ctx_id in _suspended:
        frame_ids = ctl.StackTrace.getChildren(ctx_id).get()
        if frame_ids:
            error, args = ctl.StackTrace.getContext(frame_ids)
            if not error: print "Stack contexts:", args
    def gotBreakpoints(error, bps):
        print "Got breakpoint list:", bps
    ctl.Breakpoints.getIDs(onDone=gotBreakpoints)
    try:
        print ctl.Processes.getChildren(None, False)
    except:
        pass # no Processes service

def testTasks(c):
    if not _suspended: return
    from tcf.services import expressions
    from tcf.util import task
    def compute(expr, done=None):
        es = c.getRemoteService(expressions.NAME)
        if not es:
            done(Exception("No Expressions service"), None)
            return
        def doneCreate(token, error, ctx):
            if error:
                done(error, None)
                return
            def doneEval(token, error, val):
                done(error, val)
            es.evaluate(ctx.getID(), doneEval)
        es.create(_suspended[0], None, expr, doneCreate)
    t = task.Task(compute, "1+2*(3-4/2)", channel=c)
    val = t.get()
    print "Task result:", val

def testEvents(c):
    from tcf.util import event
    recorder = event.EventRecorder(c)
    recorder.record("RunControl")
    ctl = sync.CommandControl(c)
    try:
        rc = ctl.RunControl
    except AttributeError:
        # no RunControl service
        return
    ctxs = rc.getChildren(None).get()
    if not ctxs: return
    ctx = ctxs[0]
    rc.resume(ctx, 0, 1, None).wait()
    print recorder
    rc.suspend(ctx).wait()
    print recorder
    recorder.stop()

def testDataCache(c):
    from tcf.util import cache
    from tcf.services import runcontrol
    class ContextsCache(cache.DataCache):
        def startDataRetrieval(self):
            rc = self._channel.getRemoteService(runcontrol.NAME)
            if not rc:
                self.set(None, Exception("No RunControl service"), None)
                return
            cache = self
            pending = []
            contexts = []
            class DoneGetChildren(runcontrol.DoneGetChildren):
                def doneGetChildren(self, token, error, context_ids):
                    pending.remove(token)
                    if error:
                        protocol.log("Error from RunControl.GetChildren", error)
                    else:
                        for c in context_ids:
                            contexts.append(c)
                            pending.append(rc.getChildren(c, self))
                    if len(pending) == 0:
                        cache.set(None, None, contexts)
            pending.append(rc.getChildren(None, DoneGetChildren()))
    contextsCache = ContextsCache(c)
    def done():
        print "ContextsCache is valid:", contextsCache.getData()
    protocol.invokeLater(contextsCache.validate, done)

def testProcesses(c):
    from tcf.services import processes, processes_v1
    def processTest():
        proc = c.getRemoteService(processes_v1.NAME) or c.getRemoteService(processes.NAME)
        if not proc:
            return
        class DoneGetChildren(processes.DoneGetChildren):
            def doneGetChildren(self, token, error, context_ids):
                if error:
                    protocol.log("Error from Processes.GetChildren", error)
                else:
                    print "Processes:", context_ids
        proc.getChildren(None, False, DoneGetChildren())
    protocol.invokeLater(processTest)

def testFileSystem(c):
    cmd = sync.CommandControl(c)
    try:
        fs = cmd.FileSystem
    except AttributeError:
        # no FileSystem service
        return
    roots = fs.roots().get()
    print "FileSystem roots:", roots
    user = fs.user().get()
    print "User info: ", user

def testMemory(c):
    lock = threading.Condition()
    from tcf.services import memory
    def getContexts():
        mem = c.getRemoteService(memory.NAME)
        pending = []
        class DoneGetContext(memory.DoneGetContext):
            def doneGetContext(self, token, error, context):
                pending.remove(token)
                if error:
                    protocol.log("Error from Memory.getContext", error)
                else:
                    print context
                if len(pending) == 0:
                    with lock:
                        lock.notify()
        class DoneGetChildren(memory.DoneGetChildren):
            def doneGetChildren(self, token, error, context_ids):
                pending.remove(token)
                if error:
                    protocol.log("Error from Memory.GetChildren", error)
                else:
                    for c in context_ids:
                        _memory.append(c)
                        pending.append(mem.getContext(c, DoneGetContext()))
                        pending.append(mem.getChildren(c, self))
                if len(pending) == 0:
                    with lock:
                        lock.notify()
        pending.append(mem.getChildren(None, DoneGetChildren()))
    with lock:
        protocol.invokeLater(getContexts)
        lock.wait(5)

def testMemoryMap(c):
    if not _memory: return
    cmd = sync.CommandControl(c)
    try:
        mm = cmd.MemoryMap
    except AttributeError:
        # no MemoryMap service
        return
    id = _memory[0]
    lock = threading.Condition()
    from tcf.services import memorymap
    def getMap():
        mm = c.getRemoteService(memorymap.NAME)
        class DoneGet(memorymap.DoneGet):
            def doneGet(self, token, error, map):
                if error:
                    protocol.log("Error from MemoryMap.get", error)
                else:
                    print map
                with lock:
                    lock.notify()
        mm.get(id, DoneGet())
    with lock:
        protocol.invokeLater(getMap)
        lock.wait(1)
    def setMap():
        mm = c.getRemoteService(memorymap.NAME)
        class DoneSet(memorymap.DoneSet):
            def doneSet(self, token, error):
                if error:
                    protocol.log("Error from MemoryMap.set", error)
                with lock:
                    lock.notify()
        mm.set(id, {memorymap.PROP_FILE_NAME : "/tmp/system.elf"}, DoneSet())
    with lock:
        protocol.invokeLater(setMap)
        lock.wait(1)
    map = mm.get(id).get()
    print "Memory map:", map

def testPathMap(c):
    cmd = sync.CommandControl(c)
    try:
        pm = cmd.PathMap
    except AttributeError:
        # no PathMap service
        return
    lock = threading.Condition()
    from tcf.services import pathmap
    def getMap():
        pm = c.getRemoteService(pathmap.NAME)
        class DoneGet(pathmap.DoneGet):
            def doneGet(self, token, error, map):
                if error:
                    protocol.log("Error from PathMap.get", error)
                else:
                    print map
                with lock:
                    lock.notify()
        pm.get(DoneGet())
    with lock:
        protocol.invokeLater(getMap)
        lock.wait(1)
    def setMap():
        pm = c.getRemoteService(pathmap.NAME)
        class DoneSet(pathmap.DoneSet):
            def doneSet(self, token, error):
                if error:
                    protocol.log("Error from PathMap.set", error)
                with lock:
                    lock.notify()
        pm.set({pathmap.PROP_SOURCE : "/tmp",
                pathmap.PROP_DESTINATION : "/home"}, DoneSet())
    with lock:
        protocol.invokeLater(setMap)
        lock.wait(1)
    map = pm.get().get()
    print "Path map:", map

def testSysMonitor(c):
    cmd = sync.CommandControl(c)
    try:
        sm = cmd.SysMonitor
    except AttributeError:
        # no SysMonotor service
        return
    lock = threading.Condition()
    from tcf.services import sysmonitor
    processes = []
    def getProcesses():
        sm = c.getRemoteService(sysmonitor.NAME)
        pending = []
        class DoneGetChildren(sysmonitor.DoneGetChildren):
            def doneGetChildren(self, token, error, context_ids):
                pending.remove(token)
                if error:
                    protocol.log("Error from SysMonitor.getChildren", error)
                else:
                    class DoneGetContext(sysmonitor.DoneGetContext):
                        def doneGetContext(self, token, error, context):
                            pending.remove(token)
                            if error:
                                protocol.log("Error from SysMonitor.getContext", error)
                            else:
                                processes.append(context)
                            if not pending:
                                with lock:
                                    lock.notify()
                    for id in context_ids:
                        pending.append(sm.getContext(id, DoneGetContext()))
                if not pending:
                    with lock:
                        lock.notify()
        pending.append(sm.getChildren(None, DoneGetChildren()))
    with lock:
        protocol.invokeLater(getProcesses)
        lock.wait(5)
    print "%d processes found:" % len(processes)
    for p in processes:
        print p
        cmdl = sm.getCommandLine(p.getID()).get()
        if cmdl: print "Command line: ", cmdl
        envp = sm.getEnvironment(p.getID()).get()
        print "Environment: ", envp

if __name__ == '__main__':
    test()
