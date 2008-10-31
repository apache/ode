package org.apache.ode.bpel.rapi;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Engine provided methods for variable management.
 *
 * @author mszefler
 *
 */
public interface VariableContext {

    /**
     * Create a scope instance object. As far as the engine is concerned
     * a scope is an abstract entity for grouping variables of various sorts.
     * @param parentScopeId id of parent scope (null if root scope)
     * @param scopename the type of scope, i.e. the name of the scope
     * @return scope instance identifier
     */
    Long createScopeInstance(Long parentScopeId, String scopename,
                             int scopemodelid);


    /**
     * Checks for variable initialization, i.e. has had a 'write'
     * @param variable variable
     *
     * @return <code>true</code> if initialized
     */
    boolean isVariableInitialized(Variable variable);

    /**
     * Fetch variable data from store.
     */
    Node fetchVariableData(Variable var, boolean forWriting);

    /**
     * Save changes to variable.
     * @param var variable identifier
     * @param changes changes
     */
    void commitChanges(Variable var, Node changes);

    /**
     * Initialize variable with a value.
     * @param var variable identifier
     * @param initData value
     * @return mutable copy of data
     */
    Node initializeVariable(Variable var, Node initData);

    public Node readExtVar(Variable variable, Node reference) throws ExternalVariableModuleException;

    public ValueReferencePair writeExtVar(Variable variable, Node reference, Node value)
            throws ExternalVariableModuleException;

    /**
     * Read variable property. Variable properties are simple nv-pair
     * annotations that can be assigned to each variable.
     * @param variable
     * @param property
     * @return
     */
    String readVariableProperty(Variable variable, QName property) throws UninitializedVariableException;

    /**
     * Write variable property.
     * @param variable
     * @param property
     * @param value
     * @throws UninitializedVariableException
     */
    void writeVariableProperty(Variable variable, QName property, String value) throws UninitializedVariableException;


    /**
     * Initializes endpoint references for partner links inside a scope.
     * @param parentScopeId
     * @param partnerLinks
     */
    void initializePartnerLinks(Long parentScopeId,
                                Collection<? extends PartnerLinkModel> partnerLinks);

    void initializeResource(Long parentScopeId, ResourceModel resource, String url);

    void checkResourceRoute(Resource instance, String pickResponseChannel, int selectorIdx);

    /**
     * Fetches the my-role endpoint reference data.
     *
     * @param plink
     * @return
     * @throws FaultException
     */
    Element fetchMyRoleEndpointReferenceData(PartnerLink plink);

    Element fetchPartnerRoleEndpointReferenceData(PartnerLink pLink);

    /**
     * Determine if the partner role of an endpoint has been initialized (either
     * explicitly throug assginment or via the deployment descriptor)
     *
     * @param pLink partner link
     * @return
     */
    boolean isPartnerRoleEndpointInitialized(PartnerLink pLink);

    /**
     * Fetches our session id associated with the partner link instance. This
     * will always return a non-null value.
     *
     * @param pLink partner link
     */
    String fetchMySessionId(PartnerLink pLink);

    /**
     * Fetches the partner's session id associated with the partner link
     * instance.
     *
     * @param pLink partner link
     */
    String fetchPartnersSessionId(PartnerLink pLink);

    /**
     * Initialize the partner's session id for this partner link instance.
     *
     * @param pLink partner link
     * @param session session identifier
     */
    void initializePartnersSessionId(PartnerLink pLink, String session);



    /**
     * Writes a partner EPR.
     *
     * @param partnerLink
     * @param data
     * @throws FaultException
     */
    void writeEndpointReference(PartnerLink partnerLink, Element data);

    Node convertEndpointReference(Element epr, Node targetNode);

    //
    // Correlation variables
    //

    boolean isCorrelationInitialized(CorrelationSet cset);

    CorrelationKey readCorrelation(CorrelationSet cset);

    void writeCorrelation(CorrelationSet cset, QName[] propNames,
                          CorrelationKey correlation);

    public class ValueReferencePair {
        public Node value;
        public Node reference;
    }

}
