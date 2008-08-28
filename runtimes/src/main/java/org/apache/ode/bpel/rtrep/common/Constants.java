package org.apache.ode.bpel.rtrep.common;

import org.apache.ode.utils.Namespaces;

public class Constants {

    /**
     * Extension function bpws:getVariableData('variableName', 'partName'?,
     * 'locationPath'?)
     */
    public static final String EXT_FUNCTION_GETVARIABLEDATA = "getVariableData";

    /**
     * Extension function
     * bpws:getVariableProperty('variableName','propertyName')
     */
    public static final String EXT_FUNCTION_GETVARIABLEPROPRTY = "getVariableProperty";

    /**
     * Extension function bpws:getLinkStatus('getLinkName')
     */
    public static final String EXT_FUNCTION_GETLINKSTATUS = "getLinkStatus";

    /**
     * Extension function bpws:getLinkStatus('getLinkName')
     */
    public static final String EXT_FUNCTION_DOXSLTRANSFORM = "doXslTransform";

    /**
     * Non standard extension function ode:splitToElements(sourceText, 'separator' 'targetLocalName', 'targetNS'?)
     */
    public static final String NON_STDRD_FUNCTION_SPLITTOELEMENTS = "splitToElements";
    public static final String NON_STDRD_FUNCTION_COMBINE_URL = "combineUrl";
    public static final String NON_STDRD_FUNCTION_COMPOSE_URL = "composeUrl";
    public static final String NON_STDRD_FUNCTION_EXPAND_TEMPLATE = "expandTemplate";
    public static final String NON_STDRD_FUNCTION_DOM_TO_STRING= "domToString";


    public static boolean isBpelNamespace(String uri){
        return Namespaces.WS_BPEL_20_NS.equals(uri) || Namespaces.WSBPEL2_0_FINAL_EXEC.equals(uri)
                || Namespaces.BPEL11_NS.equals(uri);
    }
}
