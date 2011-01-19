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
#

gem "buildr", "~>1.4.3"
require "buildr"
require "buildr/xmlbeans.rb"
require "buildr/openjpa"
require "buildr/javacc"
require "buildr/jetty"
require "buildr/hibernate"

Buildr.settings.build['jmock'] = "1.2.0"

Buildr::Hibernate::REQUIRES[:xdoclet] = Buildr.group("xdoclet", "xdoclet-xdoclet-module", "xdoclet-hibernate-module", 
 :under=>"xdoclet", :version=>"1.2.3") + ["xdoclet:xjavadoc:jar:1.1-j5"] 

# dirty workaround for BUILDR-541/BUILDR-508
Java.classpath << Buildr::OpenJPA::REQUIRES

require File.join(File.dirname(__FILE__), 'repositories.rb')
require File.join(File.dirname(__FILE__), 'dependencies.rb')

# Keep this structure to allow the build system to update version numbers.
VERSION_NUMBER = "1.4-SNAPSHOT"

# Apache Nexus Repositories
if VERSION_NUMBER =~ /SNAPSHOT/
    # Apache Development Snapshot Repository
    repositories.release_to[:url] = 'https://repository.apache.org/content/repositories/snapshots'
else
    # Apache Release Distribution Repository
    repositories.release_to[:url] = 'https://repository.apache.org/service/local/staging/deploy/maven2'
end

BUNDLE_VERSIONS = {
  "ode.version" => VERSION_NUMBER,
  "commons.collections.version" => artifact(COMMONS.collections).version,
  "commons.httpclient.bundle.version" => "3.1_1",
  "commons.lang.version" => artifact(COMMONS.lang).version,
  "commons.pool.version" => artifact(COMMONS.pool).version,
  "derby.version" => artifact(DERBY).version,
  "geronimo.specs.version" => "1.1.1",
  "servicemix.nmr.version" => "1.1.0-SNAPSHOT",
  "servicemix.shared.version" => "2009.02-SNAPSHOT",
  "servicemix.specs.version" => "1.4-SNAPSHOT",
}

Release.tag_name = lambda { |version| "APACHE_ODE_#{version.upcase}" } if Release

desc "Apache ODE"
define "ode" do
  project.version = VERSION_NUMBER
  project.group = "org.apache.ode"

  compile.options.source = "1.5"
  compile.options.target = "1.5"
  manifest["Implementation-Vendor"] = "Apache Software Foundation"
  meta_inf << file("NOTICE")

  desc "ODE Axis Integration Layer"
  define "axis2" do
    compile.with projects("bpel-api", "bpel-connector", "bpel-dao", "bpel-epr", "bpel-runtime",
      "scheduler-simple", "bpel-schemas", "bpel-store", "utils", "agents"),
      AXIOM, AXIS2_ALL, COMMONS.lang, COMMONS.logging, COMMONS.collections, COMMONS.httpclient, COMMONS.lang,
      DERBY, GERONIMO.kernel, GERONIMO.transaction, JAVAX.activation, JAVAX.servlet, JAVAX.stream,
      JAVAX.transaction, JENCKS, WSDL4J, WS_COMMONS, XMLBEANS, AXIS2_MODULES.libs

    test.exclude 'org.apache.ode.axis2.management.*'
    test.with project("tools"), AXIOM, JAVAX.javamail, COMMONS.codec, COMMONS.httpclient, XERCES, WOODSTOX

    package :jar
  end

  desc "ODE Axis2 Based Web Application"
  define "axis2-war" do
    libs = projects("axis2", "bpel-api", "bpel-compiler", "bpel-connector", "bpel-dao",
      "bpel-epr", "bpel-obj", "bpel-ql", "bpel-runtime", "scheduler-simple",
      "bpel-schemas", "bpel-store", "dao-hibernate", "jacob", "jca-ra", "jca-server",
      "utils", "dao-jpa", "agents"),
      AXIS2_ALL, ANNONGEN, BACKPORT, COMMONS.codec, COMMONS.collections, COMMONS.fileupload, COMMONS.io, COMMONS.httpclient, COMMONS.beanutils,
      COMMONS.lang, COMMONS.logging, COMMONS.pool, DERBY, DERBY_TOOLS, JAXEN, JAVAX.activation, JAVAX.ejb, JAVAX.javamail,
      JAVAX.connector, JAVAX.jms, JAVAX.persistence, JAVAX.transaction, JAVAX.stream,  JIBX,
      GERONIMO.connector, GERONIMO.kernel, GERONIMO.transaction, LOG4J, OPENJPA, SAXON, TRANQL,
      WOODSTOX, WSDL4J, WS_COMMONS, XALAN, XERCES, XMLBEANS, SPRING,
      AXIS2_MODULES.libs

    package(:war).with(:libs=>libs).path("WEB-INF").tap do |web_inf|
      web_inf.merge project("dao-jpa-ojpa-derby").package(:zip)
      web_inf.merge project("dao-hibernate-db").package(:zip)
      web_inf.include project("axis2").path_to("src/main/wsdl/*")
      web_inf.include project("bpel-schemas").path_to("src/main/xsd/pmapi.xsd")
    end
    package(:war).path("WEB-INF/modules").include(artifacts(AXIS2_MODULES.mods))
    package(:war).tap do |root|
      root.merge(artifact(AXIS2_WAR)).exclude("WEB-INF/*").exclude("META-INF/*")
    end

    task("start"=>[package(:war), jetty.use]) do |task|
      class << task ; attr_accessor :url, :path ; end
      task.url = "http://localhost:8080/ode"
      task.path = jetty.deploy(task.url, task.prerequisites.first)
      jetty.teardown task("stop")
    end

    task("stop") do |task|
      if url = task("start").url rescue nil
        jetty.undeploy url
      end
    end

    test.using :testng, :forkmode=>'perTest', :properties=>{ "log4j.debug" => true,  "log4j.configuration"=>"test-log4j.properties", "test.ports" => ENV['TEST_PORTS'], "org.apache.ode.scheduler.deleteJobsOnStart" => "true", "org.apache.ode.autoRetireProcess"=>"true" }
        #:java_args=>['-Xdebug', '-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=6001', '-Xmx1024M', '-XX:MaxPermSize=1024m']
    test.with projects("tools"), libs, AXIS2_MODULES.mods, AXIOM, JAVAX.servlet, Buildr::Jetty::REQUIRES, HIBERNATE, DOM4J, SLF4J, LOG4J, H2::REQUIRES, SPRING_TEST
    webapp_dir = "#{test.compile.target}/webapp"
    test.setup task(:prepare_webapp) do |task|
      cp_r _("src/main/webapp"), test.compile.target.to_s
      rm_rf Dir[_(webapp_dir) + "/**/.svn"]
      cp_r _("src/test/webapp"), test.compile.target.to_s
      rm_rf Dir[_(webapp_dir) + "/**/.svn"]
      cp_r Dir[_("src/main/webapp/WEB-INF/classes/*")], test.compile.target.to_s
      rm_rf Dir[_(webapp_dir) + "/**/.svn"]
      cp Dir[project("axis2").path_to("src/main/wsdl/*")], "#{webapp_dir}/WEB-INF"
      cp project("bpel-schemas").path_to("src/main/xsd/pmapi.xsd"), "#{webapp_dir}/WEB-INF"
      rm_rf Dir[_(webapp_dir) + "/**/.svn"]
      mkdir "#{webapp_dir}/WEB-INF/processes" unless File.exist?("#{webapp_dir}/WEB-INF/processes")
      mkdir "#{webapp_dir}/WEB-INF/modules" unless File.exist?("#{webapp_dir}/WEB-INF/modules")
      # move around some property files for test purposes
      mv Dir["#{test.compile.target}/TestEndpointProperties/*_global_conf*.endpoint"], "#{webapp_dir}/WEB-INF/conf"
      Dir["#{webapp_dir}/WEB-INF/conf.*"].each {|d| cp "#{webapp_dir}/WEB-INF/conf/global-config.endpoint",d}
      artifacts(AXIS2_MODULES.mods).each(&:invoke)
      cp AXIS2_MODULES.mods.map {|a| repositories.locate(a)} , "#{webapp_dir}/WEB-INF/modules"
    end
    test.setup unzip("#{webapp_dir}/WEB-INF"=>project("dao-jpa-ojpa-derby").package(:zip))
    test.setup unzip("#{webapp_dir}/WEB-INF"=>project("dao-hibernate-db").package(:zip))
    test.exclude('*') if Buildr.environment == 'hudson' || ENV["notestng"]

    NativeDB.prepare_configs test, _(".")

    test.setup WSSecurity.prepare_secure_services_tests("#{test.resources.target}/TestRampartBasic/secured-services", "sample*.axis2", AXIS2_MODULES.mods)
    test.setup WSSecurity.prepare_secure_services_tests("#{test.resources.target}/TestRampartPolicy/secured-services", "sample*-policy.xml", AXIS2_MODULES.mods)

    test.setup WSSecurity.prepare_secure_processes_tests("#{test.resources.target}/TestRampartBasic/secured-processes", AXIS2_MODULES.mods)
    test.setup WSSecurity.prepare_secure_processes_tests("#{test.resources.target}/TestRampartPolicy/secured-processes", AXIS2_MODULES.mods)
  end

  desc "ODE APIs"
  define "bpel-api" do
    compile.with projects("utils", "bpel-obj", "bpel-schemas"), WSDL4J, COMMONS.logging, XERCES
    package :jar
  end

  desc "ODE JCA connector"
  define "bpel-api-jca" do
    compile.with project("bpel-api"), JAVAX.connector
    package :jar
  end

  desc "ODE BPEL Compiler"
  define "bpel-compiler" do
    compile.with projects("bpel-api", "bpel-obj", "bpel-schemas", "utils"),
      COMMONS.logging, JAVAX.stream, JAXEN, SAXON, WSDL4J, XALAN, XERCES, COMMONS.collections
    test.resources { filter(project("bpel-scripts").path_to("src/main/resources")).into(test.resources.target).run }
    test.with LOG4J
    package :jar
    test.with SLF4J, LOG4J
  end

  desc "ODE JCA Connector Implementation"
  define "bpel-connector" do
    compile.with projects("bpel-api", "bpel-api-jca", "bpel-runtime", "jca-ra", "jca-server")
    package :jar
  end

  desc "ODE DAO Interfaces"
  define "bpel-dao" do
    compile.with project("bpel-api")
    package :jar
  end

  desc "ODE Interface Layers Common"
  define "bpel-epr" do
    compile.with projects("utils", "bpel-dao", "bpel-api"),
      AXIOM, COMMONS.lang, COMMONS.logging, COMMONS.beanutils, DERBY, JAVAX.connector, JAVAX.stream, JAVAX.transaction, GERONIMO.transaction, GERONIMO.connector, TRANQL, XMLBEANS
    package :jar
  end

  desc "ODE BPEL Object Model"
  define "bpel-obj" do
    compile.with project("utils"), SAXON, WSDL4J, COMMONS.collections
    package :jar
  end

  desc "ODE BPEL Query Language"
  define "bpel-ql" do
    pkg_name = "org.apache.ode.ql.jcc"
    jjtree = jjtree(_("src/main/jjtree"), :in_package=>pkg_name)
    compile.from javacc(jjtree, :in_package=>pkg_name), jjtree
    compile.with projects("bpel-api", "bpel-compiler", "bpel-obj", "jacob", "utils")

    package :jar
  end

  desc "ODE Runtime Engine"
  define "bpel-runtime" do
    compile.with projects("bpel-api", "bpel-compiler", "bpel-dao", "bpel-epr", "bpel-obj", "bpel-schemas",
      "bpel-store", "jacob", "utils", "agents"),
      COMMONS.logging, COMMONS.collections, COMMONS.httpclient, JAXEN, JAVAX.persistence, JAVAX.stream, SAXON, WSDL4J, XMLBEANS,
      SPRING


    test.with projects("scheduler-simple", "dao-jpa", "dao-hibernate", "bpel-epr"),
        BACKPORT, COMMONS.pool, COMMONS.lang, COMMONS.io, DERBY, JAVAX.connector, JAVAX.transaction,
        GERONIMO.transaction, GERONIMO.kernel, GERONIMO.connector, TRANQL, HSQLDB, JAVAX.ejb,
        OPENJPA, XERCES, XALAN, LOG4J, SLF4J,
        DOM4J, HIBERNATE, SPRING_TEST,
        "tranql:tranql-connector-derby-common:jar:1.1"

    package :jar
  end

  desc "ODE Simple Scheduler"
  define "scheduler-simple" do
    compile.with projects("bpel-api", "utils"), COMMONS.collections, COMMONS.logging, JAVAX.transaction, LOG4J
    test.compile.with HSQLDB, GERONIMO.kernel, GERONIMO.transaction
    test.with H2::REQUIRES, HSQLDB, JAVAX.transaction, JAVAX.resource, JAVAX.connector, LOG4J,
          GERONIMO.kernel, GERONIMO.transaction, GERONIMO.connector, TRANQL, BACKPORT, JAVAX.ejb
    test.exclude('*') if Buildr.environment == 'hudson'
    package :jar
  end

  desc "ODE Schemas"
  define "bpel-schemas" do
    compile_xml_beans _("src/main/xsd/*.xsdconfig"), _("src/main/xsd")
    package :jar
  end

  desc "ODE BPEL Test Script Files"
  define "bpel-scripts" do
    package :jar
  end

  desc "ODE Process Store"
  define "bpel-store" do
    compile.with projects("bpel-api", "bpel-compiler", "bpel-dao", "bpel-obj", "bpel-schemas", "bpel-epr",
      "dao-hibernate", "dao-jpa", "utils"),
      COMMONS.logging, JAVAX.persistence, JAVAX.stream, JAVAX.transaction, HIBERNATE, HSQLDB, XMLBEANS, XERCES, WSDL4J, OPENJPA, SPRING
    compile { open_jpa_enhance }
    resources hibernate_doclet(:package=>"org.apache.ode.store.hib", :excludedtags=>"@version,@author,@todo")

    test.with COMMONS.collections, COMMONS.lang, JAVAX.connector, JAVAX.transaction, DOM4J, LOG4J,
      XERCES, XALAN, JAXEN, SAXON, OPENJPA, GERONIMO.transaction, SLF4J, SPRING_TEST, DERBY,
      GERONIMO.transaction, GERONIMO.kernel, GERONIMO.connector, JAVAX.connector, JAVAX.ejb
    package :jar
  end

  desc "ODE BPEL Tests"
  define "bpel-test" do
    compile.with projects("bpel-api", "bpel-compiler", "bpel-dao", "bpel-runtime",
      "bpel-store", "utils", "bpel-epr", "dao-hibernate", "agents", "scheduler-simple"),
      DERBY, JUnit.dependencies, JAVAX.persistence, OPENJPA, WSDL4J, COMMONS.httpclient, COMMONS.io, COMMONS.logging,
      GERONIMO.transaction, GERONIMO.kernel, GERONIMO.connector, JAVAX.connector, JAVAX.ejb, JAVAX.transaction, TRANQL, "tranql:tranql-connector-derby-common:jar:1.1",
      SPRING_TEST,
    COMMONS.codec

    test.using :properties=>{ "org.apache.ode.autoRetireProcess"=>"true" }
    test.with projects("bpel-obj", "jacob", "bpel-schemas",
      "bpel-scripts"),
      COMMONS.collections, COMMONS.lang, DERBY, JAVAX.connector,
      JAVAX.stream, JAVAX.transaction, JAXEN, HSQLDB, LOG4J, SAXON, XERCES, XMLBEANS, XALAN, GERONIMO.transaction, SPRING, HIBERNATE, SLF4J, DOM4J,
      JAVAX.connector

    package :jar
  end

  desc "ODE Hibernate DAO Implementation"
  define "dao-hibernate" do
    compile.with projects("bpel-api", "bpel-dao", "bpel-ql", "utils"),
      COMMONS.lang, COMMONS.logging, JAVAX.transaction, HIBERNATE, DOM4J
    resources hibernate_doclet(:package=>"org.apache.ode.daohib.bpel.hobj", :excludedtags=>"@version,@author,@todo")

    # doclet does not support not-found="ignore"
    task "hbm-hack" do |task|
      process_instance_hbm_file = project.path_to("target/classes/org/apache/ode/daohib/bpel/hobj/HProcessInstance.hbm.xml")
      process_instance_hbm = File.read(process_instance_hbm_file)
      if !process_instance_hbm.include? "not-found=\"ignore\""
        process_instance_hbm.insert(process_instance_hbm.index("class=\"org.apache.ode.daohib.bpel.hobj.HProcess\"") - 1, "not-found=\"ignore\" ")
        File.open(process_instance_hbm_file, "w") { |f| f << process_instance_hbm }
      end
    end
    task "compile" => "hbm-hack"

    test.with project("bpel-epr"), BACKPORT, COMMONS.collections, COMMONS.lang, DERBY, COMMONS.logging, COMMONS.pool, COMMONS.dbcp, LOG4J, SLF4J,
      GERONIMO.transaction, GERONIMO.kernel, GERONIMO.connector, JAVAX.connector, JAVAX.ejb, SPRING, SPRING_TEST

    package :jar
  end

  desc "ODE Hibernate Compatible Databases"
  define "dao-hibernate-db" do
    predefined_for = lambda { |name| _("src/main/sql/simplesched-#{name}.sql") }
    properties_for = lambda { |name| _("src/main/sql/ode.#{name}.properties") }

    dao_hibernate = project("dao-hibernate").compile.target
    bpel_store = project("bpel-store").compile.target

    hibernate_requires[:xdoclet] = Buildr.group("xdoclet", "xdoclet-xdoclet-module", "xdoclet-hibernate-module", 
      :under=>"xdoclet", :version=>"1.2.3") + ["xdoclet:xjavadoc:jar:1.1-j5"] + projects("dao-hibernate")

    export = lambda do |properties, source, target|
      file(target=>[properties, source]) do |task|
        mkpath File.dirname(target), :verbose=>false

        hibernate_schemaexport target do |task, ant|
          ant.schemaexport(:properties=>properties.to_s, :quiet=>"yes", :text=>"yes", :delimiter=>";",
                           :drop=>"no", :create=>"yes", :output=>target) do
            ant.fileset(:dir=>source.to_s) {
              ant.include :name=>"**/*.hbm.xml"
              ant.exclude :name=>"**/HMessageExchangeProperty.hbm.xml"}
          end
        end
      end
    end

    runtime_sql = export[ properties_for[:derby], dao_hibernate, _("target/runtime.sql") ]
    store_sql = export[ properties_for[:derby], bpel_store, _("target/store.sql") ]
    common_sql = _("src/main/sql/common.sql")
    derby_sql = concat(_("target/derby.sql")=>[ predefined_for[:derby], common_sql, runtime_sql, store_sql ])
    derby_db = Derby.create(_("target/derby/hibdb")=>derby_sql)
    build derby_db
    
    h2_sql = _("src/schema/ode-hib-h2.sql")
    h2_db = H2.create("ode-hib-h2", _("target/h2/hibdb")=>h2_sql)
    build h2_db

    %w{ mysql firebird hsql postgres sqlserver oracle }.each do |db|
      partial = export[ properties_for[db], dao_hibernate, _("target/partial.#{db}.sql") ]
      build concat(_("target/#{db}.sql")=>[ common_sql, predefined_for[db], partial ])
    end

    NativeDB.create_dbs self, _("."), :hib

    package(:zip).include(derby_db, h2_db)
  end

  desc "ODE OpenJPA DAO Implementation"
  define "dao-jpa" do
    compile.with projects("bpel-api", "bpel-dao", "utils"),
      COMMONS.collections, COMMONS.logging, JAVAX.connector, JAVAX.persistence, JAVAX.transaction,
      OPENJPA, XERCES
    compile { open_jpa_enhance }
    package :jar
  end

  desc "ODE OpenJPA Derby Database"
  define "dao-jpa-ojpa-derby" do
    %w{ derby mysql oracle }.each do |db|
      db_xml = _("src/main/descriptors/persistence.#{db}.xml")
      scheduler_sql = _("src/main/scripts/simplesched-#{db}.sql")
      common_sql = _("src/main/scripts/common.sql")
      partial_sql = file("target/partial.#{db}.sql"=>db_xml) do |task|
        mkpath _("target"), :verbose=>false
        Buildr::OpenJPA.mapping_tool :properties=>db_xml, :action=>"build", :sql=>task.name,
          :classpath=>projects("bpel-store", "dao-jpa", "bpel-api", "bpel-dao", "utils" )
      end
      sql = concat(_("target/#{db}.sql")=>[_("src/main/scripts/license-header.sql"), common_sql, partial_sql, scheduler_sql])
      build sql
    end
    derby_db = Derby.create(_("target/derby/jpadb")=>_("target/derby.sql"))

    test.with projects("bpel-api", "bpel-dao", "bpel-obj", "bpel-epr", "dao-jpa", "utils"),
      BACKPORT, COMMONS.collections, COMMONS.lang, COMMONS.logging, GERONIMO.transaction,
      GERONIMO.kernel, GERONIMO.connector, HSQLDB, JAVAX.connector, JAVAX.ejb, JAVAX.persistence,
      JAVAX.transaction, LOG4J, OPENJPA, XERCES, WSDL4J

    build derby_db

    NativeDB.create_dbs self, _("."), :jpa

    package(:zip).include(derby_db)
  end

  desc "ODE JAva Concurrent OBjects"
  define "jacob" do
    compile.with projects("utils"), COMMONS.logging

    package :jar
  end

  desc "ODE JBI Integration Layer"
  define "jbi" do
    compile.with projects("bpel-api", "bpel-connector", "bpel-dao", "bpel-epr", "bpel-obj",
      "bpel-runtime", "scheduler-simple", "bpel-schemas", "bpel-store", "utils", "agents"),
      AXIOM, COMMONS.logging, COMMONS.pool, JAVAX.transaction, JBI, LOG4J, WSDL4J, XERCES

    package(:jar)
    package(:jbi).tap do |jbi|
      libs = artifacts(package(:jar),
        projects("bpel-api", "bpel-api-jca", "bpel-compiler", "bpel-connector", "bpel-dao",
        "bpel-epr", "jca-ra", "jca-server", "bpel-obj", "bpel-ql", "bpel-runtime",
        "scheduler-simple", "bpel-schemas", "bpel-store", "dao-hibernate", "dao-jpa",
        "jacob", "utils", "agents"),
        ANT, AXIOM, BACKPORT, COMMONS.codec, COMMONS.collections, COMMONS.dbcp, COMMONS.lang, COMMONS.pool,
        COMMONS.primitives, DERBY, GERONIMO.connector, GERONIMO.transaction, JAXEN, JAVAX.connector,
        JAVAX.ejb, JAVAX.jms, JAVAX.persistence, JAVAX.stream, JAVAX.transaction, LOG4J, OPENJPA,
        SAXON, TRANQL, XALAN, XERCES, XMLBEANS, WSDL4J)

      jbi.component :type=>:service_engine, :name=>"OdeBpelEngine", :description=>self.comment
      jbi.component :class_name=>"org.apache.ode.jbi.OdeComponent", :libs=>libs
      jbi.bootstrap :class_name=>"org.apache.ode.jbi.OdeBootstrap", :libs=>libs
      jbi.merge project("dao-hibernate-db").package(:zip)
      jbi.merge project("dao-jpa-ojpa-derby").package(:zip)
      jbi.include path_to("src/main/jbi/ode-jbi.properties")
    end

    test.using :properties=>{ "java.naming.factory.initial" => "org.apache.xbean.spring.jndi.SpringInitialContextFactory", "org.apache.ode.autoRetireProcess"=>"true"}, :java_args=>ENV['TEST_JVM_ARGS']
    test.with projects("dao-jpa", "dao-hibernate", "bpel-compiler", "bpel-api-jca", "jca-ra",
      "jca-server", "jacob"),
      BACKPORT, COMMONS.lang, COMMONS.io, COMMONS.collections, DERBY, GERONIMO.connector, GERONIMO.kernel,
      GERONIMO.transaction, JAVAX.connector, JAVAX.ejb, JAVAX.persistence, JAVAX.stream,
      JAVAX.transaction, JAXEN, JBI, OPENJPA, SAXON, SERVICEMIX, SPRING, TRANQL,
      XALAN, XBEAN, XMLBEANS,
      SLF4J,
      LOG4J,
      DOM4J,
      HIBERNATE

      test.setup unzip(_("target/test/smx/ode")=>project("dao-jpa-ojpa-derby").package(:zip))
      test.setup unzip(_("target/test/smx/ode")=>project("dao-hibernate-db").package(:zip))
      test.setup task(:prepare_jbi_tests) do |task|
      cp _("src/test/jbi/ode-jbi.properties"), _("target/test/smx/ode")
      cp _("src/main/jbi/hibernate.properties"), _("target/test/smx/ode")
      rm_rf Dir["target/test/resources"]
      cp_r _("src/test/resources"), _("target/test/resources")
    end
  end

  desc "ODE Commmands for Karaf"
  define "jbi-karaf-commands" do
    compile.with projects("bpel-schemas", "jbi"), JBI, KARAF, XMLBEANS, COMMONS.logging
    libs = artifacts(projects("bpel-schemas", "jbi"), JBI, KARAF, XMLBEANS, COMMONS.logging)
    package(:bundle).tap do |bnd|
      bnd.classpath = [_("target/classes"), libs].flatten
      BUNDLE_VERSIONS.each {|key, value| bnd[key] = value }
      bnd['Bundle-Name'] = "Apache ODE :: Commands"
      bnd['Bundle-Version'] = VERSION_NUMBER
      bnd['Require-Bundle'] = "org.apache.ode.ode-jbi-bundle;bundle-version=#{osgi_version_for(VERSION_NUMBER)}"
      bnd['Import-Package'] = 'org.osgi.service.command,org.apache.karaf.shell.console;version="[2.1,2.2)",*'
      bnd['Private-Package'] = "org.apache.ode.karaf.commands;version=#{VERSION_NUMBER}"
      bnd['Include-Resource'] = _('src/main/resources')
    end
  end

  desc "ODE Examples for Karaf"
  define "jbi-karaf-examples",
    :group => "org.apache.ode.examples",
    :base_dir => "distro/src/examples-jbi/maven2" do
    
    define "helloworld2-osgi" do
      package(:bundle, :id => "helloworld-bundle").tap do |bnd|
        bnd.classpath = [KARAF, project("ode:jbi-bundle")]
        bnd['Bundle-Name'] = "Apache ODE :: Hello World Example"
        bnd['Bundle-SymbolicName'] = "org.apache.ode.examples-helloworld2-bundle"
        bnd['Bundle-Version'] = VERSION_NUMBER
        bnd['Require-Bundle'] = "org.apache.ode.ode-jbi-bundle;bundle-version=#{osgi_version_for(VERSION_NUMBER)}"
        bnd['Import-Package'] = "org.apache.servicemix.cxfbc,org.apache.servicemix.common.osgi"
        bnd['Export-Package'] = ""
        bnd['-exportcontents'] = ""
        bnd['Include-Resource'] = _('src/main/resources')
      end
      # we package sources and javadocs separately to give them a custom id
      package(:sources, :id => "helloworld-bundle")
      
      # This project does not contain java classes, hence there are no javadocs. 
      # But since Nexus will complain about a missing javadoc artifact, we make sure that an empty one is created.
      package(:javadoc, :id => "helloworld-bundle").enhance { mkdir_p _("target/doc") }
    end
    
    define "ping-pong-osgi" do
      compile
      package(:bundle, :id => "ping-pong-bundle").tap do |bnd|
        bnd.classpath = [_("target/classes"), KARAF, project("ode:jbi-bundle")].flatten
        bnd['Bundle-Name'] = "Apache ODE :: Ping-Pong Example"
        bnd['Bundle-SymbolicName'] = "org.apache.ode.examples-ping-pong-bundle"
        bnd['Bundle-Version'] = VERSION_NUMBER
        bnd['Require-Bundle'] = "org.apache.ode.ode-jbi-bundle;bundle-version=#{osgi_version_for(VERSION_NUMBER)}"
        bnd['Import-Package'] = "org.apache.servicemix.cxfbc,org.apache.servicemix.common.osgi"
        bnd['Export-Package'] = "org.apache.ode.ping"
        bnd['Include-Resource'] = _('src/main/resources')
      end
      
      # we package sources and javadocs separately to give them a custom id
      package(:sources, :id => "ping-pong-bundle")
      package(:javadoc, :id => "ping-pong-bundle")
    end
  end

  define "jbi-karaf-pmapi-httpbinding" do
    package(:bundle).tap do |bnd|
      bnd.classpath = [KARAF, project("ode:jbi-bundle")]
      bnd['Bundle-Name'] = "Apache ODE :: PMAPI HTTP Binding"
      bnd['Bundle-SymbolicName'] = "org.apache.ode-pmapi-httpbinding"
      bnd['Bundle-Version'] = VERSION_NUMBER
      bnd['Require-Bundle'] = "org.apache.ode.ode-jbi-bundle;bundle-version=#{osgi_version_for(VERSION_NUMBER)}"
      bnd['Import-Package'] = "org.apache.servicemix.cxfbc,org.apache.servicemix.common.osgi"
      bnd['-exportcontents'] = ""
      bnd['Include-Resource'] = _('src/main/resources')
    end
  end

  desc "ODE JBI Packaging for Karaf"
  define "jbi-karaf" do
    resources.filter.using(BUNDLE_VERSIONS)
    package :jar
    # Generate features.xml
    def package_as_feature(file_name)
      file file_name => [_("src/main/resources/features.xml")] do
        filter(_("src/main/resources")).include("features.xml").into(_("target")).using(BUNDLE_VERSIONS).run
        mv _("target/features.xml"), file_name
      end
    end
    def package_as_feature_spec(spec)
      spec.merge({ :type=>:xml, :classifier=>'features' })
    end
    package(:feature)
  end

  desc "ODE JBI Bundle"
  define "jbi-bundle" do
    ode_libs = artifacts(projects("bpel-api", "bpel-api-jca", "bpel-compiler", "bpel-connector", "bpel-dao",
                                  "bpel-epr", "jca-ra", "jca-server", "bpel-obj", "bpel-ql", "bpel-runtime",
                                  "scheduler-simple", "bpel-schemas", "bpel-store", "dao-hibernate", "dao-jpa",
                                  "jacob", "utils", "agents"))
    libs = artifacts(ANT, AXIOM, BACKPORT, COMMONS.codec, COMMONS.collections, COMMONS.dbcp, COMMONS.lang, COMMONS.pool,
                     COMMONS.primitives, COMMONS.io, DERBY, GERONIMO.connector, GERONIMO.transaction, JAXEN, JAVAX.connector, 
                     JAVAX.ejb, JAVAX.jms, JAVAX.persistence, JAVAX.stream, JAVAX.transaction, LOG4J, OPENJPA, 
                     SAXON, TRANQL, XALAN, XERCES, XMLBEANS, WSDL4J, KARAF)
    compile.with projects("bpel-schemas", "jbi", "bpel-api"), JBI, libs, KARAF, SPRING, SPRING_OSGI

    package(:bundle).tap do |bnd|
      # inline dao zip files
      zips = artifacts(project("dao-hibernate-db").package(:zip), project("dao-jpa-ojpa-derby").package(:zip))
      inlines = zips.map{|item| "@" + item.to_s}

      # embed jars
      bnd_libs = ode_libs + artifacts(AXIOM, BACKPORT, GERONIMO.connector, JAXEN, 
                                      JAVAX.connector, JAVAX.persistence, JAVAX.ejb, 
                                      OPENJPA, SAXON, TRANQL, 
                                      XALAN, XERCES, XMLBEANS, WSDL4J)
      includes = bnd_libs.map{|item| File.basename(item.to_s)} 
      bnd["includes"] = includes.join(', ') 

      # embedd *.xsd, *.xml, xmlbeans* from ode libs
      embedres = ode_libs.map {|pkg| ['**.xsd', '**.xml', 'schemaorg_apache_xmlbeans/**'].map {|x| '@' + pkg.to_s + '!/' + x}}.join(', ')
      bnd['Export-Package'] = "org.apache.ode*;version=#{VERSION_NUMBER};-split-package:=merge-first"
      bnd['Import-Package'] = '!com.sun.mirror*, !org.apache.axis2.client*, javax.jbi*;version="1.0", javax.transaction*;version="1.1", org.tranql.connector.jdbc, org.apache.commons.httpclient*;version="3.0", org.apache.commons.logging*;version="1.1", org.apache.commons*, org.apache.geronimo.transaction.manager;version="2.0", org.osgi.service.command;version="[0.2,1)", org.springframework.beans.factory.xml;version="2.5", org.w3c.dom, org.xml.sax, org.xml.sax.ext, org.xml.sax.helpers, org.jaxen.saxpath,net.sf.saxon.xpath,*;resolution:=optional'
      bnd['Embed-Dependency'] = '*;inline=**.xsd|schemaorg_apache_xmlbeans/**|**.xml'
      bnd['DynamicImport-Package'] = '*'
      bnd['Include-Resource'] = [embedres, _('src/main/resources'), inlines].flatten.join(', ')
      bnd['Bundle-Vendor'] = 'Apache Software Foundation'
      bnd['Bundle-License'] = 'http://www.apache.org/licenses/LICENSE-2.0'
      bnd['Bundle-DocURL'] = 'http://ode.apache.org'
      bnd['Bundle-Name'] = 'Apache ODE :: BPEL Service Engine'
      bnd.classpath = [project.compile.target, bnd_libs, artifacts(project("jbi").package(:jar)), libs].flatten

      BUNDLE_VERSIONS.each {|key, value| bnd[key] = value }
    end
  end

  desc "ODE JCA Resource Archive"
  define "jca-ra" do
    compile.with project("utils"), JAVAX.connector
    package :jar
  end

  desc "ODE JCA Server"
  define "jca-server" do
    compile.with projects("jca-ra", "utils"), COMMONS.logging
    package :jar
  end

  desc "ODE Tools"
  define "tools" do
    compile.with projects("bpel-compiler", "utils"), ANT, COMMONS.httpclient, COMMONS.logging
    package :jar
  end

  desc "ODE Utils"
  define "utils" do
    compile.with AXIOM, AXIS2_ALL, COMMONS.lang, COMMONS.collections, COMMONS.logging, COMMONS.pool, COMMONS.httpclient, COMMONS.codec, LOG4J, XERCES, JAVAX.stream, WSDL4J, SAXON
    # env variable required by HierarchicalPropertiesTest
    test.using :environment=>{ 'TEST_DUMMY_ENV_VAR'=>42}
    test.exclude "*TestResources"
    package :jar
  end

  desc "ODE Agents"
  define "agents" do
     compile
     package(:jar).with :manifest=>_("src/main/resources/META-INF/MANIFEST.MF")
  end

  # sources and javadocs of jbi-karaf-examples are packaged separately.
  package_with_sources :except => ["jbi-karaf-examples:helloworld2-osgi", "jbi-karaf-examples:ping-pong-osgi"]
  package_with_javadoc :except => ["jbi-karaf-examples:helloworld2-osgi", "jbi-karaf-examples:ping-pong-osgi"] unless ENV["JAVADOC"] =~ /^(no|off|false|skip)$/i

  gpg_sign_before_upload
  
  task :pmd do
    pmd_classpath = transitive('pmd:pmd:jar:4.2.5').each(&:invoke).map(&:to_s).join(File::PATH_SEPARATOR)
    mkdir_p _(:reports)
    ant("pmd-report") do |ant|
      ant.taskdef :name=> 'pmd', :classpath=>pmd_classpath, :classname=>'net.sourceforge.pmd.ant.PMDTask'
      # rulesets: basic,imports,unusedcode,strings,optimizations,logging-jakarta-commons,migrating,design
      ant.pmd :rulesetfiles => 'basic,unusedcode,optimizations,design' do
        ant.formatter :type=>'html', :toFile=> _(:reports, 'pmd.html')
        projects.each do |pr|
          pr.compile.sources.each do |src|
            ant.fileset :dir=> src, :includes=>'**/*.java'
          end
        end
      end
    end  
  end
end

define "apache-ode" do
  [:version, :group, :manifest, :meta_inf].each { |prop| send "#{prop}=", project("ode").send(prop) }

  def distro(project, postfix)
    id = project.parent.id + postfix
    # distros require all packages in project("ode") to be built first
    project.package(:zip, :id=>id).enhance(project("ode").projects.map(&:packages).flatten) do |pkg|
      pkg.path("#{id}-#{version}").tap do |zip|
        zip.include meta_inf + ["RELEASE_NOTES", "README"].map { |f| path_to(f) }
        zip.path("examples").include(project.path_to("src/examples"+postfix), :as=>".").exclude "**/target"

        # Libraries
        zip.path("lib").include artifacts(COMMONS.logging, COMMONS.codec, COMMONS.httpclient,
          COMMONS.pool, COMMONS.collections, JAXEN, SAXON, LOG4J, WSDL4J, XALAN, XERCES)
        project("ode").projects("utils", "tools", "bpel-compiler", "bpel-api", "bpel-obj", "bpel-schemas").
          map(&:packages).flatten.each do |pkg|
          zip.include(pkg.to_s, :as=>"#{pkg.id}.#{pkg.type}", :path=>"lib")
        end

        # Including third party licenses
        Dir["#{project.path_to("license")}/*LICENSE"].each { |l| zip.include(l, :path=>"lib") }
        zip.include(project.path_to("target/LICENSE"))

        # Include supported database schemas
        Dir["#{project("ode:dao-jpa-ojpa-derby").path_to("target")}/*.sql"].each do |f|
          zip.include(f, :path=>"sql") unless f =~ /partial/
        end

        # Tools scripts (like bpelc and sendsoap)
        bins = file(project.path_to("target/bin")=>FileList[project.path_to("src/bin/*")]) do |task|
          mkdir_p project.path_to("target/bin")
          mkpath task.name
          cp task.prerequisites, task.name
          chmod 0755, FileList[task.name + "/*"], :verbose=>false
        end
        bins.invoke
        zip.include bins

        yield zip
        project.check zip, "should contain mysql.sql" do
          it.should contain("sql/mysql.sql")
        end
        project.check zip, "should contain sendsoap.bat" do
          it.should contain("bin/sendsoap.bat")
        end
      end
    end
  end

  desc "ODE Axis2 Based Distribution"
  define "distro" do
    parent.distro(self, "-war") { |zip| zip.include project("ode:axis2-war").package(:war), :as=>"ode.war" }
    parent.distro(self, "-jbi") { |zip| zip.include project("ode:jbi").package(:zip) }

    # Preparing third party licenses
    build do
      Dir.mkdir(project.path_to("target")) unless File.exist?(project.path_to("target"))
      cp parent.path_to("LICENSE"), project.path_to("target/LICENSE")
      File.open(project.path_to("target/LICENSE"), "a+") do |l|
        l <<  Dir["#{project.path_to("license")}/*LICENSE"].map { |f| "lib/"+f[/[^\/]*$/] }.join("\n")
      end
    end

    project("ode:axis2-war").task("start").enhance do |task|
      target = "#{task.path}/webapp/WEB-INF/processes"
      puts "Deploying processes to #{target}" if verbose
      verbose(false) do
        mkpath target
        cp_r FileList[_("src/examples/*")].to_a, target
        rm Dir.glob("#{target}/*.deployed")
      end
    end
  end

  package(:zip, :id=>"#{id}-sources").path("#{id}-sources-#{version}").tap do |zip|
    if File.exist?(".svn")
      `svn status -v`.reject { |l| l[0] == ?? || l[0] == ?D || l.strip.empty? || l[0...3] == "---"}.
        map { |l| l.split.last }.reject { |f| File.directory?(f) }.
        each { |f| zip.include f, :as=>f.gsub("\\", "/") }
    elsif File.exist? '.git/config'
      `git ls-files`.split("\n").each { |f| zip.include f, :as=>f.gsub("\\", "/") }
    else
      zip.include Dir.pwd, :as=>"."
    end
  end

  package(:zip, :id=>"#{id}-docs").include(doc.from(project("ode").projects).
    using(:javadoc, :windowtitle=>"Apache ODE #{project.version}").target, :as=>"#{id}-docs-#{version}") unless ENV["JAVADOC"] =~ /^(no|off|false|skip)$/i
    
  # sign packages
  gpg_sign_before_upload

end
