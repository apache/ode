package org.apache.ode.axis2.service;

import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.description.AxisService;

import javax.xml.namespace.QName;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.*;
import java.util.List;
import java.util.Arrays;

/**
 * handles a set of URLs all starting with /deployment to publish all files in
 * deployed bundles, services and processes. 
 */
public class DeploymentBrowser {

    private ProcessStoreImpl _store;
    private AxisConfiguration _config;

    public DeploymentBrowser(ProcessStoreImpl _store, AxisConfiguration _config) {
        this._store = _store;
        this._config = _config;
    }

    // A fake filter, directly called from the ODEAxisServlet
    public boolean doFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        int deplUri = requestURI.indexOf("/deployment");
        if (deplUri > 0) {
            int offset = requestURI.length() > (deplUri + 11) ? 1 : 0;
            final String[] segments = requestURI.substring(deplUri + 11 + offset).split("/");
            if (segments.length == 0 || segments[0].length() == 0) {
                renderHtml(response, "ODE Deployment Browser", new DocBody() {
                    public void render(Writer out) throws IOException {
                        out.write("<p><a href=\"services/\">Process Services</a></p>");
                        out.write("<p><a href=\"processes/\">Deployed Processes</a></p>");
                        out.write("<p><a href=\"bundles/\">Deployed Bundles</a></p>");
                    }
                });
            } else if (segments.length > 0) {
                if ("services".equals(segments[0])) {
                    if (segments.length == 1) {
                        renderHtml(response, "Services Implemented by Your Processes", new DocBody() {
                            public void render(Writer out) throws IOException {
                                for (Object serviceName : _config.getServices().keySet())
                                    if (!"Version".equals(serviceName))
                                        out.write("<p><a href=\"" + serviceName + "\">" + serviceName + "</a></p>");
                            }
                        });
                    } else {
                        final String serviceName = requestURI.substring(deplUri + 12 + 9);
                        final AxisService axisService = _config.getService(serviceName);
                        if (axisService != null && axisService.getFileName() != null) {
                            renderXml(response, new DocBody() {
                                public void render(Writer out) throws IOException {
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
                                for (QName process :_store.getProcesses())
                                    out.write("<p><a href=\"" + process.getLocalPart() + "?ns=" + process.getNamespaceURI() + "\">" + process + "</a></p>");
                            }
                        });
                    } else {
                        final String processName = requestURI.substring(deplUri + 12 + 10);
                        final String processNs = request.getParameter("ns");
                        final ProcessConf pconf = _store.getProcessConfiguration(new QName(processNs, processName));
                        if (pconf != null) {
                            renderXml(response, new DocBody() {
                                public void render(Writer out) throws IOException {
                                    write(out, new File(pconf.getBaseURI().toURL().getFile(), pconf.getBpelDocument()).getPath());
                                }
                            });
                        } else {
                            renderHtml(response, "Process Not Found", new DocBody() {
                                public void render(Writer out) throws IOException {
                                    out.write("<p>Couldn't find process " + new QName(processNs, processName) + "</p>");
                                }
                            });
                        }
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
                                        String relativePath = file.getPath().substring(_store.getDeployDir().getPath().length() + 1);
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
                                if (file.getPath().indexOf(relativePath) >= 0) {
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
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");

        Writer out = response.getWriter();
        docBody.render(out);
    }

    private void write(Writer out, String filePath) throws IOException {
        BufferedReader wsdlReader = new BufferedReader(new FileReader(filePath));
        String line;
        while((line = wsdlReader.readLine()) != null) out.write(line + "\n");
        wsdlReader.close();
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
