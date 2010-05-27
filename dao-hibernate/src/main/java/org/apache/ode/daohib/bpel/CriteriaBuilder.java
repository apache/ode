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

package org.apache.ode.daohib.bpel;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.common.Filter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.utils.ISO8601DateParser;
import org.apache.ode.utils.RelativeDateParser;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

/**
 * Class used for converting "filter" objects into Hibernate
 * {@link org.hibernate.Criteria} objects.
 */
class CriteriaBuilder {
    static final Log __log = LogFactory.getLog(CriteriaBuilder.class);

    /**
     * Build a HQL query from an instance filter.
     * @param filter filter
     */
    Query buildHQLQuery(Session session, InstanceFilter filter) {
        Map<String, Object> parameters = new HashMap<String, Object>();

        StringBuffer query = new StringBuffer();

        query.append("select pi from HProcessInstance as pi left join fetch pi.fault ");

        if (filter != null) {
            // Building each clause
            ArrayList<String> clauses = new ArrayList<String>();

            // iid filter
            if ( filter.getIidFilter() != null ) {
                StringBuffer filters = new StringBuffer();
                List<String> iids = filter.getIidFilter();
                for (int m = 0; m < iids.size(); m++) {
                    filters.append(" pi.id = :iid").append(m);
                    parameters.put("iid" + m, Long.parseLong(iids.get(m)));
                    if (m < iids.size() - 1) filters.append(" or");
                }
                clauses.add(" (" + filters + ")");
            }

            // pid filter
            if (filter.getPidFilter() != null) {
                StringBuffer filters = new StringBuffer();
                List<String> pids = filter.getPidFilter();
                String cmp;
                if (filter.arePidsNegative()) {
                    cmp = " != ";
                } else {
                    cmp = " = ";
                }
                for (int m = 0; m < pids.size(); m++) {
                    filters.append(" pi.process.processId ").append(cmp).append(" :pid").append(m);
                    parameters.put("pid" + m, pids.get(m));
                    if (m < pids.size() - 1) filters.append(" or");
                }
                clauses.add(" (" + filters + ")");
            }

            // name filter
            if (filter.getNameFilter() != null) {
                clauses.add(" pi.process.typeName like :pname");
                parameters.put("pname", filter.getNameFilter().replaceAll("\\*", "%"));
            }

            // name space filter
            if (filter.getNamespaceFilter() != null) {
                clauses.add(" pi.process.typeNamespace like :pnamespace");
                parameters.put("pnamespace", filter.getNamespaceFilter().replaceAll("\\*", "%"));
            }

            // started filter
            if (filter.getStartedDateFilter() != null) {
                for ( String ds : filter.getStartedDateFilter() ) {
                    // named parameters not needed as date is parsed and is hence not
                    // prone to HQL injections
                    clauses.add(" pi.created " + dateFilter(ds));
                }
            }

            // last-active filter
            if (filter.getLastActiveDateFilter() != null) {
                for ( String ds : filter.getLastActiveDateFilter() ) {
                    // named parameters not needed as date is parsed and is hence not
                    // prone to HQL injections
                    clauses.add(" pi.lastActiveTime " + dateFilter(ds));
                }
            }

            // status filter
            if (filter.getStatusFilter() != null) {
                StringBuffer filters = new StringBuffer();
                List<Short> states = filter.convertFilterState();
                for (int m = 0; m < states.size(); m++) {
                    filters.append(" pi.state = :pstate").append(m);
                    parameters.put("pstate" + m, states.get(m));
                    if (m < states.size() - 1) filters.append(" or");
                }
                clauses.add(" (" + filters.toString() + ")");
            }

            // $property filter
            if (filter.getPropertyValuesFilter() != null) {
                Map<String,String> props = filter.getPropertyValuesFilter();
                // join to correlation sets
                query.append(" inner join pi.correlationSets as cs");
                int i = 0;
                for (String propKey : props.keySet()) {
                    i++;
                    // join to props for each prop
                    query.append(" inner join cs.properties as csp"+i);
                    // add clause for prop key and value

                    // spaces have to be escaped, might be better handled in InstanceFilter
                    String value = props.get(propKey).replaceAll("&#32;", " ");
                    if (propKey.startsWith("{")) {
                        String namespace = propKey.substring(1, propKey.lastIndexOf("}"));
                        clauses.add(" csp" + i + ".name = :cspname" + i +
                                " and csp" + i + ".namespace = :cspnamespace" + i +
                                " and csp" + i + ".value = :cspvalue" + i);

                        parameters.put("cspname" + i, propKey.substring(propKey.lastIndexOf("}") + 1, propKey.length()));
                        parameters.put("cspnamespace" + i, namespace);
                        parameters.put("cspvalue" + i, value);
                    } else {
                        clauses.add(" csp" + i + ".name = :cspname" + i +
                                " and csp" + i + ".value = :cspvalue" + i);

                        parameters.put("cspname" + i, propKey);
                        parameters.put("cspvalue" + i, value);
                    }
                }
            }

            // order by
            StringBuffer orderby = new StringBuffer("");
            if (filter.getOrders() != null) {
                orderby.append(" order by");
                List<String> orders = filter.getOrders();
                for (int m = 0; m < orders.size(); m++) {
                    String field = orders.get(m);
                    String ord = " asc";
                    if (field.startsWith("-")) {
                        ord = " desc";
                    }
                    String fieldName = " pi.id";
                    if (field.endsWith("name")) {
                        fieldName = " pi.process.typeName";
                    }
                    if (field.endsWith("namespace")) {
                        fieldName = " pi.process.typeNamespace";
                    }
                    if ( field.endsWith("version")) {
                        fieldName = " pi.process.version";
                    }
                    if ( field.endsWith("status")) {
                        fieldName = " pi.state";
                    }
                    if ( field.endsWith("started")) {
                        fieldName = " pi.created";
                    }
                    if ( field.endsWith("last-active")) {
                        fieldName = " pi.lastActiveTime";
                    }
                    orderby.append(fieldName + ord);
                    if (m < orders.size() - 1) orderby.append(", ");
                }

            }

            // Preparing the statement
            if (clauses.size() > 0) {
                query.append(" where");
                for (int m = 0; m < clauses.size(); m++) {
                    query.append(clauses.get(m));
                    if (m < clauses.size() - 1) query.append(" and");
                }
            }

            query.append(orderby);
        }

        if (__log.isDebugEnabled()) {
            __log.debug(query.toString());
        }

        Query q = session.createQuery(query.toString());

        for (String p : parameters.keySet()) {
            q.setParameter(p, parameters.get(p));
        }

        if (filter.getLimit() != 0) {
            q.setMaxResults(filter.getLimit());
        }

        return q;
    }

    private static String dateFilter(String filter) {
        String date = Filter.getDateWithoutOp(filter);
        String op = filter.substring(0,filter.indexOf(date));
        Date dt = null;
        try {
            dt = ISO8601DateParser.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Timestamp ts = new Timestamp(dt.getTime());
        return op + " '" + ts.toString() + "'";
    }



  /**
   * Build a Hibernate {@link Criteria} from an instance filter.
   * @param crit target (destination) criteria
   * @param filter filter
   */
  void buildCriteria(Criteria crit, InstanceFilter filter) {
    Criteria processCrit = crit.createCriteria("process");

    // Filtering on PID
    List<String> pids = filter.getPidFilter();
    if (pids != null && pids.size() > 0) {
        Disjunction disj = Restrictions.disjunction();
        for (String pid: pids) {
            if( !filter.arePidsNegative() ) {
                disj.add(Restrictions.eq("processId", pid));
            } else {
                disj.add(Restrictions.ne("processId", pid));
            }
        }
        processCrit.add(disj);
    }

    List<String> iids = filter.getIidFilter();
    if (iids != null && iids.size() > 0) {
        Disjunction disj = Restrictions.disjunction();
        for (String iid: iids) {
            disj.add(Restrictions.eq("id", new Long(iid)));
        }
        crit.add(disj);
    }

    // Filtering on name and namespace
    if (filter.getNameFilter() != null) {
      processCrit.add(Restrictions.like("typeName", filter.getNameFilter().replaceAll("\\*", "%")));
    }
    if (filter.getNamespaceFilter() != null) {
      processCrit.add(Restrictions.like("typeNamespace", filter.getNamespaceFilter().replaceAll("\\*", "%")));
    }

    // Specific filter for status (using a disjunction between possible statuses)
    if (filter.getStatusFilter() != null) {
      List<Short> statuses = filter.convertFilterState();
      Disjunction disj = Restrictions.disjunction();
      for (short status : statuses) {
        disj.add(Restrictions.eq("state", status));
      }
      crit.add(disj);
    }

    // Specific filter for started and last active dates.
    if (filter.getStartedDateFilter() != null) {
      for (String sdf : filter.getStartedDateFilter()) {
        addFilterOnPrefixedDate(crit, sdf, "created");
      }
    }
    if (filter.getLastActiveDateFilter() != null) {
      for (String ladf : filter.getLastActiveDateFilter()) {
        addFilterOnPrefixedDate(crit, ladf,  "lastActiveTime");
      }
    }

    // Specific filter for correlation properties
    if (filter.getPropertyValuesFilter() != null) {
      Criteria propCrit = crit.createCriteria("correlationSets").createCriteria("properties");
      for (Map.Entry<String, String> corValue : filter.getPropertyValuesFilter().entrySet()) {
        String propName = (String)corValue.getKey();
        if (propName.startsWith("{")) {
          String namespace = propName.substring(1, propName.lastIndexOf("}"));
          propName = propName.substring(propName.lastIndexOf("}") + 1, propName.length());
          propCrit.add(Restrictions.eq("name", propName))
                  .add(Restrictions.eq("namespace", namespace))
                  .add(Restrictions.eq("value", corValue.getValue()));
        } else {
          propCrit.add(Restrictions.eq("name", corValue.getKey()))
                  .add(Restrictions.eq("value", corValue.getValue()));
        }
      }
    }

    // Ordering
    if (filter.orders != null) {
      for (String key : filter.orders) {
        boolean ascending = true;
        String orderKey = key;
        if (key.startsWith("+") || key.startsWith("-")) {
          orderKey = key.substring(1, key.length());
          if (key.startsWith("-")) ascending = false;
        }

        if ("name".equals(orderKey)) {
          if (ascending) processCrit.addOrder(Property.forName("typeName").asc());
          else processCrit.addOrder(Property.forName("typeName").desc());
        } else if ("namespace".equals(orderKey)) {
          if (ascending) processCrit.addOrder(Property.forName("typeNamespace").asc());
          else processCrit.addOrder(Property.forName("typeNamespace").desc());
        } else if ("pid".equals(orderKey)) {
          if (ascending) processCrit.addOrder(Property.forName("processId").asc());
          else processCrit.addOrder(Property.forName("processId").desc());
        } else if ("version".equals(orderKey)) {
          if (ascending) processCrit.addOrder(Property.forName("version").asc());
          else processCrit.addOrder(Property.forName("version").desc());
        } else if ("status".equals(orderKey)) {
          if (ascending) crit.addOrder(Property.forName("state").asc());
          else crit.addOrder(Property.forName("state").desc());
        } else if ("started".equals(orderKey)) {
          if (ascending) crit.addOrder(Property.forName("created").asc());
          else crit.addOrder(Property.forName("created").desc());
        } else if ("last-active".equals(orderKey)) {
          if (ascending) crit.addOrder(Property.forName("lastActiveTime").asc());
          else crit.addOrder(Property.forName("lastActiveTime").desc());
        }
      }
    }

    if (filter.getLimit() > 0) crit.setMaxResults(filter.getLimit());
  }

  /**
   * Build criteria for an event filter.
   * @param crit target criteria
   * @param efilter event filter
   */
  void buildCriteria(Criteria crit, BpelEventFilter efilter) {
    if (efilter.getTypeFilter() != null)
      crit.add(Restrictions.like("type", efilter.getTypeFilter().replace('*','%')));

    // Specific filter for started and last active dates.
    if (efilter.getTimestampFilter() != null) {
      for (Filter.Restriction<Date> sdf : efilter.getTimestampFilter()) {
        addFilterOnPrefixedDate(crit, sdf.op, sdf.value, "tstamp");
      }
    }

    if (efilter.limit > 0) crit.setMaxResults(efilter.limit);
  }

  void addScopeFilter(Criteria crit, String scopeId) {
    crit.add(Restrictions.eq("",scopeId));
  }

  static void addFilterOnPrefixedDate(Criteria crit, String prefixedDate, String dateAttribute) {
    Date realDate = null;
    try {
      realDate = parseDateExpression(getDateWithoutOp(prefixedDate));
    } catch (ParseException e) {
      // Never occurs, the deploy date format is pre-validated by the filter
    }
    addFilterOnPrefixedDate(crit,prefixedDate,realDate,dateAttribute);
  }

  private static Date parseDateExpression(String date) throws ParseException {
      if( date.toLowerCase().startsWith("-") && date.length() > 1 ) {
          return RelativeDateParser.parseRelativeDate(date.substring(1));
      } else {
          return ISO8601DateParser.parse(date);
      }
  }

  static void addFilterOnPrefixedDate(Criteria crit, String op, Date date, String dateAttribute) {
    if (op.startsWith("=")) {
      crit.add(Restrictions.eq(dateAttribute, date));
    } else if (op.startsWith("<=")) {
      crit.add(Restrictions.le(dateAttribute, date));
    } else if (op.startsWith(">=")) {
      crit.add(Restrictions.ge(dateAttribute, date));
    } else if (op.startsWith("<")) {
      crit.add(Restrictions.lt(dateAttribute, date));
    } else if (op.startsWith(">")) {
      crit.add(Restrictions.gt(dateAttribute, date));
    }
  }

  private static String getDateWithoutOp(String ddf) {
    return Filter.getDateWithoutOp(ddf);
  }


}
