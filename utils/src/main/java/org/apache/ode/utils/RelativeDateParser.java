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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

public class RelativeDateParser {
    private static final char YEAR_TEMPLATE_MODIFIER = 'y';
    
    private static final char MONTH_TEMPLATE_MODIFIER = 'M';
   
    private static final char WEEK_TEMPLATE_MODIFIER = 'w';
   
    private static final char DAY_TEMPLATE_MODIFIER = 'd';
   
    private static final char HOUR_TEMPLATE_MODIFIER = 'h';
   
    private static final char MINUTE_TEMPLATE_MODIFIER = 'm';
   
    private static final char SECOND_TEMPLATE_MODIFIER = 's';
   
    /**
     * This array of strings contains all supported "modifiers" for method <code>parseRelativeDate</code>.
     * NOTE: An order of elements in this array is important and used in algorithm of above method!
     */
    private static final char[] TEMPLATE_MODIFIERS_LIST = new char[] {
        YEAR_TEMPLATE_MODIFIER, MONTH_TEMPLATE_MODIFIER,
        WEEK_TEMPLATE_MODIFIER, DAY_TEMPLATE_MODIFIER,
        HOUR_TEMPLATE_MODIFIER, MINUTE_TEMPLATE_MODIFIER,
        SECOND_TEMPLATE_MODIFIER, };
  
    /**
     * Returns a date which is less than current system date/time on a specified number of days, years, minutes, weeks
     * etc.<p/>
     * A common template which is valid for this method is like that:<br/>
     * <strong>XXXXy XXXXM XXXXw XXXXd XXXXh XXXXm XXXXs</strong>, where:
     * <pre>
     * XXXX - any positive number composed of four or less digits, but not less that one digit
     * y - this symbol represents how many years must be subtracted from current system date
     * M - this symbol represents how many months must be subtracted from current system date
     * w - this symbol represents how many weeks must be subtracted rom current system date
     * d - this symbol represents how many days must be subtracted from current system date
     * h - this symbol represents how many hours must be subtracted from current system date
     * m - this symbol represents how many minutes must be subtracted from current system date
     * s - this symbol represents how many seconds must be subtracted from current system date
     * </pre>
     * Above symbols are called "modifiers".
     * <p><em>Modifiers are case sensitive</em>.
     * Modifiers separated by single space character or sequence of spaces.
     * Each modifier is optional, but an order of modifiers is matter and must be exactly the same
     * how its specified in template.</p>
     * <p/>
     * Here is a few examples of date templates and its meanings:<br/>
     * <code>"4h 3m 23s"</code> - means 4 hours, 3 minutes and 23 seconds ago from current system date<br/>
     * <code>"1y 2m 3w"</code> - means 1 year, 2 months and 3 weeks ago from current system date<br/>
     * <code>"1m 1w 13h 5m"</code> - means 1 month, 1 week, 13 hours and 5 minutes ago from current system date<br/>
     *
     * @param dateTemplate a template of a date we want to be calculated
     * @return a resulted <code>Date</code> object that is applies specified template
     * @throws java.text.ParseException if some problems were occurred while parsing specified date template
     */
    public static Date parseRelativeDate(String dateTemplate)
        throws java.text.ParseException {
      int[] agoValues = new int[TEMPLATE_MODIFIERS_LIST.length];
      int currentModifierPointer = 0;
   
      StringTokenizer tokens = new StringTokenizer(dateTemplate.trim());
      while (tokens.hasMoreTokens()) {
        String token = tokens.nextToken();
        if (token.length() < 2 || token.length() > 5) {
          throw new ParseException("Invalid token length. Token: "
              + token, dateTemplate.indexOf(token));
        }
   
        int modValue;
        try {
          modValue = Integer.parseInt(token.substring(0,
              token.length() - 1));
          if (modValue <= 0) {
            throw new ParseException(
                "Modifier value must be a positive number. Token: "
                    + token, dateTemplate.indexOf(token));
          }
        } catch (NumberFormatException nfe) {
          throw new ParseException("Can't parse integer value. Token: "
              + token, dateTemplate.indexOf(token));
        }
   
        char mod = token.charAt(token.length() - 1);
        while (true) {
          if (currentModifierPointer >= TEMPLATE_MODIFIERS_LIST.length) {
            throw new ParseException(
                "Incorrect modifier at this position. Token: "
                    + token, dateTemplate.indexOf(token)
                    + token.length() - 1);
          } else if (mod != TEMPLATE_MODIFIERS_LIST[currentModifierPointer]) {
            currentModifierPointer++;
            //continue;
          } else if (agoValues[currentModifierPointer] != 0) {
            throw new ParseException(
                "Dublicated modifier found. Token: " + token,
                dateTemplate.indexOf(token) + token.length() - 1);
          } else {
            agoValues[currentModifierPointer++] = modValue;
            break;
          }
        }
      }//while (tokens)
   
      final Calendar calendar = GregorianCalendar.getInstance();
      for (int i = 0; i < agoValues.length; i++) {
        if (agoValues[i] == 0) {
          continue;
        }
        int calendarField = templateModifierIndexToCalendarField(i);
        calendar.add(calendarField, 0 - agoValues[i]);
      }
   
      return calendar.getTime();
    }
   
    private static int templateModifierIndexToCalendarField(int modifierIndex)
        throws IllegalArgumentException {
      switch (modifierIndex) {
      case 0:
        return Calendar.YEAR;
      case 1:
        return Calendar.MONTH;
      case 2:
        return Calendar.WEEK_OF_MONTH;
      case 3:
        return Calendar.DAY_OF_MONTH;
      case 4:
        return Calendar.HOUR_OF_DAY;
      case 5:
        return Calendar.MINUTE;
      case 6:
        return Calendar.SECOND;
      }
      throw new IllegalArgumentException("Invalid template modifier index: "
          + modifierIndex);
    }
}
