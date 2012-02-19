/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator.internal.nodes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.factory.IFactoryDelegate;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.internal.ModelNodeFactoryDelegate;

/**
 * Illegal peer model node implementation. All methods does throw an {@link IllegalStateException}.
 * <p>
 * Objects of this class are returned by the {@link ModelNodeFactoryDelegate} if calling
 * {@link IFactoryDelegate#newInstance(Class)}. All what objects of this class can be used for is
 * loading adapters.
 */
public final class IllegalPeerModel implements IPeerModel {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode#add(org.eclipse.tcf.te.runtime.model.interfaces.IModelNode)
	 */
	@Override
	public boolean add(IModelNode child) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode#remove(org.eclipse.tcf.te.runtime.model.interfaces.IModelNode, boolean)
	 */
	@Override
	public boolean remove(IModelNode node, boolean recursive) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode#clear()
	 */
	@Override
	public boolean clear() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode#removeAll(java.lang.Class)
	 */
	@Override
	public <T> boolean removeAll(Class<T> nodeType) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode#getChildren()
	 */
	@Override
	public IModelNode[] getChildren() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode#getChildren(java.lang.Class)
	 */
	@Override
	public <T> List<T> getChildren(Class<T> nodeType) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode#size()
	 */
	@Override
	public int size() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode#contains(org.eclipse.tcf.te.runtime.model.interfaces.IModelNode)
	 */
	@Override
	public boolean contains(IModelNode node) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#getParent()
	 */
	@Override
	public IContainerModelNode getParent() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#getParent(java.lang.Class)
	 */
	@Override
	public <V extends IContainerModelNode> V getParent(Class<V> nodeType) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#setParent(org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode)
	 */
	@Override
	public void setParent(IContainerModelNode parent) throws IllegalStateException {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#move(org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode)
	 */
	@Override
	public void move(IContainerModelNode newParent) throws IllegalStateException {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#isVisible()
	 */
	@Override
	public boolean isVisible() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#getName()
	 */
	@Override
	public String getName() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#getImageId()
	 */
	@Override
	public String getImageId() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#getError()
	 */
	@Override
	public String getError() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#getDescription()
	 */
	@Override
	public String[] getDescription() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#setDirty(boolean)
	 */
	@Override
	public void setDirty(boolean dirty) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#isDirty()
	 */
	@Override
	public boolean isDirty() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#setPending(boolean)
	 */
	@Override
	public void setPending(boolean pending) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#isPending()
	 */
	@Override
	public boolean isPending() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNode#find(java.util.UUID)
	 */
	@Override
	public IModelNode find(UUID uuid) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#getUUID()
	 */
	@Override
	public UUID getUUID() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#setChangeEventsEnabled(boolean)
	 */
	@Override
	public boolean setChangeEventsEnabled(boolean enabled) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#changeEventsEnabled()
	 */
	@Override
	public boolean changeEventsEnabled() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#fireChangeEvent(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void fireChangeEvent(String key, Object oldValue, Object newValue) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#setProperties(java.util.Map)
	 */
	@Override
	public void setProperties(Map<String, Object> properties) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#setProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public boolean setProperty(String key, Object value) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#setProperty(java.lang.String, long)
	 */
	@Override
	public boolean setProperty(String key, long value) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#setProperty(java.lang.String, int)
	 */
	@Override
	public boolean setProperty(String key, int value) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#setProperty(java.lang.String, boolean)
	 */
	@Override
	public boolean setProperty(String key, boolean value) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#setProperty(java.lang.String, float)
	 */
	@Override
	public boolean setProperty(String key, float value) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#setProperty(java.lang.String, double)
	 */
	@Override
	public boolean setProperty(String key, double value) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#getProperties()
	 */
	@Override
	public Map<String, Object> getProperties() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String key) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#getStringProperty(java.lang.String)
	 */
	@Override
	public String getStringProperty(String key) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#getLongProperty(java.lang.String)
	 */
	@Override
	public long getLongProperty(String key) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#getIntProperty(java.lang.String)
	 */
	@Override
	public int getIntProperty(String key) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#getBooleanProperty(java.lang.String)
	 */
	@Override
	public boolean getBooleanProperty(String key) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#getFloatProperty(java.lang.String)
	 */
	@Override
	public float getFloatProperty(String key) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#getDoubleProperty(java.lang.String)
	 */
	@Override
	public double getDoubleProperty(String key) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#clearProperties()
	 */
	@Override
	public void clearProperties() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#isPropertyIgnoreCase(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isPropertyIgnoreCase(String key, String value) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#isProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public boolean isProperty(String key, Object value) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#isProperty(java.lang.String, long)
	 */
	@Override
	public boolean isProperty(String key, long value) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#isProperty(java.lang.String, int)
	 */
	@Override
	public boolean isProperty(String key, int value) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#isProperty(java.lang.String, boolean)
	 */
	@Override
	public boolean isProperty(String key, boolean value) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#isProperty(java.lang.String, float)
	 */
	@Override
	public boolean isProperty(String key, float value) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer#isProperty(java.lang.String, double)
	 */
	@Override
	public boolean isProperty(String key, double value) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean contains(ISchedulingRule rule) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel#getModel()
	 */
	@Override
	public ILocatorModel getModel() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel#getPeer()
	 */
	@Override
	public IPeer getPeer() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel#getPeerId()
	 */
	@Override
	public String getPeerId() {
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel#isComplete()
	 */
	@Override
	public boolean isComplete() {
		throw new IllegalStateException();
	}
}
