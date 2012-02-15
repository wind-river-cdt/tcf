package org.eclipse.tcf.te.tcf.filesystem.internal.url;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.osgi.service.url.AbstractURLStreamHandlerService;

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
			    if(pathext.length() == 0)
			    	throw new IllegalArgumentException(Messages.TcfURLStreamHandlerService_OnlyDiskPartError);
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
		setURL(u, TcfURLConnection.PROTOCOL_SCHEMA, peerId, -1, null, null, path, null, null);
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
		} else if(path.matches(WINPATH_PATTERN)) {
			builder.append("/"); //$NON-NLS-1$
			builder.append(path);
		} else {
			builder.append(path);
		}
		return builder.toString();
    }
}
