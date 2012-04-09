package org.eclipse.tcf.te.tcf.filesystem.core.activator;

import java.util.Hashtable;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.url.TcfURLConnection;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.url.TcfURLStreamHandlerService;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.PersistenceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

public class CorePlugin extends Plugin {

	private static BundleContext context;
	// The shared instance of this plug-in.
	private static CorePlugin plugin;
	// The service registration for the "tcf" URL stream handler.
	private ServiceRegistration<?> regURLStreamHandlerService;


	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		CorePlugin.context = bundleContext;
		plugin = this;
		// Register the "tcf" URL stream handler service.
		Hashtable<String, String[]> properties = new Hashtable<String, String[]>();
		properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { TcfURLConnection.PROTOCOL_SCHEMA });
		regURLStreamHandlerService = context.registerService(URLStreamHandlerService.class.getName(), new TcfURLStreamHandlerService(), properties);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (PersistenceManager.needsDisposal()) PersistenceManager.getInstance().dispose();
		if (regURLStreamHandlerService != null) {
			// When URL stream handler service is unregistered, any URL related operation will be invalid.
			regURLStreamHandlerService.unregister();
			regURLStreamHandlerService = null;
		}
		CorePlugin.context = null;
		plugin = null;
		super.stop(bundleContext);
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getContext() != null && getContext().getBundle() != null) {
			return getContext().getBundle().getSymbolicName();
		}
		return null;
	}
}
