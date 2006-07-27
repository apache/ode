package org.apache.ode.daohib.bpel;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.common.Filter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.utils.ISO8601DateParser;

/**
 * Class used for converting "filter" objects into Hibernate
 * {@link org.hibernate.Criteria} objects.
 */
class CriteriaBuilder {

  /**
   * Build a Hibernate {@link Criteria} from an instance filter.
   * @param crit target (destination) criteria
   * @param filter filter
   */
  void buildCriteria(Criteria crit, InstanceFilter filter) {
    Criteria processCrit = crit.createCriteria("process");

    // Filtering on PID
    if (filter.getPidFilter() != null)
      processCrit.add(Restrictions.like("processId",filter.getPidFilter().replaceAll("\\*","%")));
    
    if (filter.getIidFilter() != null)
      crit.add(Restrictions.eq("id",new Long(filter.getIidFilter())));
    
    // Filtering on name and namespace
    if (filter.getNameFilter() != null) {
      processCrit.add(Restrictions.like("processName", filter.getNameFilter().replaceAll("\\*", "%")));
    }
    if (filter.getNamespaceFilter() != null) {
      processCrit.add(Restrictions.like("processNamespace", filter.getNamespaceFilter().replaceAll("\\*", "%")));
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
      for (Map.Entry corValue : filter.getPropertyValuesFilter().entrySet()) {
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
          if (ascending) processCrit.addOrder(Property.forName("processName").asc());
          else processCrit.addOrder(Property.forName("processName").desc());
        } else if ("namespace".equals(orderKey)) {
          if (ascending) processCrit.addOrder(Property.forName("processNamespace").asc());
          else processCrit.addOrder(Property.forName("processNamespace").desc());
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
      realDate = ISO8601DateParser.parse(getDateWithoutOp(prefixedDate));
    } catch (ParseException e) {
      // Never occurs, the deploy date format is pre-validated by the filter
    }
    addFilterOnPrefixedDate(crit,prefixedDate,realDate,dateAttribute);
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
