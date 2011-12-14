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

import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Image;

/**
 * A data structure to describe a viewer filter including its id, name, description, enablement and
 * the filter itself.
 */
public class FilterDescriptor {
	// The filter's id, which is unique in a tree viewer.
	private String id;
	// The filter's name used in the filter configuration dialog as the name.
	private String name;
	// The filter's description.
	private String description;
	// If the filter is enabled.
	private boolean enabled;
	// The image of this filter.
	private Image image;

	// The viewer filter.
	private ViewerFilter filter;

	/**
	 * Get the filter's id.
	 * 
	 * @return The filter's id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the filter's id.
	 * 
	 * @param id The new id.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Get the filter's name.
	 * 
	 * @return The filter's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the filter's name.
	 * 
	 * @param name The new name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the filter's description.
	 * 
	 * @return The filter's description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the filter's description.
	 * 
	 * @param description The new description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get if the filter is enabled.
	 * 
	 * @return true if it is enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set if the filter is enabled.
	 * 
	 * @param enabled The new value.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Get the viewer filter.
	 * 
	 * @return The viewer filter.
	 */
	public ViewerFilter getFilter() {
		return filter;
	}

	/**
	 * Set the viewer filter.
	 * 
	 * @param filter The new filter.
	 */
	public void setFilter(ViewerFilter filter) {
		this.filter = filter;
	}
	
	/**
	 * Get the filter's display image.
	 * 
	 * @return The filter's image.
	 */
	public Image getImage() {
    	return image;
    }

	/**
	 * Set the filter's display image.
	 * 
	 * @param image The filter's new image.
	 */
	public void setImage(Image image) {
    	this.image = image;
    }
}
