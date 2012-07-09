/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.utils;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tcf.te.runtime.persistence.GsonMapPersistenceDelegate;

/**
 * The persistence delegate to persist or restore a map whose keys are URIs.
 */
public class PersistenceManagerDelegate extends GsonMapPersistenceDelegate {

    private static final String MAP_KEY_MTIME = "mtime"; //$NON-NLS-1$
    private static final String MAP_KEY_TARGET = "target"; //$NON-NLS-1$
    private static final String MAP_KEY_CACHE = "cache"; //$NON-NLS-1$
    private static final String MAP_KEY_BASE = "base"; //$NON-NLS-1$
    private static final String MAP_KEY_UNRESOLVED = "unresolved"; //$NON-NLS-1$
    private static final String MAP_KEY_RESOLVED = "resolved"; //$NON-NLS-1$
    private static final String MAP_KEY_PROPERTIES = "properties"; //$NON-NLS-1$
    private static final String MAP_KEY_DIGESTS = "digests"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#getPersistedClass(
     * java.lang.Object)
     */
    @Override
    public Class<?> getPersistedClass(Object context) {
        return PersistenceManager.class;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.tcf.te.runtime.persistence.AbstractGsonMapPersistenceDelegate#toMap(java.lang
     * .Object)
     */
    @Override
    protected Map<String, Object> toMap(Object context) throws IOException {
        PersistenceManager pMgr = (PersistenceManager) context;
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(MAP_KEY_DIGESTS, persistDigests(pMgr.digests));
        result.put(MAP_KEY_PROPERTIES, persistProperties(pMgr.properties));
        result.put(MAP_KEY_RESOLVED, persistResolved(pMgr.resolved));
        result.put(MAP_KEY_UNRESOLVED, persistUnresolved(pMgr.unresolved));
        return result;
    }

    private Object persistProperties(Map<URI, Map<QualifiedName, String>> properties) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (properties != null) {
            for (Entry<URI, Map<QualifiedName, String>> entry : properties.entrySet()) {
                Map<QualifiedName, String> map = entry.getValue();
                Map<String, Object> valueMap = qNames2Map(map);
                result.put(entry.getKey().toString(), valueMap);
            }
        }
        return result;
    }

    private Object persistResolved(Map<URI, IContentType> resolved) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (resolved != null) {
            for (Entry<URI, IContentType> entry : resolved.entrySet()) {
                IContentType object = entry.getValue();
                String value = object.getId();
                result.put(entry.getKey().toString(), value);
            }
        }
        return result;
    }

    private Object persistUnresolved(Map<URI, URI> unresolved) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (unresolved != null) {
            for (Entry<URI, URI> entry : unresolved.entrySet()) {
                URI uri = entry.getValue();
                String value = uri.toString();
                result.put(entry.getKey().toString(), value);
            }
        }
        return result;
    }

    private Object persistDigests(Map<URI, FileState> states) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (states != null) {
            for (Entry<URI, FileState> entry : states.entrySet()) {
                FileState fileState = entry.getValue();
                Map<String, Object> value = digest2map(fileState);
                result.put(entry.getKey().toString(), value);
            }
        }
        return result;
    }

    /**
     * Translate the specified map whose keys are QualifiedNames to a map whose keys are strings.
     * 
     * @param map The map to be translated.
     * @return a map with string keys.
     */
    private Map<String, Object> qNames2Map(Map<QualifiedName, String> map) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (Entry<QualifiedName, String> entry : map.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue());
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.tcf.te.runtime.persistence.AbstractGsonMapPersistenceDelegate#fromMap(java.util
     * .Map, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Object fromMap(Map<String, Object> map, Object context) throws IOException {
        PersistenceManager result = (PersistenceManager) context;
        Map<String, Map<String, Object>>  digests = (Map<String, Map<String, Object>>) map.get(MAP_KEY_DIGESTS);
        Map<String, Map<String, String>>  properties = (Map<String, Map<String, String>>) map.get(MAP_KEY_PROPERTIES);
        Map<String, String>  resolved = (Map<String, String>) map.get(MAP_KEY_RESOLVED);
        Map<String, String>  unresolved = (Map<String, String>) map.get(MAP_KEY_UNRESOLVED);
        restoreDigests(digests, result.digests);
        restoreProperites(properties, result.properties);
        restoreResolved(resolved, result.resolved);
        restoreUnresolved(unresolved, result.unresolved);
        return result;
    }

    private void restoreUnresolved(Map<String, String> map, Map<URI, URI> unresolved) {
        for (Entry<String, String> entry : map.entrySet()) {
            String value = entry.getValue();
            URI uri = toURI(entry.getKey());
            Assert.isNotNull(uri);
            uri = toURI(value);
            Assert.isNotNull(uri);
            unresolved.put(uri, uri);
        }
    }

    private void restoreResolved(Map<String, String> map, Map<URI, IContentType> contentTypes) {
        for (Entry<String, String> entry : map.entrySet()) {
            String value = entry.getValue();
            URI uri = toURI(entry.getKey());
            Assert.isNotNull(uri);
            IContentType contentType = Platform.getContentTypeManager().getContentType(value);
            contentTypes.put(uri, contentType);
        }
    }

    private void restoreDigests(Map<String, Map<String, Object>> map, Map<URI, FileState> states) {
        for (Entry<String, Map<String, Object>> entry : map.entrySet()) {
            Map<String, Object> value = entry.getValue();
            URI uri = toURI(entry.getKey());
            Assert.isNotNull(uri);
            FileState digest = map2digest(value);
            states.put(uri, digest);
        }
    }

    private void restoreProperites(Map<String, Map<String, String>> map, Map<URI, Map<QualifiedName, String>> properties) {
        for (Entry<String, Map<String, String>> entry : map.entrySet()) {
            Map<String, String> value = entry.getValue();
            URI uri = toURI(entry.getKey());
            Assert.isNotNull(uri);
            Map<QualifiedName, String> valueMap = toQNameMap(value);
            properties.put(uri, valueMap);
        }
    }

    private FileState map2digest(Map<String, Object> value) {
        byte[] base_digest = string2digest((String) value.get(MAP_KEY_BASE));
        byte[] cache_digest = string2digest((String) value.get(MAP_KEY_CACHE));
        byte[] target_digest = string2digest((String) value.get(MAP_KEY_TARGET));
        Number number = (Number) value.get(MAP_KEY_MTIME);
        long mtime = number.longValue();
        return new FileState(mtime, cache_digest, target_digest, base_digest);
    }

    private Map<String, Object> digest2map(FileState digest) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(MAP_KEY_BASE, digest2string(digest.getBaseDigest()));
        map.put(MAP_KEY_CACHE, digest2string(digest.getCacheDigest()));
        map.put(MAP_KEY_TARGET, digest2string(digest.getTargetDigest()));
        map.put(MAP_KEY_MTIME, Long.valueOf(digest.getCacheMTime()));
        return map;
    }

    private String digest2string(byte[] digest) {
        if (digest != null && digest.length > 0) {
            StringBuilder buffer = new StringBuilder();
            for (byte element : digest) {
                int d = element & 0xff;
                String sByte = Integer.toHexString(d);
                while (sByte.length() < 2)
                {
                    sByte = "0" + sByte; //$NON-NLS-1$
                }
                buffer.append(sByte.toLowerCase());
            }
            return buffer.toString();
        }
        return ""; //$NON-NLS-1$
    }

    private byte[] string2digest(String string) {
        if (string != null && string.length() > 0) {
            int count = string.length() / 2;
            byte[] digest = new byte[count];
            for (int i = 0; i < count; i++) {
                try {
                    String seg = string.substring(2 * i, 2 * (i + 1));
                    int d = Integer.parseInt(seg, 16);
                    digest[i] = (byte) d;
                }
                catch (Exception e) {
                }
            }
            return digest;
        }
        return new byte[0];
    }

    /**
     * Translate the specified map with string keys to a map whose keys are qualified names.
     * 
     * @param strMap The map with string keys.
     * @return A map with qualified names as keys.
     */
    private Map<QualifiedName, String> toQNameMap(Map<String, String> strMap) {
        Map<QualifiedName, String> result = new HashMap<QualifiedName, String>();
        for (Entry<String, String> entry : strMap.entrySet()) {
            int dot = entry.getKey().lastIndexOf(":"); //$NON-NLS-1$
            String qualifier = null;
            String local = entry.getKey();
            if (dot != -1) {
                qualifier = entry.getKey().substring(0, dot);
                local = entry.getKey().substring(dot + 1);
            }
            QualifiedName name = new QualifiedName(qualifier, local);
            result.put(name, strMap.get(entry.getKey()));
        }
        return result;
    }

    /**
     * Convert the string to a URI.
     * 
     * @param string The string to be converted.
     * @return the URI or null if there're issues when parsing.
     */
    private URI toURI(final String string) {
        final AtomicReference<URI> ref = new AtomicReference<URI>();
        SafeRunner.run(new ISafeRunnable() {
            @Override
            public void handleException(Throwable exception) {
                // Ignore on purpose.
            }

            @Override
            public void run() throws Exception {
                ref.set(new URI(string));
            }
        });
        return ref.get();
    }
}
