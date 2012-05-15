/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.core.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

/**
 * A base class whose subclasses should implement getParent to use its
 * getAncestor(s) methods.
 *
 * @param <T> The element type.
 */
public abstract class Ancestor<T> {

	/**
	 * Get an element which is the common ancestor of all the specified elements.
	 *
	 * @param elements The element list.
	 * @return The ancestor element.
	 */
	protected T getAncestor(List<T> elements) {
		if (elements.isEmpty()) return null;
		if (elements.size() == 1) return elements.get(0);
		T element1 = elements.get(0);
		for (int i = 1; i < elements.size(); i++) {
			T element2 = elements.get(i);
			element1 = getCommonAncestor(element1, element2);
			if (element1 == null) return null;
		}
		return element1;
	}

	/**
	 * Get the top most elements of the specified list. 
	 *
	 * @param elements The original list.
	 * @return The top most elements.
	 */
	protected List<T> getAncestors(List<T> elements) {
		List<T> result = new ArrayList<T>();
		for (T element : elements) {
			if (!hasAncestor(element, elements)) {
				result.add(element);
			}
		}
		return result;
	}

	/**
	 * Get the common ancestor of the specified two elements.
	 *
	 * @param element1 The first element.
	 * @param element2 The second element.
	 * @return The common ancestor.
	 */
	private T getCommonAncestor(T element1, T element2) {
		Assert.isNotNull(element1);
		Assert.isNotNull(element2);
		if (isAncestorOf(element1, element2)) {
			return element1;
		}
		if (isAncestorOf(element2, element1)) {
			return element2;
		}
		T ancestor = null;
		T parent1 = getParent(element1);
		if(parent1 != null) {
			ancestor = getCommonAncestor(parent1, element2);
		}
		if(ancestor != null) return ancestor;
		T parent2 = getParent(element2);
		if(parent2 != null) {
			ancestor = getCommonAncestor(element1, parent2);
		}
		if(ancestor != null) return ancestor;
		if(parent1 != null && parent2 != null) {
			ancestor = getCommonAncestor(parent1, parent2);
		}
		return ancestor;
	}

	/**
	 * If the target element has ancestor in the specified list.
	 *
	 * @param element The element to be tested.
	 * @param elements The element list to search in.
	 * @return true if the element has an ancestor in the list.
	 */
	private boolean hasAncestor(T element, List<T> elements) {
		for (T node : elements) {
			if (isAncestorOf(node, element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Judges if the first element is an ancestor of the second element.
	 *
	 * @param element1 The first element to be tested.
	 * @param element2 The second element to be tested.
	 * @return true if the first element is the ancestor of the second element.
	 */
	private boolean isAncestorOf(T element1, T element2) {
		if (element2 == null) return false;
		T parent = getParent(element2);
		if (parent == element1) return true;
		return isAncestorOf(element1, parent);
    }

	/**
	 * Get the parent of the specified element in the display thread.
	 * 
	 * @param element The element
	 * @return its parent.
	 */
	protected abstract T getParent(T element);
}
