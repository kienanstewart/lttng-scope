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
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.StringDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StringDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class <code>StringDeclarationTest</code> contains tests for the class
 * <code>{@link StringDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
class StringDeclarationTest {

    private StringDeclaration fixture;

    /**
     * Perform pre-test initialization.
     */
    @BeforeEach
    void setUp() {
        fixture = StringDeclaration.getStringDeclaration(Encoding.ASCII);
    }

    /**
     * Run the StringDeclaration() constructor test.
     */
    @Test
    void testStringDeclaration() {
        StringDeclaration result = StringDeclaration.getStringDeclaration(Encoding.UTF8);

        assertNotNull(result);
        String string = "[declaration] string[";
        assertEquals(string, result.toString().substring(0, string.length()));
    }

    /**
     * Run the StringDeclaration(Encoding) constructor test.
     */
    @Test
    void testStringDeclaration_2() {
        Encoding encoding = Encoding.ASCII;
        StringDeclaration result = StringDeclaration.getStringDeclaration(encoding);

        assertNotNull(result);
        String string = "[declaration] string[";
        assertEquals(string, result.toString().substring(0, string.length()));
    }

    /**
     * Run the StringDefinition createDefinition(DefinitionScope,String) method
     * test.
     *
     * @throws CTFException
     *             out of buffer exception
     */
    @Test
    void testCreateDefinition() throws CTFException {
        IDefinitionScope definitionScope = null;
        String fieldName = "id";
        ByteBuffer allocate = ByteBuffer.allocate(100);
        BitBuffer bb = new BitBuffer(allocate);
        StringDefinition result = fixture.createDefinition(definitionScope,
                fieldName, bb);

        assertNotNull(result);
    }

    /**
     * Run the Encoding getEncoding() method test.
     */
    @Test
    void testGetEncoding() {
        Encoding result = fixture.getEncoding();

        assertNotNull(result);
        assertEquals("ASCII", result.name());
        assertEquals("ASCII", result.toString());
        assertEquals(1, result.ordinal());
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToString() {
        String result = fixture.toString();
        String left = "[declaration] string[";
        String right = result.substring(0, left.length());

        assertEquals(left, right);
    }

    /**
     * Test the hashcode
     */
    @Test
    void hashcodeTest() {
        assertEquals(32, fixture.hashCode());
        StringDeclaration a = StringDeclaration.getStringDeclaration(Encoding.ASCII);
        StringDeclaration b = StringDeclaration.getStringDeclaration();
        StringDeclaration c = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        StringDeclaration d = StringDeclaration.getStringDeclaration(Encoding.ASCII);
        assertEquals(b.hashCode(), c.hashCode());
        assertEquals(a.hashCode(), d.hashCode());
        assertEquals(a.hashCode(), a.hashCode());
    }

    /**
     * Test the equals
     */
    @Test
    void equalsTest() {
        StringDeclaration a = StringDeclaration.getStringDeclaration(Encoding.ASCII);
        StringDeclaration b = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        StringDeclaration c = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        StringDeclaration d = StringDeclaration.getStringDeclaration(Encoding.ASCII);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a, d);
        assertNotEquals(b, a);
        assertNotEquals(c, a);
        assertEquals(d, a);
        assertEquals(a, a);
    }
}