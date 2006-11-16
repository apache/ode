package org.apache.ode.dao.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

@Entity
@Table(name="TEST_FAULT")
public class FaultTest {
	
	@Lob @Column(name="NAME") private String name;
	@Basic @Column(name="PROPERTIES") private Properties props;
	@OneToMany(fetch=FetchType.LAZY,mappedBy="faultTest",cascade={CascadeType.ALL})
	private Collection<MessageTest> faultMessages = new ArrayList<MessageTest>();

	
	public String getName() {
		return name;
	}
	
	public Collection<MessageTest> getFaultMessages() {
		return faultMessages;
	}
	
	public void setName(String value) {
		name = value;
	}
	
	public void addProp(String key, String value) {
		if ( props == null ) props = new Properties();
		props.put(key, value);
	}
	
	public void addMessage (String msg) {
		//if ( faultMessages == null ) faultMessages = new ArrayList<MessageTest>();
		faultMessages.add(new MessageTest(this,"Test DATA"));
	}

}
