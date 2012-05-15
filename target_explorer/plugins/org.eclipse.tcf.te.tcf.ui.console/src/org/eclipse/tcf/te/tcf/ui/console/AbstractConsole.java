/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tcf.te.runtime.preferences.ScopedEclipsePreferences;
import org.eclipse.tcf.te.tcf.ui.console.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.console.interfaces.IPreferenceKeys;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Abstract console implementation.
 */
public class AbstractConsole extends MessageConsole implements IConsoleListener, IPreferenceChangeListener, IPropertyChangeListener {

	/**
	 * Immutable buffered console line.
	 */
	protected static final class ConsoleBufferLine {
		public final String message;
		public final char messageType;

		/**
		 * Constructor.
		 *
		 * @param message The message. Must not be <code>null</code>.
		 * @param messageType The message type.
		 */
		public ConsoleBufferLine(String message, char messageType) {
			Assert.isNotNull(message);
			this.message = message;
			this.messageType = messageType;
		}
	}

	/**
	 * AbstractConsole buffer implementation. Buffers the lines to output if the console itself is
	 * invisible. Once the console gets visible, the buffered lines are send to the console itself.
	 */
	protected static class ConsoleBuffer {
		// The buffer limit (in characters) is retrieved from the console
		// preferences. If 0, there is no limit.
		private int bufferLimit = 0;
		// The consumed characters
		private int charactersConsumed = 0;

		// The buffer of console lines
		private final List<ConsoleBufferLine> lines = new ArrayList<ConsoleBufferLine>();

		/**
		 * Constructor.
		 */
		public ConsoleBuffer() {
		}

		/**
		 * Set the buffer limit in characters.
		 *
		 * @param limit The buffer limit or 0 for unlimited.
		 */
		public void setBufferLimit(int limit) {
			this.bufferLimit = limit > 0 ? limit : 0;
		}

		/**
		 * Adds a new line to the buffer.
		 *
		 * @param line The line. Must not be <code>null</code>.
		 */
		public void addLine(ConsoleBufferLine line) {
			Assert.isNotNull(line);

			// If the limit has been reached, we have to remove the oldest buffered line first.
			while (bufferLimit > 0 && charactersConsumed + line.message.length() >= bufferLimit) {
				ConsoleBufferLine droppedLine = lines.remove(0);
				charactersConsumed -= droppedLine.message.length();
				if (charactersConsumed < 0) charactersConsumed = 0;
			}

			// Add the new line at the end of the buffer
			lines.add(line); charactersConsumed += line.message.length();
		}

		/**
		 * Returns the list of currently buffered lines. The list
		 * of buffered lines is cleared.
		 *
		 * @return The currently buffered lines or an empty list.
		 */
		public ConsoleBufferLine[] getLinesAndClear() {
			ConsoleBufferLine[] result = lines.toArray(new ConsoleBufferLine[lines.size()]);
			lines.clear();
			return result;
		}
	}

	// Map to store the created colors per stream type (dispose on shutdown!)
	private final Map<String, Color> streamColors = new HashMap<String, Color>();
	// Map to store the created message streams per stream type
	private final Map<String, MessageConsoleStream> streams = new HashMap<String, MessageConsoleStream>();

	// The console buffer
	private final ConsoleBuffer buffer = new ConsoleBuffer();

	// Flag to show the console on every output
	private boolean showOnOutput;
	// Flag to show the console on first output only
	private boolean showOnFirstOutput;
	// Flag to mark if the console is visible in the console view.
	private boolean visible = false;
	// Flag to mark if the console is fully initialized (means all colors and streams created).
	private boolean initialized = false;

	/**
	 * Constructor.
	 * <p>
	 * Initializes preferences and colors but doesn't create the console page yet.
	 *
	 * @param name The console name.
	 * @param imageDescriptor. The console image descriptor or <code>null</code>.
	 */
	public AbstractConsole(String name, ImageDescriptor imageDescriptor) {
		super(name, imageDescriptor);
		// Initialize the flags from the preferences
		showOnOutput = UIPlugin.getScopedPreferences().getBoolean(IPreferenceKeys.PREF_CONSOLE_SHOW_ON_OUTPUT);
		showOnFirstOutput = !showOnOutput;
		// Listen to preference change events
		UIPlugin.getScopedPreferences().addPreferenceChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.MessageConsole#dispose()
	 */
	@Override
	protected void dispose() {
		// Do not disconnect the partitioner yet. Dispose is called
		// if the console is removed from the console view. This does
		// not mean the user does not want to show the console again later
		// with the full content preserved. The console will be destroyed
		// if shutdown() is called.
		synchronized (buffer) {
			visible = false;
			JFaceResources.getFontRegistry().removeListener(this);
		}
	}

	/**
	 * Shutdown and cleanup the console.
	 */
	public void shutdown() {
		// Detach the preferences listener
		UIPlugin.getScopedPreferences().removePreferenceChangeListener(this);

		// Partitioner should be disconnected before disposing the colors
		super.dispose();

		// Dispose the colors
		for (Color color : streamColors.values()) color.dispose();
		streamColors.clear();

		// Clean the streams
		streams.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.AbstractConsole#init()
	 */
	@Override
	protected void init() {
		// Called when console is added to the console view
		super.init();

		initConsoleOutputLimitSettings();
		initConsoleWidthSetting();

		//	Ensure that initialization occurs in the UI thread
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
            public void run() {
				JFaceResources.getFontRegistry().addListener(AbstractConsole.this);
				initializeStreams();
				flushBuffer();
			}
		});
	}

	/**
	 * Appends the cached lines from the buffer to the console.
	 */
	protected final void flushBuffer() {
		synchronized(buffer) {
			visible = true;
			for (ConsoleBufferLine line : buffer.getLinesAndClear()) {
				appendMessage(line.messageType, line.message);
			}
		}
	}

	/**
	 * Helper method to initialize the console width settings
	 */
	private void initConsoleWidthSetting() {
		// Get the preferences store
		ScopedEclipsePreferences store = UIPlugin.getScopedPreferences();
		// If the console width shall be limited
		if(store.getBoolean(IPreferenceKeys.PREF_CONSOLE_FIXED_WIDTH)) {
			// Set the width limit
			setConsoleWidth(store.getInt(IPreferenceKeys.PREF_CONSOLE_WIDTH));
		} else {
			// Reset to unlimited width
			setConsoleWidth(-1);
		}
	}

	/**
	 * Helper method to initialize the console output limit settings.
	 */
	private void initConsoleOutputLimitSettings() {
		// Get the preferences store
		ScopedEclipsePreferences store = UIPlugin.getScopedPreferences();
		// If the console output limit shall be set
		if(store.getBoolean(IPreferenceKeys.PREF_CONSOLE_LIMIT_OUTPUT)) {
			// Apply the limits
			setWaterMarks(1000, store.getInt(IPreferenceKeys.PREF_CONSOLE_BUFFER_SIZE));
		} else {
			// Reset to no limits
			setWaterMarks(-1, 0);
		}
		// Update the buffer too
		synchronized (buffer) { buffer.setBufferLimit(getHighWaterMark()); }
	}

	// Internal list of available stream types
	private static final String[] STREAM_TYPES = new String[] { IPreferenceKeys.PREF_CONSOLE_COLOR_TEXT,
		IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND, IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND_RESPONSE,
		IPreferenceKeys.PREF_CONSOLE_COLOR_EVENT, IPreferenceKeys.PREF_CONSOLE_COLOR_PROGRESS,
		IPreferenceKeys.PREF_CONSOLE_COLOR_ERROR
	};

	/*
	 * Initialize the streams of the console.
	 * <p>
	 * Must be called from the UI thread.
	 */
	void initializeStreams() {
		// Synchronize on the buffer to avoid something is dumped
		// if the console streams have not been fully initialized
		synchronized (buffer) {
			if (!initialized) {
				// Black is the default color. Get the corresponding RGB from the platform
				RGB defaultColorBlack = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLACK).getRGB();
				// create the message streams and associate stream colors
				for (String streamType : STREAM_TYPES) {
					MessageConsoleStream stream = newMessageStream();
					Color color = new Color(PlatformUI.getWorkbench().getDisplay(),
					                        StringConverter.asRGB(UIPlugin.getScopedPreferences().getString(streamType), defaultColorBlack));
					stream.setColor(color);
					streams.put(streamType, stream);
					streamColors.put(streamType, color);
				}

				// Apply font
				setFont(PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(IPreferenceKeys.PREF_CONSOLE_FONT));

				// The console is now initialized
				initialized = true;
			}
		}
	}

	/**
	 * Returns if or if not this console is visible in the console view.
	 *
	 * @return <code>True</code> if the console is visible, <code>false</code> otherwise.
	 */
	public final boolean isVisible() {
		return visible;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
	 */
	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		Assert.isNotNull(event);

		String property = event.getKey();
		if (property == null) return;

		// Apply the common properties if changed
		if(IPreferenceKeys.PREF_CONSOLE_FIXED_WIDTH.equals(property)) {
			initConsoleWidthSetting();
		} else if(IPreferenceKeys.PREF_CONSOLE_LIMIT_OUTPUT.equals(property)) {
			initConsoleOutputLimitSettings();
		} else if (IPreferenceKeys.PREF_CONSOLE_SHOW_ON_OUTPUT.equals(property)) {
			showOnOutput = UIPlugin.getScopedPreferences().getBoolean(IPreferenceKeys.PREF_CONSOLE_SHOW_ON_OUTPUT);
		}

		// Color changes are applied only if the console is visible
		if (isVisible()) {
			// Black is the default color. Get the corresponding RGB from the platform
			RGB defaultColorBlack = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLACK).getRGB();
			for (String streamType : STREAM_TYPES) {
				if (property.equals(streamType)) {
					// Get the new RGB values from the preferences
					RGB newRGB = StringConverter.asRGB(UIPlugin.getScopedPreferences().getString(streamType), defaultColorBlack);
					// Get the old color
					Color oldColor = streamColors.get(streamType);
					// Update the stream color will work only if the color is really different from the old one
					if ((oldColor == null && newRGB != null) || (oldColor != null && !oldColor.getRGB().equals(newRGB))) {
						// Create the new color object
						Color newColor = new Color(PlatformUI.getWorkbench().getDisplay(), newRGB);
						// Update the stream
						MessageConsoleStream stream = streams.get(streamType);
						stream.setColor(newColor);
						// Dispose the old color
						if (oldColor != null) oldColor.dispose();
						// and update the stream color map
						streamColors.put(streamType, newColor);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleListener#consolesAdded(org.eclipse.ui.console.IConsole[])
	 */
	@Override
    public void consolesAdded(IConsole[] consoles) {
		for (int i = 0; i < consoles.length; i++) {
			IConsole console = consoles[i];
			if (console == AbstractConsole.this) {
				ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(this);
				init();
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleListener#consolesRemoved(org.eclipse.ui.console.IConsole[])
	 */
	@Override
    public void consolesRemoved(IConsole[] consoles) {
		for (int i = 0; i < consoles.length; i++) {
			IConsole console = consoles[i];
			if (console == AbstractConsole.this) {
				ConsolePlugin.getDefault().getConsoleManager().removeConsoleListener(this);
				dispose();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
    public void propertyChange(PropertyChangeEvent event) {
		String property = event != null ? event.getProperty() : null;

		// Font changes are applied only if the console is visible
		if (property != null && isVisible()) {
			if (property.equals(IPreferenceKeys.PREF_CONSOLE_FONT)) {
				setFont(((FontRegistry) event.getSource()).get(IPreferenceKeys.PREF_CONSOLE_FONT));
			}
		}
	}

	/**
	 * Append message to the console.
	 *
	 * @param type The message type.
	 * @param message The message.
	 */
	public void appendMessage(char type, String message) {
		// If no message is passed in, nothing to do
		if (message == null) return;
		// Open the console if necessary
		showConsole(false);

		// Append the message to the console
		synchronized (buffer) {
			if (isVisible()) {
				switch (type) {
					case 'C':
						MessageConsoleStream stream = streams.get(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND);
						if (stream != null) stream.println(message);
						break;
					case 'R':
						stream = streams.get(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND_RESPONSE);
						if (stream != null) stream.println(message);
						break;
					case 'E':
						stream = streams.get(IPreferenceKeys.PREF_CONSOLE_COLOR_EVENT);
						if (stream != null) stream.println(message);
						break;
					case 'P':
						stream = streams.get(IPreferenceKeys.PREF_CONSOLE_COLOR_PROGRESS);
						if (stream != null) stream.println(message);
						break;
					case 'N':
					case 'F':
						stream = streams.get(IPreferenceKeys.PREF_CONSOLE_COLOR_TEXT);
						if (stream != null) stream.println(message);
						break;
					default:
						stream = streams.get(IPreferenceKeys.PREF_CONSOLE_COLOR_ERROR);
						if (stream != null) stream.println(message);
				}
			} else {
				buffer.addLine(new ConsoleBufferLine(message, type));
			}
		}
	}

	/**
	 * Ensure that the console is shown if needed.
	 *
	 * @param showNoMatterWhat If <code>True</code>, the console will be forced to be shown.
	 */
	private void showConsole(boolean showNoMatterWhat) {
		if(showNoMatterWhat || showOnOutput || showOnFirstOutput) {
			showOnFirstOutput = false;
			ConsolePlugin.getDefault().getConsoleManager().showConsoleView(this);
		}
    }
}
