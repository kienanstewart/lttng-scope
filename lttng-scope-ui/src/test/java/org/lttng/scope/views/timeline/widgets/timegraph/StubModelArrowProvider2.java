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
import com.efficios.jabberwocky.views.common.ColorDefinition;
import com.efficios.jabberwocky.views.timegraph.model.provider.arrows.TimeGraphModelArrowProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.TimeGraphEvent;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrow;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrowRender;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrowSeries;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrowSeries.LineStyle;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.FutureTask;

class StubModelArrowProvider2 extends TimeGraphModelArrowProvider {

    public static final String SERIES_NAME = "Green";

    private static final TimeGraphArrowSeries ARROW_SERIES = new TimeGraphArrowSeries(
            SERIES_NAME,
            new ColorDefinition(0, 255, 0, ColorDefinition.MAX),
            LineStyle.FULL);

    public StubModelArrowProvider2() {
        super(ARROW_SERIES);
    }

    @Override
    public TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender, TimeRange timeRange, @Nullable FutureTask<?> task) {
        TimeGraphArrowSeries series = getArrowSeries();
        List<TimeGraphTreeElement> treeElems = treeRender.getAllTreeElements();

        /* Draw 2 arrows total */
        TimeGraphEvent startEvent = new TimeGraphEvent(ts(timeRange, 0.3), treeElems.get(6));
        TimeGraphEvent endEvent = new TimeGraphEvent(ts(timeRange, 0.8), treeElems.get(4));
        TimeGraphArrow arrow1 = new TimeGraphArrow(startEvent, endEvent, series);

        startEvent = new TimeGraphEvent(ts(timeRange, 0.5), treeElems.get(10));
        endEvent = new TimeGraphEvent(ts(timeRange, 0.6), treeElems.get(7));
        TimeGraphArrow arrow2 = new TimeGraphArrow(startEvent, endEvent, series);

        List<TimeGraphArrow> arrows = ImmutableList.of(arrow1, arrow2);
        return new TimeGraphArrowRender(timeRange, arrows);
    }

    private static long ts(TimeRange range, double ratio) {
        return (long) (range.getDuration() * ratio + range.getStartTime());
    }

}
