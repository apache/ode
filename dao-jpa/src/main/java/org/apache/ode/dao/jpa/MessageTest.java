package org.apache.ode.dao.jpa;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="TEST_MESSAAGE")
public class MessageTest {
	
	@Basic @Column(name="MESSAGE") private String message;
	@ManyToOne @Column(name="FAULT_TEST_ID")
	private FaultTest faultTest;
	
	public MessageTest() {}
	

	public MessageTest(FaultTest input,String msg) {
		message = msg;
		faultTest = input;
	}
	
	public String getMessage() {
		return message;
	}
}
