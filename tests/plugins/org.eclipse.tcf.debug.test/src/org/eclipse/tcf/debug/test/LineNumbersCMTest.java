/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import org.eclipse.tcf.debug.test.util.Transaction;
import org.eclipse.tcf.services.ILineNumbers.CodeArea;
import org.junit.Assert;

public class LineNumbersCMTest extends AbstractCMTest {

    public void testLineNumbersCMResetOnContextRemove() throws Exception {
        final TestProcessInfo processInfo = startProcess("tcf_test_func0");

        // Retrieve the current PC for use later
        final String pc = new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                return validate(fRunControlCM.getState(processInfo.fThreadId)).pc;
            }
        }.get();

        final BigInteger pcNumber = new BigInteger(pc);
        final BigInteger pcNumberPlusOne = pcNumber.add(BigInteger.valueOf(1));

        // Retrieve the line number for current PC.
        final CodeArea[] pcCodeAreas = new Transaction<CodeArea[]>() {
            @Override
            protected CodeArea[] process() throws InvalidCacheException, ExecutionException {
                CodeArea[] areas = validate(fLineNumbersCM.mapToSource(processInfo.fProcessId, pcNumber, pcNumberPlusOne));
                Assert.assertNotNull(areas);
                Assert.assertTrue(areas.length != 0);

                areas = validate(fLineNumbersCM.mapToSource(processInfo.fThreadId, pcNumber, pcNumberPlusOne));
                Assert.assertNotNull(areas);
                Assert.assertTrue(areas.length != 0);

                CodeArea[] areas2 = validate(fLineNumbersCM.mapToMemory(processInfo.fProcessId, areas[0].file, areas[0].start_line, areas[0].start_column));
                Assert.assertNotNull(areas2);
                Assert.assertTrue(areas2.length != 0);

                areas2 = validate(fLineNumbersCM.mapToMemory(processInfo.fThreadId, areas[0].file, areas[0].start_line, areas[0].start_column));
                Assert.assertNotNull(areas2);
                Assert.assertTrue(areas2.length != 0);

                return areas;
            }
        }.get();

        // End test, check that all caches were reset and now return an error.
        new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                validate( fDiagnosticsCM.cancelTest(processInfo.fTestId, this) );
                validate( fRunControlCM.waitForContextRemoved(processInfo.fProcessId, this) );
                try {
                    validate(fLineNumbersCM.mapToSource(processInfo.fProcessId, pcNumber, pcNumberPlusOne));
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    validate(fLineNumbersCM.mapToSource(processInfo.fThreadId, pcNumber, pcNumberPlusOne));
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    CodeArea[] areas3 = validate(fLineNumbersCM.mapToMemory(processInfo.fProcessId, pcCodeAreas[0].file, pcCodeAreas[0].start_line, pcCodeAreas[0].start_column));
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    validate(fLineNumbersCM.mapToMemory(processInfo.fThreadId, pcCodeAreas[0].file, pcCodeAreas[0].start_line, pcCodeAreas[0].start_column));
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}

                return null;
            }
        }.get();
    }

}
