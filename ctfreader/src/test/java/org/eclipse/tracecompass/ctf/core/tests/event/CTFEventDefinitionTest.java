/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.event;

import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.*;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the event definition
 *
 * @author Matthew Khouzam
 *
 */
class CTFEventDefinitionTest {
    List<EventDefinition> fixture;

    /**
     * Making a power set of configurations to test the event definition
     */
    @BeforeEach
    void init() {
        fixture = new ArrayList<>();
        IntegerDeclaration pidDec = IntegerDeclaration.createDeclaration(5, false, 10, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, "", 8);
        IntegerDeclaration ctxDec = IntegerDeclaration.createDeclaration(16, false, 10, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, "", 8);
        IntegerDefinition pid = new IntegerDefinition(pidDec, null, "pid", 3);
        IntegerDefinition pod = new IntegerDefinition(pidDec, null, "pod", 3);
        IntegerDefinition ctx = new IntegerDefinition(pidDec, null, "ctx", 3);

        StructDeclaration streamContextDec = new StructDeclaration(8);
        streamContextDec.addField("pid", pidDec);
        streamContextDec.addField("ctx", ctxDec);
        StructDeclaration eventContextDec = new StructDeclaration(8);
        eventContextDec.addField("pod", pidDec);
        eventContextDec.addField("ctx", pidDec);
        StructDeclaration fDec = new StructDeclaration(8);
        EventDeclaration eventDeclaration = new EventDeclaration();

        fDec.addField("pid", pidDec);
        fDec.addField("ctx", ctxDec);
        fDec.addField("pod", pidDec);

        Definition[] sDefs = { pid, ctx };
        Definition[] eDefs = { pod, ctx };
        Definition[] fDefs = { pid, ctx, pod };

        StructDeclaration pContextDec = new StructDeclaration(8);

        StructDefinition sContext = new StructDefinition(streamContextDec, null, ILexicalScope.STREAM_PACKET_CONTEXT.getPath(), sDefs);
        StructDefinition eContext = new StructDefinition(eventContextDec, null, ILexicalScope.STREAM_EVENT_CONTEXT.getPath(), eDefs);
        StructDefinition pContext = new StructDefinition(pContextDec, null, ILexicalScope.FIELDS.getPath(), new Definition[0]);
        StructDefinition fields = new StructDefinition(fDec, null, ILexicalScope.FIELDS.getPath(), fDefs);

        int cpu = IEventDefinition.UNKNOWN_CPU;

        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, null, null, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, null, null, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, null, pContext, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, null, pContext, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, eContext, null, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, eContext, null, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, eContext, pContext, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, eContext, pContext, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, null, null, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, null, null, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, null, pContext, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, null, pContext, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, eContext, null, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, eContext, null, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, eContext, pContext, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, eContext, pContext, fields, null));
    }

    /**
     * Test all the events
     */
    @Test
    void testEvents() {
        int i = 0;
        for (EventDefinition ed : fixture) {
            test(i, ed);
            i++;
        }
    }

    private static void test(int rank, EventDefinition ed) {
        String title = "event #" + rank;
        assertEquals(100L, ed.getTimestamp(), title);
        ICompositeDefinition context = ed.getContext();
        if (rank >= 4) {
            assertNotNull(context, title);
            if (rank >= 12) {
                assertEquals(3, context.getFieldNames().size(), title);
            } else {
                assertEquals(2, context.getFieldNames().size(), title);
            }

        } else {
            assertNull(context, title);
        }
        if (((rank / 4) % 2) == 1) {
            assertNotNull(ed.getEventContext(), title);
        }else{
            assertNull(ed.getEventContext(), title);
        }
        if (rank % 2 == 1) {
            assertNotNull(ed.getFields(), title);
            assertEquals(3, ed.getFields().getFieldNames().size(), title);
        } else {
            assertNull(ed.getFields(), title);
        }
        assertTrue(ed.toString().startsWith("Event type: null" + System.getProperty("line.separator") + "Timestamp: 100"), title);
    }

}
