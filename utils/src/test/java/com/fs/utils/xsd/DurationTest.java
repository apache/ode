/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.xsd;

import java.util.Calendar;

import junit.framework.TestCase;

/**
 * @author jguinney
 *
 */
public class DurationTest extends TestCase {
	/**
	 * Constructor for DurationTest.
	 * @param arg0
	 */
	public DurationTest(String arg0) {
		super(arg0);
	}
	
	public void testDuration1() {
		String TEST ="P10Y10M10DT10H10M10.1S";
		Duration d = new Duration(TEST);
		long time = System.currentTimeMillis();
		
		Calendar c1 = Calendar.getInstance();
		c1.setTimeInMillis(time);
		Calendar c2 = Calendar.getInstance();
		c2.setTimeInMillis(time);
		
		d.addTo(c1);
		c2.add(Calendar.YEAR, 10);
		c2.add(Calendar.MONTH, 10);
		c2.add(Calendar.DAY_OF_MONTH, 10);
		c2.add(Calendar.HOUR, 10);
		c2.add(Calendar.MINUTE, 10);
		c2.add(Calendar.SECOND, 10);
		c2.add(Calendar.MILLISECOND, 100);
		
		assertEquals(c2.getTimeInMillis(), c1.getTimeInMillis());
	}
	
	public void testDuration2() {
		String TEST ="P10Y10M10D";
		Duration d = new Duration(TEST);
		long time = System.currentTimeMillis();
		
		Calendar c1 = Calendar.getInstance();
		c1.setTimeInMillis(time);
		Calendar c2 = Calendar.getInstance();
		c2.setTimeInMillis(time);
		
		d.addTo(c1);
		c2.add(Calendar.YEAR, 10);
		c2.add(Calendar.MONTH, 10);
		c2.add(Calendar.DAY_OF_MONTH, 10);
		
		assertEquals(c2.getTimeInMillis(), c1.getTimeInMillis());
	}
	
	public void testDuration3() {
		String TEST ="P10M";
		Duration d = new Duration(TEST);
		long time = System.currentTimeMillis();
		
		Calendar c1 = Calendar.getInstance();
		c1.setTimeInMillis(time);
		Calendar c2 = Calendar.getInstance();
		c2.setTimeInMillis(time);
		
		d.addTo(c1);
		c2.add(Calendar.MONTH, 10);
		
		assertEquals(c2.getTimeInMillis(), c1.getTimeInMillis());
	}
	
	public void testDuration4() {
		String TEST ="P10YT90S";
		Duration d = new Duration(TEST);
		long time = System.currentTimeMillis();
		
		Calendar c1 = Calendar.getInstance();
		c1.setTimeInMillis(time);
		Calendar c2 = Calendar.getInstance();
		c2.setTimeInMillis(time);
		
		d.addTo(c1);
		c2.add(Calendar.YEAR, 10);
		c2.add(Calendar.SECOND, 90);
		
		assertEquals(c2.getTimeInMillis(), c1.getTimeInMillis());
	}
	
}
