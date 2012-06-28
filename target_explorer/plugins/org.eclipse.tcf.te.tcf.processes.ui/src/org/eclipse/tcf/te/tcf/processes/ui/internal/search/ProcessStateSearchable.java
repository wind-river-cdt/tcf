/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;

/**
 * The searchable that provides a UI to collect and test
 * the state of a process during searching.
 * <p>
 * Note the state of a process is expressed using a character from
 *  "RSDZTW"  where  R  is  running,  S  is
 *  sleeping  in  an  interruptible wait, D is waiting in uninterruptible
 *  disk sleep, Z is zombie, T is traced or stopped (on a signal), and  W
 *  is paging.
 */
public class ProcessStateSearchable extends ProcessBaseSearchable {
	// Constant values of state options
	private static final int OPTION_NOT_REMEMBER = 0;
	private static final int OPTION_SPECIFIED = 1;

	// The choice selected
	private int choice;

	// UI elements for input
	private Button fBtnNotRem;
	private Button fBtnSpecified;
	
	private Button fBtnR;
	private Button fBtnS;
	private Button fBtnD;
	private Button fBtnZ;
	private Button fBtnT;
	private Button fBtnW;
	
	// The flags indicating if a certain state is included.
	private boolean fIncludeR;
	private boolean fIncludeS;
	private boolean fIncludeD;
	private boolean fIncludeZ;
	private boolean fIncludeT;
	private boolean fIncludeW;
	
	// The current selected states expressed in the above characters.
	private String fStates;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.utils.AbstractSearchable#createAdvancedPart(org.eclipse.swt.widgets.Composite)
	 */
	@Override
    public void createAdvancedPart(Composite parent) {
		SelectionListener l = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				optionChecked(e);
			}
		};
		Composite stateComposite = createSection(parent, Messages.ProcessStateSearchable_SectionChooseState);
		stateComposite.setLayout(new GridLayout());
		
		fBtnNotRem = new Button(stateComposite, SWT.RADIO);
		fBtnNotRem.setText(Messages.ProcessStateSearchable_NotSure);
		fBtnNotRem.setSelection(true);
		GridData data = new GridData();
		fBtnNotRem.setLayoutData(data);
		fBtnNotRem.addSelectionListener(l);
		
		fBtnSpecified = new Button(stateComposite, SWT.RADIO);
		fBtnSpecified.setText(Messages.ProcessStateSearchable_SpecifyState);
		data = new GridData();
		fBtnSpecified.setLayoutData(data);
		fBtnSpecified.addSelectionListener(l);
		
		Composite cmpStates = new Composite(stateComposite, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		cmpStates.setLayoutData(data);
		GridLayout layout = new GridLayout(3, true);
		layout.marginLeft = 20;
		cmpStates.setLayout(layout);
		
		fBtnR = new Button(cmpStates, SWT.CHECK);
		fBtnR.setText(Messages.ProcessStateSearchable_StateRunning);
		fBtnR.setEnabled(false);
		data = new GridData();
		fBtnR.setLayoutData(data);
		fBtnR.addSelectionListener(l);
		
		fBtnS = new Button(cmpStates, SWT.CHECK);
		fBtnS.setText(Messages.ProcessStateSearchable_StateSleeping);
		fBtnS.setEnabled(false);
		data = new GridData();
		fBtnS.setLayoutData(data);
		fBtnS.addSelectionListener(l);
		
		fBtnD = new Button(cmpStates, SWT.CHECK);
		fBtnD.setText(Messages.ProcessStateSearchable_StateWaiting);
		fBtnD.setEnabled(false);
		data = new GridData();
		fBtnD.setLayoutData(data);
		fBtnD.addSelectionListener(l);
		
		fBtnZ = new Button(cmpStates, SWT.CHECK);
		fBtnZ.setText(Messages.ProcessStateSearchable_StateZombie);
		fBtnZ.setEnabled(false);
		data = new GridData();
		fBtnZ.setLayoutData(data);
		fBtnZ.addSelectionListener(l);
		
		fBtnT = new Button(cmpStates, SWT.CHECK);
		fBtnT.setText(Messages.ProcessStateSearchable_StateTraced);
		fBtnT.setEnabled(false);
		data = new GridData();
		fBtnT.setLayoutData(data);
		fBtnT.addSelectionListener(l);
		
		fBtnW = new Button(cmpStates, SWT.CHECK);
		fBtnW.setText(Messages.ProcessStateSearchable_StatePaging);
		fBtnW.setEnabled(false);
		data = new GridData();
		fBtnW.setLayoutData(data);
		fBtnW.addSelectionListener(l);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.utils.AbstractSearchable#isInputValid()
	 */
	@Override
    public boolean isInputValid() {
		if(choice == OPTION_SPECIFIED) {
			boolean valid = fIncludeR || fIncludeS || fIncludeD || fIncludeZ || fIncludeT || fIncludeW;
			return valid;
		}
	    return true;
    }

	/**
	 * The method handling the selection event.
	 * 
	 * @param e The selection event.
	 */
	protected void optionChecked(SelectionEvent e) {
		Object src = e.getSource();
		if(src == fBtnNotRem) {
			choice = OPTION_NOT_REMEMBER;
			setButtonStates(false);
		}
		else if(src == fBtnSpecified) {
			choice = OPTION_SPECIFIED;
			setButtonStates(true);
			fStates = getSelectedStates();
		}
		else if(src == fBtnR) {
			fIncludeR = fBtnR.getSelection();
			fStates = getSelectedStates();
		}
		else if(src == fBtnS) {
			fIncludeS = fBtnS.getSelection();
			fStates = getSelectedStates();
		}
		else if(src == fBtnD) {
			fIncludeD = fBtnD.getSelection();
			fStates = getSelectedStates();
		}
		else if(src == fBtnZ) {
			fIncludeZ = fBtnZ.getSelection();
			fStates = getSelectedStates();
		}
		else if(src == fBtnT) {
			fIncludeT = fBtnT.getSelection();
			fStates = getSelectedStates();
		}
		else if(src == fBtnW) {
			fIncludeW = fBtnW.getSelection();
			fStates = getSelectedStates();
		}
		fireOptionChanged();
    }
	
	/**
	 * Get the current state strings expressed in the character set
	 * mentioned above.
	 * 
	 * @return A string that contains all the selected states.
	 */
	private String getSelectedStates() {
		StringBuilder builder = new StringBuilder();
		if(fIncludeR) {
			builder.append("R"); //$NON-NLS-1$
		}
		if(fIncludeS) {
			builder.append("S"); //$NON-NLS-1$
		}
		if(fIncludeD) {
			builder.append("D"); //$NON-NLS-1$
		}
		if(fIncludeZ) {
			builder.append("Z"); //$NON-NLS-1$
		}
		if(fIncludeT) {
			builder.append("T"); //$NON-NLS-1$
		}
		if(fIncludeW) {
			builder.append("W"); //$NON-NLS-1$
		}
		return builder.toString();
	}

	/**
	 * Enable the state buttons using the specified
	 * enablement flag.
	 * 
	 * @param enabled the enablement flag.
	 */
	private void setButtonStates(boolean enabled) {
	    fBtnR.setEnabled(enabled);
	    fBtnS.setEnabled(enabled);
	    fBtnD.setEnabled(enabled);
	    fBtnZ.setEnabled(enabled);
	    fBtnT.setEnabled(enabled);
	    fBtnW.setEnabled(enabled);
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchMatcher#match(java.lang.Object)
	 */
	@Override
    public boolean match(Object element) {
		if (element instanceof ProcessTreeNode) {
			switch (choice) {
			case OPTION_NOT_REMEMBER:
				return true;
			case OPTION_SPECIFIED:
				ProcessTreeNode node = (ProcessTreeNode) element;
				String state = node.state;
				if(state != null && state.length() > 0) {
					state = state.toUpperCase();
					return fStates.indexOf(state) != -1;
				}
			}
		}
		return false;
    }
}
