/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.launch.ui.tabs.filetransfers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tcf.te.launch.core.persistence.filetransfer.FileTransfersPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
import org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart;
import org.eclipse.tcf.te.launch.ui.nls.Messages;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.services.interfaces.filetransfer.IFileTransferItem;
import org.eclipse.tcf.te.ui.forms.parts.AbstractTableSection;
import org.eclipse.tcf.te.ui.swt.listener.AbstractDecorationCellPaintListener;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * AbstractFileTransferSection
 */
public abstract class AbstractFileTransferSection extends AbstractTableSection implements ILaunchConfigurationTabFormPart {

	protected ControlDecoration controlDecoration;
	protected IModelNode launchContext = null;

	protected static final String PROPERTY_VALIDATION_RESULT = "validation_result.transient"; //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param form
	 * @param parent
	 */
	public AbstractFileTransferSection(IManagedForm form, Composite parent) {
		super(form, parent, SWT.NONE, new String[]{
						Messages.FileTransferSection_add_button,
						Messages.FileTransferSection_edit_button,
						Messages.FileTransferSection_delete_button,
						null,
						Messages.FileTransferSection_up_button,
						Messages.FileTransferSection_down_button});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		IFileTransferItem[] items = FileTransfersPersistenceDelegate.getFileTransfers(configuration);
		getTablePart().getViewer().setInput(items);
		if (items != null && items.length > 0) {
			((TableViewer)getTablePart().getViewer()).setSelection(new StructuredSelection(items[0]), true);
		}
		launchContext = LaunchContextsPersistenceDelegate.getFirstLaunchContext(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy wc) {
		FileTransfersPersistenceDelegate.setFileTransfers(wc, (IFileTransferItem[])getTablePart().getViewer().getInput());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration configuration) {
		return validateInputList();
	}

	protected abstract boolean validateInputList();

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected void createClient(Section section, FormToolkit toolkit) {
		Assert.isNotNull(section);
		Assert.isNotNull(toolkit);

		section.setText(Messages.FileTransferSection_title);
		section.setDescription(Messages.FileTransferSection_description);

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

		((CheckboxTableViewer)viewer).setCheckStateProvider(new FileTransferCheckStateProvider());
		((CheckboxTableViewer)viewer).addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getElement() instanceof IFileTransferItem) {
					IFileTransferItem item = (IFileTransferItem)event.getElement();
					item.setProperty(IFileTransferItem.PROPERTY_ENABLED, event.getChecked());
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

		TableViewerColumn tvHostCol = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn colHost = tvHostCol.getColumn();
		colHost.setText(Messages.FileTransferSection_host_column);
		colHost.setResizable(true);
		tvHostCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IFileTransferItem) {
					IFileTransferItem item = (IFileTransferItem)element;
					Map<String,String> invalid = (Map<String,String>)item.getProperty(PROPERTY_VALIDATION_RESULT);
					if (invalid != null && invalid.containsKey(IFileTransferItem.PROPERTY_HOST)) {
						return invalid.get(IFileTransferItem.PROPERTY_HOST);
					}
					String host = item.getStringProperty(IFileTransferItem.PROPERTY_HOST);
					return host != null ? new Path(host).toOSString() : host;
				}
				return super.getText(element);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof IFileTransferItem) {
					IFileTransferItem item = (IFileTransferItem)element;
					String host = item.getStringProperty(IFileTransferItem.PROPERTY_HOST);
					return host != null ? new Path(host).toOSString() : host;
				}
				return super.getText(element);
			}
		});

		TableViewerColumn tvDirCol = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn colDir = tvDirCol.getColumn();
		colDir.setResizable(false);
		colDir.setAlignment(SWT.CENTER);
		tvDirCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IFileTransferItem) {
					IFileTransferItem item = (IFileTransferItem)element;
					switch (item.getIntProperty(IFileTransferItem.PROPERTY_DIRECTION)) {
					case IFileTransferItem.TARGET_TO_HOST:
						return Messages.FileTransferSection_toHost_tooltip;
					default:
						return Messages.FileTransferSection_toTarget_tooltip;
					}
				}
				return super.getToolTipText(element);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof IFileTransferItem) {
					IFileTransferItem item = (IFileTransferItem)element;
					switch (item.getIntProperty(IFileTransferItem.PROPERTY_DIRECTION)) {
					case IFileTransferItem.TARGET_TO_HOST:
						return Messages.FileTransferSection_toHost_text;
					default:
						return Messages.FileTransferSection_toTarget_text;
					}
				}
				return super.getText(element);
			}
		});
		tvDirCol.setEditingSupport(new EditingSupport(tvDirCol.getViewer()) {
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof IFileTransferItem) {
					IFileTransferItem item = (IFileTransferItem)element;
					item.setProperty(IFileTransferItem.PROPERTY_DIRECTION,
									Boolean.parseBoolean(value.toString()) ? IFileTransferItem.HOST_TO_TARGET : IFileTransferItem.TARGET_TO_HOST);
					getManagedForm().dirtyStateChanged();
					getViewer().refresh();
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof IFileTransferItem) {
					IFileTransferItem item = (IFileTransferItem)element;
					return new Boolean(item.getIntProperty(IFileTransferItem.PROPERTY_DIRECTION) != IFileTransferItem.TARGET_TO_HOST);
				}
				return null;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new CheckboxCellEditor();
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});

		TableViewerColumn tvTargetCol = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn colTarget = tvTargetCol.getColumn();
		colTarget.setText(Messages.FileTransferSection_target_column);
		colTarget.setResizable(true);
		tvTargetCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IFileTransferItem) {
					IFileTransferItem item = (IFileTransferItem)element;
					Map<String,String> invalid = (Map<String,String>)item.getProperty(PROPERTY_VALIDATION_RESULT);
					if (invalid != null && invalid.containsKey(IFileTransferItem.PROPERTY_TARGET)) {
						return invalid.get(IFileTransferItem.PROPERTY_TARGET);
					}
					String target = item.getStringProperty(IFileTransferItem.PROPERTY_TARGET);
					return target != null ? new Path(target).toPortableString() : target;
				}
				return super.getText(element);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof IFileTransferItem) {
					IFileTransferItem item = (IFileTransferItem)element;
					String target = item.getStringProperty(IFileTransferItem.PROPERTY_TARGET);
					return target != null ? new Path(target).toPortableString() : target;
				}
				return super.getText(element);
			}
		});

		TableViewerColumn tvOptionCol = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn colOption = tvOptionCol.getColumn();
		colOption.setText(Messages.FileTransferSection_options_column);
		colOption.setResizable(true);
		tvOptionCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IFileTransferItem) {
					IFileTransferItem item = (IFileTransferItem)element;
					return item.getStringProperty(IFileTransferItem.PROPERTY_OPTIONS);
				}
				return super.getText(element);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof IFileTransferItem) {
					IFileTransferItem item = (IFileTransferItem)element;
					return item.getStringProperty(IFileTransferItem.PROPERTY_OPTIONS);
				}
				return super.getText(element);
			}
		});

		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnPixelData(30, true));
		tableLayout.addColumnData(new ColumnPixelData(200, true));
		tableLayout.addColumnData(new ColumnPixelData(30, true));
		tableLayout.addColumnData(new ColumnPixelData(200, true));
		tableLayout.addColumnData(new ColumnPixelData(100, true));
		table.setLayout(tableLayout);
		GridData data = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING);
		table.setLayoutData(data);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		table.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int width = table.getSize().x - 4 - colEnable.getWidth() - colHost.getWidth() - colDir.getWidth() - colTarget.getWidth();
				colOption.setWidth(Math.max(width, 100));
			}
		});
		colHost.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int colWidth = colHost.getWidth();
				if (colWidth < 100) {
					event.doit = false;
					colHost.setWidth(100);
					colWidth = 100;
				}
				int width = table.getSize().x - 4 - colWidth - colEnable.getWidth() - colDir.getWidth() - colTarget.getWidth();
				colOption.setWidth(Math.max(width, 100));
			}
		});
		colTarget.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int colWidth = colTarget.getWidth();
				if (colWidth < 100) {
					event.doit = false;
					colTarget.setWidth(100);
					colWidth = 100;
				}
				int width = table.getSize().x - 4 - colWidth - colEnable.getWidth() - colHost.getWidth() - colDir.getWidth();
				colOption.setWidth(Math.max(width, 100));
			}
		});

		@SuppressWarnings("unused")
		AbstractDecorationCellPaintListener cpl = new AbstractDecorationCellPaintListener(table, 1, 3) {
			@Override
			protected int getDecorationState(Object data, int columnIndex) {
				if (data instanceof IFileTransferItem) {
					IFileTransferItem item = (IFileTransferItem)data;
					if (item.getBooleanProperty(IFileTransferItem.PROPERTY_ENABLED)) {
						Map<String,String> invalid = (Map<String,String>)item.getProperty(PROPERTY_VALIDATION_RESULT);
						if (invalid != null) {
							switch (columnIndex) {
							case 1:
								if (invalid.containsKey(IFileTransferItem.PROPERTY_HOST)) {
									return STATE_ERROR;
								}
								break;
							case 3:
								if (invalid.containsKey(IFileTransferItem.PROPERTY_TARGET)) {
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

		viewer.setContentProvider(new FileTransferContentProvider());
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

	protected List<IFileTransferItem> getInputList() {
		return new ArrayList<IFileTransferItem>(Arrays.asList((IFileTransferItem[])getTablePart().getViewer().getInput()));
	}

	protected void setInputList(List<IFileTransferItem> list) {
		getTablePart().getViewer().setInput(list.toArray(new IFileTransferItem[list.size()]));
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		onButtonEditClick();
	}

	@Override
	protected void onButtonSelected(Button button) {
		int selIndex = ((TableViewer)getTablePart().getViewer()).getTable().getSelectionIndex();
		List<IFileTransferItem> list = getInputList();

		switch (((Integer)button.getData()).intValue()) {
		case 0: // Add
			onButtonAddClick();
			break;
		case 1: // Edit
			onButtonEditClick();
			break;
		case 2: // Delete
			list.remove(selIndex);
			setInputList(list);
			if (!list.isEmpty()) {
				((TableViewer)getTablePart().getViewer()).setSelection(
								new StructuredSelection(list.get(selIndex < list.size() ? selIndex : list.size()-1)), true);
			}
			break;
		case 4: // Up
			list.add(selIndex-1, list.remove(selIndex));
			setInputList(list);
			((TableViewer)getTablePart().getViewer()).setSelection(new StructuredSelection(list.get(selIndex-1)), true);
			break;
		case 5: // Down
			list.add(selIndex+1, list.remove(selIndex));
			setInputList(list);
			((TableViewer)getTablePart().getViewer()).setSelection(new StructuredSelection(list.get(selIndex+1)), true);
			break;
		}
		validateInputList();
		getManagedForm().dirtyStateChanged();
	}

	protected abstract void onButtonAddClick();

	protected abstract void onButtonEditClick();

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
		getTablePart().getButton(2).setEnabled(getTablePart().isEnabled() && singleSelection);
		getTablePart().getButton(4).setEnabled(getTablePart().isEnabled() && singleSelection && selIndex > 0);
		getTablePart().getButton(5).setEnabled(getTablePart().isEnabled() && singleSelection && selIndex < count-1);
	}
}
