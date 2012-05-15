/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.url;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;
import org.osgi.service.url.AbstractURLStreamHandlerService;

/**
 * The stream handler service used to parse tcf stream protocol. 
 */
public class TcfURLStreamHandlerService extends AbstractURLStreamHandlerService {
	// The pattern of a windows path.
	private static final String WINPATH_PATTERN = "[A-Za-z]:.*"; //$NON-NLS-1$
	private static final char[] WINPATH_FORBIDDEN_CHARS = {':', '*', '?', '"', '<', '>', '|' };
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.service.url.AbstractURLStreamHandlerService#openConnection(java.net.URL)
	 */
	@Override
	public URLConnection openConnection(URL u) throws IOException {
		return new TcfURLConnection(u);
	}

	/**
	 * Parse the given spec to the specified URL object. The expected format is:
	 * <p>
	 * 
	 * <pre>
	 * TCF_URL = tcf:/<strong>PEER_ID</strong>/(<strong>URL_PATH</strong>)? 
	 * PEER_ID = (.^/)+ 
	 * URL_PATH = <strong>WIN_PATH</strong> | <strong>RELATIVE_PATH</strong>
	 * WIN_PATH = <strong>DISK_SEG</strong> / (<strong>RELATIVE_PATH</strong>)? 
	 * DISK_SEG = [a-zA-Z]: 
	 * RELATIVE_PATH = <strong>PATH_SEG</strong> | <strong>PATH_SEG</strong>/<strong>RELATIVE_PATH</strong> 
	 * Unix/Linux PATH_SEG = (.^[/])+
	 * Windows PATH_SEG = (.^[\/:*?"<>|])+
	 * </pre>
	 */
	@Override
	protected void parseURL(URL u, String spec, int start, int limit) {
		if (u.getPath() != null) {
			String path = u.getPath();
			if (!path.endsWith("/")) { //$NON-NLS-1$
				path += "/"; //$NON-NLS-1$
			}
			path += spec;
			setURL(u, u.getProtocol(), u.getHost(), u.getPort(), u.getAuthority(), u.getUserInfo(), path, u.getQuery(), u.getRef());
		}
		else {
			IllegalArgumentException errorFormat = new IllegalArgumentException(Messages.TcfURLStreamHandlerService_ErrorURLFormat);
			int end = spec.indexOf("/", start); //$NON-NLS-1$
			if (end == -1) throw errorFormat;
			start = end + 1;
			end = spec.indexOf("/", start); //$NON-NLS-1$
			if (end == -1) throw errorFormat;
			String peerId = spec.substring(start, end);
			if (peerId.trim().length() == 0) throw errorFormat;
			start = end + 1;
			String path = spec.substring(start);
			if (path.length() > 0) {
				if (path.matches(WINPATH_PATTERN)) {
					String pathext = path.substring(2); // Cut the path after ':'.
					if (pathext.length() == 0) throw new IllegalArgumentException(Messages.TcfURLStreamHandlerService_OnlyDiskPartError);
					pathext = pathext.substring(1); // Cut the path after the disk part.
					checkWinPath(pathext);
				}
				else {
					path = "/" + path; //$NON-NLS-1$
				}
			}
			else {
				path = "/"; //$NON-NLS-1$
			}
			final String path2decode = path;
			final AtomicReference<String> pathRef = new AtomicReference<String>();
			SafeRunner.run(new ISafeRunnable(){
				@Override
	            public void handleException(Throwable exception) {
					// Ignore on purpose
	            }
				@Override
	            public void run() throws Exception {
					pathRef.set(decodeURLPath(path2decode));
	            }});
			path = pathRef.get();
			setURL(u, TcfURLConnection.PROTOCOL_SCHEMA, peerId, -1, null, null, path, null, null);
		}
	}
	
	/**
	 * Decode the path from URI compatible path to a 
	 * file system path.
	 * 
	 * @see FSTreeNode#getURLEncodedPath
	 * @param path The URL whose path is to be decoded.
	 * @return The file system path.
	 * @throws UnsupportedEncodingException
	 */
	String decodeURLPath(String path) throws UnsupportedEncodingException {
		StringTokenizer st = new StringTokenizer(path, "/"); //$NON-NLS-1$
		StringBuilder builder = new StringBuilder();
		while(st.hasMoreTokens()) {
			if(builder.length() > 0) {
				builder.append("/"); //$NON-NLS-1$
			}
			String segment = st.nextToken();
			builder.append(URLDecoder.decode(segment, "UTF-8")); //$NON-NLS-1$
		}
		String relative = builder.toString();
		return path.startsWith("/") ? "/" + relative : relative;  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	/**
	 * Check the format of the specified windows path.
	 * 
	 * @param path The relative path to a disk part.
	 */
	private void checkWinPath(String path) {
	    for (int i = 0; i < path.length(); i++) {
	    	char c = path.charAt(i);
	    	for(int j=0;j<WINPATH_FORBIDDEN_CHARS.length;j++) {
	    		if(c==WINPATH_FORBIDDEN_CHARS[j]) {
	    			throw new IllegalArgumentException(NLS.bind(Messages.TcfURLStreamHandlerService_IllegalCharacter, "'"+c+"'"));  //$NON-NLS-1$//$NON-NLS-2$
	    		}
	    	}
	    }
    }

	/**
	 * Encode the path from a file system path to 
	 * URI compatible path.
	 * 
	 * @see FSTreeNode#getURLEncodedPath
	 * @param path The URL whose path is to be decoded.
	 * @return The file system path.
	 * @throws UnsupportedEncodingException
	 */
	String encodeURLPath(String path) throws UnsupportedEncodingException {
		StringTokenizer st = new StringTokenizer(path, "/"); //$NON-NLS-1$
		StringBuilder builder = new StringBuilder();
		while(st.hasMoreTokens()) {
			if(builder.length() > 0) {
				builder.append("/"); //$NON-NLS-1$
				String segment = st.nextToken();
				builder.append(URLEncoder.encode(segment, "UTF-8")); //$NON-NLS-1$
			}
			else {
				String segment = st.nextToken();
				if(path.matches(WINPATH_PATTERN)) {
					builder.append(segment);
				}
				else{
					builder.append(URLEncoder.encode(segment, "UTF-8")); //$NON-NLS-1$
				}
			}
		}
		String relative = builder.toString();
		return path.startsWith("/") ? "/" + relative : relative;  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.service.url.AbstractURLStreamHandlerService#toExternalForm(java.net.URL)
	 */
	@Override
    public String toExternalForm(final URL u) {
		String peerId = u.getHost();
		StringBuilder builder = new StringBuilder();
		builder.append(TcfURLConnection.PROTOCOL_SCHEMA);
		builder.append(":/"); //$NON-NLS-1$
		builder.append(peerId);
		final AtomicReference<String> pathRef = new AtomicReference<String>();
		SafeRunner.run(new ISafeRunnable(){
			@Override
            public void handleException(Throwable exception) {
				// Ignore
            }
			@Override
            public void run() throws Exception {
				pathRef.set(encodeURLPath(u.getPath()));
            }});
		String path = pathRef.get();
		if(path == null) {
			builder.append("/"); //$NON-NLS-1$
		} else if(path.length() == 0) {
			builder.append("/"); //$NON-NLS-1$
		} else if(path.matches(WINPATH_PATTERN)) {
			builder.append("/"); //$NON-NLS-1$
			builder.append(path);
		} else {
			builder.append(path);
		}
		return builder.toString();
    }
}
