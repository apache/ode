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

package org.apache.ode.ql.jcc;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.ode.ql.tree.Builder;
import org.apache.ode.ql.tree.nodes.Conjunction;
import org.apache.ode.ql.tree.nodes.Disjunction;
import org.apache.ode.ql.tree.nodes.Equality;
import org.apache.ode.ql.tree.nodes.GE;
import org.apache.ode.ql.tree.nodes.Greater;
import org.apache.ode.ql.tree.nodes.Identifier;
import org.apache.ode.ql.tree.nodes.In;
import org.apache.ode.ql.tree.nodes.LE;
import org.apache.ode.ql.tree.nodes.Less;
import org.apache.ode.ql.tree.nodes.Like;
import org.apache.ode.ql.tree.nodes.Limit;
import org.apache.ode.ql.tree.nodes.LogicNode;
import org.apache.ode.ql.tree.nodes.OrderBy;
import org.apache.ode.ql.tree.nodes.OrderByElement;
import org.apache.ode.ql.tree.nodes.OrderByType;
import org.apache.ode.ql.tree.nodes.Query;
import org.apache.ode.ql.tree.nodes.Value;

public class TreeBuilder extends Builder<String> {
    
    public org.apache.ode.ql.tree.nodes.Node build(String query) {
      try {
        org.apache.ode.ql.jcc.ASTStart start = new Parser(query).start();
        return build(start);
      }catch(ParseException ex) {
        //TODO create common exception which will indicate parsing exception
        throw new RuntimeException(ex.getMessage(), ex);
      }
    }
    private org.apache.ode.ql.tree.nodes.Node build(Node node) {
        if(node instanceof ASTAnd) {
            return createConjunction(node);
        } 
        if(node instanceof ASTOr) {
            return createDisjunction(node);
        } 
        if(node instanceof ASTLess) {
            return createLess(node);
        }
        if(node instanceof ASTGreater) {
            return createGreater(node);
        }
        if(node instanceof ASTLE) {
            return createLE(node);
        }
        if(node instanceof ASTIn) {
            return createIn(node);
        }
        if(node instanceof ASTGE) {
            return createGE(node);
        }
        if(node instanceof ASTEqual) {
            return createEquality(node);
        }
        if(node instanceof ASTLike) {
            return createLike(node);
        }
        if(node instanceof ASTStart) {
            return createSelection((ASTStart)node);
        }
        throw new IllegalArgumentException("Unsupported node type "+node.getClass());
    }
    
    private Query createSelection(ASTStart node) {
        Collection<Object> childs = new ArrayList<Object>(node.jjtGetNumChildren());
        OrderBy orderBy = null;
        Limit limit = null;
        for(int index = 0;index < node.jjtGetNumChildren();index++) {
          Node childNode = node.jjtGetChild(index);
            if(childNode instanceof ASTOrderBy) {
              orderBy = createOrderBy(childNode);
            } else if(childNode instanceof ASTLimit) {
              limit = createLimit(childNode);
            } else {
              Object child = build(childNode);
              childs.add(child);
            }
        }
        return new Query(childs, orderBy, limit);
    }
    private OrderBy createOrderBy(Node node) {
        Collection<OrderByElement> orders = new ArrayList<OrderByElement>(node.jjtGetNumChildren());
        for(int i = 0;i < node.jjtGetNumChildren();i++) {
          orders.add(createOrderByElement((ASTOrderByField)node.jjtGetChild(i)));
        }
        return new OrderBy(orders);
    }
    private OrderByElement createOrderByElement(ASTOrderByField node) {
        int childsNum = node.jjtGetNumChildren();
        OrderByType type;
        if(childsNum==1) {
            type = OrderByType.ASC;
        } if(childsNum==2) {
            ASTOrderType astType = (ASTOrderType)extractChildNode(node, 1);
            type = OrderByType.valueOf(astType.getValue().toUpperCase());
        } else {
            //TODO
            throw new IllegalArgumentException();
        }
        Identifier id = createIdentifier(node, 0);
        return new OrderByElement(id, type);
    }
    private Conjunction createConjunction(Node node) {
        Collection<LogicNode> childs = extractLogicNodes(node);
        return new Conjunction(childs);
    }
    
    private Disjunction createDisjunction(Node node) {
        Collection<LogicNode> childs = extractLogicNodes(node);
        return new Disjunction(childs);
    }
    
    private LE createLE(Node node) {
        checkChildsNumber(node, 2);
        return new LE(createIdentifier(node, 0), createValue(node, 1));
    }

    private In createIn(Node node) {
        checkChildsNumber(node, 2);
        Node inValuesNode = extractChildNode(node, 1, ASTInValues.class);
        Collection<Value> values = new ArrayList<Value>(inValuesNode.jjtGetNumChildren());
        for(int index = 0;index < inValuesNode.jjtGetNumChildren();index++) {
            values.add(createValue(inValuesNode, index));
        }
        return new In(createIdentifier(node, 0), values);
    }
    
    private GE createGE(Node node) {
        checkChildsNumber(node, 2);
        return new GE(createIdentifier(node, 0), createValue(node, 1));
    }
    private Less createLess(Node node) {
        checkChildsNumber(node, 2);
        return new Less(createIdentifier(node, 0), createValue(node, 1));
    }
    private Greater createGreater(Node node) {
        checkChildsNumber(node, 2);
        return new Greater(createIdentifier(node, 0), createValue(node, 1));
    }
    private Equality createEquality(Node node) {
        checkChildsNumber(node, 2);
        return new Equality(createIdentifier(node, 0), createValue(node, 1));
    }
    private Like createLike(Node node) {
        checkChildsNumber(node, 2);
        return new Like(createIdentifier(node, 0), createValue(node, 1));
    }
    
    private Value createValue(Node parentNode, int index) {
        return new Value<String>(extractValue(parentNode, index).value);
    }
    private Limit createLimit(Node node) {
        return new Limit(((ASTLimit)node).getNumber());
    }
    private Identifier createIdentifier(Node parentNode, int index) {
        Node node = extractChildNode(parentNode, index);
        if(node instanceof ASTField) {
            return new org.apache.ode.ql.tree.nodes.Field(((ASTField)node).name);
        }
        if(node instanceof ASTProperty) {
            return new org.apache.ode.ql.tree.nodes.Property(((ASTProperty)node).getName());
        }
        //TODO
        throw new IllegalArgumentException("");
    }
    private ASTValue extractValue(Node parentNode, int index) {
        return (ASTValue)extractChildNode(parentNode, index, ASTValue.class);
    }
    
    @SuppressWarnings("unchecked")
    private Node extractChildNode(Node parentNode, int index, Class expected) {
        Node node = extractChildNode(parentNode, index);
        if(!(expected.isAssignableFrom(node.getClass()))) {
            //TODO
            throw new IllegalArgumentException("");
        }
        return node;
    }
    private Node extractChildNode(Node parentNode, int index) {
        if(parentNode.jjtGetNumChildren()<=index) {
            //TODO
            throw new IllegalArgumentException("");
        }
        return parentNode.jjtGetChild(index);
    }
    
    private Collection<LogicNode> extractLogicNodes(Node parentNode) {
        Collection<LogicNode> childs = new ArrayList<LogicNode>(parentNode.jjtGetNumChildren());
        for(int index = 0;index < parentNode.jjtGetNumChildren();index++) {
            childs.add((LogicNode)build(parentNode.jjtGetChild(index)));
        }
        return childs;
    }

    /*
    private static boolean checkInheritance(Object obj, Class clazz) {
        return clazz.isAssignableFrom(clazz.getClass());
    }
    */
    private static void checkChildsNumber(Node node, int expected) {
        int actual = node.jjtGetNumChildren();
        if(actual!=expected) {
            throw new IllegalArgumentException("Expected childs cound("+actual+") differes from expected "+expected);
        }
    }
}
