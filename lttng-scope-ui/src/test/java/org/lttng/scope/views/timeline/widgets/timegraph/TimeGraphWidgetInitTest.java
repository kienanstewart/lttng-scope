/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph;

import com.efficios.jabberwocky.common.TimeRange;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.lttng.scope.common.tests.StubTrace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link TimeGraphWidget} test checking the initial conditions.
 */
@Disabled("Needs reimplementation in proper testing framework")
class TimeGraphWidgetInitTest extends TimeGraphWidgetTestBase {

    /**
     * Test that the initial visible position of both the control and the view
     * are as expected by the trace's configuration.
     */
    @Test
    void testInitialPosition() {
        repaint();
        TimeGraphWidget viewer = getWidget();

        final long expectedStart = StubTrace.FULL_TRACE_START_TIME;
        final long expectedEnd = StubTrace.FULL_TRACE_END_TIME;

        /* Check the control */
        TimeRange visibleRange = viewer.getControl().getViewContext().getVisibleTimeRange();
        assertEquals(expectedStart, visibleRange.getStartTime());
        assertEquals(expectedEnd, visibleRange.getEndTime());

        /* Check the view itself */
        TimeRange timeRange = viewer.getTimeGraphEdgeTimestamps(null);
        assertEquals(expectedStart, timeRange.getStartTime());
        /*
         * The timegraph may not show the whole range if the window is small.
         * In any case, its end time should never be greater than the trace's end time.
         */
        assertThat(timeRange.getEndTime()).isLessThanOrEqualTo(expectedEnd);
    }
}
