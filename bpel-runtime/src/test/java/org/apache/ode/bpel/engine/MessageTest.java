package org.apache.ode.bpel.engine;

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;
/**
 * Tests for org.apache.ode.bpel.iapi.MessageImpl 
 */
public class MessageTest extends TestCase {
    /**
     * Test that setting a header replaces existing header of same name
     */
    public void testSetHeaderPart() {
        String headerName = "h1";
        MessageDAOMock mock = new MessageDAOMock();
        MessageImpl message = new MessageImpl(mock);
        message.setHeaderPart(headerName, "hello");
        
        // overwrite existing header
        String expected = "bye";
        message.setHeaderPart(headerName, expected);
        assertHasHeaders(message, mock, 1, headerName, expected);
        
        // try setting element content
        Document doc = DOMUtils.newDocument();
        Element content = doc.createElement("content");
        content.setTextContent(expected);
        message.setHeaderPart(headerName, content);
        assertHasHeaders(message, mock, 1, headerName, expected);
        
        // check children not removed
        content.setTextContent(expected);
        message.setHeaderPart("content", "testing");
        assertHasHeaders(message, mock, 2, headerName, expected);
        Element el = message.getHeaderPart(headerName);
        assertTrue("Missing header content",el.getElementsByTagName("content").getLength() == 1);
        
        // add new header
        String newHeader = "h2";
        String newExpected = "why";
        message.setHeaderPart(newHeader, newExpected);
        assertHasHeaders(message, mock, 3, newHeader, newExpected);
    }
    
    private static void assertHasHeaders(MessageImpl message, MessageDAOMock mock, int size, String name, String expected) {
        Map<String, Node> headers = message.getHeaderParts();
        assertTrue("Expected: "+size+" headers but found: "+headers.size(),headers.size() == size);
        assertEquals("Expected header content: "+expected+" but found: "+headers.get(name).getTextContent(),headers.get(name).getTextContent(), expected);
        // check underlying document to be sure
        Element el = mock.getHeader();
        NodeList list = el.getElementsByTagName(name);
        assertTrue("There should only be 1 element in headers for name: "+name, list.getLength() == 1);
    }
    
    /**
     * Mock MessageDAO to test simple Message operations
     */
    private class MessageDAOMock implements MessageDAO {
        Document _doc = null;
        Element _header = null;
        
        public MessageDAOMock() {
            _doc = DOMUtils.newDocument();
            _header = _doc.createElement("header");
        }
        
        public Element getData() {
            return null;
        }

        public Element getHeader() {
            return _header;
        }

        public MessageExchangeDAO getMessageExchange() {
            return null;
        }

        public QName getType() {
            return null;
        }

        public void setData(Element value) {
            
        }

        public void setHeader(Element value) {
            _header = value;
        }

        public void setType(QName type) {
            
        }
    }
}
