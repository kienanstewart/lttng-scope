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
import com.efficios.jabberwocky.views.timegraph.model.render.LineThickness;
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateInterval;
import com.google.common.base.MoreObjects;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lttng.scope.common.jfx.CountingGridPane;
import org.lttng.scope.common.jfx.JfxColorFactory;
import org.lttng.scope.views.timeline.DebugOptions;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * {@link Rectangle} object used to draw states in the timegraph. It attaches
 * the {@link TimeGraphStateInterval} that represents this state.
 *
 * @author Alexandre Montplaisir
 */
public class StateRectangle extends Rectangle {

    private final TimeGraphWidget fWidget;
    private final int fEntryIndex;
    private final TimeGraphStateInterval fInterval;

    private transient @Nullable Paint fBaseColor;
    private transient @Nullable Paint fSelectedColor;

    private @Nullable Tooltip fTooltip = null;

    /**
     * Constructor
     *
     * @param viewer
     *            The viewer in which the rectangle will be placed
     * @param interval
     *            The source interval model object
     * @param entryIndex
     *            The index of the entry to which this state belongs.
     */
    public StateRectangle(TimeGraphWidget viewer, TimeGraphStateInterval interval, int entryIndex) {
        fWidget = viewer;
        fEntryIndex = entryIndex;
        fInterval = interval;

        /*
         * It is possible, especially when re-opening already-indexed traces,
         * that the indexer and the state system do not report the same
         * start/end times. Make sure to clamp the interval's bounds to the
         * valid values.
         */
        TimeRange traceRange = viewer.getControl().getViewContext().getCurrentProjectFullRange();
        long traceStart = traceRange.getStartTime();
        long intervalStart = interval.getStartTime();
        double xStart = viewer.timestampToPaneXPos(Math.max(traceStart, intervalStart));

        long traceEnd = traceRange.getEndTime();
        long intervalEndTime = interval.getEndTime();
        double xEnd = viewer.timestampToPaneXPos(Math.min(traceEnd, intervalEndTime));

        double width = Math.max(1.0, xEnd - xStart) + 1.0;
        double height = getHeightFromThickness(interval.getLineThickness().get());
        double y = computeY(height);

        setX(0);
        setLayoutX(xStart);
        setY(y);
        setWidth(width);
        setHeight(height);

        double opacity = viewer.getDebugOptions().getStateIntervalOpacity().get();
        setOpacity(opacity);

        updatePaint();

        /* Set initial selection state and selection listener. */
        if (this.equals(viewer.getSelectedState())) {
            setSelected(true);
            viewer.setSelectedState(this, false);
        } else {
            setSelected(false);
        }

        setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) {
                return;
            }
            viewer.setSelectedState(this, true);
        });

        /*
         * Initialize the tooltip when the mouse enters the rectangle, if it was
         * not done previously.
         */
        setOnMouseEntered(e -> generateTooltip());
    }

    private void generateTooltip() {
        if (fTooltip != null) {
            return;
        }
        TooltipContents ttContents = new TooltipContents(fWidget.getDebugOptions());
        ttContents.addTooltipRow(Messages.statePropertyElement, fInterval.getTreeElement().getName());
        ttContents.addTooltipRow(Messages.statePropertyStateName, fInterval.getStateName());
        ttContents.addTooltipRow(Messages.statePropertyStartTime, fInterval.getStartTime());
        ttContents.addTooltipRow(Messages.statePropertyEndTime, fInterval.getEndTime());
        ttContents.addTooltipRow(Messages.statePropertyDuration, fInterval.getDuration() + " ns"); //$NON-NLS-1$
        /* Add rows corresponding to the properties from the interval */
        Map<String, String> properties = fInterval.getProperties();
        properties.forEach((k, v) -> ttContents.addTooltipRow(k, v));

        Tooltip tt = new Tooltip();
        tt.setGraphic(ttContents);
        Tooltip.install(this, tt);
        fTooltip = tt;
    }

    /**
     * Return the model interval representing this state
     *
     * @return The interval model object
     */
    public TimeGraphStateInterval getStateInterval() {
        return fInterval;
    }

    public void updatePaint() {
        /* Update the color */
        /* Set a special paint for multi-state intervals */
        if (fInterval.isMultiState()) {
            Paint multiStatePaint = fWidget.getDebugOptions().getMultiStatePaint().get();
            fBaseColor = multiStatePaint;
            fSelectedColor = multiStatePaint;
        } else {
            fBaseColor = JfxColorFactory.getColorFromDef(fInterval.getColorDefinition().get());
            fSelectedColor = JfxColorFactory.getDerivedColorFromDef(fInterval.getColorDefinition().get());
        }
        setFill(fBaseColor);

        /* Update the line thickness */
        LineThickness lt = fInterval.getLineThickness().get();
        double height = getHeightFromThickness(lt);
        setHeight(height);
        /* We need to adjust the y position too */
        setY(computeY(height));
    }

    /**
     * Compute the Y property (the Y position of the *top* of the rectangle)
     * this rectangle should have on its pane. This takes into consideration the
     * entry it belongs to, as well as its target height.
     *
     * For example, if the line thickness of the rectangle changes, its Y has to
     * be recomputed so that the rectangle remains centered on its entry line.
     *
     * This method does not change the yProperty of the rectangle.
     */
    private double computeY(double height) {
        double yOffset = (TimeGraphWidget.ENTRY_HEIGHT - height) / 2;
        double y = fEntryIndex * TimeGraphWidget.ENTRY_HEIGHT + yOffset;
        return y;
    }

    public void setSelected(boolean isSelected) {
        if (isSelected) {
            setFill(fSelectedColor);
        } else {
            setFill(fBaseColor);
            hideTooltip();
        }
    }

    public void showTooltip(boolean beginning) {
        generateTooltip();
        Tooltip tt = requireNonNull(fTooltip);

        /*
         * Show the tooltip first, then move it to the correct location. It
         * needs to be shown for its getWidth() etc. to be populated.
         */
        tt.show(this, 0, 0);

        Point2D position;
        if (beginning) {
            /* Align to the bottom-left of the rectangle, left-aligned. */
            /* Yes, it needs to be getX() here (0), not getLayoutX(). */
            position = this.localToScreen(getX(), getY() + getHeight());
        } else {
            /* Align to the bottom-right of the rectangle, right-aligned */
            position = this.localToScreen(getX() + getWidth() - tt.getWidth(), getY() + getHeight());
        }

        tt.setAnchorX(position.getX());
        tt.setAnchorY(position.getY());
    }

    public void hideTooltip() {
        Tooltip tt = fTooltip;
        if (tt != null) {
            Platform.runLater(() -> {
                tt.hide();
            });
        }
    }

    @Override
    protected void finalize() {
        hideTooltip();
    }

    public static double getHeightFromThickness(LineThickness lt) {
        switch (lt) {
        case NORMAL:
        default:
            return TimeGraphWidget.ENTRY_HEIGHT - 4;
        case SMALL:
            return TimeGraphWidget.ENTRY_HEIGHT - 8;
        case TINY:
            return TimeGraphWidget.ENTRY_HEIGHT - 12;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(fWidget, fInterval);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StateRectangle other = (StateRectangle) obj;
        return Objects.equals(fWidget, other.fWidget)
                && Objects.equals(fInterval, other.fInterval);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("interval", fInterval) //$NON-NLS-1$
                .toString();
    }

    private static class TooltipContents extends CountingGridPane {

        private final DebugOptions fOpts;

        public TooltipContents(DebugOptions opts) {
            fOpts = opts;
        }

        public void addTooltipRow(Object... objects) {
            Node[] labels = Arrays.stream(objects)
                    .map(Object::toString)
                    .map(Text::new)
                    .peek(text -> {
                        text.fontProperty().bind(fOpts.getToolTipFont());
                        text.fillProperty().bind(fOpts.getToolTipFontFill());
                    })
                    .toArray(Node[]::new);
            appendRow(labels);
        }
    }
}
