/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar;

import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelProvider;
import com.efficios.jabberwocky.views.timegraph.model.provider.arrows.TimeGraphModelArrowProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrowSeries;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.paint.Color;
import org.lttng.scope.common.jfx.Arrow;
import org.lttng.scope.common.jfx.JfxColorFactory;
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Menu-button for listing the available arrow series.
 *
 * The available arrow series come from the time graph model. More than one mode
 * (or none) can be active at the same time, so we are using CheckMenuItems for
 * the menu items.
 *
 * @author Alexandre Montplaisir
 */
class ArrowSeriesMenuButton extends MenuButton {

    private static final double ARROW_GRAPHIC_LENGTH = 10;

    public ArrowSeriesMenuButton(TimeGraphWidget widget) {
        ITimeGraphModelProvider modelProvider = widget.getControl().getRenderProvider();
        Collection<TimeGraphModelArrowProvider> arrowProviders = modelProvider.getArrowProviders();

        Collection<CheckMenuItem> arrowSeriesItems = arrowProviders.stream()
                .map(arrowProvider -> {
                    TimeGraphArrowSeries series = arrowProvider.getArrowSeries();
                    String name = series.getSeriesName();
                    Arrow graphic = getArrowGraphicForSeries(series);
                    CheckMenuItem cmi = new CheckMenuItem(name, graphic);
                    cmi.selectedProperty().bindBidirectional(arrowProvider.enabledProperty());
                    return cmi;
                })
                .collect(Collectors.toList());

        setText(Messages.arrowSeriesMenuButtonName);
        getItems().addAll(arrowSeriesItems);

        if (arrowSeriesItems.isEmpty()) {
            setDisable(true);
        }
    }

    private static Arrow getArrowGraphicForSeries(TimeGraphArrowSeries series) {
        Color color = JfxColorFactory.getColorFromDef(series.getColor());
        Arrow arrow = new Arrow(0, 0, ARROW_GRAPHIC_LENGTH, 0);
        arrow.setStroke(color);
        return arrow;
    }

}
