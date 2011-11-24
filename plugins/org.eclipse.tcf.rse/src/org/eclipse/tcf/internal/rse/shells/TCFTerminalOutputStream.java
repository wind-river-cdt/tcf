/*******************************************************************************
 * Copyright (c) 2010 Intel Corporation. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Liping Ke (Intel Corp.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.tcf.internal.rse.shells;

import java.io.IOException;

import java.io.OutputStream;

import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IStreams;
import org.eclipse.tcf.util.TCFTask;

public class TCFTerminalOutputStream extends OutputStream {
    private final TCFTerminalShell terminal;
    private final IStreams streams;
    private final String os_id;
    private boolean connected = true;

    public TCFTerminalOutputStream(TCFTerminalShell terminal, IStreams streams, String os_id) throws IOException{
        if (streams == null) throw new IOException("istream is null");//$NON-NLS-1$
        this.terminal = terminal;
        this.streams = streams;
        this.os_id = os_id;
    }

    @Override
    public synchronized void write(final byte b[], final int off, final int len) throws IOException {
        /* If eof is written, we can't write anything into the stream */
        if (!connected) throw new IOException("stream is not connected!");//$NON-NLS-1$
        new TCFTask<Object>() {
            public void run() {
                streams.write(os_id, b, off, len, new IStreams.DoneWrite() {
                    public void doneWrite(IToken token, Exception error) {
                        if (error != null) error(error);
                        else done(this);
                    }
                });
            }
        }.getIO();
    }

    @Override
    public synchronized void write(int b) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte)b;
        write(buf, 0, 1);
    }

    /* close must be called --Need to reconsider it in the future*/
    public void close() throws IOException {
        if (!connected) return;
        connected = false;
        new TCFTask<Object>() {
            public void run() {
                streams.eos(os_id, new IStreams.DoneEOS() {
                    public void doneEOS(IToken token, Exception error) {
                        if (error != null) {
                            error(error);
                            return;
                        }
                        streams.disconnect(os_id, new IStreams.DoneDisconnect() {
                            public void doneDisconnect(IToken token, Exception error) {
                                if (error != null) {
                                    error(error);
                                    return;
                                }
                                terminal.onOutputStreamClosed();
                                done(this);
                            }
                        });
                    }
                });
            }
        }.getIO();
    }
}
