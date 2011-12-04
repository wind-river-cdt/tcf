/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.stepper;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.tcf.te.runtime.stepper.StepperManager;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IContextStep;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepper;
import org.eclipse.tcf.te.tests.CoreTestCase;

/**
 * Stepper engine test cases.
 */
public class StepperTestCase extends CoreTestCase {

	/**
	 * Provides a test suite to the caller which combines all single
	 * test bundled within this category.
	 *
	 * @return Test suite containing all test for this test category.
	 */
	public static Test getTestSuite() {
		TestSuite testSuite = new TestSuite("Test stepper engine"); //$NON-NLS-1$

			// add ourself to the test suite
			testSuite.addTestSuite(StepperTestCase.class);

		return testSuite;
	}

	/**
	 * Test the stepper extension point mechanism.
	 */
	public void testStepperContributions() {
		assertNotNull("Unexpected return value 'null'.", StepperManager.getInstance()); //$NON-NLS-1$
		assertNotNull("Unexpected return value 'null'.", StepperManager.getInstance().getStepperExtManager()); //$NON-NLS-1$

		// Lookup the default multi-context and single-context stepper contributions
		boolean multiContext = false;
		boolean singleContext = false;

		IStepper[] steppers = StepperManager.getInstance().getStepperExtManager().getStepper(false);
		for (IStepper stepper : steppers) {
			if (stepper.getId().equals("org.eclipse.tcf.te.runtime.stepper.multiContext")) { //$NON-NLS-1$
				multiContext = true;
			}
			if (stepper.getId().equals("org.eclipse.tcf.te.runtime.stepper.singleContext")) { //$NON-NLS-1$
				singleContext = true;
			}
			if (multiContext && singleContext) break;
		}
		assertTrue("Default multi context stepper contribution not found.", multiContext); //$NON-NLS-1$
		assertTrue("Default single context stepper contribution not found.", singleContext); //$NON-NLS-1$
	}

	public void testStepContributions() {
		assertNotNull("Unexpected return value 'null'.", StepperManager.getInstance()); //$NON-NLS-1$
		assertNotNull("Unexpected return value 'null'.", StepperManager.getInstance().getStepExtManager()); //$NON-NLS-1$

		IContextStep[] steps = StepperManager.getInstance().getStepExtManager().getSteps(false);
		int testStepCount = 0;

		for (IContextStep step : steps) {
			if (step.getId().startsWith("org.eclipse.tcf.te.tests.stepper.step")) { //$NON-NLS-1$
				testStepCount++;
			} else {
				continue;
			}

			if (step.getId().endsWith(".step1")) { //$NON-NLS-1$
				assertEquals("Unexpected step label found.", "Test Step 1", step.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
				assertEquals("Unexpected step description found.", "", step.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
				assertTrue("Unexpected step class type found.", step instanceof TestStep); //$NON-NLS-1$
				assertEquals("Unexpected number of dependencies found.", 0, step.getDependencies().length); //$NON-NLS-1$
			}

			if (step.getId().endsWith(".step2")) { //$NON-NLS-1$
				assertEquals("Unexpected step label found.", "Test Step 2", step.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
				assertEquals("Unexpected step description found.", "", step.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
				assertTrue("Unexpected step class type found.", step instanceof ParameterizedTestStep); //$NON-NLS-1$

				Map<?,?> params = ((ParameterizedTestStep)step).params;
				assertNotNull("Unexpected value 'null'.", params); //$NON-NLS-1$
				assertEquals("Unexpected number of parameter found.", 1, params.keySet().size()); //$NON-NLS-1$
				assertTrue("Missing expected key 'param1'.", params.containsKey("param1")); //$NON-NLS-1$ //$NON-NLS-2$
				assertTrue("Missing expected value 'value1'.", params.containsValue("value1")); //$NON-NLS-1$ //$NON-NLS-2$

				assertEquals("Unexpected number of dependencies found.", 0, step.getDependencies().length); //$NON-NLS-1$
			}

			if (step.getId().endsWith(".step3")) { //$NON-NLS-1$
				assertEquals("Unexpected step label found.", "Test Step 3", step.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
				assertEquals("Unexpected step description found.", "Just another test step", step.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
				assertTrue("Unexpected step class type found.", step instanceof TestStep); //$NON-NLS-1$
				assertEquals("Unexpected number of dependencies found.", 0, step.getDependencies().length); //$NON-NLS-1$
			}

			if (step.getId().endsWith(".step4")) { //$NON-NLS-1$
				assertEquals("Unexpected step label found.", "Test Step 4", step.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
				assertEquals("Unexpected step description found.", "", step.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
				assertTrue("Unexpected step class type found.", step instanceof TestStep); //$NON-NLS-1$
				assertEquals("Unexpected number of dependencies found.", 1, step.getDependencies().length); //$NON-NLS-1$
			}

		}

		assertEquals("Unexpected number of test steps found.", 4, testStepCount); //$NON-NLS-1$

	}

}
