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
import org.eclipse.tracecompass.ctf.core.event.types.*;
import org.eclipse.tracecompass.ctf.core.tests.io.Util;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class <code>VariantDefinitionTest</code> contains tests for the class
 * <code>{@link VariantDefinition}</code>.
 *
 * @author ematkho
 */
class VariantDefinitionTest {

    private VariantDefinition fixture;

    private StructDefinition fStructDefinition;
    private static final @NotNull String TEST_STRUCT_ID = "testStruct";
    private static final @NotNull String ENUM_7 = "g";
    private static final @NotNull String ENUM_6 = "f";
    private static final @NotNull String ENUM_5 = "e";
    private static final @NotNull String ENUM_4 = "d";
    private static final @NotNull String ENUM_3 = "c";
    private static final @NotNull String ENUM_2 = "b";
    private static final @NotNull String ENUM_1 = "a";
    private static final @NotNull String TAG_ID = "a";
    private static final @NotNull String LENGTH_SEQ = "_len";
    private static final @NotNull String VAR_FIELD_NAME = "var";
    private static final @NotNull String ENUM_8 = "bbq ribs";

    /**
     * Perform pre-test initialization.
     *
     * Not sure it needs to be that complicated, oh well...
     *
     * @throws CTFException
     *             won't happen
     */
    @BeforeEach
    void setUp() throws CTFException {
        StructDeclaration sDec = new StructDeclaration(12);
        StructDeclaration smallStruct = new StructDeclaration(8);
        IntegerDeclaration iDec = IntegerDeclaration.createDeclaration(32, false, 32, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 8);
        IntegerDeclaration lenDec = IntegerDeclaration.createDeclaration(8, false, 8, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 8);
        StringDeclaration strDec = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        EnumDeclaration enDec = new EnumDeclaration(iDec);
        VariantDeclaration varDec = new VariantDeclaration();
        EnumDeclaration tagDec = new EnumDeclaration(iDec);
        CompoundDeclaration arrDec = new ArrayDeclaration(2, iDec);
        FloatDeclaration fDec = new FloatDeclaration(8, 24, ByteOrder.BIG_ENDIAN, 8);
        tagDec.add(0, 1, ENUM_1);
        tagDec.add(2, 3, ENUM_2);
        tagDec.add(4, 5, ENUM_3);
        tagDec.add(8, 9, ENUM_5);
        tagDec.add(10, 11, ENUM_6);
        tagDec.add(12, 13, ENUM_7);
        varDec.addField(ENUM_4, lenDec);
        varDec.addField(ENUM_7, fDec);
        varDec.addField(ENUM_6, smallStruct);
        varDec.addField(ENUM_5, enDec);
        varDec.addField(ENUM_3, arrDec);
        varDec.addField(ENUM_2, iDec);
        varDec.addField(ENUM_1, strDec);

        sDec.addField(TAG_ID, tagDec);
        sDec.addField(LENGTH_SEQ, lenDec);

        sDec.addField(VAR_FIELD_NAME, varDec);
        varDec.setTag(TAG_ID);

        final ByteBuffer byteBuffer = Util.testMemory(ByteBuffer.allocate(100));
        BitBuffer bb = new BitBuffer(byteBuffer);
        byteBuffer.mark();
        byteBuffer.putInt(1);
        byteBuffer.putInt(2);
        byteBuffer.putInt(3);
        byteBuffer.reset();
        fStructDefinition = sDec.createDefinition(null, TEST_STRUCT_ID, bb);
        fixture = (VariantDefinition) fStructDefinition.getDefinition(VAR_FIELD_NAME);
    }

    /**
     * Run the VariantDefinition(VariantDeclaration,DefinitionScope,String)
     *
     * @throws CTFException
     *             should not happen
     */
    @Test
    void testVariantDefinition() throws CTFException {
        VariantDeclaration declaration = new VariantDeclaration();
        declaration.setTag("");
        VariantDeclaration variantDeclaration = new VariantDeclaration();
        variantDeclaration.addField("", new EnumDeclaration(IntegerDeclaration.INT_32B_DECL));
        variantDeclaration.addField("a", IntegerDeclaration.INT_64B_DECL);
        declaration.addField(ENUM_3, StringDeclaration.getStringDeclaration(Encoding.UTF8));
        variantDeclaration.setTag("a");

        byte[] bytes = new byte[128];
        ByteBuffer byb = ByteBuffer.wrap(bytes);
        byb.mark();
        byb.putInt(0);
        byb.putShort((short) 2);
        byb.put(new String("hello").getBytes());
        byb.reset();
        BitBuffer bb = new BitBuffer(byb);
        VariantDefinition variantDefinition = variantDeclaration.createDefinition(fStructDefinition, "field", bb);
        EnumDeclaration declaration2 = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        declaration2.add(0, 2, ENUM_3);
        EnumDefinition enumDefinition = new EnumDefinition(
                declaration2,
                null,
                "a",
                new IntegerDefinition(
                        IntegerDeclaration.INT_8_DECL,
                        null,
                        "A",
                        1
                ));
        final StructDeclaration declarationScope = new StructDeclaration(1L);
        declarationScope.addField("", enumDefinition.getDeclaration());
        declarationScope.addField("variant", variantDeclaration);
        IDefinitionScope definitionScope = new StructDefinition(
                declarationScope,
                variantDefinition,
                "",
                new Definition[] { enumDefinition, variantDefinition }
                );
        String fieldName = "";
        declaration.setTag("");
        VariantDefinition result = declaration.createDefinition(definitionScope, fieldName, bb);
        assertNotNull(result);
    }

    /**
     * Run the Definition getCurrentField() method test.
     */
    @Test
    void testGetCurrentField() {
        IDefinition result = fixture.getCurrentField();
        assertNotNull(result);
    }

    /**
     * Run the String getCurrentFieldName() method test.
     */
    @Test
    void testGetCurrentFieldName() {
        String result = fixture.getCurrentFieldName();
        assertNotNull(result);
    }

    /**
     * Run the VariantDeclaration getDeclaration() method test.
     */
    @Test
    void testGetDeclaration() {
        VariantDeclaration result = fixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the HashMap<String, Definition> getDefinitions() method test.
     */
    @Test
    void testGetDefinitions() {
        IDefinition result = fixture.getCurrentField();
        assertNotNull(result);
    }

    /**
     * Run the String getPath() method test.
     */
    @Test
    void testGetPath() {
        String result = fixture.getScopePath().getPath();
        assertNotNull(result);
    }

    /**
     * Run the ArrayDefinition lookupArray(String) method test.
     */
    @Test
    void testLookupArray() {
        AbstractArrayDefinition result = fixture.lookupArrayDefinition(ENUM_3);
        assertNull(result);
    }

    /**
     * Run the Definition lookupDefinition(String) method test.
     */
    @Test
    void testLookupDefinition() {
        IDefinition result = fixture.lookupDefinition(ENUM_1);
        assertNotNull(result);
        assertEquals("a", ((EnumDefinition) result).getStringValue());
    }

    /**
     * Run the EnumDefinition lookupEnum(String) method test.
     */
    @Test
    void testLookupEnum() {
        EnumDefinition result = fixture.lookupEnum(ENUM_5);
        assertNull(result);
    }

    /**
     * Run the IntegerDefinition lookupInteger(String) method test.
     */
    @Test
    void testLookupInteger() {
        IntegerDefinition result = fixture.lookupInteger(ENUM_2);
        assertNull(result);
    }

    /**
     * Run the StringDefinition lookupString(String) method test.
     */
    @Test
    void testLookupString() {
        StringDefinition result = fixture.lookupString(ENUM_1);
        assertNull(result);
    }

    /**
     * Run the StructDefinition lookupStruct(String) method test.
     */
    @Test
    void testLookupStruct() {
        StructDefinition result = fixture.lookupStruct(ENUM_6);
        assertNull(result);
    }

    /**
     * Run the VariantDefinition lookupVariant(String) method test.
     */
    @Test
    void testLookupVariant() {
        VariantDefinition result = fixture.lookupVariant(ENUM_8);
        assertNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToString() {
        String result = fixture.toString();
        assertEquals("{ a = \"\" }", result);
    }
}
