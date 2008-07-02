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

package org.apache.ode.axis2.httpbinding;

import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.StreamUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class ArithmeticsJettyWrapper {

    protected Server server;
    private ContextHandlerCollection handlerColl;

    public ArithmeticsJettyWrapper() throws Exception {
        this(7070);
    }

    public ArithmeticsJettyWrapper(int port) throws Exception {
        server = new Server(port);
        // Adding the buildr handler to control our server lifecycle
        ContextHandler context = new ContextHandler();
        context.setContextPath("/HttpBindingTestService");
        Handler handler = new ArithmeticsServiceHandler();
        context.setHandler(handler);

        handlerColl = new ContextHandlerCollection();
        handlerColl.setHandlers(new Handler[]{context});

        server.addHandler(handlerColl);
    }

    private class ArithmeticsServiceHandler extends AbstractHandler {
        /*
        8 urls to handle:
        (GET)       http://localhost:8080/HttpBindingTestService/OlaElMundo-GET/plus/(left):(right)
        (GET)       http://         ........                    /OlaElMundo-GET/minus?left=&right=
        (DELETE)    http://localhost:8080/HttpBindingTestService/OlaElMundo-DELETE/plus/(left):(right)
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
                } else if (uri.contains("OlaElMundo-POST") || uri.contains("OlaElMundo-PUT")) {
                    if (!uri.contains("plus") && !uri.contains("minus")) {
                        response.sendError(404);
                    } else {
                        // parse body, form-urlencoded
                        int res = Integer.MIN_VALUE;
                        boolean ok = true;
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
                                    Element res = bodyDoc.createElementNS("http://ode/bpel/arithmetics", "theresult");
                                    res.setTextContent(String.valueOf(Integer.valueOf(left) + Integer.valueOf(right)));
                                    response.getOutputStream().print(DOMUtils.domToString(res));
                                    response.setStatus(200);
                                }
                            }

                        } else if (uri.contains("sumOfIntegers")) {
                            Document bodyDoc = parseBody(request.getInputStream(), response);
                            if (bodyDoc != null) {
                                Element firstOperand = DOMUtils.getFirstChildElement(bodyDoc.getDocumentElement());
                                Element secondElement = DOMUtils.getNextSiblingElement(firstOperand);
                                int left = Integer.valueOf(DOMUtils.getTextContent(firstOperand));
                                int right = Integer.valueOf(DOMUtils.getTextContent(secondElement));

                                int min = Math.min(left,right);
                                int max = Math.max(left,right);
//                                Element arrayElt = bodyDoc.createElement("sumOfInteger");
                                Element anElt = bodyDoc.createElementNS("http://ode/bpel/arithmetics", "sumOfInteger");
                                Element msg = bodyDoc.createElement("msg");
                                Element resultIs = bodyDoc.createElement("resultIs");
                                msg.setTextContent("A dummy message we don't care about. Only purpose is to have a complex type");
                                resultIs.setTextContent(String.valueOf((max*(max+1)-min*(min+1))/2));

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

    public static void main(String[] args) {
        try {
            new ArithmeticsJettyWrapper();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
