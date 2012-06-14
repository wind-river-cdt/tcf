/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.adapters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.ui.forms.FormLayoutFactory;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.tcf.te.ui.utils.AbstractSearchable;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

public abstract class FSBaseSearchable extends AbstractSearchable implements ISearchMatcher {
	/**
	 * Create a collapseable section with the specified title and return the
	 * content composite.
	 * 
	 * @param parent The parent where the section is to be created.
	 * @param title The title of the section.
	 * @return The content composite.
	 */
	protected Composite createSection(Composite parent, String title) {
		Section section = new Section(parent, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
		section.setText(title);
		section.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, 2));
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		section.setLayoutData(layoutData);
		
		final Composite client = new Composite(section, SWT.NONE);
		client.setBackground(section.getBackground());
		section.setClient(client);
		
		section.addExpansionListener(new IExpansionListener(){
			@Override
            public void expansionStateChanging(ExpansionEvent e) {
            }
			@Override
            public void expansionStateChanged(ExpansionEvent e) {
				Shell shell = client.getShell();
				boolean state = e.getState();
				int client_height = client.getSize().y;
				Point p = shell.getSize();
				p.y = state ? p.y + client_height : p.y - client_height;
				shell.setSize(p.x, p.y);
            }});
		return client;
	}

	@Override
	public ISearchMatcher getMatcher() {
		return this;
	}
}
