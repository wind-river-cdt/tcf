/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.launch;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate.PathMapRule;
import org.eclipse.tcf.internal.debug.model.TCFSourceRef;
import org.eclipse.tcf.services.ILineNumbers;
import org.eclipse.tcf.services.IPathMap;

/**
 * The TCF source lookup participant knows how to translate
 * a ILineNumbers.CodeArea into a source file name.
 */
public class TCFSourceLookupParticipant extends AbstractSourceLookupParticipant {

    @SuppressWarnings("serial")
    private final LinkedHashMap<String,Object[]> cache = new LinkedHashMap<String,Object[]>(511, 0.75f, true) {
        @Override
        @SuppressWarnings("rawtypes")
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 1023;
        }
    };

    @Override
    public void sourceContainersChanged(ISourceLookupDirector director) {
        cache.clear();
    }

    public String getSourceName(Object object) throws CoreException {
        if (object instanceof String) {
            return (String)object;
        }
        if (object instanceof ILineNumbers.CodeArea) {
            ILineNumbers.CodeArea area = (ILineNumbers.CodeArea)object;
            return toFileName(area);
        }
        if (object instanceof TCFSourceRef) {
            TCFSourceRef ref = (TCFSourceRef)object;
            if (ref.area == null) return null;
            return toFileName(ref.area);
        }
        return null;
    }

    public static String toFileName(ILineNumbers.CodeArea area) {
        if (area.directory != null && area.file != null && !isAbsolutePath(area.file)) {
            return area.directory + "/" + area.file;
        }
        return area.file;
    }

    public static String toFileName(IPathMap.PathMapRule r, String fnm) {
        try {
            String src = r.getSource();
            if (src == null) return null;
            if (!fnm.startsWith(src)) return null;
            String host = r.getHost();
            if (host != null && host.length() > 0) {
                if (!InetAddress.getLocalHost().equals(InetAddress.getByName(host))) return null;
            }
            String dst = r.getDestination();
            if (dst == null || dst.length() == 0) return null;
            int l = src.length();
            if (dst.endsWith("/") && l < fnm.length() && fnm.charAt(l) == '/') l++;
            return dst + fnm.substring(l);
        }
        catch (Exception x) {
            return null;
        }
    }

    private static boolean isAbsolutePath(String fnm) {
        if (fnm.length() == 0) return false;
        char ch = fnm.charAt(0);
        if (ch == '/' || ch == '\\') return true;
        if (fnm.length() >= 3 && fnm.charAt(1) == ':') {
            ch = fnm.charAt(2);
            if (ch == '/' || ch == '\\') return true;
        }
        return false;
    }

    private String applyPathMap(String fnm) {
        ILaunchConfiguration cfg = getDirector().getLaunchConfiguration();
        if (cfg == null) return fnm;
        try {
            String path_map = cfg.getAttribute(TCFLaunchDelegate.ATTR_PATH_MAP, "");
            if (path_map.length() == 0) return fnm;
            ArrayList<PathMapRule> map = TCFLaunchDelegate.parsePathMapAttribute(path_map);
            for (PathMapRule r : map) {
                String query = r.getContextQuery();
                if (query != null && query.length() > 0 && !query.equals("*")) continue;
                String res = toFileName(r, fnm);
                if (res != null) return res;
            }
            if (fnm.startsWith("/cygdrive/")) {
                fnm = fnm.substring(10, 11) + ":" + fnm.substring(11);
            }
            return fnm;
        }
        catch (Exception x) {
            return fnm;
        }
    }

    private Object[] findSource(String name) throws CoreException {
        Object[] res;
        File file = new File(applyPathMap(name));
        if (file.isAbsolute() && file.exists() && file.isFile()) {
            res = new Object[]{ new LocalFileStorage(file) };
        }
        else {
            res = super.findSourceElements(name);
        }
        ArrayList<Object> list = new ArrayList<Object>();
        for (Object o : res) {
            if (o instanceof IStorage && !(o instanceof IFile)) {
                IPath path = ((IStorage)o).getFullPath();
                if (path != null) {
                    URI uri = URIUtil.toURI(path);
                    IFile[] arr = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
                    if (arr != null && arr.length > 0) {
                        int cnt = list.size();
                        for (IFile resource : arr) {
                            if (resource.isAccessible()) {
                                list.add(resource);
                            }
                        }
                        if (list.size() > cnt) continue;
                    }
                }
            }
            list.add(o);
        }
        return list.toArray(new Object[list.size()]);
    }

    @Override
    public Object[] findSourceElements(Object object) throws CoreException {
        String name = getSourceName(object);
        if (name == null) return null;
        Object[] res = cache.get(name);
        if (res != null) return res;
        res = findSource(name);
        cache.put(name, res);
        return res;
    }
}
