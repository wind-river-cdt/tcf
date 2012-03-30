/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.tcf.launch.ui.remote.app.projects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tcf.te.launch.core.interfaces.IReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.persistence.projects.ReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.persistence.projects.ReferencedProjectItemValidator;
import org.eclipse.tcf.te.launch.core.persistence.projects.ReferencedProjectsPersistenceDelegate;
import org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart;
import org.eclipse.tcf.te.tcf.launch.ui.nls.Messages;
import org.eclipse.tcf.te.ui.forms.parts.AbstractTableSection;
import org.eclipse.tcf.te.ui.swt.listener.AbstractDecorationCellPaintListener;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * ReferencedProjectsSection
 * @author tobias.schwarz@windriver.com
 */
public class ReferencedProjectsSection extends AbstractTableSection implements ILaunchConfigurationTabFormPart {

	private ControlDecoration controlDecoration;

	protected static final String PROPERTY_VALIDATION_RESULT = "validation_result.transient"; //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param form
	 * @param parent
	 */
	public ReferencedProjectsSection(IManagedForm form, Composite parent) {
		super(form, parent, SWT.NONE, new String[]{
						Messages.ReferencedProjectsSection_add_button,
						Messages.ReferencedProjectsSection_delete_button,
						null,
						Messages.ReferencedProjectsSection_up_button,
						Messages.ReferencedProjectsSection_down_button});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		IReferencedProjectItem[] items = ReferencedProjectsPersistenceDelegate.getReferencedProjects(configuration);
		getTablePart().getViewer().setInput(items);
		if (items != null && items.length > 0) {
			((TableViewer)getTablePart().getViewer()).setSelection(new StructuredSelection(items[0]), true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy wc) {
		ReferencedProjectsPersistenceDelegate.setReferencedProjects(wc, (IReferencedProjectItem[])getTablePart().getViewer().getInput());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration configuration) {
		return validateInputList();
	}

	private boolean validateInputList() {
		List<IReferencedProjectItem> list = getInputList();
		boolean valid = true;
		for (IReferencedProjectItem item : list) {
			Map<String,String> invalid = item.getBooleanProperty(IReferencedProjectItem.PROPERTY_ENABLED) ? ReferencedProjectItemValidator.validate(item) : null;
			item.setProperty(PROPERTY_VALIDATION_RESULT, invalid);
			if (valid && invalid != null) {
				valid = false;
				setMessage(invalid.get(invalid.keySet().toArray()[0]), IMessageProvider.ERROR);
			}
		}
		return valid;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected void createClient(Section section, FormToolkit toolkit) {
		Assert.isNotNull(section);
		Assert.isNotNull(toolkit);

		section.setText(Messages.ReferencedProjectsSection_title);
		section.setDescription(Messages.ReferencedProjectsSection_description);

		section.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL, SWT.CENTER, true, true));

		Composite client = createClientContainer(section, 2, toolkit);
		client.setBackground(section.getBackground());

		section.setClient(client);

		createPartControl((Composite)section.getClient(), SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION, 2, toolkit);
	}

	@Override
	protected TableViewer createTableViewer(Composite parent, int style) {
		return new CheckboxTableViewer(new Table(parent, style | SWT.CHECK));
	}

	@Override
	protected void configureTableViewer(TableViewer viewer) {
		super.configureTableViewer(viewer);

		ColumnViewerToolTipSupport.enableFor(viewer);

		final Table table = viewer.getTable();

		((CheckboxTableViewer)viewer).setCheckStateProvider(new ICheckStateProvider() {
			@Override
			public boolean isGrayed(Object element) {
				return false;
			}
			@Override
			public boolean isChecked(Object element) {
				if (element instanceof IReferencedProjectItem) {
					IReferencedProjectItem item = (IReferencedProjectItem)element;
					return item.getBooleanProperty(IReferencedProjectItem.PROPERTY_ENABLED);
				}
				return false;
			}
		});
		((CheckboxTableViewer)viewer).addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getElement() instanceof IReferencedProjectItem) {
					IReferencedProjectItem item = (IReferencedProjectItem)event.getElement();
					item.setProperty(IReferencedProjectItem.PROPERTY_ENABLED, event.getChecked());
					getManagedForm().dirtyStateChanged();
				}
			}
		});

		TableViewerColumn tvEnableCol = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn colEnable = tvEnableCol.getColumn();
		colEnable.setResizable(false);
		tvEnableCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return null;
			}
		});

		TableViewerColumn tvProjectCol = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn colProject = tvProjectCol.getColumn();
		colProject.setText(Messages.ReferencedProjectsSection_project_column);
		colProject.setResizable(true);
		tvProjectCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IReferencedProjectItem) {
					IReferencedProjectItem item = (IReferencedProjectItem)element;
					Map<String,String> invalid = (Map<String,String>)item.getProperty(PROPERTY_VALIDATION_RESULT);
					if (invalid != null && invalid.containsKey(IReferencedProjectItem.PROPERTY_PROJECT_NAME)) {
						return invalid.get(IReferencedProjectItem.PROPERTY_PROJECT_NAME);
					}
					return item.getStringProperty(IReferencedProjectItem.PROPERTY_PROJECT_NAME);
				}
				return super.getText(element);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof IReferencedProjectItem) {
					IReferencedProjectItem item = (IReferencedProjectItem)element;
					return item.getStringProperty(IReferencedProjectItem.PROPERTY_PROJECT_NAME);
				}
				return super.getText(element);
			}
		});

		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnPixelData(30, true));
		tableLayout.addColumnData(new ColumnPixelData(200, true));
		table.setLayout(tableLayout);
		GridData data = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING);
		table.setLayoutData(data);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		table.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int width = table.getSize().x - 4 - colEnable.getWidth();
				colProject.setWidth(Math.max(width, 100));
			}
		});

		@SuppressWarnings("unused")
		AbstractDecorationCellPaintListener cpl = new AbstractDecorationCellPaintListener(table, 1) {
			@Override
			protected int getDecorationState(Object data, int columnIndex) {
				if (data instanceof IReferencedProjectItem) {
					IReferencedProjectItem item = (IReferencedProjectItem)data;
					if (item.getBooleanProperty(IReferencedProjectItem.PROPERTY_ENABLED)) {
						Map<String,String> invalid = (Map<String,String>)item.getProperty(PROPERTY_VALIDATION_RESULT);
						if (invalid != null) {
							switch (columnIndex) {
							case 1:
								if (invalid.containsKey(IReferencedProjectItem.PROPERTY_PROJECT_NAME)) {
									return STATE_ERROR;
								}
								break;
							}
						}
					}
				}
				return STATE_NONE;
			}
		};

		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				viewer.refresh();
			}
			@Override
			public Object[] getElements(Object inputElement) {
				return inputElement instanceof Object[] ? (Object[])inputElement : new Object[0];
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				initializeButtonsEnablement();
			}
		});

		doCreateControlDecoration(table);
		configureControlDecoration(getControlDecoration());
	}

	/**
	 * Creates a new instance of a {@link ControlDecoration} object associated with
	 * the given control. The method is called after the control has been created.
	 *
	 * @param control The control. Must not be <code>null</code>.
	 * @return The control decoration object instance.
	 */
	public ControlDecoration doCreateControlDecoration(Control control) {
		Assert.isNotNull(control);
		controlDecoration = new ControlDecoration(control, SWT.TOP | SWT.LEFT);
		return controlDecoration;
	}

	/**
	 * Returns the control decoration.
	 *
	 * @return The control decoration instance or <code>null</code> if not yet created.
	 */
	public final ControlDecoration getControlDecoration() {
		return controlDecoration;
	}

	/**
	 * Configure the given control decoration.
	 *
	 * @param decoration The control decoration. Must not be <code>null</code>.
	 */
	protected void configureControlDecoration(ControlDecoration decoration) {
		Assert.isNotNull(decoration);
		decoration.setShowOnlyOnFocus(false);
	}

	/**
	 * Updates the control decoration to represent the given message and message type.
	 * If the message is <code>null</code> or the message type is IMessageProvider.NONE,
	 * no decoration will be shown.
	 *
	 * @param message The message.
	 * @param messageType The message type.
	 */
	public void updateControlDecoration(String message, int messageType) {
		if (getControlDecoration() != null) {
			// The description is the same as the message
			getControlDecoration().setDescriptionText(message);

			// The icon depends on the message type
			FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();

			// Determine the id of the decoration to show
			String decorationId = FieldDecorationRegistry.DEC_INFORMATION;
			if (messageType == IMessageProvider.ERROR) {
				decorationId = FieldDecorationRegistry.DEC_ERROR;
			} else if (messageType == IMessageProvider.WARNING) {
				decorationId = FieldDecorationRegistry.DEC_WARNING;
			}

			// Get the field decoration
			FieldDecoration fieldDeco = registry.getFieldDecoration(decorationId);
			if (fieldDeco != null) {
				getControlDecoration().setImage(fieldDeco.getImage());
			}

			if (message == null || messageType == IMessageProvider.NONE) {
				getControlDecoration().hide();
			}
			else {
				getControlDecoration().show();
			}
		}
	}

	private List<IReferencedProjectItem> getInputList() {
		return new ArrayList<IReferencedProjectItem>(Arrays.asList((IReferencedProjectItem[])getTablePart().getViewer().getInput()));
	}

	private void setInputList(List<IReferencedProjectItem> list) {
		getTablePart().getViewer().setInput(list.toArray(new IReferencedProjectItem[list.size()]));
	}

	@Override
	protected void onButtonSelected(Button button) {
		int selIndex = ((TableViewer)getTablePart().getViewer()).getTable().getSelectionIndex();
		List<IReferencedProjectItem> list = getInputList();

		switch (((Integer)button.getData()).intValue()) {
		case 0: // Add
			onButtonAddClick();
			break;
		case 1: // Edit
			list.remove(selIndex);
			setInputList(list);
			if (!list.isEmpty()) {
				((TableViewer)getTablePart().getViewer()).setSelection(
								new StructuredSelection(list.get(selIndex < list.size() ? selIndex : list.size()-1)), true);
			}
			break;
		case 3: // Up
			list.add(selIndex-1, list.remove(selIndex));
			setInputList(list);
			((TableViewer)getTablePart().getViewer()).setSelection(new StructuredSelection(list.get(selIndex-1)), true);
			break;
		case 4: // Down
			list.add(selIndex+1, list.remove(selIndex));
			setInputList(list);
			((TableViewer)getTablePart().getViewer()).setSelection(new StructuredSelection(list.get(selIndex+1)), true);
			break;
		}
		validateInputList();
		getManagedForm().dirtyStateChanged();
	}

	private boolean contains(List<IReferencedProjectItem> list, IProject project) {
		for (IReferencedProjectItem item : list) {
			if (item.isProperty(IReferencedProjectItem.PROPERTY_PROJECT_NAME, project.getName())) {
				return false;
			}
		}
		return false;
	}

	private void onButtonAddClick() {
		int selIndex = ((TableViewer)getTablePart().getViewer()).getTable().getSelectionIndex();
		List<IReferencedProjectItem> list = getInputList();

		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<String> unreferencedProjects = new ArrayList<String>();
		for (IProject project : allProjects) {
			if (project.isOpen() && !contains(list, project)) {
				unreferencedProjects.add(project.getName());
			}
		}

		ILabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) {
				return (String)element;
			}
		};

		IStructuredContentProvider contentProvider = new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			@Override
			public void dispose() {
			}
			@Override
			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}
		};

		ListSelectionDialog dialog = new ListSelectionDialog(getSection().getShell(),
						unreferencedProjects.toArray(), contentProvider, labelProvider, Messages.ReferencedProjectsSection_addDialog_message);

		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();

			if (result != null) {
				IReferencedProjectItem first = null;
				for (Object name : result) {
					IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject((String)name);
					if (prj != null && prj.isOpen()) {
						IReferencedProjectItem item = new ReferencedProjectItem();
						item.setProperty(IReferencedProjectItem.PROPERTY_ENABLED, true);
						item.setProperty(IReferencedProjectItem.PROPERTY_PROJECT_NAME, prj.getName());
						list.add(selIndex != -1 ? selIndex++ : 0, item);
						if (first == null) {
							first = item;
						}
					}
				}
				setInputList(list);
				if (first != null) {
					((TableViewer)getTablePart().getViewer()).setSelection(new StructuredSelection(first), true);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractTableSection#initializeButtonsEnablement()
	 */
	@Override
	protected void initializeButtonsEnablement() {
		ISelection selection = ((TableViewer)getTablePart().getViewer()).getSelection();
		boolean singleSelection = selection instanceof IStructuredSelection && ((IStructuredSelection)selection).size() == 1;
		int selIndex = ((TableViewer)getTablePart().getViewer()).getTable().getSelectionIndex();
		int count = ((TableViewer)getTablePart().getViewer()).getTable().getItemCount();

		getTablePart().getButton(0).setEnabled(getTablePart().isEnabled());
		getTablePart().getButton(1).setEnabled(getTablePart().isEnabled() && singleSelection);
		getTablePart().getButton(3).setEnabled(getTablePart().isEnabled() && singleSelection && selIndex > 0);
		getTablePart().getButton(4).setEnabled(getTablePart().isEnabled() && singleSelection && selIndex < count-1);
	}
}
