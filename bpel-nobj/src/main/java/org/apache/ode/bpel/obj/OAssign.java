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
package org.apache.ode.bpel.obj;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.obj.OMessageVarType.Part;
import org.apache.ode.bpel.obj.OProcess.OPropertyAlias;
import org.apache.ode.bpel.obj.OScope.Variable;
import org.w3c.dom.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class OAssign extends OActivity {
	private static final String COPY = "copy";

	public OAssign(OProcess owner, OActivity parent) {
		super(owner, parent);
		setCopy(new ArrayList<Copy>());
	}

	@Override
	public void dehydrate() {
		super.dehydrate();
		for (Copy copy : getCopy()) {
			copy.dehydrate();
		}
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<Copy> getCopy() {
		return (List<Copy>) fieldContainer.get(COPY);
	}

	public void setCopy(List<Copy> copy) {
		if (getCopy() == null){
			fieldContainer.put(COPY, copy);
		}
	}

	public String toString() {
		return "{OAssign : " + getName() + ", joinCondition="
				+ getJoinCondition() + "}";
	}

	/**
	 * Assignmenet copy entry, i.e. what the assignment consits of.
	 */
	public static class Copy extends OBase {
		private static final String TO = "to";
		private static final String FROM = "from";
		private static final String KEEPSRCELEMENTNAME = "keepSrcElementName";
		private static final String IGNOREMISSINGFROMDATA = "ignoreMissingFromData";
		private static final String IGNOREUNINITIALIZEDFROMVARIABLE = "ignoreUninitializedFromVariable";
		private static final String INSERTMISSINGTODATA = "insertMissingToData";

		public Copy(OProcess owner) {
			super(owner);
		}

		@Override
		public void dehydrate() {
			super.dehydrate();
			setTo(null);
			setFrom(null);
		}

		@JsonIgnore
		public RValue getFrom() {
			return (RValue) fieldContainer.get(FROM);
		}

		@JsonIgnore
		public boolean isIgnoreMissingFromData() {
			return (boolean) fieldContainer.get(IGNOREMISSINGFROMDATA);
		}

		@JsonIgnore
		public boolean getIgnoreUninitializedFromVariable() {
			return (boolean) fieldContainer
					.get(IGNOREUNINITIALIZEDFROMVARIABLE);
		}

		@JsonIgnore
		public boolean getInsertMissingToData() {
			return (boolean) fieldContainer.get(INSERTMISSINGTODATA);
		}

		@JsonIgnore
		public boolean getKeepSrcElementName() {
			return (boolean) fieldContainer.get(KEEPSRCELEMENTNAME);
		}

		@JsonIgnore
		public LValue getTo() {
			return (LValue) fieldContainer.get(TO);
		}

		public void setFrom(RValue from) {
			fieldContainer.put(FROM, from);
		}

		public void setIgnoreMissingFromData(boolean ignoreMissingFromData) {
			fieldContainer.put(IGNOREMISSINGFROMDATA, ignoreMissingFromData);
		}

		public void setIgnoreUninitializedFromVariable(
				boolean ignoreUninitializedFromVariable) {
			fieldContainer.put(IGNOREUNINITIALIZEDFROMVARIABLE,
					ignoreUninitializedFromVariable);
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
	}

	/**
	 * Direct reference: selects named child of the message document element.
	 * This is used for access to extensions (SOAP headers for example).
	 * @author mszefler
	 */
	public static class DirectRef extends OBase implements RValue, LValue {
		/** Referenced Variable */
		private static final String VARIABLE = "variable";

		/** Name of the element referenced. */
		private static final String ELNAME = "elName";

		public DirectRef(OProcess owner) {
			super(owner);
		}

		@JsonIgnore
		public QName getElName() {
			return (QName) fieldContainer.get(ELNAME);
		}

		@JsonIgnore
		public OScope.Variable getVariable() {
			return (OScope.Variable) fieldContainer.get(VARIABLE);
		}

		public void setElName(QName elName) {
			fieldContainer.put(ELNAME, elName);
		}

		public void setVariable(Variable variable) {
			fieldContainer.put(VARIABLE, variable);
		}
	}

	public static class Expression extends OBase implements RValue {
		private static final String EXPRESSION = "expression";

		public Expression(OProcess owner, OExpression compiledExpression) {
			super(owner);
			setExpression(compiledExpression);
		}

		@JsonIgnore
		public OExpression getExpression() {
			return (OExpression) fieldContainer.get(EXPRESSION);
		}

		public void setExpression(OExpression expression) {
			fieldContainer.put(EXPRESSION, expression);
		}

		public String toString() {
			return getExpression().toString();
		}
	}

	public static class Literal extends OBase implements RValue {
		private static final String XMLLITERAL = "xmlLiteral";

		public Literal(OProcess owner, Document xmlLiteral) {
			super(owner);
			if (xmlLiteral == null)
				throw new IllegalArgumentException("null xmlLiteral!");
			setXmlLiteral(org.apache.ode.utils.DOMUtils.domToString(xmlLiteral));
		}

		//TODO: transient fields, custome read/writeObject.
		@JsonIgnore
		public String getXmlLiteral() {
			return (String) fieldContainer.get(XMLLITERAL);
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

	public static class LValueExpression extends OBase implements LValue {
		private static final String EXPRESSION = "expression";

		public LValueExpression(OProcess owner,
				OLValueExpression compiledExpression) {
			super(owner);
			setExpression(compiledExpression);
		}

		@JsonIgnore
		public OLValueExpression getExpression() {
			return (OLValueExpression) fieldContainer.get(EXPRESSION);
		}

		/**
		 * @see org.apache.ode.bpel.obj.OAssign.LValue#getVariable()
		 */
		public Variable getVariable() {
			return getExpression().getVariable();
		}

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

	public static class PartnerLinkRef extends OBase implements RValue, LValue {
		private static final String PARTNERLINK = "partnerLink";
		private static final String ISMYENDPOINTREFERENCE = "isMyEndpointReference";

		public PartnerLinkRef(OProcess owner) {
			super(owner);
		}

		@JsonIgnore
		public boolean isIsMyEndpointReference() {
			return (boolean) fieldContainer.get(ISMYENDPOINTREFERENCE);
		}

		@JsonIgnore
		public OPartnerLink getPartnerLink() {
			return (OPartnerLink) fieldContainer.get(PARTNERLINK);
		}

		// Must fit in a LValue even if it's not variable based
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
			return "{PLinkRef " + getPartnerLink() + "!"
					+ isIsMyEndpointReference() + "}";
		}
	}

	public static class PropertyRef extends OBase implements RValue, LValue {
		private static final String VARIABLE = "variable";
		private static final String PROPERTYALIAS = "propertyAlias";

		public PropertyRef(OProcess owner) {
			super(owner);
		}

		@JsonIgnore
		public OPropertyAlias getPropertyAlias() {
			return (OPropertyAlias) fieldContainer.get(PROPERTYALIAS);
		}

		@JsonIgnore
		public Variable getVariable() {
			return (Variable) fieldContainer.get(VARIABLE);
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

	public static class VariableRef extends OBase implements RValue, LValue {
		private static final String VARIABLE = "variable";
		private static final String PART = "part";
		private static final String HEADERPART = "headerPart";
		private static final String LOCATION = "location";

		public VariableRef(OProcess owner) {
			super(owner);
		}

		@JsonIgnore
		public Part getHeaderPart() {
			return (Part) fieldContainer.get(HEADERPART);
		}

		@JsonIgnore
		public OExpression getLocation() {
			return (OExpression) fieldContainer.get(LOCATION);
		}

		@JsonIgnore
		public Part getPart() {
			return (Part) fieldContainer.get(PART);
		}

		@JsonIgnore
		public Variable getVariable() {
			return (Variable) fieldContainer.get(VARIABLE);
		}

		public boolean isHeaderRef() {
            return getVariable().getType() instanceof OMessageVarType && getHeaderPart() != null && getLocation() == null;
        }

		/**
		 * Report whether this is a reference to a whole "message"
		 * @return <code>true</code> if whole-message reference
		 */
		public boolean isMessageRef() {
            return getVariable().getType() instanceof OMessageVarType && getPart() == null && getHeaderPart() == null && getLocation() == null;
        }

		/**
		 * Report whether this is a reference to a message part.
		 * @return <code>true</code> if reference to a message part
		 */
		public boolean isPartRef() {
            return getVariable().getType() instanceof OMessageVarType && getPart() != null && getLocation() == null;
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
			return "{VarRef " + getVariable()
					+ (getPart() == null ? "" : "." + getPart())
					+ (getLocation() == null ? "" : getLocation().toString())
					+ "}";
		}
	}
}
