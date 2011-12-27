package org.eclipse.tcf.te.tcf.processes.ui.internal.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.runtime.utils.Host;
import org.eclipse.tcf.te.tcf.processes.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.processes.ui.internal.dialogs.EditSpeedGradeDialog;
import org.eclipse.tcf.te.tcf.processes.ui.internal.dialogs.NewSpeedGradeDialog;
import org.eclipse.tcf.te.tcf.processes.ui.model.IntervalGrade;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class ProcessPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IPreferenceConsts {

	TableViewer gradesTable;
	Button addButton;
	Button editButton;
	Button removeButton;
	Text mruCountText;
	List<IntervalGrade> grades;

	public ProcessPreferencePage() {
		setDescription(Messages.ProcessPreferencePage_PageDescription);
	}

	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem()
		                .setHelp(getControl(), UIPlugin.getUniqueIdentifier() + ".preferencePage"); //$NON-NLS-1$
	}

	@Override
	protected Control createContents(Composite parent) {
		Font font = parent.getFont();

		// The main composite
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 0;
		layout.verticalSpacing = 10;
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setFont(font);
		Composite mruGroup = new Composite(composite, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		mruGroup.setLayoutData(data);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		mruGroup.setLayout(layout);
		mruGroup.setFont(font);
		Label label = new Label(mruGroup, SWT.NONE);
		label.setText(Messages.ProcessPreferencePage_MRUCountLabel);
		mruCountText = new Text(mruGroup, SWT.BORDER | SWT.SINGLE);
		IPreferenceStore prefStore = UIPlugin.getDefault().getPreferenceStore();
		int maxCount = prefStore.getInt(IPreferenceConsts.PREF_INTERVAL_MRU_COUNT);
		mruCountText.setText("" + maxCount); //$NON-NLS-1$
		data = new GridData();
		data.widthHint = 50;
		mruCountText.setLayoutData(data);
		mruCountText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		createTable(composite);
		createButtons(composite);
		return composite;
	}

	/**
	 * Validate the current input and update the button and the error message.
	 */
	void validateInput() {
		IStatus status = isInputValid();
		setValid(status.isOK());
		if (status.isOK()) {
			setMessage(null);
		}
		else {
			int severity = status.getSeverity();
			int msgType = IMessageProvider.NONE;
			String message = status.getMessage();
			switch (severity) {
			case IStatus.ERROR:
				msgType = IMessageProvider.ERROR;
				break;
			case IStatus.INFO:
				msgType = IMessageProvider.INFORMATION;
				break;
			case IStatus.WARNING:
				msgType = IMessageProvider.WARNING;
				break;
			case IStatus.CANCEL:
				msgType = IMessageProvider.NONE;
				break;
			}
			setMessage(message, msgType);
		}
	}

	private IStatus isInputValid() {
		String txt = mruCountText.getText();
		if (txt == null || txt.trim().length() == 0) {
			return new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), Messages.ProcessPreferencePage_MaxMRUCount);
		}
		try {
			int interval = Integer.parseInt(txt.trim());
			if (interval <= 0) return new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), Messages.ProcessPreferencePage_BiggerThanZero);
		}
		catch (NumberFormatException e) {
			return new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), Messages.ProcessPreferencePage_InvalidNumber);
		}
		if (grades.isEmpty()) {
			return new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), Messages.ProcessPreferencePage_DefineMoreThanOne);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Creates and configures the table containing launch configuration variables and their
	 * associated value.
	 */
	private void createTable(Composite parent) {
		Font font = parent.getFont();
		// Create table composite
		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 150;
		tableComposite.setLayout(layout);
		tableComposite.setLayoutData(gridData);
		tableComposite.setFont(font);
		// Create table
		gradesTable = new TableViewer(tableComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = gradesTable.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(font);
		gridData = new GridData(GridData.FILL_BOTH);
		gradesTable.getControl().setLayoutData(gridData);
		gradesTable.setContentProvider(new ListContentProvider());

		gradesTable.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
            public void selectionChanged(SelectionChangedEvent event) {
				handleTableSelectionChanged(event);
			}
		});

		gradesTable.addDoubleClickListener(new IDoubleClickListener() {
			@Override
            public void doubleClick(DoubleClickEvent event) {
				if (!gradesTable.getSelection().isEmpty()) {
					handleEditButtonPressed();
				}
			}
		});
		gradesTable.getTable().addKeyListener(new KeyAdapter() {
			@Override
            public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					handleRemoveButtonPressed();
				}
			}
		});

		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setResizable(true);
		column.setText(Messages.ProcessPreferencePage_NameLabel);
		column.setWidth(100);
		column = new TableColumn(table, SWT.RIGHT);
		column.setResizable(true);
		column.setText(Messages.ProcessPreferencePage_ValueLabel);
		column.setWidth(100);
		//Create an empty column to pad the table (on Linux).
		if (!Host.isWindowsHost()) {
			column = new TableColumn(table, SWT.LEFT);
			column.setText(""); //$NON-NLS-1$
			column.setWidth(100);
		}

		gradesTable.setInput(getGrades());
		gradesTable.setLabelProvider(new GradeLabelProvider());
	}

	Button createPushButton(Composite parent, String label, Image image) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		if (image != null) {
			button.setImage(image);
		}
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		button.setLayoutData(gd);
		setButtonDimensionHint(button);
		return button;
	}

	void setButtonDimensionHint(Button button) {
		Assert.isNotNull(button);
		Object gd = button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData) gd).widthHint = getButtonWidthHint(button);
			((GridData) gd).horizontalAlignment = GridData.FILL;
		}
	}

	int getButtonWidthHint(Button button) {
		/* button.setFont(JFaceResources.getDialogFont()); */
		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

	/**
	 * Creates the new/edit/remove buttons for the variable table
	 * 
	 * @param parent the composite in which the buttons should be created
	 */
	private void createButtons(Composite parent) {
		// Create button composite
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout glayout = new GridLayout();
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		glayout.numColumns = 1;
		GridData gdata = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		buttonComposite.setLayout(glayout);
		buttonComposite.setLayoutData(gdata);
		buttonComposite.setFont(parent.getFont());

		// Create buttons
		addButton = createPushButton(buttonComposite, Messages.ProcessPreferencePage_NewButtonLabel, null);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
            public void widgetSelected(SelectionEvent event) {
				handleAddButtonPressed();
			}
		});
		editButton = createPushButton(buttonComposite, Messages.ProcessPreferencePage_EditButtonLabel, null);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
            public void widgetSelected(SelectionEvent event) {
				handleEditButtonPressed();
			}
		});
		editButton.setEnabled(false);
		removeButton = createPushButton(buttonComposite, Messages.ProcessPreferencePage_RemoveButtonLabel, null);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
            public void widgetSelected(SelectionEvent event) {
				handleRemoveButtonPressed();
			}
		});
		removeButton.setEnabled(false);
	}

	void handleAddButtonPressed() {
		NewSpeedGradeDialog dialog = new NewSpeedGradeDialog(getShell());
		dialog.setGrades(grades);
		if (dialog.open() == Window.OK) {
			IntervalGrade grade = dialog.getResult();
			grades.add(grade);
			gradesTable.refresh();
		}
		validateInput();
	}

	void handleEditButtonPressed() {
		IStructuredSelection selection = (IStructuredSelection) gradesTable.getSelection();
		IntervalGrade grade = (IntervalGrade) selection.getFirstElement();
		if (grade != null) {
			EditSpeedGradeDialog dialog = new EditSpeedGradeDialog(getShell());
			dialog.setGrades(grades);
			dialog.setGrade(grade);

			if (dialog.open() == Window.OK) {
				gradesTable.update(grade, null);
			}
			validateInput();
		}
	}

	/**
	 * Remove the selection variables.
	 */
	void handleRemoveButtonPressed() {
		IStructuredSelection selection = (IStructuredSelection) gradesTable.getSelection();
		List<IntervalGrade> toRemove = selection.toList();
		Iterator<IntervalGrade> iter = toRemove.iterator();
		while (iter.hasNext()) {
			IntervalGrade grade = iter.next();
			grades.remove(grade);
		}
		gradesTable.refresh();
		validateInput();
	}

	/**
	 * Responds to a selection changed event in the variable table
	 * 
	 * @param event the selection change event
	 */
	void handleTableSelectionChanged(SelectionChangedEvent event) {
		IStructuredSelection selection = ((IStructuredSelection) event.getSelection());
		IntervalGrade variable = (IntervalGrade) selection.getFirstElement();
		if (variable == null) {
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
		}
		else {
			editButton.setEnabled(selection.size() == 1);
			removeButton.setEnabled(selection.size() > 0);
		}
	}

	@Override
    public void init(IWorkbench workbench) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
    protected void performDefaults() {
		grades = null;
		gradesTable.setInput(getGrades());
		gradesTable.refresh();
		validateInput();
		super.performDefaults();
	}

	class ListContentProvider implements IStructuredContentProvider {
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List<?>) {
				return ((List<?>) inputElement).toArray();
			}
			return null;
		}
	}

	/**
	 * Get the current the speed grades in an array of Grade.
	 * 
	 * @return The current speed grades.
	 */
	List<IntervalGrade> getGrades() {
		if (grades == null) {
			grades = new ArrayList<IntervalGrade>();
			IPreferenceStore prefStore = UIPlugin.getDefault().getPreferenceStore();
			String gradestr = prefStore.getString(IPreferenceConsts.PREF_INTERVAL_GRADES);
			Assert.isNotNull(gradestr);
			StringTokenizer st = new StringTokenizer(gradestr, "|"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				StringTokenizer st2 = new StringTokenizer(token, ":"); //$NON-NLS-1$
				String name = st2.nextToken();
				String value = st2.nextToken();
				try {
					int seconds = Integer.parseInt(value);
					if (seconds > 0) {
						IntervalGrade grade = new IntervalGrade(name, seconds);
						grades.add(grade);
					}
				}
				catch (NumberFormatException nfe) {
				}
			}
		}
		return grades;
	}

	class GradeLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
        public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
        public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IntervalGrade) {
				IntervalGrade grade = (IntervalGrade) element;
				switch (columnIndex) {
				case 0:
					return grade.getName();
				case 1:
					int value = grade.getValue();
					if (value == 0) {
						return ""; //$NON-NLS-1$
					}
					return "" + value; //$NON-NLS-1$
				}
			}
			return null;
		}
	}
}
