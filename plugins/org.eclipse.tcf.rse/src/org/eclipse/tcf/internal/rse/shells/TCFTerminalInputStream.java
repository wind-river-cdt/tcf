/*******************************************************************************
 * Copyright (c) 2010, 2011 Intel Corporation. and others.
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
import java.io.InputStream;

import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IStreams;
import org.eclipse.tcf.util.TCFTask;

public class TCFTerminalInputStream extends InputStream {
    private final TCFTerminalShell terminal;
    private final IStreams streams;
    private final String is_id;
    private boolean connected = true; /* The stream is connected or not */
    private boolean bEof = false;;

    public TCFTerminalInputStream(TCFTerminalShell terminal, IStreams streams, String is_id) throws IOException{
        if (streams == null) throw new IOException("TCP streams is null");//$NON-NLS-1$
        this.terminal = terminal;
        this.streams = streams;
        this.is_id = is_id;
    }

    /* read must be synchronized */
    @Override
    public synchronized int read() throws IOException {
        if (!connected) throw new IOException("istream is not connected");//$NON-NLS-1$
        if (bEof) return -1;
        try {
            return new TCFTask<Integer>() {
                public void run() {
                    streams.read(is_id, 1, new IStreams.DoneRead() {
                        public void doneRead(IToken token, Exception error, int lostSize,
                                byte[] data, boolean eos) {
                            if (error != null) {
                                error(error);
                                return;
                            }
                            bEof = eos;
                            if (data != null) {
                                done(data[0] & 0xff);
                            }
                            else {
                                done(-1);
                            }
                        }
                    });
                }
            }.getIO();
        }
        catch (IOException e) {
            if (!connected) return -1;
            throw e;
        }
    }

    public synchronized int read(byte b[], final int off, final int len) throws IOException {
        if (!connected) throw new IOException("istream is not connected");//$NON-NLS-1$
        if (bEof) return -1;
        if (b == null) throw new NullPointerException();
        if (off < 0 || len < 0 || len > b.length - off) throw new IndexOutOfBoundsException();
        if (len == 0) return 0;
        try {
            byte[] data = new TCFTask<byte[]>() {
                public void run() {
                    streams.read(is_id, len, new IStreams.DoneRead() {
                        public void doneRead(IToken token, Exception error, int lostSize,
                                byte[] data, boolean eos) {
                            if (error != null) {
                                error(error);
                                return;
                            }
                            bEof = eos;
                            done(data);
                        }
                    });
                }
            }.getIO();

            if (data != null) {
                int length = data.length;
                System.arraycopy(data, 0, b, off, length);
                return length;
            }
            if (bEof) return -1;
            return 0;
        }
        catch (IOException e) {
            if (!connected) return -1;
            throw e;
        }
    }

    public void close() throws IOException {
        if (!connected) return;
        connected = false;
        new TCFTask<Object>() {
            public void run() {
                streams.disconnect(is_id, new IStreams.DoneDisconnect() {
                    public void doneDisconnect(IToken token, Exception error) {
                        if (error != null) {
                            error(error);
                            return;
                        }
                        terminal.onInputStreamClosed();
                        done(this);
                    }
                });
            }
        }.getIO();
    }
}
