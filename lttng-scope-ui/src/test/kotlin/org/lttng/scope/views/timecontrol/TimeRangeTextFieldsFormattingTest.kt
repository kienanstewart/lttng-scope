/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timecontrol

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.tests.JavaFXTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.lttng.scope.application.ScopeOptions
import org.lttng.scope.common.TimestampFormat
import java.time.ZoneId

class TimeRangeTextFieldsFormattingTest : JavaFXTestBase() {

    companion object {
        private const val PROJECT_START = 1331668247_314038062 // 2012-03-13 19:50:47.314038062
        private const val PROJECT_END = 1331668259_054285979 // 2012-03-13 15:50:59.054285979

        private const val INITIAL_START = 1331668247_425034591 // 2012-03-13 19:50:47.425034591
        private const val INITIAL_END = 1331668249_647057621 // 2012-03-13 19:50:49.647057621
    }

    private val fixture = TimeRangeTextFields(TimeRange.of(PROJECT_START, PROJECT_END), null)


    @BeforeEach
    fun setup() {
        TimestampFormat.systemTimeZone = ZoneId.of("EST", ZoneId.SHORT_IDS)
        fixture.timeRange = TimeRange.of(INITIAL_START, INITIAL_END)
    }

    /**
     * Test that the field contents update appropriately whenever the configured
     * timestamp formatting option is changed.
     */
    @Test
    fun testFormattingChange() {
        ScopeOptions.timestampTimeZone = ScopeOptions.DisplayTimeZone.UTC
        TimestampFormat.values().forEach {
            ScopeOptions.timestampFormat = it
            when (it) {
                TimestampFormat.YMD_HMS_N_TZ -> {
                    assertEquals("2012-03-13 19:50:47.425034591 +00:00", fixture.startTextField.text)
                    assertEquals("2012-03-13 19:50:49.647057621 +00:00", fixture.endTextField.text)
                    assertEquals("2.222023030", fixture.durationTextField.text)
                }
                TimestampFormat.YMD_HMS_N -> {
                    assertEquals("2012-03-13 19:50:47.425034591", fixture.startTextField.text)
                    assertEquals("2012-03-13 19:50:49.647057621", fixture.endTextField.text)
                    assertEquals("2.222023030", fixture.durationTextField.text)
                }

                TimestampFormat.HMS_N -> {
                    assertEquals("19:50:47.425034591", fixture.startTextField.text)
                    assertEquals("19:50:49.647057621", fixture.endTextField.text)
                    assertEquals("2.222023030", fixture.durationTextField.text)
                }

                TimestampFormat.SECONDS_POINT_NANOS -> {
                    assertEquals("1331668247.425034591", fixture.startTextField.text)
                    assertEquals("1331668249.647057621", fixture.endTextField.text)
                    assertEquals("2.222023030", fixture.durationTextField.text)
                }
            }
        }

        ScopeOptions.timestampTimeZone = ScopeOptions.DisplayTimeZone.LOCAL
        TimestampFormat.values().forEach {
            ScopeOptions.timestampFormat = it
            when (it) {
                TimestampFormat.YMD_HMS_N_TZ -> {
                    assertEquals("2012-03-13 14:50:47.425034591 -05:00", fixture.startTextField.text)
                    assertEquals("2012-03-13 14:50:49.647057621 -05:00", fixture.endTextField.text)
                    assertEquals("2.222023030", fixture.durationTextField.text)
                }
                TimestampFormat.YMD_HMS_N -> {
                    assertEquals("2012-03-13 14:50:47.425034591", fixture.startTextField.text)
                    assertEquals("2012-03-13 14:50:49.647057621", fixture.endTextField.text)
                    assertEquals("2.222023030", fixture.durationTextField.text)
                }

                TimestampFormat.HMS_N -> {
                    assertEquals("14:50:47.425034591", fixture.startTextField.text)
                    assertEquals("14:50:49.647057621", fixture.endTextField.text)
                    assertEquals("2.222023030", fixture.durationTextField.text)
                }

                TimestampFormat.SECONDS_POINT_NANOS -> {
                    assertEquals("1331668247.425034591", fixture.startTextField.text)
                    assertEquals("1331668249.647057621", fixture.endTextField.text)
                    assertEquals("2.222023030", fixture.durationTextField.text)
                }
            }
        }
    }

    @Test
    fun testDisplayTimeZoneChange() {
        ScopeOptions.timestampFormat = TimestampFormat.YMD_HMS_N

        ScopeOptions.timestampTimeZone = ScopeOptions.DisplayTimeZone.UTC
        assertEquals("2012-03-13 19:50:47.425034591", fixture.startTextField.text)
        assertEquals("2012-03-13 19:50:49.647057621", fixture.endTextField.text)
        assertEquals("2.222023030", fixture.durationTextField.text)

        ScopeOptions.timestampTimeZone = ScopeOptions.DisplayTimeZone.LOCAL
        assertEquals("2012-03-13 14:50:47.425034591", fixture.startTextField.text)
        assertEquals("2012-03-13 14:50:49.647057621", fixture.endTextField.text)
        assertEquals("2.222023030", fixture.durationTextField.text)
    }
}
