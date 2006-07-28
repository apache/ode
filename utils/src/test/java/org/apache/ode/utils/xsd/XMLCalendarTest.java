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
package org.apache.ode.utils.xsd;

import java.util.Calendar;
import junit.framework.TestCase;

/**
 * @author jguinney
 *
 */
public class XMLCalendarTest extends TestCase {
	public XMLCalendarTest(String id){
		super(id);
	}
	
	public void testNoTimeZone(){
		String TEST = "2000-10-05T23:12:35.5Z";
		
		Calendar c = new XMLCalendar(TEST);
		assertEquals(2000, c.get(Calendar.YEAR));
		assertEquals(10, c.get(Calendar.MONTH));
		assertEquals(5, c.get(Calendar.DAY_OF_MONTH));
		assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
		assertEquals(12, c.get(Calendar.MINUTE));
		assertEquals(35, c.get(Calendar.SECOND));
		assertEquals(500, c.get(Calendar.MILLISECOND));
		assertEquals(0, c.get(Calendar.ZONE_OFFSET));
	}
	
	public void testTimeZoneMinus(){
		String TEST = "0001-01-01T01:01:01.1-08:00";
		Calendar c = new XMLCalendar(TEST);
		assertEquals(1, c.get(Calendar.YEAR));
		assertEquals(1, c.get(Calendar.MONTH));
		assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
		assertEquals(1, c.get(Calendar.HOUR_OF_DAY));
		assertEquals(1, c.get(Calendar.MINUTE));
		assertEquals(1, c.get(Calendar.SECOND));
		assertEquals(100, c.get(Calendar.MILLISECOND));
		assertEquals( -8 * 60 * 60 * 1000, c.get(Calendar.ZONE_OFFSET));
	}
	
	public void testTimeZonePlus(){
		String TEST = "3000-11-25T00:00:00+08:30";
		Calendar c = new XMLCalendar(TEST);
		assertEquals(3000, c.get(Calendar.YEAR));
		assertEquals(11, c.get(Calendar.MONTH));
		assertEquals(25, c.get(Calendar.DAY_OF_MONTH));
		assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, c.get(Calendar.MINUTE));
		assertEquals(0, c.get(Calendar.SECOND));
		assertEquals(0, c.get(Calendar.MILLISECOND));
		assertEquals((int)(8.5 * 60 * 60 * 1000), c.get(Calendar.ZONE_OFFSET));
	}

}
