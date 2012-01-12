/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.commands;

import java.util.Vector;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Group;
 
/**
 * This dialog is used to add/edit user-defined register groups.
 */
public class CustomPropertiesDialog extends ApplicationWindow {

    private Button[] radios;

    private Button[] control;

    private String selectedType = "noType";
    
    private int regSize = 0;

    public CustomPropertiesDialog(Shell parent, int regsize) {
        super(parent);
        regSize = regsize;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());

        ((GridLayout)composite.getLayout()).marginWidth = 10;
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createDataWidgets(composite);
        setBlockOnOpen(true);
        return composite;
    }

    private void createDataWidgets(Composite parent) {
        
        // determine possible element type to propose
        Vector<String> buttons = new Vector<String> ();
        
        if (regSize % 2 == 0)
            buttons.add ("short");
        if (regSize % 4 == 0) 
            buttons.add ("integer");
        if (regSize % 8 == 0)
            buttons.add ("double");
        if (regSize % 16 == 0)
            buttons.add ("long double");
        
        if (buttons.size () == 0)
            return;
        
        RowLayout layout = new RowLayout(SWT.VERTICAL);
        layout.marginLeft = 60;
        layout.marginTop = 10;
        layout.marginBottom = 10;
        layout.spacing = 5;

        Group group = new Group(parent, SWT.NULL);
        group.setText("Element type:");
        group.setBounds(20, 0, 50, 50);
        group.pack();
        group.setLayout(layout);


        radios = new Button[buttons.size()];

        for (int ix = 0; ix < radios.length; ix++) {

            radios[ix] = new Button(group, SWT.RADIO);
            radios[ix].setText(buttons.get(ix));
            radios[ix].pack();
        }

        radios[0].setSelection(true);

        control = new Button[2];

        control[0] = new Button(parent, SWT.PUSH);
        control[0].setText("OK");
        control[0].addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                // TODO Auto-generated method stub
                for (int i = 0; i < radios.length; i++) {
                    if (radios[i].getSelection()) {
                        selectedType = radios[i].getText();
                    }
                }
                close();

            }

            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }
        });
        control[0].setLocation(20, 45);
        control[0].pack();

        control[1] = new Button(parent, SWT.PUSH);
        control[1].setText("Cancel");
        control[1].setLocation(30, 45);
        control[1].addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                // TODO Auto-generated method stub
                close();

            }

            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub
                
            }
        });
        control[1].pack();

        control[0].setSelection(true);

        parent.setSize(600, 250);
    }

    public String getSelectedType() {
        return (selectedType);
    }
}

