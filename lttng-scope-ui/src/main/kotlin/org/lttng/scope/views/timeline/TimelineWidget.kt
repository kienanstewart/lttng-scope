/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline

import javafx.scene.Parent
import javafx.scene.control.ScrollPane
import javafx.scene.control.SplitPane
import javafx.scene.shape.Rectangle

interface TimelineWidget {

    val name: String

    /**
     * UI weight of this widget. The default sort order of the widgets in the
     * timeline view will be by ascending weight, with the "lighter" ones on top.
     */
    val weight: Int

    val rootNode: Parent

    fun dispose()

    interface TimelineWidgetUpdateTask : Runnable

    val timelineWidgetUpdateTask: TimelineWidgetUpdateTask?

    /**
     * Many widgets will use a SplitPane to separate a tree or info pane on the
     * left, and a time-based pane on the right. This method is used to return
     * this pane so the manager can apply common operations on them.
     *
     * @return The horizontal split pane, or 'null' if the widget doesn't use
     *         one and uses the full horizontal width of the view.
     */
    val splitPane: SplitPane?

    val timeBasedScrollPane: ScrollPane?

    val selectionRectangle: Rectangle?

    val ongoingSelectionRectangle: Rectangle?
}