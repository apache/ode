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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Support for xml schema duration.
 *
 * Does NOT support negative duration, only positive!!
 */
public class Duration {
    static final Pattern PATTERN =
            Pattern.compile("P(\\d+Y)?(\\d+M)?(\\d+D)?(T(\\d+H)?(\\d+M)?((\\d+\\.?\\d*|\\.\\d+)S)?)?");

    static final int YEAR_MG = 1;
    static final int MONTH_MG = 2;
    static final int DAY_MG = 3;
    static final int HOUR_MG = 5;
    static final int MIN_MG = 6;
    static final int SEC_MG = 8;

    private int _years;
    private int _months;
    private int _days;
    private int _hours;
    private int _minutes;
    private BigDecimal _seconds;

    /**
     *
     */
    public Duration(String duration) {
        Matcher m = PATTERN.matcher(duration);
        if(m.matches()){
            _years = parseInt(m.group(YEAR_MG));
            _months = parseInt(m.group(MONTH_MG));
            _days = parseInt(m.group(DAY_MG));
            _hours = parseInt(m.group(HOUR_MG));
            _minutes = parseInt(m.group(MIN_MG));
            _seconds = m.group(SEC_MG) == null
                ? null : new BigDecimal(m.group(SEC_MG));
        }
        else{
            throw new IllegalArgumentException("Bad duration: " + duration);
        }
    }

    /**
     * Adds current duration to a calendar object.
     * @param calendar
     */
    public void addTo(Calendar calendar){

        calendar.add( Calendar.YEAR, _years );
      calendar.add( Calendar.MONTH, _months );
      calendar.add( Calendar.DAY_OF_MONTH, _days );
      calendar.add( Calendar.HOUR, _hours );
      calendar.add( Calendar.MINUTE, _minutes );
      calendar.add( Calendar.SECOND,  (_seconds == null)
            ? 0 : _seconds.intValue());

      if(_seconds!=null) {
      BigDecimal fraction = _seconds.subtract(_seconds.setScale(0,BigDecimal.ROUND_DOWN));
      int millisec = fraction.movePointRight(3).intValue();
      calendar.add( Calendar.MILLISECOND, millisec );
      }
    }

    private static int parseInt(String value){
        if(value == null)
            return 0;
        else{
            return Integer.parseInt(value.substring(0, value.length()-1));
        }
    }
}
