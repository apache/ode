/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.xsd;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML schema aware calendar : capable of parsing xml schema string of form
 * CCYY-MM-DDThh:mm:ss.ss
 * 
 */
public class XMLCalendar extends GregorianCalendar {

	private static final long serialVersionUID = 4394008655499855392L;

	static final int YEAR_MG = 1;

	static final int MONTH_MG = 2;

	static final int DAY_MG = 3;

	static final int HOUR_MG = 4;

	static final int MIN_MG = 5;

	static final int SEC_MG = 6;

	static final int TZ_MG = 7;

	static final int TZ_SIGN = 8;

	static final int TZ_HOUR_MG = 9;

	static final int TZ_MIN_MG = 10;

	static final Pattern PATTERN =
    Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2}\\.?\\d*)(Z|(\\+|-)(\\d{2}):(\\d{2}))");

	public XMLCalendar(long time) {
		this.setTimeInMillis(time);
	}

	public XMLCalendar(String dateTime) {
		super();
		Matcher m = PATTERN.matcher(dateTime);
		if (m.matches()) {
      this.set(Integer.valueOf(m.group(YEAR_MG)), Integer.valueOf(m.group(MONTH_MG)),
          Integer.valueOf(m.group(DAY_MG)), Integer.valueOf(m.group(HOUR_MG)),
          Integer.valueOf(m.group(MIN_MG)));
      BigDecimal sec = new BigDecimal(m.group(SEC_MG));
      this.set(SECOND, sec.intValue());
      BigDecimal fraction = sec.subtract(sec.setScale(0, BigDecimal.ROUND_DOWN));
      int millisec = fraction.movePointRight(3).intValue();
      this.set(Calendar.MILLISECOND, millisec);

      String tz = m.group(TZ_MG);
      if ("Z".equals(tz)) {
        this.setTimeZone(TimeZone.getTimeZone("GMT"));
      }
      else {
        int hr = Integer.valueOf(m.group(TZ_HOUR_MG));
        int min = Integer.valueOf(m.group(TZ_MIN_MG));
        boolean plus = "+".equals(m.group(TZ_SIGN));
        this.setTimeZone(new SimpleTimeZone((hr * 60 + min) * (plus ? 1 : -1) * 60 * 1000, ""));
      }
    }
    else {
      throw new IllegalArgumentException("Bad dateTime: " + dateTime);
    }
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(64);
		sb.append(Integer.toString(get(Calendar.YEAR)));
		sb.append('-');
		formatNumber(sb, get(Calendar.MONTH), 2);
		sb.append('-');
		formatNumber(sb, get(Calendar.DAY_OF_MONTH), 2);
		sb.append('T');
		formatNumber(sb, get(Calendar.HOUR_OF_DAY), 2);
		sb.append(':');
		formatNumber(sb, get(Calendar.MINUTE), 2);
		sb.append(':');
		formatNumber(sb, get(Calendar.SECOND), 2);
		int ms = get(Calendar.MILLISECOND);
		if (ms > 0) {
			String str = Double.toString(ms / 1000d);
			for (int i = 0; i < str.length(); ++i) {
				if (str.charAt(0) == '.')
					break;
				str = str.substring(1);
			}
			sb.append(str);
		}

		int offsetInMinutes = (get(Calendar.ZONE_OFFSET) + get(Calendar.DST_OFFSET))
				/ (60 * 1000);

		if (offsetInMinutes == 0) {
			sb.append('Z');
		} else {
			if (offsetInMinutes < 0) {
				sb.append('-');
				offsetInMinutes *= -1;
			} else {
				sb.append('+');
			}
			formatNumber(sb, offsetInMinutes / 60, 2);
			sb.append(':');
			formatNumber(sb, offsetInMinutes % 60, 2);
		}
		return sb.toString();
	}

	private static void formatNumber(StringBuffer sb, int num, int digits) {
		String s = Integer.toString(num);
		for (int i = s.length(); i < digits; ++i) {
			sb.append('0');
    }
		sb.append(s);
	}
}
