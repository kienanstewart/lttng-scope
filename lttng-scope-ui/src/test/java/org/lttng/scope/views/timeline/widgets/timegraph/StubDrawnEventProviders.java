/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph;

import com.efficios.jabberwocky.common.ConfigOption;
import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.views.common.ColorDefinition;
import com.efficios.jabberwocky.views.common.EventSymbolStyle;
import com.efficios.jabberwocky.views.timegraph.model.provider.drawnevents.TimeGraphDrawnEventProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.TimeGraphEvent;
import com.efficios.jabberwocky.views.timegraph.model.render.drawnevents.TimeGraphDrawnEvent;
import com.efficios.jabberwocky.views.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import com.efficios.jabberwocky.views.timegraph.model.render.drawnevents.TimeGraphDrawnEventSeries;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.stream.DoubleStream;

final class StubDrawnEventProviders {

    private StubDrawnEventProviders() {}

    public static class StubDrawnEventProvider1 extends TimeGraphDrawnEventProvider {

        public static final int NB_SYMBOLS = 20;

        private static final TimeGraphDrawnEventSeries EVENT_SERIES = new TimeGraphDrawnEventSeries(
                "Event Series Alpha",
                new ConfigOption<>(new ColorDefinition(0, 255, 0, ColorDefinition.MAX)),
                new ConfigOption<>(EventSymbolStyle.CIRCLE));

        public StubDrawnEventProvider1() {
            super(EVENT_SERIES);
        }

        @Override
        public @NotNull TimeGraphDrawnEventRender getEventRender(TimeGraphTreeRender treeRender, TimeRange timeRange, @Nullable FutureTask<?> task) {
            TimeGraphDrawnEventSeries series = getDrawnEventSeries();

            List<TimeGraphDrawnEvent> events = treeRender.getAllTreeElements().stream()
                    .filter(treeElem -> treeElem != StubModelProvider.ROOT_ELEMENT)
                    /* Keep only entries (2, 4, 6, 8) */
                    .filter(treeElem -> getIndexOfTreeElement(treeElem) < 10)
                    .filter(treeElem -> getIndexOfTreeElement(treeElem) % 2 == 0)

                    /* Draw symbols at positions (.1, .3, .5, .7, .9) */
                    .flatMap(treeElem -> DoubleStream.iterate(0.1, i -> i + 0.2).limit(5)
                            .mapToObj(i -> new TimeGraphEvent(ts(timeRange, i), treeElem)))
                    .map(event -> new TimeGraphDrawnEvent(event, series, null))
                    .collect(ImmutableList.toImmutableList());

            /* There should be 20 symbols total */
            return new TimeGraphDrawnEventRender(timeRange, events);
        }
    }

    public static class StubDrawnEventProvider2 extends TimeGraphDrawnEventProvider {

        public static final int NB_SYMBOLS = 16;

        private static final TimeGraphDrawnEventSeries EVENT_SERIES = new TimeGraphDrawnEventSeries(
                "Event Series Zeta",
                new ConfigOption<>(new ColorDefinition(255, 255, 0, ColorDefinition.MAX)),
                new ConfigOption<>(EventSymbolStyle.CROSS));

        public StubDrawnEventProvider2() {
            super(EVENT_SERIES);
        }

        @Override
        public @NotNull TimeGraphDrawnEventRender getEventRender(TimeGraphTreeRender treeRender, TimeRange timeRange, @Nullable FutureTask<?> task) {
            TimeGraphDrawnEventSeries series = getDrawnEventSeries();

            List<TimeGraphDrawnEvent> events = treeRender.getAllTreeElements().stream()
                    .filter(treeElem -> treeElem != StubModelProvider.ROOT_ELEMENT)
                    /* Keep only entries (1, 3, 5, 7) */
                    .filter(treeElem -> getIndexOfTreeElement(treeElem) < 8)
                    .filter(treeElem -> (getIndexOfTreeElement(treeElem) + 1) % 2 == 0)

                    /* Draw symbols at positions (.2, .4, .6, .8) */
                    .flatMap(treeElem -> DoubleStream.iterate(0.2, i -> i + 0.2).limit(4)
                            .mapToObj(i -> new TimeGraphEvent(ts(timeRange, i), treeElem)))
                    .map(event -> new TimeGraphDrawnEvent(event, series, null))
                    .collect(ImmutableList.toImmutableList());

            /* There should be 16 symbols total */
            return new TimeGraphDrawnEventRender(timeRange, events);
        }
    }

    private static int getIndexOfTreeElement(TimeGraphTreeElement treeElem) {
        /* The "dummy" element is not part of the time graph, so say -1 */
        if (treeElem == TimeGraphTreeElement.DUMMY_ELEMENT) {
            return -1;
        }
        String nb = treeElem.getName().substring("Entry #".length());
        return Integer.parseInt(nb);
    }

    private static long ts(TimeRange range, double ratio) {
        return (long) (range.getDuration() * ratio + range.getStartTime());
    }
}
