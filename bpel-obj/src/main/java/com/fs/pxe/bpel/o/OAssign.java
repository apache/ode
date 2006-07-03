/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

import com.fs.pxe.bpel.o.OMessageVarType.Part;
import com.fs.pxe.bpel.o.OScope.Variable;
import com.fs.utils.DOMUtils;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OAssign extends OActivity {
  static final long serialVersionUID = -1L  ;
  
  public final List<Copy> copy = new ArrayList<Copy>();

  public OAssign(OProcess owner) {
    super(owner);
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

    public Copy(OProcess owner) {
      super(owner);
    }

    public String toString() {
      return "{OCopy " + to + "=" + from + "}";
    }
  }

  public interface LValue {
    OScope.Variable getVariable();
  }

  public interface RValue { }

  public static class Literal extends OBase implements RValue {
    private static final long serialVersionUID = 1L;
		public transient Document xmlLiteral;

    public Literal(OProcess owner, Document xmlLiteral) {
      super(owner);
      if (xmlLiteral == null)
        throw new IllegalArgumentException("null xmlLiteral!");
      this.xmlLiteral = xmlLiteral;
    }

    public String toString() {
      return "{Literal " + xmlLiteral + "}";
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException
    {
      out.writeUTF(DOMUtils.domToString(xmlLiteral));
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException
    {
      String domStr = in.readUTF();
      try {
        xmlLiteral = DOMUtils.stringToDOM(domStr).getOwnerDocument();
      } catch (Exception ex) {
        throw (IOException)(new IOException("XML de-serialization error.")).initCause(ex);
      }
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
		 * @see com.fs.pxe.bpel.o.OAssign.LValue#getVariable()
		 */
		public Variable getVariable() {
			return expression.getVariable();
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

  public static class VariableRef extends OBase implements RValue, LValue {
    private static final long serialVersionUID = 1L;
		public OScope.Variable variable;
    public OMessageVarType.Part part;
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
    	return variable.type instanceof OMessageVarType && 
    	part == null && location == null;
    }
    
    /**
     * Report whether this is a reference to a message part. 
     * @return <code>true</code> if reference to a message part
     */
    public boolean isPartRef() {
    	return variable.type instanceof OMessageVarType && 
    	part != null && location == null;
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
}
