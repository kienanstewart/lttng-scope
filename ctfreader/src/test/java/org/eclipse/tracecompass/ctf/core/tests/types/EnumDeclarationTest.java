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
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.tests.io.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class <code>EnumDeclarationTest</code> contains tests for the class
 * <code>{@link EnumDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
class EnumDeclarationTest {

    private EnumDeclaration fixture;

    /**
     * Perform pre-test initialization.
     */
    @BeforeEach
    void setUp() {
        fixture = new EnumDeclaration(IntegerDeclaration.createDeclaration(1, false, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 8));
    }

    /**
     * Run the EnumDeclaration(IntegerDeclaration) constructor test.
     */
    @Test
    void testEnumDeclaration() {
        IntegerDeclaration containerType = IntegerDeclaration.createDeclaration(1, false, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 8);

        EnumDeclaration result = new EnumDeclaration(containerType);

        assertNotNull(result);
        String left = "[declaration] enum[";
        assertEquals(left, result.toString().substring(0, left.length()));
    }

    /**
     * Run the boolean add(long,long,String) method test.
     */
    @Test
    void testAdd() {
        long low = 1L;
        long high = 1L;
        String label = "";

        assertTrue(fixture.add(low, high, label));
        assertEquals("", fixture.query(1));
    }

    /**
     * Run the boolean add(long,long,String) method test several times out of
     * order.
     */
    @Test
    void testAddMany() {
        assertTrue(fixture.add(00, 01, "fork"));
        assertTrue(fixture.add(02, 03, "tork"));
        assertTrue(fixture.add(04, 07, "mork"));
        assertTrue(fixture.add(10, 20, "zork"));
        assertTrue(fixture.add(22, 27, "york"));
        assertTrue(fixture.add(21, 21, "bork"));
        assertTrue(fixture.add(28, 50, "dork"));
        assertEquals("fork", fixture.query(0));
        assertEquals("fork", fixture.query(1));
        assertEquals("tork", fixture.query(2));
        assertEquals("tork", fixture.query(3));
        assertEquals("mork", fixture.query(4));
        assertEquals("mork", fixture.query(5));
        assertEquals("mork", fixture.query(6));
        assertEquals("zork", fixture.query(10));
        assertEquals("zork", fixture.query(19));
        assertEquals("bork", fixture.query(21));
        assertEquals("york", fixture.query(22));
    }

    /**
     * Tests adding two of the same elements, this is allowed in the ctf spec
     */
    @Test
    void testDubs() {
        assertTrue(fixture.add(00, 01, "fork"));
        assertTrue(fixture.add(02, 03, "fork"));
        assertNull(fixture.query(-1));
        assertEquals("fork", fixture.query(0));
        assertEquals("fork", fixture.query(1));
        assertEquals("fork", fixture.query(2));
        assertEquals("fork", fixture.query(3));
        assertNull(fixture.query(5));
    }

    /**
     * Tests adding two of the same elements
     */
    @Test
    void testOverlap1() {
        assertTrue(fixture.add(00, 01, "fork"));
        assertFalse(fixture.add(01, 03, "zork"));
    }

    /**
     * Tests adding two of the same elements
     */
    @Test
    void testOverlap2() {
        assertTrue(fixture.add(00, 02, "fork"));
        assertFalse(fixture.add(01, 03, "zork"));
    }

    /**
     * Tests adding two of the same elements
     */
    @Test
    void testOverlap3() {
        assertTrue(fixture.add(00, 03, "fork"));
        assertFalse(fixture.add(01, 02, "zork"));
    }

    /**
     * Tests adding two of the same elements
     */
    @Test
    void testOverlap4() {
        assertTrue(fixture.add(01, 03, "fork"));
        assertFalse(fixture.add(00, 02, "zork"));
    }

    /**
     * Run the EnumDefinition createDefinition(DefinitionScope,String) method
     * test.
     *
     * @throws CTFException
     *             out of bounds error, won't happen
     */
    @Test
    void testCreateDefinition() throws CTFException {
        IDefinitionScope definitionScope = null;
        String fieldName = "";
        byte[] array = { 't', 'e', 's', 't', '\0', 't', 'h', 'i', 's', '\0' };
        BitBuffer bb = new BitBuffer(Util.testMemory(ByteBuffer.wrap(array)));

        EnumDefinition result = fixture.createDefinition(definitionScope,
                fieldName, bb);

        assertNotNull(result);
    }

    /**
     * Run the String query(long) method test.
     */
    @Test
    void testQuery() {
        long value = 0;
        String result = fixture.query(value);

        assertNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToString() {
        String result = fixture.toString();

        String left = "[declaration] enum[";
        assertEquals(left, result.substring(0, left.length()));
    }

    /**
     * Test the hashcode
     */
    @Test
    void hashcodeTest() {
        EnumDeclaration b = new EnumDeclaration(IntegerDeclaration.createDeclaration(1, false, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 8));
        assertEquals(b.hashCode(), fixture.hashCode());
        fixture.add(0, 1, "hello");
        fixture.add(2, 3, "kitty");
        b.add(0, 1, "hello");
        b.add(2, 3, "kitty");
        assertEquals(fixture.hashCode(), b.hashCode());
    }

    /**
     * Test the equals
     */
    @Test
    void equalsTest() {
        EnumDeclaration a = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        EnumDeclaration b = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        b.add(2, 19, "hi");
        EnumDeclaration c = new EnumDeclaration(IntegerDeclaration.INT_32B_DECL);
        EnumDeclaration d = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, c);
        assertEquals(a, d);
        assertNotEquals(b, a);
        assertNotEquals(c, a);
        assertNotEquals(c, b);
        assertEquals(d, a);
        a.add(2, 19, "hi");
        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertNotEquals(a, d);
        assertNotEquals(d, a);
        d.add(2, 22, "hi");
        assertNotEquals(a, d);
        assertNotEquals(d, a);
    }

    /**
     * Test the isBinaryEquivalent
     */
    @Test
    void binaryEquivalentTest() {
        EnumDeclaration a = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        EnumDeclaration b = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        b.add(2, 19, "hi");
        EnumDeclaration c = new EnumDeclaration(IntegerDeclaration.INT_32B_DECL);
        EnumDeclaration d = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        assertFalse(a.isBinaryEquivalent(null));
        assertFalse(a.isBinaryEquivalent(IntegerDeclaration.INT_32B_DECL));
        assertFalse(a.isBinaryEquivalent(b));
        assertFalse(a.isBinaryEquivalent(c));
        assertFalse(b.isBinaryEquivalent(c));
        assertTrue(a.isBinaryEquivalent(d));
        assertFalse(b.isBinaryEquivalent(a));
        assertFalse(c.isBinaryEquivalent(a));
        assertFalse(c.isBinaryEquivalent(b));
        assertTrue(d.isBinaryEquivalent(a));
        a.add(2, 19, "hi");
        assertTrue(a.isBinaryEquivalent(a));
        assertTrue(a.isBinaryEquivalent(b));
        assertTrue(b.isBinaryEquivalent(a));
        assertFalse(a.isBinaryEquivalent(d));
        assertFalse(d.isBinaryEquivalent(a));
        d.add(2, 22, "hi");
        assertFalse(a.isBinaryEquivalent(d));
        assertFalse(d.isBinaryEquivalent(a));
    }

}
