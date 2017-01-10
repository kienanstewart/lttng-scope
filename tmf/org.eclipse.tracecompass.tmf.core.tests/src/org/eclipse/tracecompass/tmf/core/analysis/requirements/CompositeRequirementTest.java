/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.requirements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement.PriorityLevel;
import org.eclipse.tracecompass.tmf.core.tests.stubs.trace.TmfTraceStub;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test the {@link TmfCompositeAnalysisRequirement} class
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault({})
public class CompositeRequirementTest {

    private static final @NonNull TmfAbstractAnalysisRequirement FALSE_REQ1 = new TmfAbstractAnalysisRequirement(Collections.EMPTY_SET, PriorityLevel.MANDATORY) {
        @Override
        public boolean test(ITmfTrace trace) {
            return false;
        }
    };

    private static final @NonNull TmfAbstractAnalysisRequirement FALSE_REQ2 = new TmfAbstractAnalysisRequirement(Collections.EMPTY_SET, PriorityLevel.MANDATORY) {
        @Override
        public boolean test(ITmfTrace trace) {
            return false;
        }
    };

    private static final @NonNull TmfAbstractAnalysisRequirement TRUE_REQ1 = new TmfAbstractAnalysisRequirement(Collections.EMPTY_SET, PriorityLevel.MANDATORY) {
        @Override
        public boolean test(ITmfTrace trace) {
            return true;
        }
    };

    private static final @NonNull TmfAbstractAnalysisRequirement TRUE_REQ2 = new TmfAbstractAnalysisRequirement(Collections.EMPTY_SET, PriorityLevel.MANDATORY) {
        @Override
        public boolean test(ITmfTrace trace) {
            return true;
        }
    };

    private ITmfTrace fTrace;

    /**
     * Setup a trace to be used in tests
     */
    @Before
    public void setupTrace() {
        fTrace = new TmfTraceStub();
    }

    /**
     * Clean up
     */
    @After
    public void cleanup() {
        if (fTrace != null) {
            fTrace.dispose();
        }
    }

    /**
     * Test composite requirement with {@link PriorityLevel#MANDATORY} level
     */
    @Test
    public void testMandatory() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        TmfAbstractAnalysisRequirement req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(TRUE_REQ1), PriorityLevel.MANDATORY);
        assertTrue(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(TRUE_REQ1, TRUE_REQ2), PriorityLevel.MANDATORY);
        assertTrue(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(FALSE_REQ1), PriorityLevel.MANDATORY);
        assertFalse(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(FALSE_REQ1, TRUE_REQ1), PriorityLevel.MANDATORY);
        assertFalse(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(FALSE_REQ1, FALSE_REQ2), PriorityLevel.MANDATORY);
        assertFalse(req.test(trace));
    }

    /**
     * Test composite requirement with {@link PriorityLevel#AT_LEAST_ONE} level
     */
    @Test
    public void testAtLeastOne() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        TmfAbstractAnalysisRequirement req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(TRUE_REQ1), PriorityLevel.AT_LEAST_ONE);
        assertTrue(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(TRUE_REQ1, TRUE_REQ2), PriorityLevel.AT_LEAST_ONE);
        assertTrue(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(FALSE_REQ1), PriorityLevel.AT_LEAST_ONE);
        assertFalse(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(FALSE_REQ1, TRUE_REQ1), PriorityLevel.AT_LEAST_ONE);
        assertTrue(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(FALSE_REQ1, FALSE_REQ2), PriorityLevel.AT_LEAST_ONE);
        assertFalse(req.test(trace));
    }

    /**
     * Test composite requirement with {@link PriorityLevel#ALL_OR_NOTHING} level
     */
    @Test
    public void testAllOrNothing() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        TmfAbstractAnalysisRequirement req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(TRUE_REQ1), PriorityLevel.ALL_OR_NOTHING);
        assertTrue(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(TRUE_REQ1, TRUE_REQ2), PriorityLevel.ALL_OR_NOTHING);
        assertTrue(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(FALSE_REQ1), PriorityLevel.ALL_OR_NOTHING);
        assertTrue(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(FALSE_REQ1, TRUE_REQ1), PriorityLevel.ALL_OR_NOTHING);
        assertFalse(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(FALSE_REQ1, FALSE_REQ2), PriorityLevel.ALL_OR_NOTHING);
        assertTrue(req.test(trace));
    }

    /**
     * Test composite requirement with {@link PriorityLevel#OPTIONAL} level
     */
    @Test
    public void testOptional() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        TmfAbstractAnalysisRequirement req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(TRUE_REQ1), PriorityLevel.OPTIONAL);
        assertTrue(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(TRUE_REQ1, TRUE_REQ2), PriorityLevel.OPTIONAL);
        assertTrue(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(FALSE_REQ1), PriorityLevel.OPTIONAL);
        assertTrue(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(FALSE_REQ1, TRUE_REQ1), PriorityLevel.OPTIONAL);
        assertTrue(req.test(trace));

        req = new TmfCompositeAnalysisRequirement(ImmutableSet.of(FALSE_REQ1, FALSE_REQ2), PriorityLevel.OPTIONAL);
        assertTrue(req.test(trace));
    }

}
