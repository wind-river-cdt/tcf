/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.trees;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.te.ui.interfaces.IViewerCellEditorFactory;

/**
 * <code>TreeViewerEditorActivationStrategy</code> is a subclass of
 * <code>ColumnViewerEditorActivationStrategy</code> that parses the extensions of
 * "org.eclipse.tcf.te.ui.cellEditors" for Target Explorer, creating a map of
 * <code>IViewerCellEditorFactory</code> with the activation expressions. When requested to judge if
 * a <code>ColumnViewerEditorActivationEvent</code> triggers the cell editing in method
 * <code>isEditorActivationEvent</code>, it traverses the map and finds an appropriate cell editor
 * factory by evaluating its activation expression. If such a factory is found, add the cell editing
 * support to Target Explorer viewer with it and return true to activate the cell editing. If no
 * such a factory is found, then return false to deactivate the cell editing.
 */
public class TreeViewerEditorActivationStrategy extends ColumnViewerEditorActivationStrategy {
	// The extension point id.
	private static final String EXTENSION_POINT_ID = "org.eclipse.tcf.te.ui.cellEditors"; //$NON-NLS-1$
	// The common viewer's id.
	String viewerId;
	// The common viewer to add editing support.
	TreeViewer viewer;
	// The registered cell editor factories map.
	Map<Expression, IViewerCellEditorFactory> factories;

	/**
	 * Create an instance with the specified viewer id and the common viewer.
	 * 
	 * @param viewerId
	 * @param viewer
	 */
	public TreeViewerEditorActivationStrategy(String viewerId, TreeViewer viewer) {
		super(viewer);
		Assert.isNotNull(viewerId);
		this.viewerId = viewerId;
		this.viewer = viewer;
		loadFactories();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy#isEditorActivationEvent(org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent)
	 */
	@Override
	protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
		IViewerCellEditorFactory factory = getFactory(event);
		if (factory != null) {
			// If an appropriate factory is found, initialize the cell editors.
			viewer.setColumnProperties(factory.getColumnProperties());
			viewer.setCellEditors(factory.getCellEditors());
			viewer.setCellModifier(factory.getCellModifier());
		}
		return factory != null;
	}

	/**
	 * Get an appropriate cell editor factory based on the event and the current
	 * selection in the viewer.
	 *  
	 * @param event the event triggering the action 
	 * @return The cell editor factory is appropriate.
	 */
	private IViewerCellEditorFactory getFactory(ColumnViewerEditorActivationEvent event) {
		// Prepare the evaluation context.
		ISelection selection = viewer.getSelection();
		final EvaluationContext context = new EvaluationContext(null, selection);
		context.addVariable("selection", selection); //$NON-NLS-1$
		context.addVariable("event", event); //$NON-NLS-1$
		final IViewerCellEditorFactory[] result = new IViewerCellEditorFactory[1];
		for (Expression expression : factories.keySet()) {
			final Expression exp = expression;
			SafeRunner.run(new SafeRunnable() {
				@Override
                public void handleException(Throwable e) {
					// Ignore exception
                }
				@Override
				public void run() throws Exception {
					EvaluationResult evaluate = exp.evaluate(context);
					if (evaluate == EvaluationResult.TRUE) {
						result[0] = factories.get(exp);
					}
				}
			});
			if (result[0] != null) return result[0];
		}
		return null;
	}

	/**
	 * Load the currently registered cell editor factories.
	 */
	private void loadFactories() {
		factories = Collections.synchronizedMap(new HashMap<Expression, IViewerCellEditorFactory>());
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT_ID);
		IConfigurationElement[] configurations = extensionPoint.getConfigurationElements();
		for (IConfigurationElement configuration : configurations) {
			String name = configuration.getName();
			if ("cellEditor".equals(name)) { //$NON-NLS-1$
				if (isApplicable(configuration)) {
					addFactory(configuration);
				}
			}
		}
	}
	
	/**
	 * If the configuration has a contribution viewer that has the same id with the specified viewerId.
	 * 
	 * @param configuration The cellEditor element.
	 * @return true if it has a specified viewer element.
	 */
	private boolean isApplicable(IConfigurationElement configuration) {
		IConfigurationElement[] children = configuration.getChildren("contributeTo"); //$NON-NLS-1$
		for(IConfigurationElement child : children) {
			String viewerId = child.getAttribute("viewerId"); //$NON-NLS-1$
			if(this.viewerId.equals(viewerId))
				return true;
		}
		return false;
	}

	/**
	 * Create and add the cell editor factory that is defined in the configuration element
	 * into the map.
	 * @param configuration The configuration element that defines the cell editor factory.
	 */
	private void addFactory(final IConfigurationElement configuration) {
		IConfigurationElement[] children = configuration.getChildren("activation"); //$NON-NLS-1$
		Assert.isTrue(children != null && children.length == 1);
		children = children[0].getChildren();
		Assert.isTrue(children != null && children.length == 1);
		final IConfigurationElement config = children[0];
		SafeRunner.run(new SafeRunnable() {
			@Override
            public void handleException(Throwable e) {
				// Ignore exception
            }
			@Override
			public void run() throws Exception {
				Expression expression = ExpressionConverter.getDefault().perform(config);
				IViewerCellEditorFactory factory = (IViewerCellEditorFactory) configuration.createExecutableExtension("editorFactory"); //$NON-NLS-1$
				if (expression != null && factory != null) {
					factory.init(viewer);
					factories.put(expression, factory);
				}
			}
		});
	}
}
