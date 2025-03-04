/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Alexandre Montplaisir - Split out in separate class
 *   Matthew Khouzam - update api (exceptions)
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.types;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Endianness test for {@link IntegerDefinition}.
 *
 * @author Geneviève Bastien
 */
class IntegerEndiannessTest {

    private static final @NotNull String name = "testInt";
    private static final @NotNull String clockName = "clock";

    private ByteBuffer bb;

    private @NotNull BitBuffer input = new BitBuffer();

    /**
     * Set up the bit-buffer to be used
     */
    @BeforeEach
    void setUp() {
        bb = java.nio.ByteBuffer.allocateDirect(8);
        final ByteBuffer byb = bb;
        bb.put((byte) 0xab);
        bb.put((byte) 0xcd);
        bb.put((byte) 0xef);
        bb.put((byte) 0x12);
        bb.put((byte) 0x34);
        bb.put((byte) 0x56);
        bb.put((byte) 0x78);
        bb.put((byte) 0x9a);

        input = new BitBuffer(byb);
    }

    /**
     * Read 32-bits BE
     *
     * @throws CTFException
     *             error
     */
    @Test
    void test32BE() throws CTFException {
        IntegerDeclaration be = IntegerDeclaration.createDeclaration(32, true, 1, ByteOrder.BIG_ENDIAN, Encoding.NONE, clockName, 8);
        IntegerDefinition fixture_be = be.createDefinition(null, name, input);
        assertEquals(0xabcdef12, fixture_be.getValue());
    }

    /**
     * Read 64-bits BE
     *
     * @throws CTFException
     *             error
     */
    @Test
    void test64BE() throws CTFException {
        IntegerDeclaration be = IntegerDeclaration.createDeclaration(64, true, 1, ByteOrder.BIG_ENDIAN, Encoding.NONE, clockName, 8);
        IntegerDefinition fixture_be = be.createDefinition(null, name, input);
        assertEquals(0xabcdef123456789aL, fixture_be.getValue());
    }

    /**
     * Read 32-bits LE
     *
     * @throws CTFException
     *             error
     */
    @Test
    void test32LE() throws CTFException {
        IntegerDeclaration le = IntegerDeclaration.createDeclaration(32, true, 1, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, clockName, 8);
        IntegerDefinition fixture_le = le.createDefinition(null, name, input);
        assertEquals(0x12efcdab, fixture_le.getValue());
    }

    /**
     * Read 64-bits LE
     *
     * @throws CTFException
     *             error
     */
    @Test
    void test64LE() throws CTFException {
        IntegerDeclaration le = IntegerDeclaration.createDeclaration(64, true, 1, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, clockName, 8);
        IntegerDefinition fixture_le = le.createDefinition(null, name, input);
        assertEquals(0x9a78563412efcdabL, fixture_le.getValue());
    }
}
