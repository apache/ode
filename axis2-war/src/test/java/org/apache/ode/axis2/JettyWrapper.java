/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.axis2;

import org.apache.commons.lang.StringUtils;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.StreamUtils;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class JettyWrapper {

    protected Server server;

    public JettyWrapper() throws Exception {
        this(7070);
    }

    public JettyWrapper(int port) throws Exception {
        server = new Server(port);
        addDefaultHandlers();
    }


    public JettyWrapper(int port, ContextHandler handler) {
        server = new Server(port);
        if(handler!=null) server.addHandler(handler);
        else addDefaultHandlers();
    }


    private void addDefaultHandlers() {
        ContextHandler arithmeticsContext = new ContextHandler();
        arithmeticsContext.setContextPath("/HttpBindingTest/ArithmeticsService");
        arithmeticsContext.setHandler(new ArithmeticsServiceHandler());

        ContextHandler blogContext = new ContextHandler();
        blogContext.setContextPath("/HttpBindingTest/BlogService");
        blogContext.setHandler(new BlogServiceHandler());

        ContextHandler echoContext = new ContextHandler();
        echoContext.setContextPath("/EchoService");
        echoContext.setHandler(new EchoServiceHandler());

        ContextHandlerCollection handlerColl = new ContextHandlerCollection();
        Handler[] handlers = {arithmeticsContext, blogContext, echoContext};
        handlerColl.setHandlers(handlers);

        server.addHandler(handlerColl);
    }

    public void start() throws Exception {
        try {
            server.start();
        } catch (Exception e) {
            server.stop();
            server.start();
        }
    }

    public void stop() throws Exception {
        server.stop();
    }

    static public class EchoServiceHandler extends AbstractHandler {

        public void handle(String s, HttpServletRequest request, HttpServletResponse response, int i) throws IOException, ServletException {
            String method = request.getMethod();
            if (request.getParameter("ping")!=null) {
                response.setStatus(200);
                response.getOutputStream().println("Yep, I'm here!");
            } else {
                if (!"GET".equals(method) && !"POST".equals(method)) {
                    response.setStatus(405);
                    response.setHeader("Allow", "GET, POST");
                } else {
                    Enumeration h = request.getHeaderNames();
                    // send back all headers
                    while (h.hasMoreElements()) {
                        String hname = (String) h.nextElement();
                        Enumeration values = request.getHeaders(hname);
                        while (values.hasMoreElements()) {
                            String next = (String) values.nextElement();
                            response.addHeader(hname, next);
                        }
                    }

                    // send back the body if any
                    String body = new String(StreamUtils.read(request.getInputStream()));
                    if (StringUtils.isNotEmpty(body))
                        response.getOutputStream().println(body);

                }
            }


            ((Request) request).setHandled(true);
        }
    }

    private class ArithmeticsServiceHandler extends AbstractHandler {
        /*
        8 urls to handle:
        (GET)       http://localhost:8888/HttpBindingTestService/OlaElMundo-GET/plus/(left):(right)
        (GET)       http://         ........                    /OlaElMundo-GET/minus?left=&right=
        (DELETE)    http://localhost:8888/HttpBindingTestService/OlaElMundo-DELETE/plus/(left):(right)
        (DELETE)    http://         ........                    /OlaElMundo-DELETE/minus?left=&right=
        (POST)      http://         ........                    /OlaElMundo-POST/plus
        (POST)      http://         ........                    /OlaElMundo-POST/minus
        (PUT)       http://         ........                    /OlaElMundo-PUT/plus
        (PUT)       http://         ........                    /OlaElMundo-PUT/minus
        (POST)      http://         ........                    /SalutLaTerre/addition
        (POST)      http://         ........                    /SalutLaTerre/sumOfIntegers
        */
        public void handle(String s, HttpServletRequest request, HttpServletResponse response, int i) throws IOException, ServletException {

            try {
                String method = request.getMethod();
                String uri = request.getRequestURI();
                if ("/HttpBindingTestService/".equals(uri)) {
                    response.getWriter().println("HttpBindingTestService ready!");
                } else if (uri.contains("OlaElMundo-GET") || uri.contains("OlaElMundo-DELETE")) {
//                    if (!"GET".equalsIgnoreCase(method)) {
                    if (false) {
                        response.sendError(405, "Expecting method is GET");
                    } else {
                        if (!uri.contains("plus") && !uri.contains("minus")) {
                            response.sendError(404);
                        } else {
                            boolean ok = true;
                            int left = 0, right = 0;
                            try {
                                if (uri.contains("plus")) {
                                    int index = uri.lastIndexOf("/");
                                    String[] op = uri.substring(index + 1).split(":");
                                    left = Integer.parseInt(op[0]);
                                    right = Integer.parseInt(op[1]);
                                } else if (uri.contains("minus")) {
                                    left = Integer.parseInt(request.getParameter("left"));
                                    right = -1 * Integer.parseInt(request.getParameter("right"));
                                } else {
                                    ok = false;
                                }
                            } catch (NumberFormatException e) {
                                ok = false;
                            }

                            if (!ok) {
                                response.sendError(400);
                            } else {
                                Document doc = DOMUtils.newDocument();
                                Element resElt = doc.createElement("theresult");
                                resElt.setTextContent(String.valueOf(left + right));
                                response.getOutputStream().print(DOMUtils.domToString(resElt));
                                response.getOutputStream().close();
                                response.setStatus(200);
                            }
                        }
                    }
                } else if (uri.contains("OlaElMundo-POST") || uri.contains("OlaElMundo-PUT")) {
//                    if (!"POST".equalsIgnoreCase(method)) {
                    if (false) {
                        response.sendError(405, "Expecting method is POST");
                        return;
                    } else {
                        String operation;
                        if (!uri.contains("plus") && !uri.contains("minus")) {
                            response.sendError(404);
                        } else {
                            // parse body, form-urlencoded
                            int res = Integer.MIN_VALUE;
                            boolean ok = true;
                            StringBuffer sb = null;
                            String body = new String(StreamUtils.read(request.getInputStream()));
                            if (!body.matches("[^=]*=[+-]?\\d*&[^=]*=[+-]?\\d*")) {
                                ok = false;
                            } else {
                                String[] sp = body.split("&");
                                String[] op0 = sp[0].split("=");
                                String[] op1 = sp[1].split("=");
                                try {
                                    int left, right;
                                    if (op0[0].equals("left")) {
                                        left = Integer.valueOf(op0[1]);
                                        right = Integer.valueOf(op1[1]);
                                    } else {
                                        left = Integer.valueOf(op1[1]);
                                        right = Integer.valueOf(op0[1]);
                                    }
                                    if (uri.contains("minus")) {
                                        right = -1 * right;
                                    }
                                    res = left + right;
                                } catch (NumberFormatException e) {
                                    ok = false;
                                }
                            }
                            if (!ok) {
                                response.sendError(400);
                            } else {
                                Element resElt = DOMUtils.newDocument().createElement("theresult");
                                resElt.setTextContent(String.valueOf(res));
                                response.getOutputStream().print(DOMUtils.domToString(resElt));
                                response.setStatus(200);
                            }
                        }
                    }
                } else if (uri.contains("SalutLaTerre")) {
                    if (!"POST".equalsIgnoreCase(method)) {
                        response.sendError(405, "Expecting method is POST");
                        return;
                    } else {
                        if (uri.contains("addition")) {
                            Document bodyDoc = parseBody(request.getInputStream(), response);
                            if (bodyDoc != null) {
                                // we expect the element operandList
                                if (!"operandList".equals(bodyDoc.getDocumentElement().getNodeName())) {
                                    response.sendError(400, "The first element should be named operandList");
                                } else {
                                    Element firstOperand = DOMUtils.getFirstChildElement(bodyDoc.getDocumentElement());
                                    Element secondElement = DOMUtils.getNextSiblingElement(firstOperand);
                                    String left = DOMUtils.getTextContent(firstOperand);
                                    String right = DOMUtils.getTextContent(secondElement);
                                    Element res = bodyDoc.createElementNS("http://ode/bpel/test/arithmetics", "theresult");
                                    res.setTextContent(String.valueOf(Integer.valueOf(left) + Integer.valueOf(right)));
                                    response.getOutputStream().print(DOMUtils.domToString(res));
                                    response.setStatus(200);
                                }
                            }

                        } else if (uri.contains("sumOfIntegers")) {
                            Document bodyDoc = parseBody(request.getInputStream(), response);
                            if (bodyDoc != null) {
                                Element firstOperand = DOMUtils.getFirstChildElement(bodyDoc.getDocumentElement());
                                Element secondOperand = DOMUtils.getNextSiblingElement(firstOperand);
                                int left = Integer.valueOf(DOMUtils.getTextContent(firstOperand));
                                int right = Integer.valueOf(DOMUtils.getTextContent(secondOperand));

                                int min = Math.min(left, right);
                                int max = Math.max(left, right);
//                                Element arrayElt = bodyDoc.createElement("sumOfInteger");
                                Element anElt = bodyDoc.createElementNS("http://ode/bpel/test/arithmetics", "sumOfInteger");
                                Element msg = bodyDoc.createElement("theresult");
                                Element resultIs = bodyDoc.createElement("resultIs");
                                msg.setTextContent("A dummy message we don't care about. Only purpose is to have a complex type");
                                resultIs.setTextContent(String.valueOf((max * (max + 1) - min * (min + 1)) / 2));

                                anElt.appendChild(msg);
                                anElt.appendChild(resultIs);
                                response.getOutputStream().print(DOMUtils.domToString(anElt));
                                response.setStatus(200);
                            }
                        } else {
                            response.sendError(404);
                        }
                    }
                }
            } catch (Exception e) {
                response.sendError(500, e.getMessage());
            } finally {
                ((Request) request).setHandled(true);
            }
        }

        private Document parseBody(ServletInputStream bodyStream, HttpServletResponse response) throws IOException {
            if (bodyStream == null) {
                response.sendError(400, "Missing body!");
            } else {
                try {
                    return DOMUtils.parse(bodyStream);
                } catch (SAXException e) {
                    response.sendError(400, "Failed to parse body! " + e.getMessage());
                }
            }
            return null;
        }
    }

    private class BlogServiceHandler extends AbstractHandler {

        public void handle(String s, HttpServletRequest request, HttpServletResponse response, int i) throws IOException, ServletException {
            String method = request.getMethod();
            // actually we don't really care about this is.
            String articleId = s.substring(s.lastIndexOf("/") + 1);

            if ("GET".equalsIgnoreCase(method)) {
                doGet(request, response, articleId);
            } else if ("PUT".equalsIgnoreCase(method)) {
                doPut(request, response, articleId);
            } else if ("POST".equalsIgnoreCase(method)) {
                doPost(request, response, articleId);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                doDelete(request, response, articleId);
            }
            ((Request) request).setHandled(true);
        }

        private void doGet(HttpServletRequest request, HttpServletResponse response, String articleId) throws IOException {
            String faultType = request.getHeader("Fault-Type");
            if ("400_not_found".equals(faultType)) {
                response.setStatus(400);
            } else if ("500_operation_with_no_fault_failed".equals(faultType)) {
                response.setStatus(500);
            } else if ("200_missing_part_in_header".equals(faultType)) {
                // a part is bound to a custom header
                // this test does not set it on purpose

                response.setContentType("text/xml");
                Element articleEl = createArticleDoc(articleId);

                response.getOutputStream().print(DOMUtils.domToString(articleEl));
                response.setStatus(200);
            } else if ("200_missing_body".equals(faultType)) {
                response.setHeader("TimestampHeader", request.getHeader("TimestampHeader"));
                response.setHeader("From", request.getHeader("From"));

                response.setStatus(200);
            } else if ("200_malformed_body".equals(faultType)) {
                // parts to http headers, just send them back and let the caller check the received values
                response.setHeader("TimestampHeader", request.getHeader("TimestampHeader"));
                response.setHeader("From", request.getHeader("From"));

                response.setContentType("text/xml");
                response.getOutputStream().print("<book><abstract>Lorem ipsum dolor sit amet, consectetuer adipiscing elit.</abstract>");
                response.setStatus(200);
            } else if ("202_empty_body".equals(faultType) || "204_empty_body".equals(faultType)) {
                response.setHeader("TimestampHeader", request.getHeader("TimestampHeader"));
                response.setHeader("From", request.getHeader("From"));

                response.setStatus(Integer.parseInt(faultType.substring(0, 3)));
            } else {
                // some parts are bound to http headers
                //  just send them back and let the caller check the received values
                response.setHeader("TimestampHeader", request.getHeader("TimestampHeader"));
                response.setHeader("From", request.getHeader("From"));

                response.setContentType("text/xml");
                Element articleEl = createArticleDoc(articleId);

                response.getOutputStream().print(DOMUtils.domToString(articleEl));
                response.setStatus(200);
            }
        }

        private Element createArticleDoc(String articleId) {
            Document doc = DOMUtils.newDocument();
            Element articleEl = doc.createElementNS("http://ode/bpel/test/blog", "article");
            Element idEl = doc.createElementNS("http://ode/bpel/test/blog", "id");
            Element titleEl = doc.createElementNS("http://ode/bpel/test/blog", "title");

            articleEl.appendChild(idEl);
            articleEl.appendChild(titleEl);

            idEl.setTextContent(articleId);
            titleEl.setTextContent("A title with a random number " + System.currentTimeMillis());
            return articleEl;
        }

        private void doPost(HttpServletRequest request, HttpServletResponse response, String articleId) {
            response.setHeader("Location", "http://examples.org/a_new_comment_on_article_" + articleId);
            response.setStatus(201); // Created
        }


        private void doPut(HttpServletRequest request, HttpServletResponse response, String articleId) throws IOException {
            String faultType = request.getHeader("Fault-Type");
            if ("500_no_body".equals(faultType)) {
                response.setStatus(500);
            } else if ("500_text_body".equals(faultType)) {
                response.setContentType("text");
                response.getOutputStream().print("Lorem ipsum dolor sit amet, consectetuer adipiscing elit.");
                response.setStatus(500);
            } else if ("500_malformed_xml_body".equals(faultType)) {
                response.setContentType("text/xml");
                response.getOutputStream().print("<book><abstract>Lorem ipsum dolor sit amet, consectetuer adipiscing elit.</abstract>");
                response.setStatus(500);
            } else if ("500_unknown_xml_body".equals(faultType)) {
                response.setContentType("text/xml");
                response.getOutputStream().print("<book><abstract>Lorem ipsum dolor sit amet, consectetuer adipiscing elit.</abstract></book>");
                response.setStatus(500);
            } else if ("500_expected_xml_body".equals(faultType)) {
                response.setContentType("text/xml");

                Document doc = DOMUtils.newDocument();
                Element faultEl = doc.createElementNS("http://ode/bpel/test/blog", "fault");
                Element timestamptEl = doc.createElementNS("http://ode/bpel/test/blog", "timestamp");
                Element detailsEl = doc.createElementNS("http://ode/bpel/test/blog", "details");

                faultEl.appendChild(timestamptEl);
                faultEl.appendChild(detailsEl);

                timestamptEl.setTextContent("" + System.currentTimeMillis());
                detailsEl.setTextContent("Fake fault with the expected xml body.");

                response.getOutputStream().print(DOMUtils.domToString(faultEl));
                response.setStatus(500);
            } else {
                response.setStatus(200);
            }
        }

        private void doDelete(HttpServletRequest request, HttpServletResponse response, String articleId) {
            response.setHeader("TimestampHeader", request.getHeader("TimestampHeader"));
            response.setHeader("User-Agent", request.getHeader("User-Agent"));
            response.setStatus(204); // No content
        }
    }

    public static void main(String[] args) {
        try {
            new JettyWrapper().server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
