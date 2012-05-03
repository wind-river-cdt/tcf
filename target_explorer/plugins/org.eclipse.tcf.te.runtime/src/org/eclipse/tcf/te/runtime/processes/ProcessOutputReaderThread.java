/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.processes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.tcf.te.runtime.activator.CoreBundleActivator;

/**
 * Monitor a given input streams and reads any incoming text from the streams.
 * If more than one stream is specified, the lines read from all streams are
 * combined within the reader to one single output.
 */
public class ProcessOutputReaderThread extends Thread {
	// Prefix for any output produced
	private String prefix;
	// reader instances to wrap the input streams
	private BufferedReader[] reader;
	// String buffer to collect the read lines
	private StringBuffer lines;
	private String lastLine;

	// finished reading all the output
	private boolean finished;
	private boolean waiting;
	private Object waiterSemaphore;

	/**
	 * Constructor.
	 * <p>
	 * Monitor multiple streams in one.
	 *
	 * @param prefix A <code>String</code> prefixing every line of might be produced output, or <code>null</code>.
	 * @param streams The <code>InputStream</code>'s to monitor. Must not be <code>null</code>!
	 */
	public ProcessOutputReaderThread(String prefix, InputStream[] streams) {
		super("ProcessOutputReader-" + (prefix == null ? "" : prefix)); //$NON-NLS-1$ //$NON-NLS-2$
		assert streams != null;

		lastLine = ""; //$NON-NLS-1$
		finished = false;
		waiting = false;
		waiterSemaphore = new Object();
		if (prefix == null) {
			this.prefix = ""; //$NON-NLS-1$
		} else if (!prefix.trim().endsWith(":")) { //$NON-NLS-1$
			this.prefix = prefix.trim() + ": "; //$NON-NLS-1$
		} else {
			this.prefix = prefix;
		}

		// connect to a stream reader
		reader = new BufferedReader[streams.length];
		for (int i = 0; i < streams.length; i++) {
			reader[i] = new BufferedReader(new InputStreamReader(streams[i]));
		}

		lines = new StringBuffer();
	}

	/**
	 * Returns if or if not the process output reader thread has finished.
	 *
	 * @return <code>true</code> if the thread is finished, <code>false</code> otherwise.
	 */
	public synchronized boolean isFinished() {
		return finished;
	}

	/**
	 * Wait at most timeout milliseconds, or until the process we are reading is finished.
	 *
	 * @param timeout Timeout in milliseconds to wait for (maximum).
	 */
	public void waitForFinish(long timeout) {
		synchronized (waiterSemaphore) {
			if (finished) {
				return;
			}
			waiting = true;
			try {
				waiterSemaphore.wait(timeout);
			} catch (InterruptedException e) {
				// just end the wait
			}
		}
	}

	/**
	 * Wait until the process we are reading is finished.
	 */
	public void waitForFinish() {
		waitForFinish(0);
	}

	/**
	 * Returns the monitored output till the time of the call.
	 *
	 * @return <code>String</code> containing the monitored output.
	 */
	public synchronized String getOutput() {
		return lines.toString();
	}

	/**
	 * Get the last line that was read.
	 *
	 * @return String last line
	 */
	public synchronized String getLastLine() {
		return lastLine;
	}

	/**
	 * Process one line of output. May be overridden by subclasses to extend functionality.
	 * @param line last line that was read
	 */
	protected synchronized void processLine(String line) {
		if (line != null) {
			StringBuffer buffer = new StringBuffer(line.trim());
			while (buffer.length() > 0 && (buffer.charAt(buffer.length() - 1) == '\r' || buffer.charAt(buffer.length() - 1) == '\n')) {
				buffer.deleteCharAt(buffer.length() - 1);
			}
			line = buffer.toString();
			lastLine = line;
			lines.append(line);
			lines.append('\n');
			CoreBundleActivator.getTraceHandler().trace(getPrefix() + " processLine: " + line, 3, this); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the trace line prefix.
	 *
	 * @return The trace line prefix or <code>null</code>.
	 */
	protected String getPrefix() {
		return prefix;
	}

	/**
	 * Called when the process finished and no more input is available. May be overridden by
	 * subclasses to extend functionality.
	 */
	protected synchronized void finish() {
		finished = true;
		if (waiting) {
			waiting = false;
			synchronized (waiterSemaphore) {
				waiterSemaphore.notifyAll();
			}
		}
	}

	/**
	 * Reads the available available input from the given stream.
	 *
	 * @return Total number of bytes read, or -1 if EOF reached.
	 */
	public synchronized int readAvailableInput(BufferedReader reader) {
		if (reader != null) {
			int bytesRead = 0;
			try {
				while (reader.ready()) {
					String line = reader.readLine();
					if (line != null) {
						bytesRead = line.length();
						processLine(line);
					}
				}
			} catch (IOException e) {
				bytesRead = -1;
			}
			return bytesRead;
		}
		return -1;
	}

	/*
	 * Workaround for the old Java I/O system not being interruptible while reading data from a stream
	 * where possibly nothing is sent: We want to be able to interrupt the reader-thread if we think
	 * that we are no more interested in the data... Unfortunately, this implementation does not
	 * detect when the Stream is closed. inputStream.available() doesn't throw an exception in this
	 * case... so either we block indefinitely (having to potentially destroy the thread), or we can't
	 * detect when the stream is closed. alas, java.nio would solve both issues much more elegant, but
	 * what can we do, getting in the crap old InputStream object...
	 *
	 * Note: Do not synchronize this method. It may lead to dead locks because of the sleep call!
	 */
	protected void readInputUntilInterrupted() {
		boolean allStreamsEOF = false;
		while (!allStreamsEOF) {
			allStreamsEOF = true;
			int totalBytesRead = 0;
			for (int i = 0; i < reader.length; i++) {
				// we do mark reader which have reached EOF already with null.
				if (reader[i] == null) {
					continue;
				}
				int bytesRead = readAvailableInput(reader[i]);
				// is EOF for the current stream
				if (bytesRead == -1) {
					try { reader[i].close(); } catch (IOException e) { /* ignored on purpose */ }
					reader[i] = null;
				} else {
					// at least this stream is still not EOF
					allStreamsEOF = false;
					if (bytesRead >= 0) {
						totalBytesRead += bytesRead;
					}
				}
			}

			if (!allStreamsEOF && totalBytesRead == 0) {
				// nothing read till here, sleep a little bit
				try {
					sleep(50);
				} catch (InterruptedException e) {
					CoreBundleActivator.getTraceHandler().trace(getPrefix() + " received interrupt request", 3, this); //$NON-NLS-1$
					// an interrupt to the sleep breaks the loop.
					allStreamsEOF = true;
				}
			}
		}
	}

	/**
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			CoreBundleActivator.getTraceHandler().trace(getPrefix() + " begin waiting for input", 3, this); //$NON-NLS-1$
			readInputUntilInterrupted();
		} finally {
			// close all readers if not done anyway
			for (BufferedReader element : reader) {
				if (element == null) {
					continue;
				}
				// should there be any input left, read it before closing the stream.
				readAvailableInput(element);
				// finally, close the stream now.
				try { element.close(); } catch (IOException e) { /* ignored on purpose */ }
			}

			// release all waiting threads.
			finish();

			CoreBundleActivator.getTraceHandler().trace(getPrefix() + " stop waiting for input", 3, this); //$NON-NLS-1$
		}
	}

}
