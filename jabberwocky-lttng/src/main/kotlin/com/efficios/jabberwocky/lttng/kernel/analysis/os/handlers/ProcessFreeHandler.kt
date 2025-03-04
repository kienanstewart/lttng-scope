/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.analysis.os.handlers

import ca.polymtl.dorsal.libdelorean.IStateSystemWriter
import com.efficios.jabberwocky.lttng.kernel.analysis.os.Attributes
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngKernelEventLayout
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue
import com.efficios.jabberwocky.trace.event.TraceEvent

class ProcessFreeHandler(layout: LttngKernelEventLayout) : KernelEventHandler(layout) {

    override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent) {
        val cpu = event.cpu
        val tid = (event.fields[layout.fieldTid] as IntegerValue).value.toInt()

        val threadAttributeName = Attributes.buildThreadAttributeName(tid, cpu) ?: return

        /*
         * Remove the process and all its sub-attributes from the current state
         */
        ss.getQuarkRelativeAndAdd(ss.getNodeThreads(), threadAttributeName)
                .let { ss.removeAttribute(event.timestamp, it) }
    }
}
