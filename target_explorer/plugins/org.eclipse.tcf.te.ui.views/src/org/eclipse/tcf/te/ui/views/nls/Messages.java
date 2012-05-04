/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.nls;

import org.eclipse.osgi.util.NLS;

/**
 * Target Explorer UI plugin externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.ui.views.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String View_title_systemManagement;

	public static String NewActionProvider_NewMenu_label;
	public static String NewActionProvider_NewWizardCommandAction_label;
	public static String NewActionProvider_NewWizardCommandAction_tooltip;

	public static String PropertiesCommandHandler_error_initPartFailed;

	public static String AddToCategoryAction_single_text;
	public static String RemoveFromCategoryAction_single_text;

	public static String AbstractCustomFormToolkitEditorPage_HelpAction_label;
	public static String AbstractCustomFormToolkitEditorPage_HelpAction_tooltip;

	public static String CommonFilterDescriptorLabelProvider_ContentExtensionDescription;

	public static String ConfigContentHandler_DialogTitle;
	public static String ConfigContentHandler_InitialFilter;
	public static String ConfigContentHandler_PromptMessage;
	public static String ConfigFiltersHandler_DialogTitle;
	public static String ConfigFiltersHandler_InitialFilter;
	public static String ConfigFiltersHandler_PromptMessage;

	public static String UpdateActiveExtensionsOperation_OperationName;
	public static String UpdateActiveFiltersOperation_OperationName;
}
