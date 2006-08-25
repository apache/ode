package org.apache.ode.bpel.memdao;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

public class MessageDAOImpl implements MessageDAO {
	
	private QName type;
	private Element data;
	private MessageExchangeDAO messageExchange;
	
	public MessageDAOImpl(MessageExchangeDAO messageExchange) {
		this.messageExchange = messageExchange;
	}

	public void setType(QName type) {
		this.type = type;
	}

	public QName getType() {
		return type;
	}

	public void setData(Element value) {
		this.data = value;
	}

	public Element getData() {
		if ( data == null ) {
			data = DOMUtils.newDocument().getDocumentElement();
		}
		return data;
	}

	public MessageExchangeDAO getMessageExchange() {
		return messageExchange;
	}

}
