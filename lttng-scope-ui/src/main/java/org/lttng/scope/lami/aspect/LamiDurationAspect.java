/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Michael Jeanson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lami.aspect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lttng.scope.lami.module.LamiTableEntry;
import org.lttng.scope.lami.types.LamiData;
import org.lttng.scope.lami.types.LamiDuration;

import java.util.Comparator;

/**
 * Aspect for a time range duration
 *
 * @author Michael Jeanson
 */
public class LamiDurationAspect extends LamiTableEntryAspect {

    private final int fColIndex;

    /**
     * Constructor
     *
     * @param colName
     *            Column name
     * @param colIndex
     *            Column index
     */
    public LamiDurationAspect(String colName, int colIndex) {
        super(colName, "ns"); //$NON-NLS-1$
        fColIndex = colIndex;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public boolean isTimeStamp() {
        return false;
    }

    @Override
    public boolean isTimeDuration() {
        return true;
    }

    @Override
    public @Nullable String resolveString(LamiTableEntry entry) {
        LamiData data = entry.getValue(fColIndex);
        if (data instanceof LamiDuration) {
            LamiDuration duration = (LamiDuration) data;

            // TODO: Consider low and high limits here.
            return String.valueOf(duration.getValue());
        }
        return data.toString();
    }

    @Override
    public @Nullable Number resolveNumber(@NotNull LamiTableEntry entry) {
        LamiData data = entry.getValue(fColIndex);
        if (data instanceof LamiDuration) {
            LamiDuration duration = (LamiDuration) data;

            // TODO: Consider low and high limits here.
            return duration.getValue();
        }
        return null;
    }

    @Override
    public @NotNull Comparator<LamiTableEntry> getComparator() {
        return (o1, o2) -> {
            Number d1 = resolveNumber(o1);
            Number d2 = resolveNumber(o2);

            if (d1 == null && d2 == null) {
                return 0;
            }
            if (d1 == null) {
                return 1;
            }

            if (d2 == null) {
                return -1;
            }

            return Long.compare(d1.longValue(), d2.longValue());
        };
    }

}
