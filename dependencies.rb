#    Licensed to the Apache Software Foundation (ASF) under one or more
#    contributor license agreements.  See the NOTICE file distributed with
#    this work for additional information regarding copyright ownership.
#    The ASF licenses this file to You under the Apache License, Version 2.0
#    (the "License"); you may not use this file except in compliance with
#    the License.  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.

ANNONGEN            = "annogen:annogen:jar:0.1.0"
ANT                 = "ant:ant:jar:1.6.5"
BACKPORT            = "backport-util-concurrent:backport-util-concurrent:jar:3.0"
AXIOM               = [ group("axiom-api", "axiom-impl", "axiom-dom",
                        :under=>"org.apache.ws.commons.axiom", :version=>"1.2.8") ]
AXIS2_ALL           = [group("axis2-kernel", "axis2-adb", "axis2-codegen", "axis2-java2wsdl",
                             "axis2-jibx", "axis2-saaj", "axis2-xmlbeans",
                            :under=>"org.apache.axis2",
                            :version=>"1.5"),
                      group("woden-api", "woden-impl-dom",
                              :under=>"org.apache.woden",
                              :version=>"1.0M8"),
                      "org.apache.axis2:axis2-transports:jar:1.0-i6",
                      "org.apache.axis2:axis2-transport-http:jar:1.5",
                      "org.apache.axis2:axis2-transport-local:jar:1.5",
                      "org.apache.axis2:axis2-transport-tcp:jar:1.0.0",
                      "org.apache.axis2:axis2-transport-jms:jar:1.0.0",
                      "org.apache.httpcomponents:httpcore:jar:4.0"]
AXIS2_MODULES        = struct(
 :mods              => ["org.apache.rampart:rampart:mar:1.4",
                         "org.apache.rampart:rahas:mar:1.4",
                         "org.apache.axis2:addressing:mar:1.4",
                         "org.apache.axis2:mex:mar:1.41"],
 :libs              => [group("rampart-core", "rampart-policy", "rampart-trust",
                              :under=>"org.apache.rampart",
                              :version=>"1.4"),
                        "org.apache.ws.security:wss4j:jar:1.5.4",
                        "org.apache.santuario:xmlsec:jar:1.4.1",
                        "org.apache.axis2:mex:jar:impl:1.41", #1.41 is not a typo!
                        "opensaml:opensaml:jar:1.1",
                        "bouncycastle:bcprov-jdk15:jar:132",
                        BACKPORT]
)
AXIS2_WAR           = "org.apache.axis2:axis2-webapp:war:1.5"
COMMONS             = struct(
  :codec            =>"commons-codec:commons-codec:jar:1.3",
  :collections      =>"commons-collections:commons-collections:jar:3.2.1",
  :dbcp             =>"commons-dbcp:commons-dbcp:jar:1.2.2",
  :fileupload       =>"commons-fileupload:commons-fileupload:jar:1.1.1",
  :httpclient       =>"commons-httpclient:commons-httpclient:jar:3.1",
  :lang             =>"commons-lang:commons-lang:jar:2.4",
  :logging          =>"commons-logging:commons-logging:jar:1.1.1",
  :io               =>"commons-io:commons-io:jar:1.4",
  :pool             =>"commons-pool:commons-pool:jar:1.4",
  :primitives       =>"commons-primitives:commons-primitives:jar:1.0",
  :beanutils        =>"commons-beanutils:commons-beanutils:jar:1.8.2"
)
DERBY               = "org.apache.derby:derby:jar:10.5.3.0_1"
DERBY_TOOLS         = "org.apache.derby:derbytools:jar:10.5.3.0_1"
DOM4J               = "dom4j:dom4j:jar:1.6.1"
GERONIMO            = struct(
  :kernel           =>"org.apache.geronimo.modules:geronimo-kernel:jar:2.0.1",
  :transaction      =>"org.apache.geronimo.components:geronimo-transaction:jar:2.0.1",
  :connector        =>"org.apache.geronimo.components:geronimo-connector:jar:2.0.1"
)
HIBERNATE           = [ "org.hibernate:hibernate-core:jar:3.3.2.GA", "javassist:javassist:jar:3.4.GA", "antlr:antlr:jar:2.7.6"
    #"asm:asm:jar:1.5.3",
    #                    , "cglib:cglib:jar:2.1_3", "net.sf.ehcache:ehcache:jar:1.2.3" 
                        ]
HSQLDB              = "hsqldb:hsqldb:jar:1.8.0.7"
JAVAX               = struct(
  :activation       =>"javax.activation:activation:jar:1.1",
  #:activation       =>"geronimo-spec:geronimo-spec-activation:jar:1.0.2-rc4",
  :connector        =>"org.apache.geronimo.specs:geronimo-j2ee-connector_1.5_spec:jar:1.0",
  :ejb              =>"org.apache.geronimo.specs:geronimo-ejb_2.1_spec:jar:1.1",
  :javamail         =>"geronimo-spec:geronimo-spec-javamail:jar:1.3.1-rc5",
  :jms              =>"geronimo-spec:geronimo-spec-jms:jar:1.1-rc4",
  :persistence      =>"javax.persistence:persistence-api:jar:1.0",
  :servlet          =>"org.apache.geronimo.specs:geronimo-servlet_2.4_spec:jar:1.0",
  :stream           =>"stax:stax-api:jar:1.0.1",
  :transaction      =>"org.apache.geronimo.specs:geronimo-jta_1.1_spec:jar:1.1",
  :resource         =>"org.apache.geronimo.specs:geronimo-j2ee-connector_1.5_spec:jar:1.0"
)
JAXEN               = "jaxen:jaxen:jar:1.1.1"
JBI                 = group("org.apache.servicemix.specs.jbi-api-1.0", :under=>"org.apache.servicemix.specs", :version=>"1.1.0")
JENCKS              = "org.jencks:jencks:jar:all:1.3"
JIBX                = "jibx:jibx-run:jar:1.1-beta3"
KARAF               = [
                        "org.apache.felix:org.osgi.core:jar:1.4.0",
                        "org.apache.felix:org.osgi.compendium:jar:1.4.0",
                        "org.apache.karaf.shell:org.apache.karaf.shell.console:jar:2.1.3",
                        group("org.apache.felix.gogo.commands","org.apache.felix.gogo.runtime",
                          :under=>"org.apache.felix.gogo", :version=>"0.4.0")
                      ]
LOG4J               = "log4j:log4j:jar:1.2.13"
OPENJPA             = ["org.apache.openjpa:openjpa:jar:1.2.1",
                       "net.sourceforge.serp:serp:jar:1.13.1"]

SAXON               = group("saxon", "saxon-xpath", "saxon-dom", "saxon-xqj", :under=>"net.sf.saxon", :version=>"9.1.0.8")
SERVICEMIX          = [
                        group("servicemix-core",
                            :under=>"org.apache.servicemix", :version=>"3.3"),
                        group("servicemix-soap", "servicemix-common", "servicemix-shared", "servicemix-http", "servicemix-eip",
                            :under=>"org.apache.servicemix", :version=>"2008.01"),
                        group("servicemix-utils",
                            :under=>"org.apache.servicemix", :version=>"1.0.0"),
                        "commons-httpclient:commons-httpclient:jar:3.0",
                        "commons-codec:commons-codec:jar:1.2",
                        "org.mortbay.jetty:jetty:jar:6.1.12rc1",
                        "org.mortbay.jetty:jetty-client:jar:6.1.12rc1",
                        "org.mortbay.jetty:jetty-sslengine:jar:6.1.12rc1",
                        "org.mortbay.jetty:servlet-api-2.5:jar:6.1.12rc1",
                        "org.mortbay.jetty:jetty-util:jar:6.1.12rc1",
                        "org.codehaus.woodstox:wstx-asl:jar:3.2.2",
                        "org.apache.geronimo.specs:geronimo-activation_1.1_spec:jar:1.0.1",
                        "org.apache.geronimo.specs:geronimo-annotation_1.0_spec:jar:1.1",
                        "org.apache.geronimo.specs:geronimo-javamail_1.4_spec:jar:1.2",
                        "org.apache.geronimo.specs:geronimo-stax-api_1.0_spec:jar:1.0.1",
                        "org.apache.geronimo.specs:geronimo-jms_1.1_spec:jar:1.1",
                        "org.jencks:jencks:jar:2.1",
                        "org.objectweb.howl:howl:jar:1.0.1-1",
                        "org.apache.activemq:activemq-core:jar:4.1.1",
                        "org.apache.activemq:activemq-ra:jar:4.1.1",
                        "commons-beanutils:commons-beanutils:jar:1.7.0",
                        "tranql:tranql-connector-derby-common:jar:1.1"
                        ]
SLF4J = group(%w{ slf4j-api slf4j-log4j12 jcl104-over-slf4j }, :under=>"org.slf4j", :version=>"1.4.3")
SPRING              = ["org.springframework:spring:jar:2.5.6"]
SPRING_OSGI         = ["org.springframework.osgi:spring-osgi-core:jar:1.2.0"]
SPRING_TEST         = ["org.springframework:spring-test:jar:2.5.6"]
TRANQL              = [ "tranql:tranql-connector:jar:1.1", COMMONS.primitives ]
WOODSTOX            = "woodstox:wstx-asl:jar:3.2.1"
WSDL4J              = "wsdl4j:wsdl4j:jar:1.6.1"
XALAN               = "xalan:xalan:jar:2.7.1"
XERCES              = "xerces:xercesImpl:jar:2.9.0"
WS_COMMONS          = struct(
  :axiom            =>AXIOM,
  :neethi           =>"org.apache.neethi:neethi:jar:2.0.4",
  :xml_schema       =>"org.apache.ws.commons.schema:XmlSchema:jar:1.3.2"
)
XBEAN               = [
  "org.apache.xbean:xbean-kernel:jar:3.3",
  "org.apache.xbean:xbean-server:jar:3.3",
  "org.apache.xbean:xbean-spring:jar:3.4.3",
  "org.apache.xbean:xbean-classloader:jar:3.4.3"
]
XMLBEANS            = "org.apache.xmlbeans:xmlbeans:jar:2.3.0"
