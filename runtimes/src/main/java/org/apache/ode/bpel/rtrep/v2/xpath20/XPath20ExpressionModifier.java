package org.apache.ode.bpel.rtrep.v2.xpath20;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpression;

import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemChecker;
import net.sf.saxon.expr.PathExpression;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.xpath.XPathExpressionImpl;

import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.NSContext;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * A helper utility that modifies XPath Expression in-place.  This is meant
 * to be reusable across the XPath and XQuery runtimes.
 */
public class XPath20ExpressionModifier {
    private NSContext contextUris;
    private NamePool namePool;

    /**
     * Creates a new XPath20ExpressionModifier object.
     *
     * @param contextUris 
     * @param namePool 
     */
    public XPath20ExpressionModifier(NSContext contextUris, NamePool namePool) {
        this.contextUris = contextUris;
        this.namePool = namePool;
    }

    /**
     * Insert nodes into the specified XPath expression wherever
     * required To be precise, an node is added to its parent if: 
     * a) the node is an element... 
     * b) that corresponds to an step... 
     * c) that has a child axis... 
     * d) whose parent had no children with its name... 
     * e) and all preceding steps are element name tests.
     *
     * @param xpathExpr
     * @param namePool
     *
     * @throws DOMException
     * @throws TransformerException
     */
    @SuppressWarnings("unchecked")
    public void insertMissingData(XPathExpression xpathExpr, Node contextNode)
        throws DOMException, TransformerException {
        if ((contextNode == null) || !(contextNode instanceof Element) ||
                !(xpathExpr instanceof XPathExpressionImpl)) {
            return;
        }

        Expression expression = ((XPathExpressionImpl) xpathExpr).getInternalExpression();
        Iterator<Expression> subExpressions = (Iterator<Expression>) expression.iterateSubExpressions();

        if (!subExpressions.hasNext()) {
            return;
        }

        Expression subExpr = (Expression) subExpressions.next();

        if (!(subExpr instanceof PathExpression)) {
            return;
        }

        Document document = DOMUtils.toDOMDocument(contextNode);
        PathExpression pathExpr = (PathExpression) subExpr;
        Expression step = pathExpr.getFirstStep();

        while (step != null) {
            if (step instanceof AxisExpression) {
                AxisExpression axisExpr = (AxisExpression) step;

                NodeTest nodeTest = axisExpr.getNodeTest();

                if (!(nodeTest instanceof NameTest)) {
                    break;
                }

                NameTest nameTest = (NameTest) nodeTest;

                QName childName = getQualifiedName(nameTest.getFingerprint(),
                        namePool, contextUris);
                
                if (Axis.CHILD == axisExpr.getAxis()) {
                    if (NodeKindTest.ELEMENT.getNodeKindMask() != nameTest.getNodeKindMask()) {
                        break;
                    }
                	
                    NodeList children = ((Element) contextNode).getElementsByTagNameNS(childName.getNamespaceURI(),
                            childName.getLocalPart());
                    if ((children == null) || (children.getLength() == 0)) {
                        Node child = document.createElementNS(childName.getNamespaceURI(),
                                DOMUtils.getQualifiedName(childName));
                        contextNode.appendChild(child);
                        contextNode = child;
                    } else if (children.getLength() == 1) {
                        contextNode = children.item(0);
                    } else {
                        break;
                    }
                } else if (Axis.ATTRIBUTE == axisExpr.getAxis()) {
                    if (NodeKindTest.ATTRIBUTE.getNodeKindMask() != nameTest.getNodeKindMask()) {
                        break;
                    }
                    
                    Attr attribute = ((Element) contextNode).getAttributeNodeNS(childName.getNamespaceURI(), childName.getLocalPart());
                    if (attribute == null) {
                    	attribute = document.createAttributeNS(childName.getNamespaceURI(), childName.getLocalPart());
                    	((Element) contextNode).setAttributeNode(attribute);
                    	contextNode = attribute;
                    } else {
                    	break;
                    }
                	
                } else {
                	break;
                }


            } else if (step instanceof ItemChecker) {
                ItemChecker itemChecker = (ItemChecker) step;
                Expression baseExpr = itemChecker.getBaseExpression();

                if (!(baseExpr instanceof VariableReference)) {
                    break;
                }
            } else {
                break;
            }

            if (pathExpr != null) {
                Expression remainingSteps = pathExpr.getRemainingSteps();

                if (remainingSteps instanceof PathExpression) {
                    pathExpr = (PathExpression) remainingSteps;
                    step = pathExpr.getFirstStep();
                } else if (remainingSteps instanceof AxisExpression) {
                    pathExpr = null;
                    step = (AxisExpression) remainingSteps;
                }
            } else {
                break;
            }
        }
    }

    /**
     * Create the QName by running the given finger print against the
     * given context
     *
     * @param fingerprint
     * @param namePool
     * @param nsContext
     *
     * @return The QName corresponding to the finger print
     */
    private QName getQualifiedName(int fingerprint, NamePool namePool,
        NSContext nsContext) {
        String localName = namePool.getLocalName(fingerprint);
        String prefix = namePool.getPrefix(fingerprint);
        String uri = namePool.getURI(fingerprint);

        // Unfortunately, NSContext.getPrefix(String URI) doesn't always work
        // So, we need to find the prefix for the URI the hard way
        if ((prefix == null) || "".equals(prefix)) {
            for (String nsPrefix : nsContext.getPrefixes()) {
                String nsUri = nsContext.getNamespaceURI(nsPrefix);

                if (nsUri.equals(uri)) {
                    prefix = nsPrefix;
                }
            }
        }

        return new QName(uri, localName, prefix);
    }
}
