/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lami.types;

import org.jetbrains.annotations.Nullable;

/**
 * Lami timestamp data type
 *
 * @author Alexandre Montplaisir
 */
public class LamiTimestamp extends LamiLongNumber {

    /**
     * Construct a time stamp from a value in ns.
     *
     * @param value
     *            The value
     */
    public LamiTimestamp(long value) {
        super(value);
    }

    /**
     * Constructor (with limits)
     *
     * @param low
     *            Lower bound of value (ns since Unix epoch)
     * @param value
     *            Value (ns since Unix epoch)
     * @param high
     *            Higher bound of value (ns since Unix epoch)
     */
    public LamiTimestamp(@Nullable Long low, @Nullable Long value, @Nullable Long high) {
        super(low, value, high);
    }
}
