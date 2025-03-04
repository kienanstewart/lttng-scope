/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.tests

import javafx.embed.swing.JFXPanel

/**
 * Test-runner that will make sure JavaFX is initialized. Have your test class
 * extend this if you get "Toolkit not initialized" errors.
 */
abstract class JavaFXTestBase {

    init {
        JFXPanel()
    }

}
