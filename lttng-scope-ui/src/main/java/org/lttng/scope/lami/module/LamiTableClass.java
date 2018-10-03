/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lami.module;

import com.google.common.collect.ImmutableList;
import org.lttng.scope.lami.aspect.LamiTableEntryAspect;

import java.util.Collection;
import java.util.List;

/**
 * The model of a table element in a LAMI analysis script output.
 *
 * Contains all the required information to build the actual UI table layout.
 *
 * @author Alexandre Montplaisir
 */
public class LamiTableClass {

    private final String fTableClassName;
    private final String fTableTitle;
    private final List<LamiTableEntryAspect> fAspects;
    private final Collection<LamiChartModel> fPredefinedViews;

    /**
     * Standard constructor. Build a new table class by specifying all
     * parameters.
     *
     * @param tableClassName
     *            The name of the table's class
     * @param tableTitle
     *            The title of this table
     * @param columnAspects
     *            The list of aspects representing the columsn of this table
     * @param predefinedViews
     *            The pre-defined views of this analysis. Viewers will be
     *            created for these views by default.
     */
    public LamiTableClass(String tableClassName, String tableTitle,
            List<LamiTableEntryAspect> columnAspects, Collection<LamiChartModel> predefinedViews) {
        fTableClassName = tableClassName;
        fTableTitle = tableTitle;
        fAspects = ImmutableList.copyOf(columnAspects);
        fPredefinedViews = ImmutableList.copyOf(predefinedViews);
    }

    /**
     * "Extension" constructor. Use an existing table class but override the
     * table name.
     *
     * @param baseClass
     *            The base table class
     * @param replacementTitle
     *            The new title to use instead
     */
    public LamiTableClass(LamiTableClass baseClass, String replacementTitle) {
        fTableClassName = Messages.LamiAnalysis_ExtendedTableNamePrefix + ' ' + baseClass.fTableClassName;
        fTableTitle = replacementTitle;
        fAspects = baseClass.fAspects; // We know it's an immutable list
        fPredefinedViews = baseClass.fPredefinedViews; // idem
    }

    /**
     * Get the name of the table's class.
     *
     * @return The table class name
     */
    public String getTableClassName() {
        return fTableClassName;
    }

    /**
     * Get the title of this table.
     *
     * @return The table title
     */
    public String getTableTitle() {
        return fTableTitle;
    }

    /**
     * Get the aspects of this table's columns.
     *
     * @return The table aspects
     */
    public List<LamiTableEntryAspect> getAspects() {
        return fAspects;
    }
    /**
     * Get the pre-defined views of this table.
     *
     * @return The predefined views
     */
    public Collection<LamiChartModel> getPredefinedViews() {
        return fPredefinedViews;
    }
}
