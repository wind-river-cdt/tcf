/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.model;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Path;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.model.ContainerModelNode;
import org.eclipse.tcf.te.runtime.model.PendingOperationModelNode;
import org.eclipse.tcf.te.runtime.model.factory.Factory;
import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.services.filetransfer.FileTransferItem;
import org.eclipse.tcf.te.runtime.services.interfaces.filetransfer.IFileTransferItem;
import org.eclipse.tcf.te.tests.CoreTestCase;

/**
 * Model test cases.
 */
public class ModelTestCase extends CoreTestCase {

	/**
	 * Provides a test suite to the caller which combines all single
	 * test bundled within this category.
	 *
	 * @return Test suite containing all test for this test category.
	 */
	public static Test getTestSuite() {
		TestSuite testSuite = new TestSuite("Test model node framework"); //$NON-NLS-1$

			// add ourself to the test suite
			testSuite.addTestSuite(ModelTestCase.class);

		return testSuite;
	}

	//***** BEGIN SECTION: Single test methods *****
	//NOTE: All method which represents a single test case must
	//      start with 'test'!

	private static class TestModelNode extends ContainerModelNode {
		/**
		 * Constructor.
		 */
		public TestModelNode(IContainerModelNode parent) {
			super();
			setParent(parent);
		}

	}

	public void testModelNode() {
		IContainerModelNode model = Factory.getInstance().newInstance(IContainerModelNode.class);
		assertNotNull("Failed to create new model node instance!", model); //$NON-NLS-1$

		TestModelNode node = new TestModelNode(model);
		assertNotNull("Failed to construct test model node instance!", node); //$NON-NLS-1$
		assertEquals("Unexpected return value: ", model, node.getParent()); //$NON-NLS-1$
		model.add(node);
		assertTrue("Failed to add model node to target manager model!", model.contains(node)); //$NON-NLS-1$
		model.remove(node, true);
		assertFalse("Failed to remove model node from target manager model!", model.contains(node)); //$NON-NLS-1$

		node.setProperty(IModelNode.PROPERTY_NAME, "TestModelNode"); //$NON-NLS-1$
		assertEquals("Unexpected return value: ", "TestModelNode", node.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		node.setProperty(IModelNode.PROPERTY_ERROR, "TestModelNode-Error"); //$NON-NLS-1$
		assertEquals("Unexpected return value: ", "TestModelNode-Error", node.getError()); //$NON-NLS-1$ //$NON-NLS-2$
		assertNotNull("Unexpected return value: null", node.getDescription()); //$NON-NLS-1$
		assertEquals("Unexpected return value: ", 0, node.getDescription().length); //$NON-NLS-1$
		assertNull("Unexpected return value: not null", node.getImageId()); //$NON-NLS-1$
		assertFalse("Unexpected return value true", node.isVisible()); //$NON-NLS-1$

		node.setProperty("PROPERTY_BOOLEAN", true); //$NON-NLS-1$
		assertEquals("Unexpected return value: ", true, node.getBooleanProperty("PROPERTY_BOOLEAN")); //$NON-NLS-1$ //$NON-NLS-2$
		node.setProperty("PROPERTY_INTEGER", 100); //$NON-NLS-1$
		assertEquals("Unexpected return value: ", 100, node.getIntProperty("PROPERTY_INTEGER")); //$NON-NLS-1$ //$NON-NLS-2$

		// Get all properties of the node
		Map<String, Object> properties = node.getProperties();
		assertEquals("Unexpected number of properties!", 4, properties.size()); //$NON-NLS-1$

		node.clearProperties();
		assertTrue("Node still contains properties after node.clearProperties()!", node.getProperties().isEmpty()); //$NON-NLS-1$

		// And set all properties again
		node.setProperties(properties);
		assertEquals("Unexpected number of properties.", 4, properties.size()); //$NON-NLS-1$

		TestModelNode child = new TestModelNode(node);
		assertNotNull("Failed to construct test model node instance!", child); //$NON-NLS-1$
		assertEquals("Unexpected return value: ", node, child.getParent()); //$NON-NLS-1$

		assertEquals("Unexpected return value: ", 0, node.size()); //$NON-NLS-1$
		node.add(child);
		assertEquals("Unexpected return value: ", 1, node.size()); //$NON-NLS-1$
		assertTrue("Unexpected return value: false", node.contains(child)); //$NON-NLS-1$

		String nodeAsString = node.toString();
		assertNotNull("Failed to convert model node into string representation!", nodeAsString); //$NON-NLS-1$

		// Move node to another container
		IContainerModelNode newContainer = new ContainerModelNode();
		node.move(newContainer);
		assertTrue("Unexpected return value: false", newContainer.contains(node)); //$NON-NLS-1$
		assertFalse("Unexpected return value: true", model.contains(node)); //$NON-NLS-1$
	}

	@SuppressWarnings("boxing")
	public void testPropertiesContainer() {
		// Create a properties container not sending notifications
		IPropertiesContainer container = new PropertiesContainer();
		assertNotNull("Failed to instantiate the properties container!", container); //$NON-NLS-1$

		container.setProperty("boolean", true); //$NON-NLS-1$
		container.setProperty("integer", Integer.MAX_VALUE); //$NON-NLS-1$
		container.setProperty("long", Long.MAX_VALUE); //$NON-NLS-1$
		container.setProperty("float", Float.MAX_VALUE); //$NON-NLS-1$
		container.setProperty("double", Double.MAX_VALUE); //$NON-NLS-1$
		container.setProperty("string", "string"); //$NON-NLS-1$ //$NON-NLS-2$
		container.setProperty("object", new Object()); //$NON-NLS-1$

		assertEquals("Unexpected return value for container.getBooleanProperty()!", true, container.getBooleanProperty("boolean")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Unexpected return value for container.getIntProperty()!", Integer.MAX_VALUE, container.getIntProperty("integer")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Unexpected return value for container.getLongProperty()!", Long.MAX_VALUE, container.getLongProperty("long")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Unexpected return value for container.getFloatProperty()!", Float.MAX_VALUE, container.getFloatProperty("float")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Unexpected return value for container.getDoubleProperty()!", Double.MAX_VALUE, container.getDoubleProperty("double")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Unexpected return value for container.getStringProperty()!", "string", container.getStringProperty("string")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertNotNull("Unexpected return value null for container.getProperty()!", container.getProperty("object")); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue("Unexpected return value for container.isProperty(...)!", container.isProperty("boolean", true)); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unexpected return value for container.isProperty(...)!", container.isProperty("integer", Integer.MAX_VALUE)); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unexpected return value for container.isProperty(...)!", container.isProperty("long", Long.MAX_VALUE)); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unexpected return value for container.isProperty(...)!", container.isProperty("float", Float.MAX_VALUE)); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unexpected return value for container.isProperty(...)!", container.isProperty("double", Double.MAX_VALUE)); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unexpected return value for container.isProperty(...)!", container.isProperty("string", "string")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testPendingOperationModelNode() {
		PendingOperationModelNode node = new PendingOperationModelNode();
		assertNotNull("Failed to create pending operation model node instance!", node); //$NON-NLS-1$
		assertEquals("Unexpected pending operation model node name: " + node.getName(), "Pending...", node.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("Pending operation model node name change executed but should be discarded!", node.setProperty(IModelNode.PROPERTY_NAME, "changed")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testFileTransferItem() {
		FileTransferItem item1 = new FileTransferItem();
		assertNotNull("Failed to create empty file transfer item!", item1); //$NON-NLS-1$

		FileTransferItem item2 = new FileTransferItem(new Path("/folk/uwe/tmp/cobble.out"), new Path("/root/cobble.out")); //$NON-NLS-1$ //$NON-NLS-2$
		assertNotNull("Failed to create file transfer item from pathes!", item2); //$NON-NLS-1$

		assertNotNull("Unexpected value 'null' for item2.getTargetPath()!", item2.getTargetPath()); //$NON-NLS-1$
		assertEquals("Invalid target path!", "/root/cobble.out", item2.getTargetPath().toString()); //$NON-NLS-1$ //$NON-NLS-2$
		assertNotNull("Unexpected value 'null' for item2.getHostPath()!", item2.getHostPath()); //$NON-NLS-1$
		assertEquals("Invalid host path!", "/folk/uwe/tmp/cobble.out", item2.getHostPath().toString()); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unexpected value 'false' for item2.isEnabled()!",item2.isEnabled()); //$NON-NLS-1$
		assertEquals("Unexpected return value:", IFileTransferItem.HOST_TO_TARGET, item2.getDirection()); //$NON-NLS-1$
		assertNull("Unexpected value 'non-null' for item.getOptions()", item2.getOptions()); //$NON-NLS-1$
		assertFalse("Hash codes should not match!", item1.hashCode() == item2.hashCode()); //$NON-NLS-1$
		assertFalse("Items should not be equal!", item1.equals(item2)); //$NON-NLS-1$
		assertTrue("Items should be equal!", item2.equals(item2)); //$NON-NLS-1$
	}

	//***** END SECTION: Single test methods *****

}
