/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import org.eclipse.tm.tcf.core.TransientPeer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IEventQueue;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * This class is user to run TCF test suite from command line.
 */
public class Main {

    private static boolean flag_t = false;

    private static final Random rnd = new Random();

    private static class EventQueue extends Thread implements IEventQueue {

        private final LinkedList<Runnable> queue = new LinkedList<Runnable>();

        EventQueue() {
            setName("TCF Event Dispatcher");
            start();
        }

        public void run() {
            try {
                while (true) {
                    Runnable r = null;
                    synchronized (this) {
                        while (queue.size() == 0) wait();
                        r = queue.removeFirst();
                    }
                    r.run();
                }
            }
            catch (Throwable x) {
                x.printStackTrace();
                System.exit(1);
            }
        }

        public synchronized int getCongestion() {
            int n = queue.size() - 100;
            if (n > 100) n = 100;
            return n;
        }

        public synchronized void invokeLater(Runnable runnable) {
            queue.add(runnable);
            notify();
        }

        public boolean isDispatchThread() {
            return Thread.currentThread() == this;
        }
    }

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
            if (s.equals("-t")) {
                flag_t = true;
                continue;
            }
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

    private static void runTestSuite(final IPeer peer) {
        TCFTestSuite.TestListener listener = new TCFTestSuite.TestListener() {

            public void done(Collection<Throwable> errors) {
                if (!flag_t && (errors == null || errors.isEmpty())) {
                    System.out.println("No errors detected.");
                    System.exit(0);
                }
                for (Throwable x : errors) {
                    x.printStackTrace(System.out);
                }
                if (!flag_t) System.exit(3);
                runTestSuite(peer);
            }

            public void progress(String label, int done, int total) {
                if (label != null) System.out.println(label);
            }

        };
        try {
            final TCFTestSuite test = new TCFTestSuite(peer, listener, null, null);
            if (flag_t) {
                test.setContinueOnError(true);
                Protocol.invokeLater(rnd.nextInt(2000), new Runnable() {
                    public void run() {
                        IChannel[] arr = test.getChannels();
                        for (IChannel c : arr) {
                            if (c.getState() == IChannel.STATE_OPEN) {
                                c.terminate(new Exception("Channel termination test"));
                                Protocol.invokeLater(rnd.nextInt(2000), this);
                                return;
                            }
                        }
                    }
                });
            }
        }
        catch (Throwable x) {
            System.err.println("Cannot start test suite:");
            x.printStackTrace();
            System.exit(2);
        }
    }

    /**
     * Command line should contain peer description string, for example:
     * "ID=Test:TransportName=TCP:Host=127.0.0.1:Port=1534"
     */
    public static void main(final String[] args) {
        if (args.length < 1) {
            System.err.println("Missing command line argument - peer identification string");
            System.exit(4);
        }
        Protocol.setEventQueue(new EventQueue());
        Protocol.invokeLater(new Runnable() {
            public void run() {
                runTestSuite(getPeer(args));
                if (flag_t) return;
                Protocol.invokeLater(10 * 60 * 1000, new Runnable() {
                    public void run() {
                        System.err.println("Error: timeout - test's not finished in 10 min");
                        System.exit(5);
                    }
                });
            }
        });
    }
}
