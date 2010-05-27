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
package org.apache.ode.bpel.pmapi;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;

/**
 * Interface to managing process instances.
 */
public interface InstanceManagement {

    /**
     * <p>
     * Retrieve and returns information about all, or some process instances.
     * The request identifies the process instances using a filter that can
     * select instances with a given name, status, property values, etc.
     * Without a filter, the operation returns all process instances up to a
     * specified <code>limit<.code>. The request also indicates which key fields
     * to use for ordering the results.
     * </p>
     *
     * <p>
     * The filter element can be used to narrow down the list of process definitions
     * by applying selection criteria. There are six filters that can be applied:
     * <ul>
     * <li><p>name -- Only process instances with this local name.</p></li>
     * <li><p>namespace -- Only process instances with this namespace URI.</p></li>
     * <li><p>status -- Only process instances with these status code(s).</p></li>
     * <li><p>started -- Only process instances started relative to this date/time.</p></li>
     * <li><p> last-active -- Only process instances last active relative to this date/time.</p></li>
     * <li><p>$property -- Only process instances with a correlation property equal to the
     * specified value.</p></li>
     * </ul>
     *
     * </p>
     * <p>
     * The name and namespace filters can do full or partial name matching. Partial matching
     * occurs if either filter ends with an asterisk (*). These filters are not case sensitive,
     * for example name=my* will match MyProcess and my-process. If unspecified, the default
     * filter is name=* namespace=*.
     * </p>
     *
     * <p>
     * The status filter can be used to filter all process definitions based on six status codes:
     * <ul>
     * <li><p>active -- All currently active process instances (excludes instances in any other
     * state).</p></li>
     * <li><p>suspended -- All process instances that have not completed, but are currently
     * suspended.</p></li>
     * <li><p>error -- All process instances that have not completed, but are currently indicate an
     * error condition.</p></li>
     * <li><p>completed -- All successfully completed process instances (excludes instances in any
     * other state). </p></li>
     * <li><p>terminated -- All process instances that were terminated.
     * <li><p>faulted -- All process instances that encountered a fault (in the global scope).
     * </ul>
     * <p>
     * The started filter can be used to filter all process instances started on or after a
     * particular date or date/time instant. The value of this filter is either an ISO-8601
     * date or ISO-8601 date/time. For example, to find all process instances started on or
     * after September 1, 2005, use started>=20050901. Similarly, the last-active filter can
     * be used to filter all process instances based on their last active time. The last
     * active time records when the process last completed performing work, and either
     * completed or is now waiting to receive a message, a timeout or some other event.
     * </p>
     *
     * <p>
     * Each process instance has one or more properties that are set its instantiation, that
     * can be used to distinguish it from other process instances. In this version of the
     * specification, we only support properties instantiated as part of correlation sets
     * defined in the global scope of the process. For example, if a process instantiates a
     * correlation set that uses the property order-id, it is possible to filter that process
     * instance based on the value of that property.
     * </p>
     *
     * <p>
     * The property name is identified by the prefix $. If the property name is an NCName,
     * the filter will match all properties with that local name. If the property name is
     * {namespace}local, the filter will match all properties with the specified namespace URI
     * and local name. For example, to retrieve a list of all active process instances with a
     * property order-id that has the value 456, use status=active $order-id=456.
     * </p>
     *
     * <p>
     * By default the response returns process instances in no particular order. The order
     * element can be used to order the results by specifying a space-separated list of keys.
     * Each key can be prefixed with a plus sign '+' to specify ascending order, or a '-'
     * minus sign to specify descending order. Without a sign the default behavior is to
     * return process instances in ascending order. The currently supported odering keys are:
     * <ul>
     * <li><p>pid</p></li> -- Order based on the process identifier.
     * <li><p>name</p></li> -- Order based on the local name of the process instance.
     * <li><p>namespace</p></li> -- Order based on the namespace URI of the process instance.
     * <li><p>version</p></li> -- Order based on the version number.
     * <li><p>status</p></li> -- Order based on the status of the process instance.
     * <li><p>started</p></li> -- Order based on the process instance start date/time.
     * <li><p>last-active</p></li> -- Order based on the process instance last active date/time.
     * </ul>
     *
     * @param filter filter string
     * @param order order keys
     * @param limit maximum number of instances to return
     * @return list of matching instances
     */
    InstanceInfoListDocument listInstances(String filter, String order, int limit);

    /**
     * List instances and only return summary information about the instance,
     * combined with all correlation properties.
     *
     * @param filter See listInstances' filter argument
     * @param order  See listInstances' order argument
     * @param limit maximum number of instances to return
     * @return list of matching instances
     */
    InstanceInfoListDocument listInstancesSummary(String filter, String order, int limit);

    /**
     * @deprecated As of Ode 1.3, this method is deprecated in favor of
     *             listInstances(filter, order, limit)
     */
    InstanceInfoListDocument queryInstances(String query);

    /**
     * List all instances in the default (database) order.
     * @see #listInstances(String, String, int)
     * @return list of matching instances
     * @deprecated As of Ode 1.3, this method is deprecated in favor of
     *             listInstancesSummary(filter, order, limit)
     */
    InstanceInfoListDocument listAllInstances();

    /**
     * List up to <code>limit</code> instances in the default (database) order.
     * @see #listInstances(String, String, int)
     * @param limit maximum number of instances to return
     * @return list of matching instances
     * @deprecated As of Ode 1.3, this method is deprecated in favor of
     *             listInstancesSummary(filter, order, limit)
     */
    InstanceInfoListDocument listAllInstancesWithLimit(int limit);

    /**
     * Get an instance by id.
     * @param iid
     * @return information about a specific instance
     * @throws InstanceNotFoundException TODO
     */
    InstanceInfoDocument getInstanceInfo(Long iid) throws InstanceNotFoundException;

    /**
     * Get info about a scope instance by id, not including activity info.
     * @see #getScopeInfoWithActivity(String, boolean)
     * @param siid scope instance identifier
     * @return information about a specific scope instance
     */
    ScopeInfoDocument getScopeInfo(String siid);


    /**
     * Get info about a scope instance by id, optionally including activity info.
     * @param siid scope instance identifier
     * @param activityInfo if <code>true</code>, include activity info
     * @return information about a specific scope instance
     */
    ScopeInfoDocument getScopeInfoWithActivity(String siid, boolean activityInfo);

    /**
     * Get info about a variable.
     * @param scopeId scope identifier
     * @param varName variable name
     * @return information about variable (basically the value)
     */
    VariableInfoDocument getVariableInfo(String scopeId, String varName) ;

    /**
     * Retrieve BPEL events. One may specify an "instance filter" and an "event filter" to
     * limit the number of events returned. The instance filter takes the exact same syntax
     * as for the {@link #listInstances(String, String, int)} method. The "event filter" employs
     * a similar syntax; the following properties may be filtered: <ol>
     * <li><em>type</em> -  the event type</li>
     * <li><em>tstamp</em> - the event timestamp</li>
     * </ol>
     * @param instanceFilter instance filter (if set,return only events for matching instances)
     * @param eventFilter event filter (event type and data range)
     * @return list of events
     */
    EventInfoListDocument listEvents(String instanceFilter, String eventFilter, int maxCount);

    /**
     * Retrieve a timeline of BPEL events.
     *
     * @param instanceFilter instance filter (if set,return only events for matching instances)
     * @param eventFilter event filter (event type and data range)
     * @return list of stringified dates (in ISO format)
     */
    List<String> getEventTimeline(String instanceFilter, String eventFilter);


    /**
     * Changes the process state from active to suspended. this affects process instances that
     * are in the active or error states.

     * @param iid instance id
     * @return post-change instance information
     */
    InstanceInfoDocument suspend(Long iid);


    /**
     * Resume the (previously suspended) instance. This operation only affects process instances
     * that are in the suspended state.
     * @param iid instance id
     * @return post-change instance information
     */
    InstanceInfoDocument resume(Long iid);

    /**
     * Causes the process instance to terminate immediately, without a chance to
     * perform any fault handling or compensation. The process transitions to the
     * terminated state. It only affects process instances that are in the active,
     * suspended or error states.
     * @param iid instance id
     * @return post-change instance information
     */
    InstanceInfoDocument terminate(Long iid);

    /**
     *
     * Causes the process instance to complete unsuccessfully by throwing the specified
     * fault in the global scope. The process is able to perform recovery using a fault
     * handler in the global scope, through termination handlers in nested scopes and
     * by invoking installed compensation handlers. The process will transition to the
     * <em>faulted</em> state.
     * @param iid instance id
     * @param faultname name of the fault
     * @param faultData fault data
     * @return post-change instance information
     */
    InstanceInfoDocument fault(Long iid, QName faultname, Element faultData);

    /**
     * Delete the process instances matching the given filter.
     * @param filter instance filter (see {@link #listInstances(String, String, int)} ).
     * @return collection of instances identfiers, corresponding to deleted
     *         instances
     */
    Collection<Long> delete(String filter);

    /**
     * Performs an activity recovery action.
     * @param iid instance id (process)
     * @param aiid instance id (activity)
     * @param action recovery action (e.g. retry, fault)
     * @return post-change instance information
    */
    InstanceInfoDocument recoverActivity(Long iid, Long aid, String action);

}
