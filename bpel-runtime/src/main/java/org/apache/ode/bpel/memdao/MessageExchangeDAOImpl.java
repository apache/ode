package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class MessageExchangeDAOImpl implements MessageExchangeDAO {

	private String messageExchangeId;
	private MessageDAO response;
	private Date createTime;
	private MessageDAO request;
	private String operation;
	private QName portType;
	private String status;
	private int partnerLinkModelId;
	private String correlationId;
	private String pattern;
	private Element ePR;
	private Element callbackEPR;
	private String channel;
	private boolean propagateTransactionFlag;
	private String fault;
    private String faultExplanation;
    private String correlationStatus;
	private ProcessDAO process;
	private ProcessInstanceDAO instance;
	private char direction;
	private QName callee;
	private Properties properties = new Properties();
    private PartnerLinkDAOImpl _plink;
	
	public MessageExchangeDAOImpl(char direction, String mesageEchangeId){
		this.direction = direction;
		this.messageExchangeId = mesageEchangeId;
	}
	
	public String getMessageExchangeId() {
		return messageExchangeId;
	}

	public MessageDAO getResponse() {
		return response;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public MessageDAO getRequest() {
		return request;
	}

	public String getOperation() {
		return operation;
	}

	public QName getPortType() {
		return portType;
	}

	public void setPortType(QName porttype) {
		this.portType = porttype;

	}

	public void setStatus(String status) {
		this.status = status;

	}

	public String getStatus() {
		return status;
	}

	public MessageDAO createMessage(QName type) {
		MessageDAO messageDAO = new MessageDAOImpl(this);
		messageDAO.setType(type);
		return messageDAO;
	}

	public void setRequest(MessageDAO msg) {
		this.request = msg;

	}

	public void setResponse(MessageDAO msg) {
		this.response = msg;

	}

	public int getPartnerLinkModelId() {
		return partnerLinkModelId;
	}

	public void setPartnerLinkModelId(int modelId) {
		this.partnerLinkModelId = modelId;

	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;

	}

	public void setPattern(String string) {
		this.pattern = string;

	}

	public void setOperation(String opname) {
		this.operation = opname;

	}

	public void setEPR(Element epr) {
		this.ePR = epr;

	}

	public Element getEPR() {
		return ePR;
	}

	public void setCallbackEPR(Element epr) {
		this.callbackEPR = epr;

	}

	public Element getCallbackEPR() {
		return callbackEPR;
	}

	public String getPattern() {
		return pattern;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String string) {
		this.channel = string;
	}

	public boolean getPropagateTransactionFlag() {
		return propagateTransactionFlag;
	}

	public String getFault() {
		return fault;
	}

	public void setFault(String faultType) {
		this.fault = faultType;
	}

    public String getFaultExplanation() {
        return faultExplanation;
    }

    public void setFaultExplanation(String explanation) {
        this.faultExplanation = explanation;
    }



    public void setCorrelationStatus(String cstatus) {
		this.correlationStatus = cstatus;
	}

	public String getCorrelationStatus() {
		return correlationStatus;
	}

	public ProcessDAO getProcess() {
		return process;
	}

	public void setProcess(ProcessDAO process) {
		this.process = process;

	}

	public void setInstance(ProcessInstanceDAO dao) {
		this.instance = dao;

	}

	public ProcessInstanceDAO getInstance() {
		return instance;
	}

	public char getDirection() {
		return direction;
	}

	public QName getCallee() {
		return callee;
	}

	public void setCallee(QName callee) {
		this.callee = callee;

	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key,value);
	}

    public void setPartnerLink(PartnerLinkDAO plinkDAO) {
        _plink = (PartnerLinkDAOImpl) plinkDAO;
        
    }

    public PartnerLinkDAO getPartnerLink() {
        return _plink;
    }

    public Set<String> getPropertyNames() {
        HashSet<String> retVal = new HashSet<String>();
        for (Entry<Object,Object> e : properties.entrySet()) {
            retVal.add((String)e.getKey());
        }
        return retVal;
    }

}
