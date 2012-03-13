/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.ui.dialogs.FilteredCheckedListDialog;
import org.eclipse.tcf.te.ui.views.nls.Messages;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * The handler to configure content extensions in Target Explorer. 
 */
public class ConfigContentHandler extends AbstractHandler implements IRegistryEventListener{
	// The extension point id to access navigator content extensions.
	private static final String EXTENSION_POINT_ID = "org.eclipse.ui.navigator.navigatorContent"; //$NON-NLS-1$
	// The map to store the visibility data of content extensions.
	private Map<String, Boolean> visibleContentMap;
	// The preference store cached for each plug-ins that declares content extensions.
	private Map<String, IPreferenceStore> prefMap;

	/**
	 * Constructor
	 */
	public ConfigContentHandler() {
		initVisibleMap();
		Platform.getExtensionRegistry().addListener(this, EXTENSION_POINT_ID);
	}

	/**
	 * Initialize the visibility map of content extensions.
	 */
	private void initVisibleMap() {
	    visibleContentMap = Collections.synchronizedMap(new HashMap<String, Boolean>());
	    prefMap = Collections.synchronizedMap(new HashMap<String, IPreferenceStore>());
		IExtensionPoint exPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
		IExtension[] extensions = exPoint.getExtensions();
		if(extensions != null) {
			addExtensions(extensions);
		}
    }

	/**
	 * Add the extensions that define "navigatorContent" elements and retrieve
	 * the visibility data from its contributed plug-in.
	 *  
	 * @param extensions The extensions that define "navigatorContent" elements.
	 */
	private void addExtensions(IExtension[] extensions) {
	    for(IExtension extension : extensions) {
	    	IConfigurationElement[] elements = extension.getConfigurationElements();
	    	for(IConfigurationElement element : elements) {
	    		if("navigatorContent".equals(element.getName())) { //$NON-NLS-1$
	    			String extensionId = element.getAttribute("id"); //$NON-NLS-1$
	    			String pluginId = element.getContributor().getName();
	    			IPreferenceStore prefStore = prefMap.get(pluginId);
	    			if(prefStore == null) {
	    				prefStore = new ScopedPreferenceStore(InstanceScope.INSTANCE,pluginId);
	    				prefMap.put(pluginId, prefStore);
	    			}
	    			String prefHide = extensionId + ".hide"; //$NON-NLS-1$
	    			visibleContentMap.put(extensionId, Boolean.valueOf(prefStore.getBoolean(prefHide)));
	    		}
	    	}
	    }
    }
	
	/**
	 * Remove the visibility data when the extensions that define "navigatorContent" elements 
	 * are removed from the extension registry.
	 *  
	 * @param extensions The extensions that define "navigatorContent" elements.
	 */
	private void removeExtensions(IExtension[] extensions) {
	    for(IExtension extension : extensions) {
	    	IConfigurationElement[] elements = extension.getConfigurationElements();
	    	for(IConfigurationElement element : elements) {
	    		if("navigatorContent".equals(element.getName())) { //$NON-NLS-1$
	    			String extensionId = element.getAttribute("id"); //$NON-NLS-1$
	    			visibleContentMap.remove(extensionId);
	    		}
	    	}
	    }
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IRegistryEventListener#added(org.eclipse.core.runtime.IExtension[])
	 */
	@Override
    public void added(IExtension[] extensions) {
		addExtensions(extensions);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IRegistryEventListener#removed(org.eclipse.core.runtime.IExtension[])
	 */
	@Override
    public void removed(IExtension[] extensions) {
		removeExtensions(extensions);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IRegistryEventListener#added(org.eclipse.core.runtime.IExtensionPoint[])
	 */
	@Override
    public void added(IExtensionPoint[] extensionPoints) {
		// Ignore on purpose
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IRegistryEventListener#removed(org.eclipse.core.runtime.IExtensionPoint[])
	 */
	@Override
    public void removed(IExtensionPoint[] extensionPoints) {
		// Ignore on purpose
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShellChecked(event);
		CommonNavigator navigator = (CommonNavigator) HandlerUtil.getActivePartChecked(event);
		FilteredCheckedListDialog contentDialog = new FilteredCheckedListDialog(shell);
		contentDialog.setTitle(Messages.ConfigContentHandler_DialogTitle);
		contentDialog.setFilterText(Messages.ConfigContentHandler_InitialFilter);
		contentDialog.setMessage(Messages.ConfigContentHandler_PromptMessage);
		contentDialog.setStatusLineAboveButtons(true);
		contentDialog.setLabelProvider(ContentDescriptorLabelProvider.instance);
		INavigatorContentService contentService = navigator.getNavigatorContentService();
		if (contentService != null) {
			INavigatorContentDescriptor[] visibleExtensions = getExtensionsVisibleInUI(contentService);
			if (visibleExtensions != null && visibleExtensions.length > 0) {
				contentDialog.setElements(visibleExtensions);
				List<INavigatorContentDescriptor> activeExtensions = new ArrayList<INavigatorContentDescriptor>();
				for (INavigatorContentDescriptor extension : visibleExtensions) {
					if (contentService.isActive(extension.getId())) {
						activeExtensions.add(extension);
					}
				}
				contentDialog.setInitialElementSelections(activeExtensions);
				if (contentDialog.open() == Window.OK) {
					Object[] result = contentDialog.getResult();
					if (result != null) {
						INavigatorContentDescriptor[] hiddenActives = getHiddenActiveExtensions(contentService);
						INavigatorContentDescriptor[] total = new INavigatorContentDescriptor[result.length + hiddenActives.length];
						System.arraycopy(result, 0, total, 0, result.length);
						System.arraycopy(hiddenActives, 0, total, result.length, hiddenActives.length);
						activateExtensions(navigator.getCommonViewer(), total);
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Get hidden extensions that are active from the content service.
	 * 
	 * @param contentService The content service.
	 * @return The hidden yet active content descriptors.
	 */
	private INavigatorContentDescriptor[] getHiddenActiveExtensions(INavigatorContentService contentService) {
		INavigatorContentDescriptor[] visibleExtensions = contentService.getVisibleExtensions();
		List<INavigatorContentDescriptor> hiddenActives = new ArrayList<INavigatorContentDescriptor>();
		if(visibleExtensions != null) {
			for(INavigatorContentDescriptor visibleExtension : visibleExtensions) {
				String extensionId = visibleExtension.getId();
				Boolean value = visibleContentMap.get(extensionId);
				boolean hide = value != null && value.booleanValue();
				if (hide && contentService.isActive(extensionId)) hiddenActives.add(visibleExtension);
			}
		}
		return hiddenActives.toArray(new INavigatorContentDescriptor[hiddenActives.size()]);
    }

	/**
	 * Get the content extensions that are configured to be visible in the "Available Extensions" dialog.
	 * The visibility data are retrieved from the preference store of the plug-ins that contribute content
	 * extensions.
	 * 
	 * @param contentService The content service.
	 * @return The content extensions visible in the configuration UI.
	 */
	private INavigatorContentDescriptor[] getExtensionsVisibleInUI(INavigatorContentService contentService) {
		INavigatorContentDescriptor[] visibleExtensions = contentService.getVisibleExtensions();
		List<INavigatorContentDescriptor> visibleInUIs = new ArrayList<INavigatorContentDescriptor>();
		if(visibleExtensions != null) {
			for(INavigatorContentDescriptor visibleExtension : visibleExtensions) {
				String extensionId = visibleExtension.getId();
				Boolean value = visibleContentMap.get(extensionId);
				boolean hide = value != null && value.booleanValue();
				if (!hide) visibleInUIs.add(visibleExtension);
			}
		}
		return visibleInUIs.toArray(new INavigatorContentDescriptor[visibleInUIs.size()]);
	}	
	
	/**
	 * Activate the specified navigator content descriptor.
	 * 
	 * @param commonViewer The common viewer of the navigator.
	 * @param contentDescriptors The content descriptor to be activated.
	 */
	private void activateExtensions(CommonViewer commonViewer, INavigatorContentDescriptor[] contentDescriptors) {
		Set<String> toActivate = new HashSet<String>();
		for (int i=0;i<contentDescriptors.length;i++) {
			INavigatorContentDescriptor contentDescriptor = contentDescriptors[i];
			toActivate.add(contentDescriptor.getId());
		}
		UpdateActiveExtensionsOperation  updateExtensions = new UpdateActiveExtensionsOperation (commonViewer, toActivate.toArray(new String[toActivate.size()]));
		updateExtensions.execute(null, null);
    }
}
