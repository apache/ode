package de.unistuttgart.iaas.bpel.extensions.bpel4restlight;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.runtime.PartnerLinkInstance;
import org.apache.ode.bpel.runtime.common.extension.AbstractSyncExtensionOperation;
import org.apache.ode.bpel.runtime.common.extension.ExtensionContext;
import org.w3c.dom.Element;

import de.unistuttgart.iaas.xml.DomXmlConverter;

/**
 * 
 * Copyright 2011 IAAS University of Stuttgart <br>
 * <br>
 * 
 * THIS CLASS IS USED FOR TEST PURPOSES!
 * 
 * @author uwe.breitenbuecher@iaas.uni-stuttgart.de
 * 
 */
public class EPRDemoOperation extends AbstractSyncExtensionOperation {
	
	/** {@inheritDoc} */
	@Override
	protected void runSync(ExtensionContext context, Element element) throws FaultException {
		System.out.println("bin hier");
		try {
			for (OPartnerLink l : context.getOActivity().getOwner().allPartnerLinks) {
				
				PartnerLinkInstance pli = context.resolvePartnerLinkInstance(l);
				
				Element epr = context.getRuntimeInstance().fetchPartnerRoleEndpointReferenceData(pli);
				System.out.println("EPR|||");
				System.out.println(DomXmlConverter.nodeToString(epr, null));
				
				System.out.println(epr.getChildNodes().item(0).getChildNodes().item(1).getNodeName());
				System.out.println(epr.getChildNodes().item(0).getChildNodes().item(1).getTextContent());
				System.out.println("..");
				System.out.println(epr.getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(0).getTextContent());
				
				epr.getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(0).setTextContent("http://localhost:8084/FRP/rrr");
				
				System.out.println(epr.getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(0).getTextContent());
				
				System.out.println(context.getDUDir().toString());
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("ich bin hiiiiiiiiiier!!");
		
		try {
			URL url = new URL("http://localhost:8084/FRP/rrr");
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("PUT");
			
			int responseCode = connection.getResponseCode();
			System.out.println(responseCode);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
