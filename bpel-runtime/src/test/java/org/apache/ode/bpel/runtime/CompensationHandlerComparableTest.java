/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.bpel.runtime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import java.util.Arrays;
import junit.framework.TestCase;

/**
 * ODE-1066: Tests the Comparable implementation of CompensationHandler.
 *
 */
public class CompensationHandlerComparableTest extends TestCase {
    DateFormat format;
    long startTime;
    long endTime;

    protected void setUp() throws Exception {
        format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        startTime=0;
        endTime=0;
    }

    public void testCompensationNestedScopes() {

        try {
            // parent scope encloses outerScope and outerScope encloses innerScope
            //  ____________________
            // | Parent             |
            // |   _______________  |
            // |  |Outer          | |
            // |  |   __________  | |
            // |  |  |Inner     | | |
            // |  |  |__________| | |
            // |  |_______________| |
            // |____________________|
            //
            // Expected CH execution order parent, outerScope, innerScope

            startTime = format.parse("2017-05-28T15:15:11.013+05:30").getTime();
            endTime = format.parse("2017-05-28T15:15:11.099+05:30").getTime();
            CompensationHandler parent = new CompensationHandler(null, null, startTime, endTime);        

            startTime = format.parse("2017-05-28T15:15:11.016+05:30").getTime();     
            endTime = format.parse("2017-05-28T15:15:11.099+05:30").getTime();
            CompensationHandler outerScope = new CompensationHandler(null, null, startTime, endTime);


            startTime = format.parse("2017-05-28T15:15:11.030+05:30").getTime();
            endTime = format.parse("2017-05-28T15:15:11.090+05:30").getTime();
            CompensationHandler innerScope = new CompensationHandler(null, null, startTime, endTime);

            List<CompensationHandler> chHandlers = new ArrayList<CompensationHandler>();
            chHandlers.add(parent);
            chHandlers.add(outerScope);
            chHandlers.add(innerScope);

            TreeSet<CompensationHandler> set = new TreeSet<CompensationHandler>();
            set.addAll(chHandlers);
            assertEquals(3, set.size());

            CompensationHandler[] expectedOrder = {parent,outerScope,innerScope};
            CompensationHandler[] actualOrder = new CompensationHandler[3];
            set.toArray(actualOrder);
            assertTrue(Arrays.equals(expectedOrder, actualOrder));
        } catch (ParseException e) {
            fail("Unparsable date format");
        }

    }

    public void testCompensationPeerScopes() {

        try {

            //  ____________________
            // | Parent             |
            // |  _______           |
            // | |Child1 |          |
            // | |_______|  _______ | 
            // |           |Child2 ||
            // |           |_______||
            // |                    |
            // |____________________|
            //
            // child1 starts and completes, child2 starts and completes and then parent completes 
            // Expected CH execution order parent, child2, child1

            //parent
            startTime = format.parse("2017-05-28T15:15:11.013+05:30").getTime();
            endTime = format.parse("2017-05-28T15:15:11.099+05:30").getTime();
            CompensationHandler parent = new CompensationHandler(null, null, startTime, endTime);        

            //child1
            startTime = format.parse("2017-05-28T15:15:11.017+05:30").getTime();
            endTime = format.parse("2017-05-28T15:15:11.030+05:30").getTime();
            CompensationHandler child1 = new CompensationHandler(null, null, startTime, endTime);

            //child2
            startTime = format.parse("2017-05-28T15:15:11.035+05:30").getTime();
            endTime = format.parse("2017-05-28T15:15:11.060+05:30").getTime();
            CompensationHandler child2 = new CompensationHandler(null, null, startTime, endTime);

            List<CompensationHandler> chHandlers = new ArrayList<CompensationHandler>();
            chHandlers.add(child2);
            chHandlers.add(child1);
            chHandlers.add(parent);

            TreeSet<CompensationHandler> set = new TreeSet<CompensationHandler>();
            set.addAll(chHandlers);
            assertEquals(3, set.size());

            CompensationHandler[] expectedOrder = {parent,child2,child1};
            CompensationHandler[] actualOrder = new CompensationHandler[3];
            set.toArray(actualOrder);
            assertTrue(Arrays.equals(expectedOrder, actualOrder));
        } catch (ParseException e) {
            fail("Unparsable date format");
        }

    }

    public void testCompensationParallelScopes() {

        try {

            //  _______________
            // | Parent        |
            // |  ____________ |
            // | |Child1      ||
            // | |____________|| 
            // |  |Child2     ||
            // |  |___________||
            // |               |
            // |_______________|
            //
            // child1 & child2 run parallel with start time slightly different but with same end time.
            // Expected CH execution order parent, child1, child2

            //parent
            startTime = format.parse("2017-05-28T15:15:11.013+05:30").getTime();
            endTime = format.parse("2017-05-28T15:15:11.099+05:30").getTime();
            CompensationHandler parent = new CompensationHandler(null, null, startTime, endTime);        

            //child1
            startTime = format.parse("2017-05-28T15:15:11.016+05:30").getTime();
            endTime = format.parse("2017-05-28T15:15:11.099+05:30").getTime();
            CompensationHandler child1 = new CompensationHandler(null, null, startTime, endTime);

            //child2
            startTime = format.parse("2017-05-28T15:15:11.017+05:30").getTime();
            endTime = format.parse("2017-05-28T15:15:11.099+05:30").getTime();
            CompensationHandler child2 = new CompensationHandler(null, null, startTime, endTime);

            List<CompensationHandler> chHandlers = new ArrayList<CompensationHandler>();
            chHandlers.add(child2);
            chHandlers.add(child1);
            chHandlers.add(parent);

            TreeSet<CompensationHandler> set = new TreeSet<CompensationHandler>();
            set.addAll(chHandlers);
            assertEquals(3, set.size());

            CompensationHandler[] expectedOrder = {parent,child1,child2};
            CompensationHandler[] actualOrder = new CompensationHandler[3];
            set.toArray(actualOrder);
            assertTrue(Arrays.equals(expectedOrder, actualOrder));

        } catch (ParseException e) {
            fail("Unparsable date format");
        }
    }
}
