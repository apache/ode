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

package org.apache.ode.axis2.service;

import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.utils.fs.FileUtils;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.commons.lang.StringUtils;

import javax.xml.namespace.QName;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * handles a set of URLs all starting with /deployment to publish all files in
 * deployed bundles, services and processes. 
 */
public class DeploymentBrowser {

    private ProcessStoreImpl _store;
    private AxisConfiguration _config;
    private File _appRoot;


    public DeploymentBrowser(ProcessStoreImpl store, AxisConfiguration config, File appRoot) {
        _store = store;
        _config = config;
        _appRoot = appRoot;
    }

    // A fake filter, directly called from the ODEAxisServlet
    public boolean doFilter(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String requestURI = request.getRequestURI();
        final int deplUri = requestURI.indexOf("/deployment");
        if (deplUri > 0) {
            final String root = request.getScheme() + "://" + request.getServerName() + 
                    ":" + request.getServerPort() + requestURI.substring(0, deplUri);
            int offset = requestURI.length() > (deplUri + 11) ? 1 : 0;
            final String[] segments = requestURI.substring(deplUri + 11 + offset).split("/");
            if (segments.length == 0 || segments[0].length() == 0) {
                renderHtml(response, "ODE Deployment Browser", new DocBody() {
                    public void render(Writer out) throws IOException {
                        out.write("<p><a href=\"bundles/\">Deployed Bundles</a></p>");
                        out.write("<p><a href=\"services/\">Process Services</a></p>");
                        out.write("<p><a href=\"processes/\">Process Definitions</a></p>");
                    }
                });
            } else if (segments.length > 0) {
                if ("services".equals(segments[0])) {
                    if (segments.length == 1) {
                        renderHtml(response, "Services Implemented by Your Processes", new DocBody() {
                            public void render(Writer out) throws IOException {
                                for (Object serviceName : _config.getServices().keySet())
                                    if (!"Version".equals(serviceName)) {
                                        AxisService service = _config.getService(serviceName.toString());

                                        // The service can be one of the dynamically registered ODE services, a process
                                        // service or an unknown service deployed in the same Axis2 instance.
                                        String url = null;
                                        if ("DeploymentService".equals(service.getName())
                                                || "InstanceManagement".equals(service.getName())
                                                || "ProcessManagement".equals(service.getName()))
                                            url = service.getName();
                                        else if (service.getFileName() != null) {
                                            String relative = bundleUrlFor(service.getFileName().getFile());
                                            if (relative != null) url = root + relative;
                                            else url = root + "/services/" + service.getName() + "?wsdl";
                                        }

                                        out.write("<p><a href=\"" + url + "\">" + serviceName + "</a></p>");

                                        out.write("<ul><li>Endpoint: " + (root + "/processes/" + serviceName) + "</li>");
                                        Iterator iter = service.getOperations();
                                        ArrayList<String> ops = new ArrayList<String>();
                                        while (iter.hasNext()) ops.add(((AxisOperation)iter.next()).getName().getLocalPart());
                                        out.write("<li>Operations: " + StringUtils.join(ops, ", ") + "</li></ul>");
                                    }
                            }
                        });
                    } else {
                        final String serviceName = requestURI.substring(deplUri + 12 + 9);
                        final AxisService axisService = _config.getService(serviceName);
                        if (axisService != null) {
                            renderXml(response, new DocBody() {
                                public void render(Writer out) throws IOException {
                                    if ("InstanceManagement".equals(serviceName) || "ProcessManagement".equals(serviceName))
                                        write(out, new File(_appRoot, "pmapi.wsdl").getPath());
                                    else if (requestURI.indexOf("pmapi.xsd") > 0)
                                        write(out, new File(_appRoot, "pmapi.xsd").getPath());
                                    else if ("DeploymentService".equals(serviceName))
                                        write(out, new File(_appRoot, "deploy.wsdl").getPath());
                                    else
                                        write(out, axisService.getFileName().getFile());
                                }
                            });
                        } else {
                            renderHtml(response, "Service Not Found", new DocBody() {
                                public void render(Writer out) throws IOException {
                                    out.write("<p>Couldn't find service " + serviceName + "</p>");
                                }
                            });
                        }
                    }
                } else if ("processes".equals(segments[0])) {
                    if (segments.length == 1) {
                        renderHtml(response, "Deployed Processes", new DocBody() {
                            public void render(Writer out) throws IOException {
                                for (QName process :_store.getProcesses()) {
                                    String url = root + bundleUrlFor(_store.getProcessConfiguration(process).getBpelDocument());
                                    String[] nameVer = process.getLocalPart().split("-");
                                    out.write("<p><a href=\"" + url + "\">" + nameVer[0] + "</a> (v" + nameVer[1] + ")");
                                    out.write(" - " + process.getNamespaceURI() + "</p>");
                                }
                            }
                        });
                    }
                } else if ("bundles".equals(segments[0])) {
                    if (segments.length == 1) {
                        renderHtml(response, "Deployment Bundles", new DocBody() {
                            public void render(Writer out) throws IOException {
                                for (String bundle : _store.getPackages())
                                    out.write("<p><a href=\"" + bundle + "\">" + bundle + "</a></p>");
                            }
                        });
                    } else if (segments.length == 2) {
                        renderHtml(response, "Files in Bundle " + segments[1], new DocBody() {
                            public void render(Writer out) throws IOException {
                                List<QName> processes = _store.listProcesses(segments[1]);
                                if (processes != null) {
                                    List<File> files = _store.getProcessConfiguration(processes.get(0)).getFiles();
                                    for (File file : files) {
                                        String relativePath = file.getPath().substring(file.getPath()
                                            .indexOf("processes")+10).replaceAll("\\\\", "/");
                                        out.write("<p><a href=\"" + relativePath + "\">" + relativePath + "</a></p>");
                                    }
                                } else {
                                    out.write("<p>Couldn't find bundle " + segments[2] + "</p>");
                                }
                            }
                        });
                    } else if (segments.length > 2) {
                        List<QName> processes = _store.listProcesses(segments[1]);
                        if (processes != null) {
                            List<File> files = _store.getProcessConfiguration(processes.get(0)).getFiles();
                            for (final File file : files) {
                                String relativePath = requestURI.substring(deplUri + 12 + 9 + segments[1].length());
                                // replace slashes with the correct file separator so the match below is not always false
                                relativePath = relativePath.replace('/', File.separatorChar);
                                if (file.getPath().endsWith(relativePath)) {
                                    renderXml(response, new DocBody() {
                                        public void render(Writer out) throws IOException {
                                            write(out, file.getPath());
                                        }
                                    });
                                    return true;
                                }
                            }
                        } else {
                            renderHtml(response, "No Bundle Found", new DocBody() {
                                public void render(Writer out) throws IOException {
                                    out.write("<p>Couldn't find bundle " + segments[2] + "</p>");
                                }
                            });
                        }
                    }
                } else if ("getBundleDocs".equals(segments[0])) {
                    if (segments.length == 1) {
                        renderXml(response, new DocBody() {
                            public void render(Writer out) throws IOException {
                                out.write("<getBundleDocsResponse>");
                                out.write("<error>Not enough args..</error>");
                                out.write("</getBundleDocsResponse>");
                            }
                        });
                    } else if (segments.length == 2) {
                        final String bundleName = segments[1];
                        final List<QName> processes = _store.listProcesses(bundleName);
                        if (processes != null) {
                            renderXml(response, new DocBody() {
                                public void render(Writer out) throws IOException {
                                    out.write("<getBundleDocsResponse><name>"+ bundleName +"</name>");
                                    //final List<File> files = _store.getProcessConfiguration(processes.get(0)).getFiles();
                                    //final String pid = _store.getProcessConfiguration(processes.get(0)).getProcessId().toString();
                            
                                    for (final QName process: processes) {
                                        List<File> files = _store.getProcessConfiguration(process).getFiles();
                                        String pid = _store.getProcessConfiguration(process).getProcessId().toString();
                                        out.write("<process><pid>"+pid+"</pid>");
                                        for (final File file : files) {
                                            if (file.getPath().endsWith(".wsdl")) {
                                                String relativePath = file.getPath().substring(_store.getDeployDir().getCanonicalPath().length() + 1);
                                                out.write("<wsdl>"+ relativePath + "</wsdl>");                                      
                                            }
                                            if (file.getPath().endsWith(".bpel")) { 
                                                String relativePath = file.getPath().substring(_store.getDeployDir().getCanonicalPath().length() + 1);
                                                out.write("<bpel>"+ relativePath + "</bpel>");
                                            }
                                            
                                        }
                                        out.write("</process>");
                                    }                                   
                                    out.write("</getBundleDocsResponse>");
                                }
                            });
                            
                        }
                    }
                } else if ("getProcessDefinition".equals(segments[0])) {
                    if (segments.length == 1) {
                        renderXml(response, new DocBody() {
                            public void render(Writer out) throws IOException{
                                out.write("<getProcessDefinitionResponse>");
                                out.write("<error>Not enough args..</error>");
                                out.write("</getProcessDefinitionResponse>");
                            }
                        });
                    } else if (segments.length == 2) {
                        String processName = segments[1]; 
                        for (QName process :_store.getProcesses()) {
                            String[] nameVer = process.getLocalPart().split("-");
                            if(processName.equals(nameVer[0])) {
                                final String url = root + bundleUrlFor(_store.getProcessConfiguration(process).getBpelDocument());
                                renderXml(response, new DocBody() {
                                    public void render(Writer out) throws IOException {
                                        out.write("<getProcessDefinition>");
                                        out.write("<url>"+ url +"</url>");
                                        out.write("</getProcessDefinition>");
                                    }
                                });
                            }
                        }
                        
                    }
                }
            }
            return true;
        }
        return false;
    }

    static interface DocBody {
        void render(Writer out) throws IOException;
    }
    private void renderHtml(HttpServletResponse response, String title, DocBody docBody) throws IOException {
        response.setContentType("text/html");

        Writer out = response.getWriter();
        out.write("<html><header><style type=\"text/css\">" + CSS + "</style></header><body>\n");
        out.write("<h2>" + title + "</h2><p/>\n");
        docBody.render(out);
        out.write("</body></html>");
    }
    private void renderXml(HttpServletResponse response, DocBody docBody) throws IOException {
        response.setContentType("application/xml; charset=utf-8");  
        //response.setContentType("application/xml");
        //response.setCharacterEncoding("UTF-8");

        Writer out = response.getWriter();
        docBody.render(out);
    }

    private void write(Writer out, String filePath) throws IOException {
        BufferedReader wsdlReader = new BufferedReader(new FileReader(filePath));
        String line;
        while((line = wsdlReader.readLine()) != null) out.write(line + "\n");
        wsdlReader.close();
    }

    private String bundleUrlFor(String docFile) {
        if (docFile.indexOf("processes") >= 0) docFile = docFile.substring(docFile.indexOf("processes")+10);
        List<File> files = FileUtils.directoryEntriesInPath(_store.getDeployDir(), null);
        for (final File bundleFile : files) {
            if (bundleFile.getPath().replaceAll("\\\\", "/").endsWith(docFile))
                return "/deployment/bundles/" + bundleFile.getPath()
                  .substring(_store.getDeployDir().getPath().length() + 1).replaceAll("\\\\", "/");
        }
        return null;
    }

    private static final String CSS =
            "body {\n" +
                    "    font: 75% Verdana, Helvetica, Arial, sans-serif;\n" +
                    "    background: White;\n" +
                    "    color: Black;\n" +
                    "    margin: 1em;\n" +
                    "    padding: 1em;\n" +
                    "}\n" +
                    "\n" +
                    "h1, h2, h3, h4, h5, h6 {\n" +
                    "    color: Black;\n" +
                    "    clear: left;\n" +
                    "    font: 100% Verdana, Helvetica, Arial, sans-serif;\n" +
                    "    margin: 0;\n" +
                    "    padding-left: 0.5em;\n" +
                    "} \n" +
                    "\n" +
                    "h1 {\n" +
                    "    font-size: 150%;\n" +
                    "    border-bottom: none;\n" +
                    "    text-align: right;\n" +
                    "    border-bottom: 1px solid Gray;\n" +
                    "}\n" +
                    "    \n" +
                    "h2 {\n" +
                    "    font-size: 130%;\n" +
                    "    border-bottom: 1px solid Gray;\n" +
                    "}\n" +
                    "\n" +
                    "h3 {\n" +
                    "    font-size: 120%;\n" +
                    "    padding-left: 1.0em;\n" +
                    "    border-bottom: 1px solid Gray;\n" +
                    "}\n" +
                    "\n" +
                    "h4 {\n" +
                    "    font-size: 110%;\n" +
                    "    padding-left: 1.5em;\n" +
                    "    border-bottom: 1px solid Gray;\n" +
                    "}\n" +
                    "\n" +
                    "p {\n" +
                    "    text-align: justify;\n" +
                    "    line-height: 1.5em;\n" +
                    "    padding-left: 1.5em;\n" +
                    "}\n" +
                    "\n" +
                    "a {\n" +
                    "    text-decoration: underline;\n" +
                    "    color: Black;\n" +
                    "}";
}
