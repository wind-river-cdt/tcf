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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tcf.te.runtime.persistence.AbstractGsonMapPersistenceDelegate;

/**
 * The persistence delegate to persist or restore a map whose keys are URIs.
 */
public class URIKeyMapPersistenceDelegate extends AbstractGsonMapPersistenceDelegate {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#getPersistedClass(java.lang.Object)
	 */
	@Override
	public Class<?> getPersistedClass(Object context) {
		return Map.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.AbstractGsonMapPersistenceDelegate#toMap(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Object> toMap(Object context) throws IOException {
		Map<URI, ?> attrs = (Map<URI, ?>) context;
		Map<String, Object> result = new HashMap<String, Object>();
		if (attrs != null) {
			for (URI key : attrs.keySet()) {
				Object object = attrs.get(key);
				if (object instanceof URI) {
					String value = "uri:" + object.toString(); //$NON-NLS-1$
					result.put(key.toString(), value);
				}
				else if (object instanceof IContentType) {
					String value = "contenttype:" + ((IContentType) object).getId(); //$NON-NLS-1$
					result.put(key.toString(), value);
				}
				else if(object instanceof FileState) {
					Map<String, Object> value = digest2map((FileState)object);
					result.put(key.toString(), value);
				}
				else if (object instanceof Map) {
					Map<QualifiedName, String> map = (Map<QualifiedName, String>) object;
					Map<String, Object> valueMap = qNames2Map(map);
					result.put(key.toString(), valueMap);
				}
				else {
					result.put(key.toString(), object);
				}
			}
		}
		return result;
	}

	/**
	 * Translate the specified map whose keys are QualifiedNames to a
	 * map whose keys are strings.
	 * 
	 * @param map The map to be translated.
	 * @return a map with string keys.
	 */
	private Map<String, Object> qNames2Map(Map<QualifiedName, String> map) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("map.type", "QNames");  //$NON-NLS-1$//$NON-NLS-2$
		for (QualifiedName name : map.keySet()) {
			result.put(name.toString(), map.get(name));
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.AbstractGsonMapPersistenceDelegate#fromMap(java.util.Map, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Object fromMap(Map<String, Object> map, Object context) throws IOException {
		if (context == null) {
			context = new HashMap<URI, Object>();
		}
		Map<URI, Object> result = (Map<URI, Object>) context;
		for (String key : map.keySet()) {
			Object value = map.get(key);
			URI uri = toURI(key);
			Assert.isNotNull(uri);
			if (value instanceof String) {
				String string = (String) map.get(key);
				Object object = null;
				if (string.startsWith("uri:")) { //$NON-NLS-1$
					string = string.substring("uri:".length()); //$NON-NLS-1$
					object = toURI(string);
					Assert.isNotNull(object);
					result.put(uri, object);
				}
				else if (string.startsWith("contenttype:")) { //$NON-NLS-1$
					string = string.substring("contenttype:".length()); //$NON-NLS-1$
					object = Platform.getContentTypeManager().getContentType(string);
					result.put(uri, object);
				}				
			}
			else if (value instanceof Map) {
				Map<String, ?> vMap = (Map<String, ?>) value;
				if("QNames".equals(vMap.get("map.type"))){  //$NON-NLS-1$//$NON-NLS-2$
					Map<QualifiedName, String> valueMap = toQNameMap((Map<String, String>) value);
					result.put(uri, valueMap);
				}
				else if("Digest".equals(vMap.get("map.type"))) {  //$NON-NLS-1$//$NON-NLS-2$
					FileState digest = map2digest((Map<String, Object>)value);
					result.put(uri, digest);
				}
			}
			else {
				result.put(uri, value);
			}
		}
		return result;
	}
	
	private FileState map2digest(Map<String, Object> value) {
		byte[] base_digest = string2digest((String) value.get("base")); //$NON-NLS-1$
		byte[] cache_digest = string2digest((String) value.get("cache")); //$NON-NLS-1$
		byte[] target_digest = string2digest((String) value.get("target")); //$NON-NLS-1$
		Number number = (Number) value.get("mtime"); //$NON-NLS-1$
		long mtime = number.longValue();
		return new FileState(mtime, cache_digest, target_digest, base_digest);
    }

	private Map<String, Object> digest2map(FileState digest) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("map.type", "Digest");  //$NON-NLS-1$//$NON-NLS-2$
		map.put("base", digest2string(digest.getBaseDigest())); //$NON-NLS-1$
		map.put("cache", digest2string(digest.getCacheDigest())); //$NON-NLS-1$
		map.put("target", digest2string(digest.getTargetDigest())); //$NON-NLS-1$
		map.put("mtime", Long.valueOf(digest.getCacheMTime())); //$NON-NLS-1$
		return map;
    }
	
	private String digest2string(byte[] digest) {
		if(digest != null && digest.length > 0) {
			StringBuilder buffer = new StringBuilder();
			for (int i = 0; i < digest.length; i++) {
				int d = digest[i] & 0xff;
				String sByte = Integer.toHexString(d);
				while(sByte.length() < 2) sByte = "0"+sByte; //$NON-NLS-1$
				buffer.append(sByte.toLowerCase());
			}
			return buffer.toString();
		}
	    return ""; //$NON-NLS-1$
	}

	private byte[] string2digest(String string) {
		if(string != null && string.length() > 0) {
			int count = string.length() / 2;
			byte[] digest = new byte[count];
			for (int i = 0; i < count; i++) {
				try {
					String seg = string.substring(2*i, 2*(i + 1));
					int d = Integer.parseInt(seg, 16);
					digest[i] = (byte)d;
				}
				catch (Exception e) {
				}
			}
			return digest;
		}
	    return new byte[0];
    }

	/**
	 * Translate the specified map with string keys to a map whose keys are
	 * qualified names.
	 * 
	 * @param strMap The map with string keys.
	 * @return A map with qualified names as keys.
	 */
	private Map<QualifiedName, String> toQNameMap(Map<String, String> strMap) {
		Map<QualifiedName, String> result = new HashMap<QualifiedName, String>();
		for (String key : strMap.keySet()) {
			int dot = key.lastIndexOf(":"); //$NON-NLS-1$
			String qualifier = null;
			String local = key;
			if(dot != -1) {
				qualifier = key.substring(0, dot);
				local = key.substring(dot + 1);
			}
			QualifiedName name = new QualifiedName(qualifier, local);
			result.put(name,  strMap.get(key));
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
		SafeRunner.run(new ISafeRunnable(){
			@Override
            public void handleException(Throwable exception) {
				// Ignore on purpose.
            }
			@Override
            public void run() throws Exception {
				ref.set(new URI(string));
            }});
		return ref.get();
	}
}
