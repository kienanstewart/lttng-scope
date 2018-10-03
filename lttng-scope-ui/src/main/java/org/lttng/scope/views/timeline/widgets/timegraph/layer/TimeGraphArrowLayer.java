/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.layer;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.views.common.ColorDefinition;
import com.efficios.jabberwocky.views.timegraph.model.provider.arrows.TimeGraphModelArrowProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrowRender;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import com.google.common.collect.ImmutableMap;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.Paint;
import org.jetbrains.annotations.Nullable;
import org.lttng.scope.common.jfx.Arrow;
import org.lttng.scope.common.jfx.JfxColorFactory;
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget;
import org.lttng.scope.views.timeline.widgets.timegraph.VerticalPosition;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TimeGraphArrowLayer extends TimeGraphLayer {

    private final Map<TimeGraphModelArrowProvider, ArrowConfig> fArrowProvidersConfig;

    public TimeGraphArrowLayer(TimeGraphWidget widget, Group parentGroup) {
        super(widget, parentGroup);

        Collection<TimeGraphModelArrowProvider> arrowProviders =
                widget.getControl().getRenderProvider().getArrowProviders();

        fArrowProvidersConfig = arrowProviders.stream()
                .collect(ImmutableMap.toImmutableMap(
                        Function.identity(),
                        ap -> {
                            Group group = new Group();
                            ColorDefinition colorDef = ap.getArrowSeries().getColor();
                            Paint stroke = JfxColorFactory.getColorFromDef(colorDef);
                            return new ArrowConfig(group, stroke);
                        }));

        fArrowProvidersConfig.values().stream()
                .map(ArrowConfig::getGroup)
                .forEach(parentGroup.getChildren()::add);

        /*
         * Add listeners to the registered arrow providers. If providers become
         * enabled or disabled, we must repaint or hide the arrows from this
         * provider's series.
         */
        arrowProviders.forEach(ap -> {
            ap.enabledProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue) {
                    /*
                     * The provider is now enabled, we must fetch and display
                     * its arrows
                     */
                    TimeRange timeRange = getWidget().getViewContext().getVisibleTimeRange();
                    TimeGraphTreeRender treeRender = getWidget().getLatestTreeRender();
                    // FIXME Not using a task here, so this might end up running
                    // on the UI thread  and not being cancellable...
                    paintArrowsOfProvider(treeRender, timeRange, ap, null);
                } else {
                    /*
                     * The provider is now disabled, we must remove the existing
                     * arrows from this provider.
                     */
                    ArrowConfig config = fArrowProvidersConfig.get(ap);
                    if (config == null) {
                        return;
                    }
                    Platform.runLater(() -> {
                        config.getGroup().getChildren().clear();
                    });
                }
            });
        });
    }

    @Override
    public void drawContents(TimeGraphTreeRender treeRender, TimeRange timeRange,
            VerticalPosition vPos, @Nullable FutureTask<?> task) {
        fArrowProvidersConfig.keySet().stream()
                .filter(arrowProvider -> arrowProvider.enabledProperty().get())
                .forEach(arrowProvider -> paintArrowsOfProvider(treeRender, timeRange, arrowProvider, task));
    }

    @Override
    public void clear() {
        /*
         * Only clear the children's children, not our direct children which
         * could still be valid.
         */
        fArrowProvidersConfig.values().stream()
                .map(ArrowConfig::getGroup)
                .forEach(group -> group.getChildren().clear());
    }

    private void paintArrowsOfProvider(TimeGraphTreeRender treeRender, TimeRange timeRange,
            TimeGraphModelArrowProvider arrowProvider, @Nullable FutureTask<?> task) {
        ArrowConfig config = fArrowProvidersConfig.get(arrowProvider);
        if (config == null) {
            /* Should not happen... */
            return;
        }

        TimeGraphArrowRender arrowRender = arrowProvider.getArrowRender(treeRender, timeRange, task);
        Collection<Arrow> arrows = prepareArrows(treeRender, arrowRender, config.getStroke());

        Platform.runLater(() -> {
            config.getGroup().getChildren().clear();
            config.getGroup().getChildren().addAll(arrows);
        });
    }

    private Collection<Arrow> prepareArrows(TimeGraphTreeRender treeRender,
            TimeGraphArrowRender arrowRender, Paint arrowStroke) {
        final double entryHeight = TimeGraphWidget.ENTRY_HEIGHT;

        Collection<Arrow> arrows = arrowRender.getArrows().stream()
            .map(timeGraphArrow -> {
                TimeGraphTreeElement startTreeElem = timeGraphArrow.getStartEvent().getTreeElement();
                TimeGraphTreeElement endTreeElem = timeGraphArrow.getEndEvent().getTreeElement();
                long startTimestamp = timeGraphArrow.getStartEvent().getTimestamp();
                long endTimestamp = timeGraphArrow.getEndEvent().getTimestamp();
                // FIXME Build and use a hashmap instead for indexes
                int startIndex = treeRender.getAllTreeElements().indexOf(startTreeElem);
                int endIndex = treeRender.getAllTreeElements().indexOf(endTreeElem);
                if (startIndex == -1 || endIndex == -1) {
                    /* We shouldn't have received this... */
                    return null;
                }

                double startX = getWidget().timestampToPaneXPos(startTimestamp);
                double endX = getWidget().timestampToPaneXPos(endTimestamp);
                double startY = startIndex * entryHeight + entryHeight / 2;
                double endY = endIndex * entryHeight + entryHeight / 2;

                Arrow arrow = new Arrow(startX, startY, endX, endY);
                arrow.setStroke(arrowStroke);
                return arrow;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return arrows;
    }

    public synchronized Collection<Arrow> getRenderedArrows() {
        /*
         * Retrieve the rendered arrows of each group, and flatten them into a
         * single collection.
         */
        return fArrowProvidersConfig.values().stream()
                .map(ArrowConfig::getGroup)
                .map(Group::getChildren)
                .flatMap(Collection::stream)
                .map(node -> (Arrow) node)
                .collect(Collectors.toList());
    }

    private static class ArrowConfig {

        private final Group fGroup;
        private final Paint fStroke;

        public ArrowConfig(Group group, Paint stroke) {
            fGroup = group;
            fStroke = stroke;
        }

        public Group getGroup() {
            return fGroup;
        }

        public Paint getStroke() {
            return fStroke;
        }
    }
}
