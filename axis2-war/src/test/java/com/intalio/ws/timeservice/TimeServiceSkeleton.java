/**
 * TimeServiceSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.1  Built on : Oct 19, 2009 (10:59:00 EDT)
 */
package com.intalio.ws.timeservice;

/**
 * TimeServiceSkeleton java skeleton for the axisService
 */
public class TimeServiceSkeleton {

    /**
     * Auto generated method signature
     * 
     * @param getCityTime
     */

    public com.intalio.ws.timeservice.GetCityTimeResponse getCityTime(
            com.intalio.ws.timeservice.GetCityTime getCityTime) {
        GetCityTimeResponse r = new GetCityTimeResponse();
        r.setGetCityTimeResult("2010-01-30T13:45:34");
        return r;
    }

    /**
     * Auto generated method signature
     * 
     * @param getUTCTime
     */

    public com.intalio.ws.timeservice.GetUTCTimeResponse getUTCTime(
            com.intalio.ws.timeservice.GetUTCTime getUTCTime) {
        GetUTCTimeResponse r = new GetUTCTimeResponse();
        r.setGetUTCTimeResult("2010-02-31T14:46:34");
        return r;
    }

}
