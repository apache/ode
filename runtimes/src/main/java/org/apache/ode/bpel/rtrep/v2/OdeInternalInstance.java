package org.apache.ode.bpel.rtrep.v2;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.rtrep.v2.channels.TimerResponseChannel;
import org.apache.ode.bpel.rtrep.v2.channels.ActivityRecoveryChannel;
import org.apache.ode.bpel.rtrep.v2.channels.PickResponseChannel;
import org.apache.ode.bpel.rtrep.v2.channels.FaultData;
import org.apache.ode.bpel.extension.ExtensionOperation;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.evt.ProcessInstanceStartedEvent;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.net.URI;


public interface OdeInternalInstance {

    boolean isCorrelationInitialized(CorrelationSetInstance correlationSet);

    String readProperty(VariableInstance variable, OProcess.OProperty property) throws FaultException;

    void writeCorrelation(CorrelationSetInstance cset, CorrelationKey ckeyVal) throws FaultException;

    Node initializeVariable(VariableInstance var, ScopeFrame scopeFrame, Node val)
            throws ExternalVariableModuleException;

    long genId();

    Long createScopeInstance(Long scopeInstanceId, OScope scopedef);

    void initializePartnerLinks(Long parentScopeId, Collection<OPartnerLink> partnerLinks);

    void initializeResource(Long parentScopeId, OResource resource, String url);

    void initializeInstantiatingUrl(String url);

    String getInstantiatingUrl();

    String invoke(String invokeId, PartnerLinkInstance instance, Operation operation, Element outboundMsg, Object object)
            throws FaultException;

    String invoke(String requestId, org.apache.ode.bpel.iapi.Resource resource, Element outgoingMessage)
            throws FaultException;

    Node getPartData(Element message, OMessageVarType.Part part);

    Element getPartnerResponse(String mexId);

    boolean isPartnerRoleEndpointInitialized(PartnerLinkInstance pLink);

    String getSourceSessionId(String mexId);

    Node getSourceEPR(String mexId);

    void writeEndpointReference(PartnerLinkInstance plval, Element element);

    void releasePartnerMex(String mexId, boolean instanceSucceeded);

    QName getPartnerFault(String mexId);

    QName getPartnerResponseType(String mexId);

    String getPartnerFaultExplanation(String mexId);

    void initializePartnersSessionId(PartnerLinkInstance instance, String partnersSessionId);

    Element getMyRequest(String mexId);

    Map<String,String> getProperties(String mexId);

    void setInstantiatingMex(String mexId);

    void registerTimer(TimerResponseChannel timerChannel, Date future);

    void registerActivityForRecovery(ActivityRecoveryChannel recoveryChannel, long id, String reason, Date dateTime,
            Element details, String[] actions, int retryCount);

    void unregisterActivityForRecovery(ActivityRecoveryChannel recoveryChannel);

    void cancelOutstandingRequests(String channelId);

    void select(PickResponseChannel pickResponseChannel, Date timeout, boolean createInstance, Selector[] selectors)
            throws FaultException;

    void checkResourceRoute(ResourceInstance resourceInstance, String mexRef,
                            PickResponseChannel pickResponseChannel, int selectorIdx);

    CorrelationKey readCorrelation(CorrelationSetInstance cset);

    ExpressionLanguageRuntimeRegistry getExpLangRuntime();

    Node fetchMyRoleEndpointReferenceData(PartnerLinkInstance link);

    Node fetchPartnerRoleEndpointReferenceData(PartnerLinkInstance link) throws FaultException;

    String fetchMySessionId(PartnerLinkInstance linkInstance);

    void cancel(PickResponseChannel responseChannel);

    Node convertEndpointReference(Element epr, Node lvaluePtr);

    void commitChanges(VariableInstance var, ScopeFrame scopeFrame, Node value) throws ExternalVariableModuleException;

    Node fetchVariableData(VariableInstance var, ScopeFrame scopeFrame,
                                  OMessageVarType.Part part, boolean forWriting) throws FaultException;

    void sendEvent(ScopeEvent event);

    void sendEvent(ProcessInstanceStartedEvent evt);

    Long getInstanceId();

    URI getBaseResourceURI();

    boolean isVariableInitialized(VariableInstance var);

    void completedFault(FaultData faultData);

    void completedOk();

    Node fetchVariableData(VariableInstance variable, ScopeFrame scopeFrame, boolean forWriting) throws FaultException;

    ExtensionOperation createExtensionActivityImplementation(QName name);
    
    void completeExtensionActivity(String channelId, FaultData faultData);

    void terminate();

    void forceFlush();

    void forceRollback();
    
	int getRetryDelay();
	
	boolean isFirstTry();

	boolean isRetryable();

	void setRetriedOnce();

	void setRetriesDone();

	void setAtomicScope(boolean atomicScope);
	
	/**
	 * Retrieves the property value that has been defined for this BPEL Process type.
	 * 
	 * @return propertyValue - the value corresponding to the process property name.
	 */
	Node getProcessProperty(QName propertyName);
    void associateEvent(PartnerLinkInstance plinkInstance, String opName, CorrelationKey key, String mexRef, String mexDAO) throws FaultException;

    void associateEvent(ResourceInstance resourceInstance, String mexRef, String scopeIid);

    void reply(PartnerLinkInstance plink, String opName, String bpelmex, Element element, QName fault)
            throws FaultException;

    void reply(ResourceInstance resource, String bpelmex, Element element, QName fault) throws FaultException;

}
