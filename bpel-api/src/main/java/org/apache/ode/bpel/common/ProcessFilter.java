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

import org.apache.ode.bpel.pmapi.InvalidRequestException;
import org.apache.ode.utils.ISO8601DateParser;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Holds a filter that will get interpreted when listing processes. The semantic
 * of the filtering is somewhat different than the one used in the ProcessQuery
 * class. Here we're introducing a pseudo process querying language.
 */
public class ProcessFilter extends Filter<ProcessFilter.FilterKeysOp> implements
    Serializable {

  private static final long serialVersionUID = 9999;

  /** If set, will filter on the process name (accepts ending with wildcard) */
  private String nameFilter;

  /** If set, will filter on the process name (accepts ending with wildcard) */
  private String namespaceFilter;

  /** If set, will filter on the process status (activated or retired) */
  private String statusFilter;

  /**
   * If set, will filter on the process deployment date. Prefixed with a
   * comparison operator (<, >, <=, >=, =). We're keeping a string and note
   * converting to a java date as ISO string dates are much easier and quicker
   * to manipulate. It's possible to have more than one date filter to handle
   * the 'between' case.
   */
  private List<String> deployedDateFilter;

  /**
   * List of additional information to include when producing the filter result,
   * currently only 'properties' and 'instance' are supported.
   */
  private List<String> includes;

  /**
   * Orders to use when sorting the result (no particular order if not set).
   * Currently /supported keys are:
   * <ul>
   * <li>name</li>
   * <li>namespace</li>
   * <li>version</li>
   * <li>status</li>
   * <li>deployed</li>
   * </ul>
   * Each key can be prefixed with a + or - sign for ascending or descending
   * orders (ascending if no sign specified)..
   */
  private List<String> orders;

  // Used both to validate filter keys and to process them
  enum FilterKeysOp {
    NAME {
      void process(ProcessFilter filter, String op, String value) {
        filter.nameFilter = value;
      }
    },
    NAMESPACE {
      void process(ProcessFilter filter, String op, String value) {
        filter.namespaceFilter = value;
      }
    },
    STATUS {
      void process(ProcessFilter filter, String op, String value) {
        filter.statusFilter = value;
      }
    },
    DEPLOYED {
      void process(ProcessFilter filter, String op, String value) {
        if (filter.deployedDateFilter == null)
          filter.deployedDateFilter = new ArrayList<String>();
        filter.deployedDateFilter.add(op + value);
      }
    };

    abstract void process(ProcessFilter filter, String op, String value);
  }

  enum OrderKeys {
    NAME, NAMESPACE, VERSION, STATUS, DEPLOYED;
  }

  enum StatusKeys {
    ACTIVATED, RETIRED;
  }

  /**
   * Initializes properly the ProcessFilter attributes by pre-parsing the filter
   * and orderKeys strings.
   *
   * @param filter
   * @param orderKeys
   */
  public ProcessFilter(String filter, String orderKeys) {
    init(filter);
    // Some additional validation on status value
    if (statusFilter != null) {
      try {
        StatusKeys.valueOf(statusFilter.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new InvalidRequestException(
            "The status you're using in your filter isn't valid, "
                + "only the activated and retired status are valid. "
                + e.toString());
      }
    }

    // Some additional validation on date format value
    if (deployedDateFilter != null) {
      for (String ddf : deployedDateFilter) {
        try {
          ISO8601DateParser.parse(getDateWithoutOp(ddf));
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
          OrderKeys.valueOf(justKey.toUpperCase());
          orders.add(orderKey);
        } catch (IllegalArgumentException e) {
          throw new InvalidRequestException(
              "One of the ordering keys isn't valid, processes can only "
                  + "be sorted by name, namespace, version, status and deployed date."
                  + e.toString());
        }
      }
    }
  }

  public String getNameFilter() {
    return nameFilter;
  }

  public String getNamespaceFilter() {
    return namespaceFilter;
  }

  public String getStatusFilter() {
    return statusFilter;
  }

  public List<String> getDeployedDateFilter() {
    return deployedDateFilter;
  }

  public List<String> getIncludes() {
    return includes;
  }

  public List<String> getOrders() {
    return orders;
  }

  public static void main(String[] args) {
    ProcessFilter pf = new ProcessFilter("name = dtc* status=activated "
        + "deployed>=2005-11-29T15:12 deployed < 2005-11-29T15:13",
        "status name -version");
    System.out.println("=> " + pf);
  }

  @Override
  protected FilterKeysOp parseKey(String keyVal) {
    return FilterKeysOp.valueOf(keyVal);
  }

  @Override
  protected FilterKeysOp[] getFilterKeys() {
    return FilterKeysOp.values();
  }

  @Override
  protected void process(FilterKeysOp key, Restriction<String> rest) {
    key.process(this, rest.op, rest.value);

  }
}
