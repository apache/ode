package org.apache.ode.bpel.rapi;

import java.util.List;

import javax.wsdl.Operation;

public interface PickReceiveModel extends ActivityModel {
	
	public boolean isCreateInstance();
	public void setCreateInstance(boolean createInstance);
	
	public List<OnAlarm> getOnAlarms();
	public static interface OnAlarm {
		public ActivityModel getActivity();
	}

	public List<OnMessage> getOnMessages();	
	public static interface OnMessage {
		public PartnerLinkModel getPartnerLink();
		public ActivityModel getActivity();
		public Operation getOperation();
	}
}
