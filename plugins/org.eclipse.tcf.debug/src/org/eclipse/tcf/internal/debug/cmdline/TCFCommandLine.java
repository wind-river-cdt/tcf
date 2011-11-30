/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.cmdline;

import org.eclipse.tcf.util.TCFTask;

public class TCFCommandLine {

    private static final class CommandInfo {
        final String name;
        final Class<?> cls;

        CommandInfo(String name, Class<?> cls) {
            this.name = name;
            this.cls = cls;
        }
    }

    private static final CommandInfo[] command_list = {
        new CommandInfo("step", CommandStep.class),
    };

    private class CommandStep extends TCFTask<String> {

        public void run() {
            done("OK");
        }
    }

    public String command(String cmd) {
        try {
            int i = 0;
            int l = cmd.length();
            StringBuffer bf = new StringBuffer();
            while (i < l) {
                char ch = cmd.charAt(i);
                if (ch == ' ' || ch == '#') break;
                bf.append(ch);
                i++;
            }
            if (bf.length() > 0) {
                CommandInfo cmd_info = null;
                String name = bf.toString();
                for (CommandInfo c : command_list) {
                    if (c.name.startsWith(name)) {
                        if (cmd_info != null) return "Ambiguous command";
                        cmd_info = c;
                    }
                }
                if (cmd_info == null) return "Unknown command";
                @SuppressWarnings("unchecked")
                TCFTask<String> task = (TCFTask<String>)cmd_info.cls.getConstructors()[0].newInstance(this);
                return task.get();
            }
        }
        catch (Throwable x) {
            return x.getMessage();
        }
        return null;
    }
}
