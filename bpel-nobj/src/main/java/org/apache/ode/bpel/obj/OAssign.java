/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ode.bpel.obj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.obj.OMessageVarType.Part;
import org.apache.ode.bpel.obj.OProcess.OPropertyAlias;
import org.apache.ode.bpel.obj.OScope.Variable;
import org.w3c.dom.Document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class OAssign extends OActivity implements Serializable {
    public static final long serialVersionUID = -1L;
    private static final String OPERATION = "operation";

    @JsonCreator
    public OAssign() {}

    public OAssign(OProcess owner, OActivity parent) {
        super(owner, parent);
        setOperations(new ArrayList<OAssignOperation>());
    }

    @Override
    public void dehydrate() {
        super.dehydrate();
        for (OAssignOperation operation : getOperations()) {
            operation.dehydrate();
        }
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public List<OAssignOperation> getOperations() {
        Object o = fieldContainer.get(OPERATION);
        return o == null ? null : (List<OAssignOperation>) o;
    }

    public void setOperations(List<OAssignOperation> operation) {
        if (getOperations() == null) {
            fieldContainer.put(OPERATION, operation);
        }
    }

    public String toString() {
        return "{OAssign : " + getName() + ", joinCondition=" + getJoinCondition() + "}";
    }

    /**
     * Base class for assign operations.
     */
    public static abstract class OAssignOperation extends OBase implements Serializable {
        private static final long serialVersionUID = -3042873658302758854L;

        public enum Type {
            Copy, ExtensionOperation
        }

        @JsonCreator
        public OAssignOperation() {}

        public OAssignOperation(OProcess owner) {
            super(owner);
        }

        public abstract Type getType();
    }

    /**
     * Assignment copy entry, i.e. what the assignment consists of.
     */
    public static class Copy extends OAssignOperation implements Serializable {
        public static final long serialVersionUID = -1L;
        private static final String TO = "to";
        private static final String FROM = "from";
        private static final String KEEPSRCELEMENTNAME = "keepSrcElementName";
        private static final String IGNOREMISSINGFROMDATA = "ignoreMissingFromData";
        private static final String IGNOREUNINITIALIZEDFROMVARIABLE =
                "ignoreUninitializedFromVariable";
        private static final String INSERTMISSINGTODATA = "insertMissingToData";

        @JsonCreator
        public Copy() {
            initPrimitive();
        }

        public Copy(OProcess owner) {
            super(owner);
            initPrimitive();
        }

        private void initPrimitive() {
            setIgnoreMissingFromData(false);
            setIgnoreUninitializedFromVariable(false);
            setInsertMissingToData(false);
            setKeepSrcElementName(false);
        }

        @Override
        public void dehydrate() {
            super.dehydrate();
            setTo(null);
            setFrom(null);
        }

        @JsonIgnore
        public RValue getFrom() {
            Object o = fieldContainer.get(FROM);
            return o == null ? null : (RValue) o;
        }

        @JsonIgnore
        public boolean isIgnoreMissingFromData() {
            Object o = fieldContainer.get(IGNOREMISSINGFROMDATA);
            return o == null ? false : (Boolean) o;
        }

        @JsonIgnore
        public boolean isIgnoreUninitializedFromVariable() {
            return (Boolean) fieldContainer.get(IGNOREUNINITIALIZEDFROMVARIABLE);
        }

        @JsonIgnore
        public boolean isInsertMissingToData() {
            Object o = fieldContainer.get(INSERTMISSINGTODATA);
            return o == null ? false : (Boolean) o;
        }

        @JsonIgnore
        public boolean isKeepSrcElementName() {
            Object o = fieldContainer.get(KEEPSRCELEMENTNAME);
            return o == null ? false : (Boolean) o;
        }

        @JsonIgnore
        public LValue getTo() {
            Object o = fieldContainer.get(TO);
            return o == null ? null : (LValue) o;
        }

        public void setFrom(RValue from) {
            fieldContainer.put(FROM, from);
        }

        public void setIgnoreMissingFromData(boolean ignoreMissingFromData) {
            fieldContainer.put(IGNOREMISSINGFROMDATA, ignoreMissingFromData);
        }

        public void setIgnoreUninitializedFromVariable(boolean ignoreUninitializedFromVariable) {
            fieldContainer.put(IGNOREUNINITIALIZEDFROMVARIABLE, ignoreUninitializedFromVariable);
        }

        public void setInsertMissingToData(boolean insertMissingToData) {
            fieldContainer.put(INSERTMISSINGTODATA, insertMissingToData);
        }

        public void setKeepSrcElementName(boolean keepSrcElementName) {
            fieldContainer.put(KEEPSRCELEMENTNAME, keepSrcElementName);
        }

        public void setTo(LValue to) {
            fieldContainer.put(TO, to);
        }

        public String toString() {
            return "{OCopy " + getTo() + "=" + getFrom() + "}";
        }

        @JsonIgnore
        public Type getType() {
            return Type.Copy;
        }
    }

    /**
     * Assignment extension operation entry, i.e. what the assignment consists of.
     */
    public static class ExtensionAssignOperation extends OAssignOperation {
        private static final long serialVersionUID = 1L;

        private static final String EXTENSIONNAME = "extensionName";
        private static final String NESTEDELEMENT = "nestedElement";

        @JsonCreator
        public ExtensionAssignOperation() {}

        public ExtensionAssignOperation(OProcess owner) {
            super(owner);
        }

        @JsonIgnore
        public QName getExtensionName() {
            Object o = fieldContainer.get(EXTENSIONNAME);
            return o == null ? null : (QName) o;
        }

        @JsonIgnore
        public String getNestedElement() {
            Object o = fieldContainer.get(NESTEDELEMENT);
            return o == null ? null : (String) o;
        }

        public void setExtensionName(QName extensionName) {
            fieldContainer.put(EXTENSIONNAME, extensionName);
        }

        public void setNestedElement(String nestedElement) {
            fieldContainer.put(NESTEDELEMENT, nestedElement);
        }

        public String toString() {
            return "{OExtensionAssignOperation; " + getExtensionName() + "}";
        }

        @Override
        public void dehydrate() {
            super.dehydrate();
            setExtensionName(null);
            setNestedElement(null);
        }

        public Type getType() {
            return Type.ExtensionOperation;
        }
    }

    /**
     * Direct reference: selects named child of the message document element. This is used for
     * access to extensions (SOAP headers for example).
     * 
     * @author mszefler
     */
    public static class DirectRef extends OBase implements RValue, LValue, Serializable {
        public static final long serialVersionUID = -1L;
        /** Referenced Variable */
        private static final String VARIABLE = "variable";

        /** Name of the element referenced. */
        private static final String ELNAME = "elName";

        @JsonCreator
        public DirectRef() {}

        public DirectRef(OProcess owner) {
            super(owner);
        }

        @JsonIgnore
        public QName getElName() {
            Object o = fieldContainer.get(ELNAME);
            return o == null ? null : (QName) o;
        }

        @JsonIgnore
        public OScope.Variable getVariable() {
            Object o = fieldContainer.get(VARIABLE);
            return o == null ? null : (OScope.Variable) o;
        }

        public void setElName(QName elName) {
            fieldContainer.put(ELNAME, elName);
        }

        public void setVariable(Variable variable) {
            fieldContainer.put(VARIABLE, variable);
        }
    }

    public static class Expression extends OBase implements RValue, Serializable {
        public static final long serialVersionUID = -1L;
        private static final String EXPRESSION = "expression";

        @JsonCreator
        public Expression() {}

        public Expression(OProcess owner, OExpression compiledExpression) {
            super(owner);
            setExpression(compiledExpression);
        }

        @JsonIgnore
        public OExpression getExpression() {
            Object o = fieldContainer.get(EXPRESSION);
            return o == null ? null : (OExpression) o;
        }

        public void setExpression(OExpression expression) {
            fieldContainer.put(EXPRESSION, expression);
        }

        public String toString() {
            return getExpression().toString();
        }
    }

    public static class Literal extends OBase implements RValue, Serializable {
        public static final long serialVersionUID = -1L;
        private static final String XMLLITERAL = "xmlLiteral";

        @JsonCreator
        public Literal() {}

        public Literal(OProcess owner, Document xmlLiteral) {
            super(owner);
            if (xmlLiteral == null)
                throw new IllegalArgumentException("null xmlLiteral!");
            setXmlLiteral(org.apache.ode.utils.DOMUtils.domToString(xmlLiteral));
        }

        @JsonIgnore
        public String getXmlLiteral() {
            Object o = fieldContainer.get(XMLLITERAL);
            return o == null ? null : (String) o;
        }

        public void setXmlLiteral(String xmlLiteral) {
            fieldContainer.put(XMLLITERAL, xmlLiteral);
        }

        public String toString() {
            return "{Literal " + getXmlLiteral() + "}";
        }
    }

    public interface LValue {
        OScope.Variable getVariable();
    }

    public static class LValueExpression extends OBase implements LValue, Serializable {
        public static final long serialVersionUID = -1L;
        private static final String EXPRESSION = "expression";

        @JsonCreator
        public LValueExpression() {}

        public LValueExpression(OProcess owner, OLValueExpression compiledExpression) {
            super(owner);
            setExpression(compiledExpression);
        }

        @JsonIgnore
        public OLValueExpression getExpression() {
            Object o = fieldContainer.get(EXPRESSION);
            return o == null ? null : (OLValueExpression) o;
        }

        /**
         * @see org.apache.ode.bpel.obj.OAssign.LValue#getVariable()
         */
        @JsonIgnore
        public Variable getVariable() {
            return getExpression().getVariable();
        }

        @JsonIgnore
        public boolean isInsertMissingToData() {
            return getExpression().isInsertMissingData();
        }

        public void setExpression(OLValueExpression expression) {
            fieldContainer.put(EXPRESSION, expression);
        }

        public void setInsertMissingToData(boolean insertMissingToData) {
            getExpression().setInsertMissingData(insertMissingToData);
        }

        public String toString() {
            return getExpression().toString();
        }

    }

    public static class PartnerLinkRef extends OBase implements RValue, LValue, Serializable {
        public static final long serialVersionUID = -1L;
        private static final String PARTNERLINK = "partnerLink";
        private static final String ISMYENDPOINTREFERENCE = "isMyEndpointReference";

        @JsonCreator
        public PartnerLinkRef() {
            setIsMyEndpointReference(false);
        }

        public PartnerLinkRef(OProcess owner) {
            super(owner);
            setIsMyEndpointReference(false);
        }

        @JsonIgnore
        public boolean isIsMyEndpointReference() {
            Object o = fieldContainer.get(ISMYENDPOINTREFERENCE);
            return o == null ? false : (Boolean) o;
        }

        @JsonIgnore
        public OPartnerLink getPartnerLink() {
            Object o = fieldContainer.get(PARTNERLINK);
            return o == null ? null : (OPartnerLink) o;
        }

        // Must fit in a LValue even if it's not variable based
        @JsonIgnore
        public Variable getVariable() {
            return null;
        }

        public void setIsMyEndpointReference(boolean isMyEndpointReference) {
            fieldContainer.put(ISMYENDPOINTREFERENCE, isMyEndpointReference);
        }

        public void setPartnerLink(OPartnerLink partnerLink) {
            fieldContainer.put(PARTNERLINK, partnerLink);
        }

        public String toString() {
            return "{PLinkRef " + getPartnerLink() + "!" + isIsMyEndpointReference() + "}";
        }
    }

    public static class PropertyRef extends OBase implements RValue, LValue, Serializable {
        public static final long serialVersionUID = -1L;
        private static final String VARIABLE = "variable";
        private static final String PROPERTYALIAS = "propertyAlias";

        @JsonCreator
        public PropertyRef() {}

        public PropertyRef(OProcess owner) {
            super(owner);
        }

        @JsonIgnore
        public OPropertyAlias getPropertyAlias() {
            Object o = fieldContainer.get(PROPERTYALIAS);
            return o == null ? null : (OPropertyAlias) o;
        }

        @JsonIgnore
        public Variable getVariable() {
            Object o = fieldContainer.get(VARIABLE);
            return o == null ? null : (Variable) o;
        }

        public void setPropertyAlias(OPropertyAlias propertyAlias) {
            fieldContainer.put(PROPERTYALIAS, propertyAlias);
        }

        public void setVariable(Variable variable) {
            fieldContainer.put(VARIABLE, variable);
        }

        public String toString() {
            return "{PropRef " + getVariable() + "!" + getPropertyAlias() + "}";
        }
    }

    public interface RValue {
    }

    public static class VariableRef extends OBase implements RValue, LValue, Serializable {
        public static final long serialVersionUID = -1L;
        private static final String VARIABLE = "variable";
        private static final String PART = "part";
        private static final String HEADERPART = "headerPart";
        private static final String LOCATION = "location";

        @JsonCreator
        public VariableRef() {}

        public VariableRef(OProcess owner) {
            super(owner);
        }

        @JsonIgnore
        public Part getHeaderPart() {
            Object o = fieldContainer.get(HEADERPART);
            return o == null ? null : (Part) o;
        }

        @JsonIgnore
        public OExpression getLocation() {
            Object o = fieldContainer.get(LOCATION);
            return o == null ? null : (OExpression) o;
        }

        @JsonIgnore
        public Part getPart() {
            Object o = fieldContainer.get(PART);
            return o == null ? null : (Part) o;
        }

        @JsonIgnore
        public Variable getVariable() {
            Object o = fieldContainer.get(VARIABLE);
            return o == null ? null : (Variable) o;
        }

        @JsonIgnore
        public boolean isHeaderRef() {
            return getVariable().getType() instanceof OMessageVarType && getHeaderPart() != null
                    && getLocation() == null;
        }

        /**
         * Report whether this is a reference to a whole "message"
         * 
         * @return <code>true</code> if whole-message reference
         */
        @JsonIgnore
        public boolean isMessageRef() {
            return getVariable().getType() instanceof OMessageVarType && getPart() == null
                    && getHeaderPart() == null && getLocation() == null;
        }

        /**
         * Report whether this is a reference to a message part.
         * 
         * @return <code>true</code> if reference to a message part
         */
        @JsonIgnore
        public boolean isPartRef() {
            return getVariable().getType() instanceof OMessageVarType && getPart() != null
                    && getLocation() == null;
        }

        public void setHeaderPart(Part headerPart) {
            fieldContainer.put(HEADERPART, headerPart);
        }

        public void setLocation(OExpression location) {
            fieldContainer.put(LOCATION, location);
        }

        public void setPart(Part part) {
            fieldContainer.put(PART, part);
        }

        public void setVariable(Variable variable) {
            fieldContainer.put(VARIABLE, variable);
        }

        public String toString() {
            return "{VarRef " + getVariable() + (getPart() == null ? "" : "." + getPart().getName())
                    + (getLocation() == null ? "" : getLocation().toString()) + "}";
        }
    }
}
