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
import org.jetbrains.annotations.Nullable;
import org.lttng.scope.lami.types.LamiData;
import org.lttng.scope.lami.types.LamiTimeRange;
import org.lttng.scope.lami.types.LamiTimestamp;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Entry of a LAMI output. Usually corresponds to one row in a JSON LAMI table
 * output.
 *
 * @author Alexandre Montplaisir
 */
public class LamiTableEntry {

    private final List<LamiData> fValues;

    /**
     * Constructor
     *
     * @param values Values contained in this row
     */
    public LamiTableEntry(List<LamiData> values) {
        fValues = ImmutableList.copyOf(values);
    }

    /**
     * Get the value at a given index
     *
     * @param index
     *            Index to look at
     * @return The value at this index
     */
    public LamiData getValue(int index) {
        return fValues.get(index);
    }

    /**
     * Get the time range represented by this row.
     *
     * If more than one exists, one of them (usually the first) is returned.
     *
     * If there are no time ranges in this row, null is returned.
     *
     * @return The time range of this row
     */
    public @Nullable LamiTimeRange getCorrespondingTimeRange() {
        /*
         * If there is one or more time range(s) in the values, return the first
         * one we find directly.
         */
        Optional<LamiTimeRange> oTimerange = fValues.stream()
                .filter(data -> (data instanceof LamiTimeRange))
                .map(data -> (LamiTimeRange) data)
                .findFirst();
        if (oTimerange.isPresent()) {
            return oTimerange.get();
        }

        /* Look for individual timestamps instead  */
        List<LamiTimestamp> timestamps = fValues.stream()
            .filter(data -> (data instanceof LamiTimestamp))
            .map(data -> (LamiTimestamp) data)
            .collect(Collectors.toList());

        if (timestamps.size() > 1) {
            /* We can try using the first two timestamps to create a range (making sure it's valid) */
            LamiTimestamp firstTs = timestamps.get(0);
            LamiTimestamp secondTs = timestamps.get(1);
            Number firstTsValue = firstTs.getValue();
            Number secondTsValue = secondTs.getValue();

            // TODO: Consider low and high limits in comparisons.
            if (firstTsValue != null && secondTsValue != null &&
                    Long.compare(firstTsValue.longValue(), secondTsValue.longValue()) <= 0) {
                return new LamiTimeRange(firstTs, secondTs);
            }
        }

        if (!timestamps.isEmpty()) {
            /* If there is only one timestamp, use it to create a punctual range */
            LamiTimestamp ts = timestamps.get(0);
            return new LamiTimeRange(ts, ts);
        }

        /* Didn't find any timestamp we can't use */
        return null;
    }
}
