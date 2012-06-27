/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.utils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.ui.forms.FormLayoutFactory;
import org.eclipse.tcf.te.ui.interfaces.IOptionListener;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.tcf.te.ui.interfaces.ISearchable;
import org.eclipse.tcf.te.ui.nls.Messages;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

/**
 * A utility searchable class that could combine several searchable objects and thus
 * divide a complex searchable into several simple ones.
 */
public abstract class CompositeSearchable implements ISearchable {
	// The delegating searchables 
	private ISearchable[] searchables;
	
	/**
	 * Constructor with several delegating searchables.
	 * 
	 * @param searchables Delegating searchable objects.
	 */
	public CompositeSearchable(ISearchable... searchables) {
		Assert.isNotNull(searchables);
		this.searchables = searchables;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#createCommonPart(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createCommonPart(Composite parent) {
		for(ISearchable searchable : searchables) {
			searchable.createCommonPart(parent);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#createAdvancedPart(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createAdvancedPart(Composite parent) {
		Section section = new Section(parent, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
		section.setText(Messages.TreeViewerSearchDialog_AdvancedOptions);
		section.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, 2));
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		section.setLayoutData(layoutData);

		final Composite advancedPart = new Composite(section, SWT.NONE);
		advancedPart.setLayout(new GridLayout());
		advancedPart.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		advancedPart.setBackground(section.getBackground());
		section.setClient(advancedPart);
		
		section.addExpansionListener(new IExpansionListener(){
			@Override
            public void expansionStateChanging(ExpansionEvent e) {
            }

			@Override
            public void expansionStateChanged(ExpansionEvent e) {
				boolean state = e.getState();
				int client_height = advancedPart.getSize().y;
				Shell shell = advancedPart.getShell();
				Point p = shell.getSize();
				p.y = state ? p.y + client_height : p.y - client_height;
				shell.setSize(p.x, p.y);
            }});
		for(ISearchable searchable : searchables) {
			searchable.createAdvancedPart(advancedPart);
		}
	}

	/**
	 * A composite matcher that could combine several simple matchers
	 * into a complex matcher object.
	 */
	static class CompositeMatcher implements ISearchMatcher {
		// The delegating matchers.
		private ISearchMatcher[] matchers;
		/**
		 * The constructors.
		 */
		public CompositeMatcher(ISearchMatcher[] matchers) {
			Assert.isNotNull(matchers);
			this.matchers = matchers;
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.tcf.te.ui.interfaces.ISearchMatcher#match(java.lang.Object)
		 */
		@Override
        public boolean match(Object element) {
			for(ISearchMatcher matcher : matchers) {
				if(!matcher.match(element)) {
					return false;
				}
			}
	        return true;
        }
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#getMatcher()
	 */
	@Override
	public ISearchMatcher getMatcher() {
		ISearchMatcher[] matchers = new ISearchMatcher[searchables.length];
		for (int i = 0; i < searchables.length; i++) {
			matchers[i] = searchables[i].getMatcher();
		}
		return new CompositeMatcher(matchers);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#isInputValid()
	 */
	@Override
	public boolean isInputValid() {
		boolean valid = true;
		for(ISearchable searchable : searchables) {
			valid &= searchable.isInputValid();
		}
		return valid;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#addOptionListener(org.eclipse.tcf.te.ui.interfaces.IOptionListener)
	 */
	@Override
	public void addOptionListener(IOptionListener listener) {
		for(ISearchable searchable : searchables) {
			searchable.addOptionListener(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#restoreValues(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	@Override
    public void restoreValues(IDialogSettings settings) {
		for(ISearchable searchable : searchables) {
			searchable.restoreValues(settings);
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#persistValues(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	@Override
    public void persistValues(IDialogSettings settings) {
		for(ISearchable searchable : searchables) {
			searchable.persistValues(settings);
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#removeOptionListener(org.eclipse.tcf.te.ui.interfaces.IOptionListener)
	 */
	@Override
	public void removeOptionListener(IOptionListener listener) {
		for(ISearchable searchable : searchables) {
			searchable.removeOptionListener(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#getPreferredSize()
	 */
	@Override
    public Point getPreferredSize() {
		Point size = null;
		for(ISearchable searchable : searchables) {
			Point prefSize = searchable.getPreferredSize();
			if(prefSize != null) {
				if(size == null) 
					size = new Point(0, 0);
				size.x = Math.max(size.x, prefSize.x);
				size.y = size.y + prefSize.y;
			}
		}
		return size;
    }
}
