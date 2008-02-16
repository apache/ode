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

package org.apache.ode.utils;

import java.text.ParseException;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

public class ISO8601DateParserTest extends TestCase {

    public void test1() throws ParseException {
        String in = "2004-05-31T09:19:31-05:00";

        Date d = ISO8601DateParser.parse(in);
        assertDate(d, 2004, 5, 31, 9, 19, 31, 0, "GMT-5");
    }

    public void test2() throws ParseException {
        String in = "2004-06-23T17:25:31-00:00";
        Date d = ISO8601DateParser.parse(in);
        assertDate(d, 2004, 6, 23, 17, 25, 31, 0, "GMT");
    }

    public void test3() throws ParseException {
        String in = "2004-06-23T17:25-00:00";
        Date d = ISO8601DateParser.parse(in);
        assertDate(d, 2004, 6, 23, 17, 25, 0, 0, "GMT");
    }

    public void test4() throws ParseException {
        String in = "2002-10-02T10:00:00-05:00";
        Date d = ISO8601DateParser.parse(in);
        assertDate(d, 2002, 10, 2, 10, 0, 0, 0, "GMT-5");
    }

    public void test5() throws ParseException {
        String in = "2008-02-15T18:44:54.9-08:00";
        Date d = ISO8601DateParser.parse(in);
        assertDate(d, 2008, 2, 15, 18, 44, 54, 9, "GMT-8");
    }

    public void test6() throws ParseException {
        String in = "2008-02-15T18:44:54.109-08:00";
        Date d = ISO8601DateParser.parse(in);
        assertDate(d, 2008, 2, 15, 18, 44, 54, 109, "GMT-8");
    }

    public void test7() throws ParseException {
        String in = "2004-06-23T17:25:31.6";
        Date d = ISO8601DateParser.parse(in);
        assertDate(d, 2004, 6, 23, 17, 25, 31, 6, null);
    }

    public void test8() throws ParseException {
        String in = "2004-06-23T17:25:10.826";
        Date d = ISO8601DateParser.parse(in);
        assertDate(d, 2004, 6, 23, 17, 25, 10, 826, null);
    }

    private void assertDate(Date d, int year, int month, int day,
                            int hour, int minute, int second, int ms, String tz) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        if (tz != null) cal.setTimeZone(TimeZone.getTimeZone(tz));

        assertEquals(year, cal.get(Calendar.YEAR));
        assertEquals(month - 1, cal.get(Calendar.MONTH));
        assertEquals(day, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(hour, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, cal.get(Calendar.MINUTE));
        assertEquals(second, cal.get(Calendar.SECOND));
        assertEquals(ms, cal.get(Calendar.MILLISECOND));
    }

}
