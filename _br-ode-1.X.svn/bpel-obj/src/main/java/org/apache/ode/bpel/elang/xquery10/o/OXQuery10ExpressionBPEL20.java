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

package org.apache.ode.bpel.elang.xquery10.o;

import javax.xml.namespace.QName;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.query.XQueryExpression;

import org.apache.ode.bpel.elang.xpath20.o.OXPath20ExpressionBPEL20;
import org.apache.ode.bpel.o.OProcess;

/**
 * A representation of an XQuery expression 
 */
public class OXQuery10ExpressionBPEL20 extends OXPath20ExpressionBPEL20 {

    private static final long serialVersionUID = -1L;
    
    public String xquery;

    public OXQuery10ExpressionBPEL20(OProcess owner, QName qname_getVariableData,
                                    QName qname_getVariableProperty, QName qname_getLinkStatus,
                                    QName qname_doXslTransform, boolean isJoinExpression) {
        super(owner, qname_getVariableData, qname_getVariableProperty, qname_getLinkStatus,
                qname_doXslTransform, isJoinExpression);
    }
}
