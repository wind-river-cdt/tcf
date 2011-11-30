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
package org.eclipse.tcf.internal.debug.ui.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.internal.debug.cmdline.TCFCommandLine;
import org.eclipse.tcf.internal.debug.ui.Activator;
import org.eclipse.tcf.internal.debug.ui.ImageCache;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

class TCFConsole {
    private final TCFModel model;
    private final IOConsole console;
    private final Display display;
    private final String process_id;
    private final LinkedList<Message> out_queue;
    private final TCFCommandLine cmd_line;

    private final byte[] prompt = { 't', 'c', 'f', '>' };
    private final StringBuffer cmd_buf = new StringBuffer();

    private static class Message {
        int stream_id;
        byte[] data;
    }

    private final Thread inp_thread = new Thread() {
        public void run() {
            try {
                IOConsoleInputStream inp = console.getInputStream();
                final byte[] buf = new byte[0x100];
                for (;;) {
                    int len = inp.read(buf);
                    if (len < 0) break;
                    // TODO: Eclipse Console view has a bad habit of replacing CR with CR/LF
                    if (len == 2 && buf[0] == '\r' && buf[1] == '\n') len = 1;
                    final int n = len;
                    Protocol.invokeAndWait(new Runnable() {
                        public void run() {
                            try {
                                if (cmd_line != null) {
                                    String s = new String(buf, 0, n, "UTF-8");
                                    int l = s.length();
                                    for (int i = 0; i < l; i++) {
                                        char ch = s.charAt(i);
                                        if (ch == '\r') {
                                            String res = cmd_line.command(cmd_buf.toString());
                                            cmd_buf.setLength(0);
                                            if (res != null) {
                                                if (res.length() > 0 && res.charAt(res.length() - 1) != '\n') {
                                                    res += '\n';
                                                }
                                                write(0, res.getBytes("UTF-8"));
                                            }
                                            write(0, prompt);
                                        }
                                        else if (ch == '\b') {
                                            int n = cmd_buf.length();
                                            if (n > 0) n--;
                                            cmd_buf.setLength(n);
                                        }
                                        else {
                                            cmd_buf.append(ch);
                                        }
                                    }
                                }
                                else {
                                    model.getLaunch().writeProcessInputStream(process_id, buf, 0, n);
                                }
                            }
                            catch (Exception x) {
                                model.onProcessStreamError(process_id, 0, x, 0);
                            }
                        }
                    });
                }
            }
            catch (Throwable x) {
                Activator.log("Cannot read console input", x);
            }
        }
    };

    private final Thread out_thread = new Thread() {
        public void run() {
            Map<Integer,IOConsoleOutputStream> out_streams =
                new HashMap<Integer,IOConsoleOutputStream>();
            try {
                for (;;) {
                    Message m = null;
                    synchronized (out_queue) {
                        while (out_queue.size() == 0) out_queue.wait();
                        m = out_queue.removeFirst();
                    }
                    if (m.data == null) break;
                    IOConsoleOutputStream stream = out_streams.get(m.stream_id);
                    if (stream == null) {
                        final int id = m.stream_id;
                        final IOConsoleOutputStream s = stream = console.newOutputStream();
                        display.syncExec(new Runnable() {
                            public void run() {
                                try {
                                    int color_id = SWT.COLOR_BLACK;
                                    switch (id) {
                                    case 1: color_id = SWT.COLOR_RED; break;
                                    case 2: color_id = SWT.COLOR_BLUE; break;
                                    case 3: color_id = SWT.COLOR_GREEN; break;
                                    }
                                    s.setColor(display.getSystemColor(color_id));
                                }
                                catch (Throwable x) {
                                    Activator.log("Cannot open console view", x);
                                }
                            }
                        });
                        out_streams.put(m.stream_id, stream);
                    }
                    stream.write(m.data, 0, m.data.length);
                }
            }
            catch (Throwable x) {
                Activator.log("Cannot write console output", x);
            }
            for (IOConsoleOutputStream stream : out_streams.values()) {
                try {
                    stream.close();
                }
                catch (IOException x) {
                    Activator.log("Cannot close console stream", x);
                }
            }
            try {
                console.getInputStream().close();
            }
            catch (IOException x) {
                Activator.log("Cannot close console stream", x);
            }
            try {
                display.syncExec(new Runnable() {
                    public void run() {
                        IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
                        manager.removeConsoles(new IOConsole[]{ console });
                    }
                });
            }
            catch (SWTException x) {
                if (x.code == SWT.ERROR_DEVICE_DISPOSED) return;
                Activator.log("Cannot remove console", x);
            }
        }
    };

    /* process_id == null means debug console */
    TCFConsole(final TCFModel model, String process_id) {
        this.model = model;
        this.process_id = process_id;
        display = model.getDisplay();
        out_queue = new LinkedList<Message>();
        String image = process_id != null ? ImageCache.IMG_PROCESS_RUNNING : ImageCache.IMG_TCF;
        console = new IOConsole(
                "TCF " + (process_id != null ? process_id : "Debugger"), null,
                ImageCache.getImageDescriptor(image), "UTF-8", true);
        cmd_line = process_id != null ? null : new TCFCommandLine();
        if (cmd_line != null) write(0, prompt);
        display.asyncExec(new Runnable() {
            public void run() {
                if (!PlatformUI.isWorkbenchRunning() || PlatformUI.getWorkbench().isStarting()) {
                    display.timerExec(200, this);
                }
                else if (!PlatformUI.getWorkbench().isClosing()) {
                    try {
                        IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
                        manager.addConsoles(new IConsole[]{ console });
                        IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                        if (w == null) return;
                        IWorkbenchPage page = w.getActivePage();
                        if (page == null) return;
                        IConsoleView view = (IConsoleView)page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
                        view.display(console);
                    }
                    catch (Throwable x) {
                        Activator.log("Cannot open console view", x);
                    }
                }
            }
        });
        inp_thread.setName("TCF Console Input");
        out_thread.setName("TCF Console Output");
        inp_thread.start();
        out_thread.start();
    }

    void write(final int stream_id, byte[] data) {
        if (data == null || data.length == 0) return;
        synchronized (out_queue) {
            Message m = new Message();
            m.stream_id = stream_id;
            m.data = data;
            out_queue.add(m);
            out_queue.notify();
        }
    }

    void close() {
        synchronized (out_queue) {
            out_queue.add(new Message());
            out_queue.notify();
        }
    }
}
