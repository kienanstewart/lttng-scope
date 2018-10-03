/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.xychart.layer

import com.efficios.jabberwocky.common.TimeRange
import javafx.event.EventHandler
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineCap
import org.lttng.scope.views.timeline.widgets.xychart.XYChartFullRangeWidget
import org.lttng.scope.views.timeline.widgets.xychart.XYChartVisibleRangeWidget
import org.lttng.scope.views.timeline.widgets.xychart.XYChartWidget

abstract class XYChartSelectionLayer(protected val widget: XYChartWidget, protected val chartBackgroundAdjustment: Double) : Pane() {

    companion object {
        /* Style settings. TODO Move to debug options? */
        private const val SELECTION_STROKE_WIDTH = 1.0
        private val SELECTION_STROKE_COLOR = Color.BLUE
        private val SELECTION_FILL_COLOR = Color.LIGHTBLUE.deriveColor(0.0, 1.2, 1.0, 0.4)

        /**
         * Factory method
         */
        fun build(widget: XYChartWidget, chartBackgroundAdjustment: Double): XYChartSelectionLayer {
            return when (widget) {
                is XYChartVisibleRangeWidget -> XYChartVisibleRangeSelectionLayer(widget, chartBackgroundAdjustment)
                is XYChartFullRangeWidget -> XYChartFullRangeSelectionLayer(widget, chartBackgroundAdjustment)
                else -> throw UnsupportedOperationException("Unknown XY Chart class")
            }
        }
    }

    protected val selectionRectangle = Rectangle().apply {
        stroke = SELECTION_STROKE_COLOR
        strokeWidth = SELECTION_STROKE_WIDTH
        strokeLineCap = StrokeLineCap.ROUND
    }

    protected val ongoingSelectionRectangle = Rectangle()

    private val selectionCtx = SelectionContext()

    init {
        isMouseTransparent = true

        listOf(selectionRectangle, ongoingSelectionRectangle).forEach {
            // deal
            with(it) {
                isMouseTransparent = true
                fill = SELECTION_FILL_COLOR

                x = 0.0
                width = 0.0
                yProperty().bind(widget.chartPlotArea.layoutYProperty().add(chartBackgroundAdjustment))
                heightProperty().bind(widget.chartPlotArea.heightProperty())

                isVisible = false
            }
        }

        children.addAll(selectionRectangle, ongoingSelectionRectangle)

        /*
         * Add mouse listeners to handle the ongoing selection.
         */
        with(widget.chart) {
            addEventHandler(MouseEvent.MOUSE_PRESSED, selectionCtx.mousePressedEventHandler)
            addEventHandler(MouseEvent.MOUSE_DRAGGED, selectionCtx.mouseDraggedEventHandler)
            addEventHandler(MouseEvent.MOUSE_RELEASED, selectionCtx.mouseReleasedEventHandler)
        }
    }

    abstract fun drawSelection(sr: TimeRange)

    /**
     * Class encapsulating the time range selection, related drawing and
     * listeners.
     */
    private inner class SelectionContext {

        /**
         * Do not handle the mouse event if it matches these condition. It should be handled
         * at another level (for moving the visible range, etc.)
         */
        private fun MouseEvent.isToBeIgnored(): Boolean =
                this.button == MouseButton.SECONDARY
                        || this.button == MouseButton.MIDDLE
                        || this.isControlDown

        private var ongoingSelection: Boolean = false
        private var mouseOriginX: Double = 0.0

        val mousePressedEventHandler = EventHandler<MouseEvent> { e ->
            if (e.isToBeIgnored()) return@EventHandler
            e.consume()

            if (ongoingSelection) return@EventHandler

            /* Remove the current selection, if there is one */
            selectionRectangle.isVisible = false

            mouseOriginX = e.x

            with(ongoingSelectionRectangle) {
                layoutX = mouseOriginX
                width = 0.0
                isVisible = true
            }

            ongoingSelection = true
        }

        val mouseDraggedEventHandler = EventHandler<MouseEvent> { e ->
            if (e.isToBeIgnored()) return@EventHandler
            e.consume()

            val newX = e.x
            val offsetX = newX - mouseOriginX

            with(ongoingSelectionRectangle) {
                if (offsetX > 0) {
                    layoutX = mouseOriginX
                    width = offsetX
                } else {
                    layoutX = newX
                    width = -offsetX
                }
            }
        }

        val mouseReleasedEventHandler = EventHandler<MouseEvent> { e ->
            if (e.isToBeIgnored()) return@EventHandler
            e.consume()

            ongoingSelectionRectangle.isVisible = false

            /* Send a time range selection signal for the currently highlighted time range */
            val startX = ongoingSelectionRectangle.layoutX
            // FIXME Possible glitch when selecting backwards outside of the window?
            val endX = startX + ongoingSelectionRectangle.width

            val localStartX = widget.chartPlotArea.parentToLocal(startX, 0.0).x - chartBackgroundAdjustment
            val localEndX = widget.chartPlotArea.parentToLocal(endX, 0.0).x - chartBackgroundAdjustment
            val tsStart = widget.mapXPositionToTimestamp(localStartX)
            val tsEnd = widget.mapXPositionToTimestamp(localEndX)

            widget.control.updateTimeRangeSelection(TimeRange.of(tsStart, tsEnd))

            ongoingSelection = false
        }
    }
}

private class XYChartFullRangeSelectionLayer(widget: XYChartFullRangeWidget,
                                             chartBackgroundAdjustment: Double) : XYChartSelectionLayer(widget, chartBackgroundAdjustment) {

    override fun drawSelection(sr: TimeRange) {
        val viewWidth = widget.chartPlotArea.width
        if (viewWidth < 1.0) return

        val project = widget.viewContext.traceProject ?: return
        val projectRange = project.fullRange

        val startRatio = (sr.startTime - projectRange.startTime) / projectRange.duration.toDouble()
        val startPos = startRatio * viewWidth + widget.chartPlotArea.layoutX + chartBackgroundAdjustment

        val endRatio = (sr.endTime - projectRange.startTime) / projectRange.duration.toDouble()
        val endPos = endRatio * viewWidth + widget.chartPlotArea.layoutX + chartBackgroundAdjustment

        with(selectionRectangle) {
            x = startPos
            width = endPos - startPos
            isVisible = true
        }
    }

}

private class XYChartVisibleRangeSelectionLayer(widget: XYChartVisibleRangeWidget,
                                                chartBackgroundAdjustment: Double) : XYChartSelectionLayer(widget, chartBackgroundAdjustment) {

    override fun drawSelection(sr: TimeRange) {
        val vr = widget.viewContext.visibleTimeRange

        if (sr.startTime <= vr.startTime && sr.endTime <= vr.startTime) {
            /* Selection is completely before the visible range, no range to display. */
            selectionRectangle.isVisible = false
            return
        }
        if (sr.startTime >= vr.endTime && sr.endTime >= vr.endTime) {
            /* Selection is completely after the visible range, no range to display. */
            selectionRectangle.isVisible = false
            return
        }

        val viewWidth = widget.chartPlotArea.width
        if (viewWidth < 1.0) return

        val startTime = (Math.max(sr.startTime, vr.startTime))
        val startRatio = (startTime - vr.startTime) / vr.duration.toDouble()
        val startPos = startRatio * viewWidth + widget.chartPlotArea.layoutX + chartBackgroundAdjustment

        val endTime = (Math.min(sr.endTime, vr.endTime))
        val endRatio = (endTime - vr.startTime) / vr.duration.toDouble()
        val endPos = endRatio * viewWidth + widget.chartPlotArea.layoutX + chartBackgroundAdjustment

        with(selectionRectangle) {
            x = startPos
            width = endPos - startPos
            isVisible = true
        }
    }
}
