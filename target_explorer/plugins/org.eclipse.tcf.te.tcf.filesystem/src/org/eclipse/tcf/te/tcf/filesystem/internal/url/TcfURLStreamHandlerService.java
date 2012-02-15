package org.eclipse.tcf.te.tcf.filesystem.internal.url;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.osgi.service.url.AbstractURLStreamHandlerService;

public class TcfURLStreamHandlerService extends AbstractURLStreamHandlerService {
	// The pattern of a windows path.
	private static final String WIN_PATH_PATTERN = "[A-Za-z]:.*"; //$NON-NLS-1$
	
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
	 * PATH_SEG = (.^[/:])+
	 * </pre>
	 */
	@Override
	protected void parseURL(URL u, String spec, int start, int limit) {
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
			if (!path.matches(WIN_PATH_PATTERN)) {
				path = "/" + path;  //$NON-NLS-1$
			}
		}
		else {
			path = "/"; //$NON-NLS-1$
		}
		setURL(u, TcfURLConnection.PROTOCOL_SCHEMA, peerId, -1, null, null, path, null, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.service.url.AbstractURLStreamHandlerService#toExternalForm(java.net.URL)
	 */
	@Override
    public String toExternalForm(URL u) {
		String peerId = u.getHost();
		StringBuilder builder = new StringBuilder();
		builder.append(TcfURLConnection.PROTOCOL_SCHEMA);
		builder.append(":/"); //$NON-NLS-1$
		builder.append(peerId);
		String path = u.getPath();
		if(path == null) {
			builder.append("/"); //$NON-NLS-1$
		} else if(path.length() == 0) {
			builder.append("/"); //$NON-NLS-1$
		} else if(path.matches(WIN_PATH_PATTERN)) {
			builder.append("/"); //$NON-NLS-1$
			builder.append(path);
		} else {
			builder.append(path);
		}
		return builder.toString();
    }
}
