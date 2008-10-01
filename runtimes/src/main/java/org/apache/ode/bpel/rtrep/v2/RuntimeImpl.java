package org.apache.ode.bpel.rtrep.v2;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.rapi.*;
import org.apache.ode.bpel.rtrep.common.ConfigurationException;
import org.apache.ode.bpel.extension.ExtensionBundleRuntime;
import org.apache.ode.jacob.soup.ReplacementMap;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;

public class RuntimeImpl implements OdeRuntime {

    private static final Log __log = LogFactory.getLog(RuntimeImpl.class);
    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    ProcessConf _pconf;
    OProcess _oprocess;
    Set<String> _mustUnderstandExtensions;
    ReplacementMap _replacementMap;
    ExpressionLanguageRuntimeRegistry _expLangRuntimeRegistry;
    Map<String, ExtensionBundleRuntime> _extensionRegistry;

    /**
     * Initialize according to process configuration.
     */
    public void init(ProcessConf pconf, ProcessModel pmodel) {
        _pconf = pconf;
        _oprocess = (OProcess) pmodel;

        _replacementMap = new ReplacementMapImpl(_oprocess);

        // Create an expression language registry for this process
        ExpressionLanguageRuntimeRegistry elangRegistry = new ExpressionLanguageRuntimeRegistry();
        for (OExpressionLanguage elang : _oprocess.expressionLanguages) {
            try {
                elangRegistry.registerRuntime(elang);
            } catch (ConfigurationException e) {
                String msg = __msgs.msgExpLangRegistrationError(elang.expressionLanguageUri, elang.properties);
                __log.error(msg, e);
                throw new BpelEngineException(msg, e);
            }
        }
        _expLangRuntimeRegistry = elangRegistry;

        // Checking for registered extension bundles, throw an exception when
        // a "mustUnderstand" extension is not available
        _mustUnderstandExtensions = new HashSet<String>();
        for (OProcess.OExtension extension : _oprocess.declaredExtensions) {
            if (extension.mustUnderstand) {
                if (_extensionRegistry.get(extension.namespaceURI) == null) {
                    String msg = __msgs.msgExtensionMustUnderstandError(_pconf.getProcessId(), extension.namespaceURI);
                    __log.error(msg);
                    throw new BpelEngineException(msg);
                } else {
                    _mustUnderstandExtensions.add(extension.namespaceURI);
                }
            } else {
                __log.warn("The process declares the extension namespace " + extension.namespaceURI
                        + " that is unkown to the engine");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ode.bpel.engine.rapi.OdeRuntime#createInstance(org.apache.ode.bpel.engine.rapi.OdeRTInstanceContext)
     */
    public OdeRTInstance newInstance(Object state) {
        return new RuntimeInstanceImpl(this, (ExecutionQueueImpl) state);
    }

    public ReplacementMap getReplacementMap(QName processName) {
        if (_pconf.getProcessId().equals(processName))
            return new ReplacementMapImpl(_oprocess);
        else throw new UnsupportedOperationException("Implement the creation of replacement map for other version.");
    }

    public ProcessModel getModel() {
        return _oprocess;
    }

    /**
     * Extract the value of a BPEL property from a BPEL messsage variable.
     *
     * @param msgData message variable data
     * @param aliasModel alias to apply
     * @param target description of the data (for error logging only)
     * @return value of the property
     * @throws org.apache.ode.bpel.common.FaultException
     */
    public String extractProperty(Element msgData, PropertyAliasModel aliasModel, String target) throws FaultException {
        OProcess.OPropertyAlias alias = (OProcess.OPropertyAlias) aliasModel;
        PropertyAliasEvaluationContext ectx = new PropertyAliasEvaluationContext(msgData, alias);
        Node lValue = ectx.getRootNode();

        if (alias.location != null)
            lValue = _expLangRuntimeRegistry.evaluateNode(alias.location, ectx);

        if (lValue == null) {
            String errmsg = __msgs.msgPropertyAliasReturnedNullSet(alias.getDescription(), target);
            if (__log.isErrorEnabled()) __log.error(errmsg);
            throw new FaultException(_oprocess.constants.qnSelectionFailure, errmsg);
        }

        if (lValue.getNodeType() == Node.ELEMENT_NODE) {
            // This is a bit hokey, we concatenate all the children's values; we really should be
            // checking to make sure that we are only dealing with text and attribute nodes.
            StringBuffer val = new StringBuffer();
            NodeList nl = lValue.getChildNodes();
            for (int i = 0; i < nl.getLength(); ++i) {
                Node n = nl.item(i);
                val.append(n.getNodeValue());
            }
            return val.toString();
        } else if (lValue.getNodeType() == Node.TEXT_NODE) {
            return ((Text) lValue).getWholeText();
        } else
            return null;
    }

    public String extractMatch(Element msgData, PropertyExtractor extractor) throws FaultException {
        OExpression expr = (OExpression) extractor;
        BoundVariableEvaluationContext ctx = new BoundVariableEvaluationContext();
        ctx.addBoundVariable(extractor.getMessageVariableName(), msgData);
        return ((Text)_expLangRuntimeRegistry.evaluateNode(expr, ctx)).getWholeText();
    }

    public void clear() {
        _pconf = null;
        _oprocess = null;
        _mustUnderstandExtensions = null;
        _replacementMap = null;
        _expLangRuntimeRegistry = null;
        _extensionRegistry = null;
    }

    public void setExtensionRegistry(Map<String, ExtensionBundleRuntime> extensionRegistry) {
        _extensionRegistry = extensionRegistry;
    }

}
