package org.opentosca.bpel4restlight.rest;

/**
 */
public class HttpResponseMessage {
	
	private int statusCode;
	private String responseBody;
	
	
	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return this.statusCode;
	}
	
	/**
	 * @param statusCode the statusCode to set
	 */
	protected void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
	/**
	 * @return the responseBody
	 */
	public String getResponseBody() {
		return this.responseBody;
	}
	
	/**
	 * @param responseBody the responseBody to set
	 */
	protected void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
	
}
