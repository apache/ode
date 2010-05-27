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
package org.apache.ode.bpel.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.pmapi.InvalidRequestException;
import org.apache.ode.utils.ISO8601DateParser;
import org.apache.ode.utils.RelativeDateParser;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Holds a filter that will get interpreted when listing processe instances. The
 * semantic of the filtering is somewhat different than the one used in the
 * ProcessQuery class. Here we're introducing a pseudo process querying
 * language.
 */
public class InstanceFilter extends Filter<InstanceFilter.Criteria> implements Serializable {
    protected static Log LOG = LogFactory.getLog(InstanceFilter.class);

    private static final long serialVersionUID = 9999;

    /** If set, will filter on the instance id (IID) and select all matching instances */
    private List<String> iids;

    /** If set, will filter on the process id (PID) and select all matching process definitions */
    private List<String> pids;

    /** If set, the PID filter will work negatively */
    private boolean arePidsNegative;

    /** If set, will filter on the process name (accepts ending with wildcard) */
    private String nameFilter;

    /** If set, will filter on the process name (accepts ending with wildcard) */
    private String namespaceFilter;

    /**
     * If set, will filter on the instance status. Status being exclusive,
     * statuses are joined with an 'or'.
     */
    private List<String> statusFilter;

    /**
     * If set, will filter on the process started date. Prefixed with a comparison
     * operator (<, >, <=, >=, =). We're keeping a string and note converting to
     * a java date as ISO string dates are much easier and quicker to manipulate.
     * It's possible to have more than one date filter to handle the 'between'
     * case.
     */
    private List<String> startedDateFilter;

    /** If set, will filter on the process last active date. */
    private List<String> lastActiveDateFilter;

    private Map<String, String> propertyValuesFilter;

    /**
     * Orders to use when sorting the result (no particular order if not set).
     * Currently /supported keys are:
     * <ul>
     * <li>pid</li>
     * <li>name</li>
     * <li>namespace</li>
     * <li>version</li>
     * <li>status</li>
     * <li>started</li>
     * <li>last-active</li>
     * </ul>
     * Each key can be prefixed with a + or - sign for ascending or descending
     * orders (ascending if no sign specified)..
     */
    public List<String> orders;

    private int limit;

    /**
     * Known criteria (and a means to process them).
     */
    enum Criteria {
        IID {
            void process(InstanceFilter filter, String key, String op, String value) {
                filter.iids = parse(value);
            }
        },
        PID {
            void process(InstanceFilter filter, String key, String op, String value) {
                filter.pids = parse(value);
                filter.arePidsNegative = "<>".equals(op);
            }
        },
        NAME {
            void process(InstanceFilter filter, String key, String op, String value) {
                filter.nameFilter = value;
            }
        },
        NAMESPACE {
            void process(InstanceFilter filter, String key, String op, String value) {
                filter.namespaceFilter = value;
            }
        },
        STATUS {
            void process(InstanceFilter filter, String key, String op, String value) {
                filter.statusFilter = parse(value);
            }
        },
        STARTED {
            void process(InstanceFilter filter, String key, String op, String value) {
                if (filter.startedDateFilter == null)
                    filter.startedDateFilter = new ArrayList<String>();
                filter.startedDateFilter.add(op + value);
            }
        },
        LAST_ACTIVE {
            void process(InstanceFilter filter, String key, String op, String value) {
                if (filter.lastActiveDateFilter == null)
                    filter.lastActiveDateFilter = new ArrayList<String>();
                filter.lastActiveDateFilter.add(op + value);
            }
        },
        PROPERTY {
            void process(InstanceFilter filter, String key, String op, String value) {
                if (filter.propertyValuesFilter == null)
                    filter.propertyValuesFilter = new HashMap<String, String>(5);
                filter.propertyValuesFilter.put(key.substring(1, key.length()), value);
            }
        };

        abstract void process(InstanceFilter filter, String key, String op, String value);
    }

    enum OrderKeys {
        PID, NAME, NAMESPACE, VERSION, STATUS, STARTED, LAST_ACTIVE;
    }

    public enum StatusKeys {
        ACTIVE, SUSPENDED, ERROR, COMPLETED, TERMINATED, FAILED, FAULTED;
    }

    /**
     * Initializes properly the InstanceFilter attributes by pre-parsing the
     * filter and orderKeys strings and setting the limit. A limit inferior than
     * or equal to 0 is ignored.
     *
     * @param filter
     * @param orderKeys
     */
    public InstanceFilter(String filter, String orderKeys, int limit) {
        init(filter);

        // Some additional validation on status values
        if (statusFilter != null) {
            for (String status : statusFilter) {
                try {
                    StatusKeys.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new InvalidRequestException(
                            "The status you're using in your filter isn't valid, "
                                    + "only the active, suspended, error, completed, terminated and faulted status are "
                                    + "valid. " + e.toString());
                }
            }
        }
        // Some additional validation on date format value
        if (startedDateFilter != null) {
            for (String ddf : startedDateFilter) {
                try {
                    parseDateExpression(getDateWithoutOp(ddf));
                } catch (ParseException e) {
                    throw new InvalidRequestException(
                            "Couldn't parse one of the filter date, please make "
                                    + "sure it follows the ISO-8601 date or date/time standard (yyyyMMddhhmmss). "
                                    + e.toString());
                }
            }
        }
        if (lastActiveDateFilter != null) {
            for (String ddf : lastActiveDateFilter) {
                try {
                    parseDateExpression(getDateWithoutOp(ddf));
                } catch (ParseException e) {
                    throw new InvalidRequestException(
                            "Couldn't parse one of the filter date, please make "
                                    + "sure it follows the ISO-8601 date or date/time standard (yyyyMMddhhmmss). "
                                    + e.toString());
                }
            }
        }

        if (orderKeys != null && orderKeys.length() > 0) {
            orders = new ArrayList<String>(3);
            for (StringTokenizer orderKeysTok = new StringTokenizer(orderKeys, " "); orderKeysTok
                    .hasMoreTokens();) {
                String orderKey = orderKeysTok.nextToken();
                try {
                    String justKey = orderKey;
                    if (justKey.startsWith("-") || justKey.startsWith("+"))
                        justKey = orderKey.substring(1, justKey.length());
                    OrderKeys.valueOf(justKey.replaceAll("-", "_").toUpperCase());
                    orders.add(orderKey);
                } catch (IllegalArgumentException e) {
                    throw new InvalidRequestException(
                            "One of the ordering keys isn't valid, processes can only "
                                    + "be sorted by pid, name, namespace, version, status, started and last-active "
                                    + "date." + e.toString());

                }
            }
        }
        if(limit<0) {
            throw new IllegalArgumentException("Limit should be greater or equal to 0.");
        }
        this.limit = limit;
    }

    public InstanceFilter(String filter) {
        this(filter, null, Integer.MAX_VALUE);
    }

    private Date parseDateExpression(String date) throws ParseException {
        if( date.toLowerCase().startsWith("-")  && date.length() > 1 ) {
            return RelativeDateParser.parseRelativeDate(date.substring(1));
        } else {
            return ISO8601DateParser.parse(date);
        }
    }

    /**
     * Converts the status filter value as given by a filter ('active',
     * 'suspended', ...) to an instance state as defined in the ProcessState
     * class.
     *
     * @return one of the STATE_XX constant in ProcessState
     */
    public List<Short> convertFilterState() {
        List<Short> result = new ArrayList<Short>(5);
        short noState = 200;
        for (String status : statusFilter) {
            if (StatusKeys.ACTIVE.toString().equalsIgnoreCase(status)) {
                result.add(ProcessState.STATE_NEW);
                result.add(ProcessState.STATE_ACTIVE);
                result.add(ProcessState.STATE_READY);
            } else if (StatusKeys.SUSPENDED.toString().equalsIgnoreCase(status)) {
                result.add(ProcessState.STATE_SUSPENDED);
            } else if (StatusKeys.ERROR.toString().equalsIgnoreCase(status)) {
                result.add(noState); // Error instance state doesn't exist yet
            } else if (StatusKeys.COMPLETED.toString().equalsIgnoreCase(status)) {
                result.add(ProcessState.STATE_COMPLETED_OK);
            } else if (StatusKeys.TERMINATED.toString().equalsIgnoreCase(status)) {
                result.add(ProcessState.STATE_TERMINATED);
            } else if (StatusKeys.FAILED.toString().equalsIgnoreCase(status)) {
                result.add(ProcessState.STATE_COMPLETED_WITH_FAULT);
            } else {
                result.add(noState); // Non existent state
            }
        }
        return result;
    }

    public String getNameFilter() {
        return nameFilter;
    }

    public String getNamespaceFilter() {
        return namespaceFilter;
    }

    public List<String> getStatusFilter() {
        return statusFilter;
    }

    public List<String> getStartedDateFilter() {
        return startedDateFilter;
    }

    public List<String> getLastActiveDateFilter() {
        return lastActiveDateFilter;
    }

    public Map<String, String> getPropertyValuesFilter() {
        return propertyValuesFilter;
    }

    public List<String> getOrders() {
        return orders;
    }

    public List<String> getPidFilter() {
        return pids;
    }

    public boolean arePidsNegative() {
        return arePidsNegative;
    }

    public List<String> getIidFilter() {
        return iids;
    }

    public static void main(String[] args) {
        InstanceFilter instf = new InstanceFilter(
                "name = dtc* namespace=http://www.intalio.com* "
                        + "status=active|terminated started>=2005-11-29T15:15:19 started<2005-11-29T15:15:20 last-active < 2005-11-30 "
                        + "${http://ode.org/}order-id= 12 $shipping-id=aa45fz", "name started", 50);
        System.out.println(instf);
    }

    @Override
    protected Criteria parseKey(String keyVal) {
        if (keyVal.startsWith("$")) return Criteria.PROPERTY;
        else return Criteria.valueOf(keyVal);
    }

    @Override
    protected Criteria[] getFilterKeys() {
        return Criteria.values();
    }

    @Override
    protected void process(Criteria key, Restriction<String> rest) {
        key.process(this, rest.originalKey, rest.op, rest.value);
    }

    /**
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }


    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("InstanceFilter {");
        buf.append("iids="+iids);
        if( !arePidsNegative ) {
            buf.append(",pids="+pids);
        } else {
            buf.append(",-pids="+pids);
        }
        buf.append(",name="+nameFilter);
        buf.append(",namespace="+namespaceFilter);
        buf.append(",status="+statusFilter);
        buf.append(",startedDate="+startedDateFilter);
        buf.append(",lastActiveDate="+lastActiveDateFilter);
        buf.append(",propertyValues="+propertyValuesFilter);
        buf.append(",orders="+orders);
        buf.append(",limit="+limit);
        buf.append("}");
        return buf.toString();
    }

    private static List<String> parse(String pipeDelimited) {
        List<String> list = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(pipeDelimited, "|");
        while (tok.hasMoreTokens()) {
            list.add(tok.nextToken());
        }
        return list;
    }
}