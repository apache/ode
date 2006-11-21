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

package org.apache.ode.daohib.bpel.ql;

import org.apache.commons.lang.StringUtils;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.daohib.bpel.hobj.HProcessInstance;
import org.apache.ode.ql.Compiler;
import org.apache.ode.ql.eval.skel.AbstractConjunction;
import org.apache.ode.ql.eval.skel.AbstractDisjunction;
import org.apache.ode.ql.eval.skel.AbstractEqualityEvaluator;
import org.apache.ode.ql.eval.skel.CommandEvaluator;
import org.apache.ode.ql.eval.skel.ConjunctionEvaluator;
import org.apache.ode.ql.eval.skel.DisjunctionEvaluator;
import org.apache.ode.ql.eval.skel.EqualityEvaluator;
import org.apache.ode.ql.eval.skel.GEEvaluator;
import org.apache.ode.ql.eval.skel.GreaterEvaluator;
import org.apache.ode.ql.eval.skel.INEvaluator;
import org.apache.ode.ql.eval.skel.LEEvaluator;
import org.apache.ode.ql.eval.skel.LessEvaluator;
import org.apache.ode.ql.eval.skel.LikeEvaluator;
import org.apache.ode.ql.eval.skel.OrderByEvaluator;
import org.apache.ode.ql.tree.Builder;
import org.apache.ode.ql.tree.BuilderFactory;
import org.apache.ode.ql.tree.nodes.*;
import org.apache.ode.utils.ISO8601DateParser;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HibernateInstancesQueryCompiler extends Compiler<List, Session> {
  private static class DBFieldValueEq extends FieldValueEquality {
    protected final Object fieldValue;

    /**
     * @param identifier
     */
    public DBFieldValueEq(String identifier, Object fieldValue) {
      super(identifier);
      this.fieldValue = fieldValue;
    }

    /**
     * @see org.apache.ode.ql.eval.skel.CommandEvaluator#evaluate(java.lang.Object)
     */
    public Criterion evaluate(Object paramValue) {
      return Restrictions.eq(identifier, fieldValue);
    }
  }

  private abstract static class FieldValueEquality extends AbstractEqualityEvaluator<String, Criterion, Object> {

    /**
     * @param identifier
     */
    public FieldValueEquality(String identifier) {
      super(identifier);
    }

  }

  //
  private final static String INSTANCE_ID_FIELD = "iid";

  private final static String PROCESS_ID_FIELD = "pid";

  private final static String PROCESS_NAME_FIELD = "name";

  private final static String PROCESS_NAMESPACE_FIELD = "namespace";

  private final static String INSTANCE_STATUS_FIELD = "status";

  private final static String INSTANCE_STARTED_FIELD = "started";

  private final static String INSTANCE_LAST_ACTIVE_FIELD = "last-active";

  /*
   * private final static String CORRELATION_NAME_FIELD = "name"; private final static String
   * CORRELATION_NAMESPACE_FIELD = "namespace"; private final static String CORRELATION_NAMESPACE_FIELD = "namespace";
   */
  // DB fields
  private final static String INSTANCE_ID_DB_FIELD = "id";

  private final static String PROCESS_ID_DB_FIELD = "process.processId";

  private final static String PROCESS_NAME_DB_FIELD = "process.typeName";

  private final static String PROCESS_NAMESPACE_DB_FIELD = "process.typeNamespace";

  private final static String INSTANCE_STATUS_DB_FIELD = "state";

  private final static String PROPERTY_NS_DB_FIELD = "process.typeNamespace";

  private final static String PROPERTY_NAME_DB_FIELD = "property.name";

  private final static String PROPERTY_VALUE_DB_FIELD = "property.value";

  private final static String INSTANCE_STARTED_DB_FIELD = "created";

  private final static String INSTANCE_LAST_ACTIVE_DB_FIELD = "lastActiveTime";

  // status fields
  private final static String STATUS_ACTIVE = "active";

  private final static String STATUS_SUSPENDED = "suspended";

  private final static String STATUS_ERROR = "error";

  private final static String STATUS_COMPLETED = "completed";

  private final static String STATUS_TERMINATED = "terminated";

  private final static String STATUS_FAULTED = "failed";

  private final static Map<String, String> nodeIdentifierToDBField = new HashMap<String, String>(20);
  //Whether property is used in query
  private boolean propertyInQuery;
  //Whether ordering by status used
  private boolean orderByStatus;
  private boolean orderByStatusDesc;
  
  static {
    nodeIdentifierToDBField.put(INSTANCE_ID_FIELD, INSTANCE_ID_DB_FIELD);
    nodeIdentifierToDBField.put(INSTANCE_ID_FIELD, INSTANCE_ID_DB_FIELD);
    nodeIdentifierToDBField.put(PROCESS_ID_FIELD, PROCESS_ID_DB_FIELD);
    nodeIdentifierToDBField.put(PROCESS_NAME_FIELD, PROCESS_NAME_DB_FIELD);
    nodeIdentifierToDBField.put(PROCESS_NAMESPACE_FIELD, PROCESS_NAMESPACE_DB_FIELD);
    nodeIdentifierToDBField.put(INSTANCE_STARTED_FIELD, INSTANCE_STARTED_DB_FIELD);
    nodeIdentifierToDBField.put(INSTANCE_LAST_ACTIVE_FIELD, INSTANCE_LAST_ACTIVE_DB_FIELD);
    nodeIdentifierToDBField.put(INSTANCE_STATUS_FIELD, INSTANCE_STATUS_DB_FIELD);
  }

  private static String getDBField(String name) {
    String dbField = nodeIdentifierToDBField.get(name);

    if (dbField == null) {
      throw new IllegalArgumentException("Unsupported field " + name);
    }
    return dbField;
  }

  private void init() {
    propertyInQuery = false;
    orderByStatus = false;
    orderByStatusDesc = false;
  }
  
  @Override
  public CommandEvaluator<List, Session> compile(final Query node) {
    init();
    
    final OrderByEvaluator<Collection<Order>, Object> orderEvaluator = (node.getOrder() != null) ? compileOrderBy(node
        .getOrder()) : null;

    final CommandEvaluator<Criterion, Object> selectionEvaluator = node.getChilds().size() == 0 ? null
        : compileEvaluator(node.getChilds().iterator().next());
    
    final boolean joinCorrelationSet = propertyInQuery;
    final boolean sortByStatus = orderByStatus;
    final boolean sortByStatusDesc = orderByStatusDesc;
    final Limit limit = node.getLimit();
    
    return new CommandEvaluator<List, Session>() {
      public List evaluate(Session session) { 
        Criteria criteria = session.createCriteria(HProcessInstance.class).createAlias("process", "process");
        if(joinCorrelationSet) {
            criteria = criteria.createAlias("correlationSets", "property");
        }
        if(selectionEvaluator!=null) {
          criteria.add(selectionEvaluator.evaluate(null));
        }
        if (orderEvaluator != null) {
          Collection<Order> orders = orderEvaluator.evaluate(null);
          for (Order order : orders) {
            criteria.addOrder(order);
          }
        }
        // setting limit
        if (limit != null) {
          criteria.setMaxResults(limit.getNumber());
        }

        List result = criteria.list();
        //check whether ordering by status
        if(sortByStatus) {
          Collections.sort(result, sortByStatusDesc?StateComparator.DESC:StateComparator.ASC);
        }
          
        return result;
      };
    };
  }

  protected ConjunctionEvaluator<Criterion, Object> compileConjunction(Collection<CommandEvaluator> childs) {
    return new AbstractConjunction<Criterion, Object>(childs) {
      public Criterion evaluate(Object arg) {
        Conjunction conj = Restrictions.conjunction();
        for (CommandEvaluator eval : childs) {
          conj.add((Criterion) eval.evaluate(null));
        }
        return conj;
      }
    };
  }

  protected DisjunctionEvaluator<Criterion, Object> compileDisjunction(Collection<CommandEvaluator> childs) {
    return new AbstractDisjunction<Criterion, Object>(childs) {
      public Criterion evaluate(Object arg) {
        Disjunction conj = Restrictions.disjunction();
        for (CommandEvaluator eval : childs) {
          conj.add((Criterion) eval.evaluate(null));
        }
        return conj;
      };
    };
  }

  protected EqualityEvaluator<String, Criterion, Object> compileEqual(final Equality eq) {
    if (eq.getIdentifier() instanceof Property) {
      propertyInQuery = true;
      final Property property = (Property) eq.getIdentifier();
      return new EqualityEvaluator<String, Criterion, Object>() {
        public Criterion evaluate(Object paramValue) {
          Conjunction conj = Restrictions.conjunction();
          if (!StringUtils.isEmpty(property.getNamespace())) {
            conj.add(Restrictions.eq(PROPERTY_NS_DB_FIELD, property.getNamespace()));
          }
          conj.add(Restrictions.eq(PROPERTY_NAME_DB_FIELD, property.getName()));
          conj.add(Restrictions.eq(PROPERTY_VALUE_DB_FIELD, eq.getValue().getValue()));

          return conj;
        };

        public String getIdentifier() {
          return property.toString();
        };
      };
    } else {
      final String fieldName = eq.getIdentifier().getName();
      final Object value = eq.getValue().getValue();

      final String dbField = getDBField(fieldName);

      if (INSTANCE_STATUS_FIELD.equals(fieldName)) {
        return new FieldValueEquality(INSTANCE_STATUS_FIELD) {
          /**
           * @see org.apache.ode.ql.eval.skel.CommandEvaluator#evaluate(java.lang.Object)
           */
          public Criterion evaluate(Object paramValue) {
            short noState = 200; // TODO move to constants
            Disjunction disj = Restrictions.disjunction();

            if (STATUS_ACTIVE.equals(paramValue)) {
              disj.add(Restrictions.eq(dbField, ProcessState.STATE_NEW));
              disj.add(Restrictions.eq(dbField, ProcessState.STATE_ACTIVE));
              disj.add(Restrictions.eq(dbField, ProcessState.STATE_READY));
            } else if (STATUS_SUSPENDED.equals(paramValue)) {
              disj.add(Restrictions.eq(dbField, ProcessState.STATE_SUSPENDED));
            } else if (STATUS_ERROR.equals(value)) {
              disj.add(Restrictions.eq(dbField, noState)); // Error instance state doesn't exist yet
            } else if (STATUS_COMPLETED.equals(paramValue)) {
              disj.add(Restrictions.eq(dbField, ProcessState.STATE_COMPLETED_OK));
            } else if (STATUS_TERMINATED.equals(paramValue)) {
              disj.add(Restrictions.eq(dbField, ProcessState.STATE_TERMINATED));
            } else if (STATUS_FAULTED.equals(paramValue)) {
              disj.add(Restrictions.eq(dbField, ProcessState.STATE_COMPLETED_WITH_FAULT));
            } else {
              disj.add(Restrictions.eq(dbField, noState)); // Non existent state
            }
            return disj;
          }
        };
      }

      return new DBFieldValueEq(dbField, value);
    }
  }

  public CommandEvaluator compileEvaluator(Object node) {
    /*
     * 
     */
    if (node instanceof In) {
      return compileIn((In) node);
    } else if (node instanceof org.apache.ode.ql.tree.nodes.Conjunction) {
      return compileConjunction(evaluate((LogicExprNode) node));
    } else if (node instanceof org.apache.ode.ql.tree.nodes.Disjunction) {
      return compileDisjunction(evaluate((LogicExprNode) node));
    } else if (node instanceof IdentifierToValueCMP) {
      return compileIdentifierToValueCMP((IdentifierToValueCMP) node);
    }
    throw new IllegalArgumentException("Unsupported node " + node.getClass());
  }

  protected CommandEvaluator<Criterion, Object> compileIdentifierToValueCMP(IdentifierToValueCMP node) {
    Identifier id = node.getIdentifier();
    if (id instanceof Field) {
      String name = id.getName();
      Value value = node.getValue();
      if (INSTANCE_ID_FIELD.equals(name)) {
        value.setValue(Long.valueOf((String) value.getValue()));
      } else if (INSTANCE_STARTED_FIELD.equals(name) || INSTANCE_LAST_ACTIVE_FIELD.equals(name)) {
        try {
          value.setValue(ISO8601DateParser.parse((String) value.getValue()));
        } catch (ParseException ex) {
          // TODO
          throw new RuntimeException(ex);
        }
      }
    }
    if (node instanceof Equality) {
      return compileEqual((Equality) node);
    } else if (node instanceof Less) {
      return compileLess((Less) node);
    } else if (node instanceof Greater) {
      return compileGreater((Greater) node);
    } else if (node instanceof GE) {
      return compileGE((GE) node);
    } else if (node instanceof LE) {
      return compileLE((LE) node);
    } else if (node instanceof Like) {
      return compileLike((Like) node);
    } else {
      throw new IllegalArgumentException("Unsupported node " + node.getClass());
    }
  }

  protected GEEvaluator<String, Criterion, Object> compileGE(final GE ge) {
    if (ge.getIdentifier() instanceof Property) {
      propertyInQuery = true;
      final Property property = (Property) ge.getIdentifier();
      return new GEEvaluator<String, Criterion, Object>() {
        public Criterion evaluate(Object paramValue) {
          Conjunction conj = Restrictions.conjunction();
          if (!StringUtils.isEmpty(property.getNamespace())) {
            conj.add(Restrictions.ge(PROPERTY_NS_DB_FIELD, property.getNamespace()));
          }
          conj.add(Restrictions.ge(PROPERTY_NAME_DB_FIELD, property.getName()));
          conj.add(Restrictions.ge(PROPERTY_VALUE_DB_FIELD, ge.getValue().getValue()));

          return conj;
        };

        public String getIdentifier() {
          return property.toString();
        };
      };
    } else {
      final String fieldName = ge.getIdentifier().getName();
      final Object objValue = ge.getValue().getValue();

      if (INSTANCE_STATUS_FIELD.equals(fieldName)) {
        throw new IllegalArgumentException("Field " + INSTANCE_STATUS_FIELD + " is not supported.");
      }

      final String dbField = getDBField(fieldName);

      return new GEEvaluator<String, Criterion, Object>() {
        public Criterion evaluate(Object paramValue) {
          return Restrictions.ge(dbField, objValue);
        }

        public String getIdentifier() {
          return fieldName;
        }
      };
    }
  }

  protected GreaterEvaluator<String, Criterion, Object> compileGreater(final Greater gt) {
    if (gt.getIdentifier() instanceof Property) {
      propertyInQuery = true;
      final Property property = (Property) gt.getIdentifier();
      return new GreaterEvaluator<String, Criterion, Object>() {
        public Criterion evaluate(Object paramValue) {
          Conjunction conj = Restrictions.conjunction();
          if (!StringUtils.isEmpty(property.getNamespace())) {
            conj.add(Restrictions.gt(PROPERTY_NS_DB_FIELD, property.getNamespace()));
          }
          conj.add(Restrictions.gt(PROPERTY_NAME_DB_FIELD, property.getName()));
          conj.add(Restrictions.gt(PROPERTY_VALUE_DB_FIELD, gt.getValue().getValue()));

          return conj;
        };

        public String getIdentifier() {
          return property.toString();
        };
      };
    } else {
      final String fieldName = gt.getIdentifier().getName();
      final Object value = gt.getValue().getValue();

      if (INSTANCE_STATUS_FIELD.equals(fieldName)) {
        throw new IllegalArgumentException("Field " + INSTANCE_STATUS_FIELD + " is not supported.");
      }

      final String dbField = getDBField(fieldName);

      return new GreaterEvaluator<String, Criterion, Object>() {
        public Criterion evaluate(Object paramValue) {
          return Restrictions.gt(dbField, value);
        }

        public String getIdentifier() {
          return fieldName;
        }
      };
    }
  }

  protected INEvaluator<String, Criterion, Object> compileIn(final In in) {
    if (in.getIdentifier() instanceof Property) {
      propertyInQuery = true;
      final Property property = (Property) in.getIdentifier();
      return new INEvaluator<String, Criterion, Object>() {
        public Criterion evaluate(Object paramValue) {
          Disjunction disj = Restrictions.disjunction();

          String propertyNS = property.getNamespace();
          String propertyName = property.getName();

          for (Value value : in.getValues()) {
            Conjunction conj = Restrictions.conjunction();
            if (!StringUtils.isEmpty(property.getNamespace())) {
              conj.add(Restrictions.gt(PROPERTY_NS_DB_FIELD, propertyNS));
            }
            conj.add(Restrictions.gt(PROPERTY_NAME_DB_FIELD, propertyName));
            conj.add(Restrictions.gt(PROPERTY_VALUE_DB_FIELD, value.getValue()));

            disj.add(conj);
          }
          return disj;
        };

        public String getIdentifier() {
          return property.toString();
        };
      };
    } else {
      final String fieldName = in.getIdentifier().getName();

      if (INSTANCE_STATUS_FIELD.equals(fieldName)) {
        short noState = 200; // TODO move to constants
        final Disjunction disj = Restrictions.disjunction();

        final Collection values = ValuesHelper.extract((Collection<Value>) in.getValues());

        if (values.contains(STATUS_ACTIVE)) {
          disj.add(Restrictions.eq(INSTANCE_STATUS_DB_FIELD, ProcessState.STATE_NEW));
          disj.add(Restrictions.eq(INSTANCE_STATUS_DB_FIELD, ProcessState.STATE_ACTIVE));
          disj.add(Restrictions.eq(INSTANCE_STATUS_DB_FIELD, ProcessState.STATE_READY));
        }
        if (values.contains(STATUS_SUSPENDED)) {
          disj.add(Restrictions.eq(INSTANCE_STATUS_DB_FIELD, ProcessState.STATE_SUSPENDED));
        }
        if (values.contains(STATUS_ERROR)) {
          disj.add(Restrictions.eq(INSTANCE_STATUS_DB_FIELD, noState)); // Error instance state doesn't exist yet
        }
        if (values.contains(STATUS_COMPLETED)) {
          disj.add(Restrictions.eq(INSTANCE_STATUS_DB_FIELD, ProcessState.STATE_COMPLETED_OK));
        }
        if (values.contains(STATUS_TERMINATED)) {
          disj.add(Restrictions.eq(INSTANCE_STATUS_DB_FIELD, ProcessState.STATE_TERMINATED));
        }
        if (values.contains(STATUS_FAULTED)) {
          disj.add(Restrictions.eq(INSTANCE_STATUS_DB_FIELD, ProcessState.STATE_COMPLETED_WITH_FAULT));
        } 
        return new INEvaluator<String, Criterion, Object>() {
          public Criterion evaluate(Object paramValue) {
            return disj;
          };

          public String getIdentifier() {
            return INSTANCE_STATUS_DB_FIELD;
          };
        };
      } else {
        final Collection objValues;
        final Collection<Value> values = in.getValues();
        if (INSTANCE_ID_FIELD.equals(fieldName)) {
          objValues = new ArrayList<Long>(values.size());
          for (Value value : values) {
            objValues.add(Long.valueOf((String) value.getValue()));
          }
        } else if (INSTANCE_STARTED_FIELD.equals(fieldName) || INSTANCE_LAST_ACTIVE_FIELD.equals(fieldName)) {
          objValues = new ArrayList<Date>(values.size());
          try {
            for (Value value : values) {
              objValues.add(ISO8601DateParser.parse((String) value.getValue()));
            }
          } catch (ParseException ex) {
            // TODO
            throw new RuntimeException(ex);
          }
        } else {
          objValues = ValuesHelper.extract((Collection<Value>) values);
        }
        final String dbField = getDBField(fieldName);
        return new INEvaluator<String, Criterion, Object>() {
          /**
           * @see org.apache.ode.ql.eval.skel.CommandEvaluator#evaluate(java.lang.Object)
           */
          public Criterion evaluate(Object paramValue) {
            return Restrictions.in(dbField, objValues);
          }

          /**
           * @see org.apache.ode.ql.eval.skel.Identified#getIdentifier()
           */
          public String getIdentifier() {
            return dbField;
          }
        };
      }
    }
  }

  protected LEEvaluator<String, Criterion, Object> compileLE(final LE le) {
    if (le.getIdentifier() instanceof Property) {
      final Property property = (Property) le.getIdentifier();
      return new LEEvaluator<String, Criterion, Object>() {
        public Criterion evaluate(Object paramValue) {
          Conjunction conj = Restrictions.conjunction();
          if (!StringUtils.isEmpty(property.getNamespace())) {
            conj.add(Restrictions.le(PROPERTY_NS_DB_FIELD, property.getNamespace()));
          }
          conj.add(Restrictions.le(PROPERTY_NAME_DB_FIELD, property.getName()));
          conj.add(Restrictions.le(PROPERTY_VALUE_DB_FIELD, le.getValue().getValue()));

          return conj;
        };

        public String getIdentifier() {
          return property.toString();
        };
      };
    } else {
      final String fieldName = le.getIdentifier().getName();
      final Object value = le.getValue().getValue();

      if (INSTANCE_STATUS_FIELD.equals(fieldName)) {
        throw new IllegalArgumentException("Field " + INSTANCE_STATUS_FIELD + " is not supported.");
      }

      final String dbField = getDBField(fieldName);

      return new LEEvaluator<String, Criterion, Object>() {
        public Criterion evaluate(Object paramValue) {
          return Restrictions.le(dbField, value);
        }

        public String getIdentifier() {
          return fieldName;
        }
      };
    }
  }

  protected LessEvaluator<String, Criterion, Object> compileLess(final Less less) {
    if (less.getIdentifier() instanceof Property) {
      propertyInQuery = true;
      final Property property = (Property) less.getIdentifier();
      return new LessEvaluator<String, Criterion, Object>() {
        public Criterion evaluate(Object paramValue) {
          Conjunction conj = Restrictions.conjunction();
          if (!StringUtils.isEmpty(property.getNamespace())) {
            conj.add(Restrictions.lt(PROPERTY_NS_DB_FIELD, property.getNamespace()));
          }
          conj.add(Restrictions.lt(PROPERTY_NAME_DB_FIELD, property.getName()));
          conj.add(Restrictions.lt(PROPERTY_VALUE_DB_FIELD, less.getValue().getValue()));

          return conj;
        };

        public String getIdentifier() {
          return property.toString();
        };
      };
    } else {
      final String fieldName = less.getIdentifier().getName();
      final Object value = less.getValue().getValue();

      if (INSTANCE_STATUS_FIELD.equals(fieldName)) {
        throw new IllegalArgumentException("Field " + INSTANCE_STATUS_FIELD + " is not supported.");
      }

      final String dbField = getDBField(fieldName);

      return new LessEvaluator<String, Criterion, Object>() {
        public Criterion evaluate(Object paramValue) {
          return Restrictions.lt(dbField, value);
        }

        public String getIdentifier() {
          return fieldName;
        }
      };
    }
  }

  protected LikeEvaluator<String, Criterion, Object> compileLike(final Like like) {
    if (like.getIdentifier() instanceof Property) {
      propertyInQuery = true;
      final Property property = (Property) like.getIdentifier();
      return new LikeEvaluator<String, Criterion, Object>() {
        public Criterion evaluate(Object paramValue) {
          Conjunction conj = Restrictions.conjunction();
          if (!StringUtils.isEmpty(property.getNamespace())) {
            conj.add(Restrictions.like(PROPERTY_NS_DB_FIELD, property.getNamespace()));
          }
          conj.add(Restrictions.like(PROPERTY_NAME_DB_FIELD, property.getName()));
          conj.add(Restrictions.like(PROPERTY_VALUE_DB_FIELD, like.getValue().getValue()));

          return conj;
        };

        public String getIdentifier() {
          return property.toString();
        };
      };
    } else {
      final String fieldName = like.getIdentifier().getName();
      if (INSTANCE_STATUS_FIELD.equals(fieldName)) {
        throw new IllegalArgumentException("Field " + INSTANCE_STATUS_FIELD + " is not supported by like operation.");
      }
      if (INSTANCE_ID_FIELD.equals(fieldName)) {
        throw new IllegalArgumentException("Field " + INSTANCE_ID_FIELD + " is not supported by like operation.");
      }

      final Object value = like.getValue().getValue();
      final String dbField = getDBField(fieldName);

      return new LikeEvaluator<String, Criterion, Object>() {
        public Criterion evaluate(Object paramValue) {
          return Restrictions.like(dbField, value);
        };

        public String getIdentifier() {
          return dbField;
        }
      };
    }
  }

  protected OrderByEvaluator<Collection<Order>, Object> compileOrderBy(OrderBy orderBy) {
    final LinkedHashMap<String, Boolean> orders = new LinkedHashMap<String, Boolean>();

    for (OrderByElement idOrder : orderBy.getOrders()) {
      if (!(idOrder.getIdentifier() instanceof Field)) {
        throw new IllegalArgumentException("Only field identifier supported by order by operator.");
      }
      String idName = idOrder.getIdentifier().getName();
      if(INSTANCE_STATUS_FIELD.equals(idName)) {
        if(orderBy.getOrders().size()>1) {
          //TODO throw appropriate exception
          throw new RuntimeException("Status field should be used alone in <order by> construction.");
        }
        orderByStatus = true;
        orderByStatusDesc = idOrder.getType()==OrderByType.DESC;
        return null;
      }
      String dbField = getDBField(idName);

      orders.put(dbField, idOrder.getType() == null || idOrder.getType() == OrderByType.ASC);
    }

    return new OrderByEvaluator<Collection<Order>, Object>() {
      public Collection<Order> evaluate(Object paramValue) {
        Collection<Order> hibernateOrders = new ArrayList<Order>(orders.size());
        for (Map.Entry<String, Boolean> order : orders.entrySet()) {
          hibernateOrders.add(order.getValue() ? Order.asc(order.getKey()) : Order.desc(order.getKey()));
        }
        return hibernateOrders;
      }
    };
  }

  protected List<CommandEvaluator> evaluate(LogicExprNode exprNode) {
    ArrayList<CommandEvaluator> commandsEv = new ArrayList<CommandEvaluator>(exprNode.getChilds().size());
    for (LogicNode node : exprNode.getChilds()) {
      commandsEv.add(compileEvaluator(node));
    }
    return commandsEv;
  }

  public static void main(String[] args) {
    String queryString = "order by last-active desc limit 1000";
    Builder<String> builder = BuilderFactory.getInstance().createBuilder();
    Node queryNode = builder.build(queryString);
    HibernateInstancesQueryCompiler compiler = new HibernateInstancesQueryCompiler();

    compiler.compile((Query) queryNode);
  }
}
