/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.types;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The class <code>SequenceDefinition2Test</code> contains tests for the class
 * <code>{@link SequenceDefinition2}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
class SequenceDefinition2Test {

    private ByteArrayDefinition fixture;
    private final static int seqLen = 15;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     */
    @BeforeEach
    void setUp() throws CTFException {
        fixture = initString();
    }

    private static ByteArrayDefinition initString() throws CTFException {
        StructDeclaration structDec;
        StructDefinition structDef;

        int len = 8;
        IntegerDeclaration id = IntegerDeclaration.createDeclaration(len, false, len,
                ByteOrder.LITTLE_ENDIAN, Encoding.UTF8, "", 8);
        String lengthName = "LengthName";
        structDec = new StructDeclaration(0);
        structDec.addField(lengthName, id);

        structDef = new StructDefinition(structDec, null, "x",  new Definition[] { new IntegerDefinition(id, null, lengthName, seqLen) });

        SequenceDeclaration sd = new SequenceDeclaration(lengthName, id);
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(seqLen * len);
        BitBuffer input = new BitBuffer(allocateDirect);
        for (int i = 0; i < seqLen; i++) {
            input.putInt(i);
        }

        ByteArrayDefinition ret = (ByteArrayDefinition) sd.createDefinition(structDef, "TestX", input);
        assertNotNull(ret);
        return ret;
    }

    /**
     * Run the FixedStringDefinition(SequenceDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    void testFixedStringDefinition() {
        assertNotNull(fixture);
    }

    /**
     * Run the SequenceDeclaration getDeclaration() method test.
     */
    @Test
    void testGetDeclaration() {
        SequenceDeclaration result = (SequenceDeclaration) fixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the Definition getElem(int) method test.
     */
    @Test
    void testGetElem() {
        int i = 1;
        IDefinition result = fixture.getDefinitions().get(i);
        assertNotNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToString() {
        String result = fixture.toString();
        assertNotNull(result);
    }

}
