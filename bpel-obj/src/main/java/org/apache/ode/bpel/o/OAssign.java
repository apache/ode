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
package org.apache.ode.bpel.o;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;

public class OAssign extends OActivity {
    static final long serialVersionUID = -1L  ;
    
    public final List<Copy> copy = new ArrayList<Copy>();

    public OAssign(OProcess owner, OActivity parent) {
        super(owner, parent);
    }


    public String toString() {
        return "{OAssign : " + name + ", joinCondition=" + joinCondition + "}";
    }

    /**
     * Assignmenet copy entry, i.e. what the assignment consits of.
     */
    public static class Copy extends OBase {
        private static final long serialVersionUID = 1L;
        public LValue to;
        public RValue from;
        public boolean keepSrcElementName;
        public boolean ignoreMissingFromData;
        public boolean ignoreUninitializedFromVariable;
        public boolean insertMissingToData;

        public Copy(OProcess owner) {
            super(owner);
        }

        public String toString() {
            return "{OCopy " + to + "=" + from + "}";
        }
        
        @Override
        public void dehydrate() {
        	super.dehydrate();
        	to = null;
        	from = null;
        }
    }

    public interface LValue {
        OScope.Variable getVariable();
    }

    public interface RValue { }

    public static class Literal extends OBase implements RValue {
        private static final long serialVersionUID = 1L;
        public transient String xmlLiteral;

        public Literal(OProcess owner, Document xmlLiteral) {
            super(owner);
            if (xmlLiteral == null)
                throw new IllegalArgumentException("null xmlLiteral!");
            this.xmlLiteral = DOMUtils.domToString(xmlLiteral);
        }

        public String toString() {
            return "{Literal " + xmlLiteral + "}";
        }

        private void writeObject(java.io.ObjectOutputStream out)
                throws IOException
        {
            out.writeObject(xmlLiteral);
        }

        private void readObject(java.io.ObjectInputStream in)
                throws IOException
        {
            String domStr = null;
            try {
                domStr = (String) in.readObject();
            } catch (ClassNotFoundException e) {
                throw (IOException)(new IOException("XML de-serialization error.")).initCause(e);
            }
            xmlLiteral = domStr;
        }

        public String getXmlLiteral() {
            return xmlLiteral;
        }
    }

    public static class LValueExpression extends OBase implements LValue {
        private static final long serialVersionUID = 1L;
        public OLValueExpression expression;

        public LValueExpression(OProcess owner, OLValueExpression compiledExpression) {
            super(owner);
            this.expression = compiledExpression;
        }

        public String toString() {
            return expression.toString();
        }
        /**
         * @see org.apache.ode.bpel.o.OAssign.LValue#getVariable()
         */
        public Variable getVariable() {
            return expression.getVariable();
        }
        public boolean isInsertMissingToData() {
        	return expression.insertMissingData;
        }
        public void setInsertMissingToData(boolean insertMissingToData) {
        	expression.insertMissingData = insertMissingToData;
        }
      
    }
    public static class Expression extends OBase implements RValue {
        private static final long serialVersionUID = 1L;
        public OExpression expression;

        public Expression(OProcess owner, OExpression compiledExpression) {
            super(owner);
            this.expression = compiledExpression;
        }

        public String toString() {
            return expression.toString();
        }
    }


    /**
     * Direct reference: selects named child of the message document element. 
     * This is used for access to extensions (SOAP headers for example).
     * @author mszefler
     */
    public static class DirectRef extends OBase implements RValue, LValue {
        private static final long serialVersionUID = 1L;
        public DirectRef(OProcess owner) {
            super(owner);
        }

        /** Referenced Variable */
        public OScope.Variable variable;
        
        /** Name of the element referenced. */
        public QName elName;

        public Variable getVariable() {
            return variable;
        }
    }
    
    public static class VariableRef extends OBase implements RValue, LValue {
        private static final long serialVersionUID = 1L;
        public OScope.Variable variable;
        public OMessageVarType.Part part;
        public OMessageVarType.Part headerPart;
        public OExpression location;

        public VariableRef(OProcess owner) {
            super(owner);
        }

        public OScope.Variable getVariable() {
            return variable;
        }

        /**
         * Report whether this is a reference to a whole "message"
         * @return <code>true</code> if whole-message reference
         */
        public boolean isMessageRef() { 
            return variable.type instanceof OMessageVarType && part == null && headerPart == null && location == null;
        }
        
        /**
         * Report whether this is a reference to a message part. 
         * @return <code>true</code> if reference to a message part
         */
        public boolean isPartRef() {
            return variable.type instanceof OMessageVarType && part != null && location == null;
        }
        
        public boolean isHeaderRef() {
            return variable.type instanceof OMessageVarType && headerPart != null && location == null;
        }

        public String toString() {
            return "{VarRef " + variable  +
                    (part==null ? "" : "." + part.name) +
                    (location == null ? "" : location.toString())+ "}";
        }
    }

    public static class PropertyRef extends OBase implements RValue, LValue {
        private static final long serialVersionUID = 1L;
        public OScope.Variable variable;
        public OProcess.OPropertyAlias propertyAlias;

        public PropertyRef(OProcess owner) { super(owner); }

        public OScope.Variable getVariable() {
            return variable;
        }

        public String toString() {
            return "{PropRef " + variable + "!" + propertyAlias+ "}";
        }
    }

    public static class PartnerLinkRef extends OBase implements RValue, LValue {
          private static final long serialVersionUID = 1L;
          public OPartnerLink partnerLink;
          public boolean isMyEndpointReference;

          public PartnerLinkRef(OProcess owner) { super(owner); }

          // Must fit in a LValue even if it's not variable based
          public Variable getVariable() {
              return null;
          }

          public String toString() {
              return "{PLinkRef " + partnerLink + "!" + isMyEndpointReference + "}";
          }
    }
    
    @Override
    public void dehydrate() {
    	super.dehydrate();
    	for (Copy copy : this.copy) {
    		copy.dehydrate();
    	}
    }
}
