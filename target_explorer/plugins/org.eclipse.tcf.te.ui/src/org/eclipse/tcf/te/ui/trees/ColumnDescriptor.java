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

import java.util.Comparator;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * <p>
 * The data descriptor to describe tree columns of a tree viewer. 
 * </p>
 * <p>
 * A ColumnDescriptor encapsulates the following information about the tree
 * column:
 * <ol>
 * <li><code>name</code>, the column's name used as the column's label.</li>
 * <li><code>description</code>, the column's description used as the tooltip
 * text of the column.</li>
 * <li><code>moveable</code>, if the column is moveable.</li>
 * <li><code>resizable</code>, if the column is resizable.</li>
 * <li><code>visible</code>, if the column is visible.</li>
 * <li><code>style</code>, the column's style when it is created.</li>
 * <li><code>alignment</code>, the alignment of the column's header text.</li>
 * <li><code>width</code>, the column's initial width when it is created.</li>
 * <li><code>image</code>, the column's image displayed in the header.</li>
 * <li><code>labelProvider</code>, the label provider of the column.</li>
 * <li><code>comparator</code>, the comparator of the column.</li>
 * </ol>
 * </p>
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 */
public class ColumnDescriptor {
	private String id;
	//The name of the column, used as the column's label.
	private String name;
	//The description of the column, used as the column's tooltip text.
	private String description;

	//If the column is moveable.
	private boolean mveable;
	//If the column is resizable, true by default.
	private boolean resizable = true;
	//If the column is visible, true by default.
	private boolean visible = true;

	//The style of the column when it is created, SWT.LEFT by default.
	private int style = SWT.LEFT;
	//The alignment of the column's header text, SWT.LEFT by default.
	private int alignment = SWT.LEFT;
	//The column's initial width when it is created, 150 by default.
	private int width = 150;

	//The column's header image.
	private Image image;

	//The label provider of the column.
	private ILabelProvider labelProvider;

	//The comparator of the column, used to sort the viewer.
	@SuppressWarnings("rawtypes")
	private Comparator comparator;

	//The corresponding tree column. Not intended to be changed by callers.
	private TreeColumn treeColumn;
	//If the column's sorting order is ascending. Defaults to true. Not intended to be changed by callers.
	private boolean ascending = true;

	/**
	 * Create a column descriptor with specified column id.
	 * 
	 * @param id
	 * 				The column id;
	 */
	public ColumnDescriptor(String id) {
		this.id = id;
	}

	/**
	 * Get the column's id.
	 * 
	 * @return the column's id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the column's id.
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Get the column's sorting orientation.
	 * 
	 * @return The sorting orientation.
	 */
	public boolean isAscending() {
		return ascending;
	}

	/**
	 * Set the column's sorting orientation.
	 * 
	 * @param a The new orientation.
	 */
	public void setAscending(boolean a) {
		this.ascending = a;
	}

	/**
	 * Set the column's sorting comparator.
	 * 
	 * @param comparator The new comparator.
	 */
	@SuppressWarnings("rawtypes")
	public void setComparator(Comparator comparator) {
		this.comparator = comparator;
	}

	/**
	 * Get the column's sorting comparator.
	 * 
	 * @return The new comparator.
	 */
	@SuppressWarnings("rawtypes")
	public Comparator getComparator() {
		return comparator;
	}

	/**
	 * Set the tree column.
	 * 
	 * @param column The tree column.
	 */
	public void setTreeColumn(TreeColumn column) {
		this.treeColumn = column;
	}

	/**
	 * Get the tree column.
	 * 
	 * @return The tree column.
	 */
	public TreeColumn getTreeColumn() {
		return treeColumn;
	}

	/**
	 * Set the visibility of this tree column.
	 * 
	 * @param v the new visibility
	 */
	public void setVisible(boolean v) {
		visible = v;
	}

	/**
	 * Get the visibility of the tree column.
	 * 
	 * @return This column's visibility.
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Set the name of the column.
	 * 
	 * @param name The new name.
	 */
	public void setName(String name) { 
		this.name = name;
	}

	/**
	 * Get the name of the column.
	 * 
	 * @return The column's name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the description of the column.
	 * 
	 * @param desc The column's description.
	 */
	public void setDescription(String desc) {
		description = desc;
	}

	/**
	 * Get the description of the column.
	 * 
	 * @return The column's description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set if the column is moveable.
	 * 
	 * @param m The new value.
	 */
	public void setMoveable(boolean m) {
		mveable = m;
	}

	/**
	 * Get if the column is moveable.
	 * 
	 * @return If the column is moveable.
	 */
	public boolean isMoveable() {
		return mveable;
	}

	/**
	 * Set the column's creation style.
	 * 
	 * @param style The column's creation style.
	 */
	public void setStyle(int style) {
		this.style = style;
	}

	/**
	 * Get the column's creation style.
	 * 
	 * @return The column's creation style.
	 */
	public int getStyle() {
		return style;
	}

	/**
	 * Set the column's alignment.
	 * 
	 * @param alignment The column's alignment.
	 */
	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	/**
	 * Get the column's alignment.
	 * 
	 * @return The column's alignment.
	 */
	public int getAlignment() {
		return alignment;
	}

	/**
	 * Set the column's image.
	 * 
	 * @param img The new image.
	 */
	public void setImage(Image img) {
		this.image = img;
	}

	/**
	 * Get the column's image.
	 * 
	 * @return The column's image.
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Set if the column is resizable.
	 * 
	 * @param r The new value.
	 */
	public void setResizable(boolean r) {
		resizable = r;
	}

	/**
	 * Get if the column is resizable.
	 * 
	 * @return If the column is resizable.
	 */
	public boolean isResizable() {
		return resizable;
	}

	/**
	 * Set the column's initial width.
	 * 
	 * @param width The new column width.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Get the column's initial width.
	 * 
	 * @return the column's initial width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Set the column's label provider.
	 * 
	 * @param p The new column label provider.
	 */
	public void setLabelProvider(ILabelProvider p) {
		labelProvider = p;
	}

	/**
	 * Get the column's label provider.
	 * 
	 * @return The column's label provider.
	 */
	public ILabelProvider getLabelProvider() {
		return labelProvider;
	}
}
