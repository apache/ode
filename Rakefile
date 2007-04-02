require "buildr/lib/buildr.rb"


# Keep this structure to allow the build system to update version numbers.
VERSION_NUMBER = "2.0-SNAPSHOT"
NEXT_VERSION = "2.1"

ANNONGEN            = "annogen:annogen:jar:0.1.0"
ANT                 = "ant:ant:jar:1.6.5"
AXIOM               = group("axiom-api", "axiom-impl", "axiom-dom", :under=>"org.apache.ws.commons.axiom", :version=>"1.2")
AXIS2               = "org.apache.axis2:axis2:jar:1.1"
AXIS2_ALL           = group("axis2", "axis2-adb", "axis2-codegen", "axis2-tools", "axis2-kernel",
                        "axis2-java2wsdl", "axis2-jibx", "axis2-kernel", "axis2-saaj", "axis2-xmlbeans",
                        :under=>"org.apache.axis2", :version=>"1.1")
BACKPORT            = "backport-util-concurrent:backport-util-concurrent:jar:3.0"
COMMONS             = OpenStruct.new(
  :codec            =>"commons-codec:commons-codec:jar:1.3",
  :collections      =>"commons-collections:commons-collections:jar:3.1",
  :fileupload       =>"commons-fileupload:commons-fileupload:jar:1.0",
  :httpclient       =>"commons-httpclient:commons-httpclient:jar:3.0",
  :lang             =>"commons-lang:commons-lang:jar:2.1",
  :logging          =>"commons-logging:commons-logging:jar:1.0.3",
  :pool             =>"commons-pool:commons-pool:jar:1.2"
)
DERBY               = "org.apache.derby:derby:jar:10.1.2.1"
DERBY_TOOLS         = "org.apache.derby:derbytools:jar:10.1.2.1"
DOM4J               = "dom4j:dom4j:jar:1.6.1"
GERONIMO            = OpenStruct.new(
  :kernel           =>"org.apache.geronimo.modules:geronimo-kernel:jar:1.2-beta",
  :transaction      =>"org.apache.geronimo.modules:geronimo-transaction:jar:1.2-beta",
  :connector        =>"org.apache.geronimo.modules:geronimo-connector:jar:1.2-beta"
)
HIBERNATE           = "org.hibernate:hibernate:jar:3.1.2"
HSQLDB              = "hsqldb:hsqldb:jar:1.8.0.7"
JAVAX               = OpenStruct.new(
  :activation       =>"javax.activation:activation:jar:1.1",
  #:activation       =>"geronimo-spec:geronimo-spec-activation:jar:1.0.2-rc4",
  :connector        =>"org.apache.geronimo.specs:geronimo-j2ee-connector_1.5_spec:jar:1.0",
  :ejb              =>"org.apache.geronimo.specs:geronimo-ejb_2.1_spec:jar:1.1",
  :javamail         =>"geronimo-spec:geronimo-spec-javamail:jar:1.3.1-rc5",
  :jms              =>"geronimo-spec:geronimo-spec-jms:jar:1.1-rc4",
  :persistence      =>"javax.persistence:persistence-api:jar:1.0",
  :servlet          =>"org.apache.geronimo.specs:geronimo-servlet_2.4_spec:jar:1.0",
  :stream           =>"stax:stax-api:jar:1.0.1",
  :transaction      =>"org.apache.geronimo.specs:geronimo-jta_1.0.1B_spec:jar:1.0"
)
JAXEN               = "jaxen:jaxen:jar:1.1-beta-8"
JBI                 = "org.apache.servicemix:servicemix-jbi:jar:3.1-incubating"
JENCKS              = "org.jencks:jencks:jar:all:1.3"
JIBX                = "jibx:jibx-run:jar:1.1-beta3"
LOG4J               = "log4j:log4j:jar:1.2.13"
OPENJPA             = ["org.apache.openjpa:openjpa-all:jar:0.9.7-incubating-SNAPSHOT",
                       "net.sourceforge.serp:serp:jar:1.12.0"]
QUARTZ              = "quartz:quartz:jar:1.5.2"
SAXON               = group("saxon", "saxon-xpath", "saxon-dom", :under=>"net.sf.saxon", :version=>"8.7")
TRANQL              = "tranql:tranql-connector:jar:1.1"
WOODSTOX            = "woodstox:wstx-asl:jar:3.0.1"
WSDL4J              = "wsdl4j:wsdl4j:jar:1.6.1"
XALAN               = "org.apache.ode:xalan:jar:2.7.0"
XERCES              = "xerces:xercesImpl:jar:2.8.0"
XSTREAM             = "xstream:xstream:jar:1.2"
WS_COMMONS          = OpenStruct.new(
  :axiom            =>AXIOM,
  :neethi           =>"org.apache.ws.commons.neethi:neethi:jar:2.0",
  :xml_schema       =>"org.apache.ws.commons.schema:XmlSchema:jar:1.2"
)
XMLBEANS            = "xmlbeans:xbean:jar:2.2.0"


repositories.remote << "http://pxe.intalio.org/public/maven2"
repositories.remote << "http://people.apache.org/repo/m2-incubating-repository"
repositories.remote << "http://repo1.maven.org/maven2"
repositories.remote << "http://people.apache.org/repo/m2-snapshot-repository"
repositories.deploy_to[:url] ||= "sftp://ode.intalio.org/var/www/public/maven2"


define "ode", :group=>"org.apache.ode", :version=>VERSION_NUMBER do

  compile.options.source = "1.5"
  compile.options.target = "1.5"
  manifest["Implementation-Vendor"] = "Apache Software Foundation"

  desc "ODE Axis Integration Layer"
  define "axis2" do
    compile.with project("ode:bpel-api"), project("ode:bpel-connector"),
      project("ode:bpel-dao"), project("ode:bpel-epr"),
      project("ode:bpel-runtime"), project("ode:bpel-scheduler-quartz"),
      project("ode:bpel-schemas"), project("ode:bpel-store"),
      project("ode:utils"),
      AXIOM, AXIS2, COMMONS.logging, COMMONS.collections,
      DERBY, GERONIMO.kernel, GERONIMO.transaction,
      JAVAX.activation, JAVAX.servlet, JAVAX.stream,
      JAVAX.transaction, JENCKS, WSDL4J, XMLBEANS

    tests.compile.with project("ode:tools")
    tests.run.with XERCES, WOODSTOX, AXIOM, WS_COMMONS.xml_schema, JAVAX.javamail

    package :jar
  end

  desc "ODE Axis2 Based Web Application"
  define "axis2-war" do
    libs = project("ode:axis2"), project("ode:bpel-api"),
      project("ode:bpel-compiler"), project("ode:bpel-connector"), project("ode:bpel-dao"),
      project("ode:bpel-epr"), project("ode:bpel-obj"),
      project("ode:bpel-ql"), project("ode:bpel-runtime"),
      project("ode:bpel-scheduler-quartz"), project("ode:bpel-schemas"),
      project("ode:bpel-store"),
      project("ode:dao-hibernate"), project("ode:jacob"),
      project("ode:jca-ra"), project("ode:jca-server"),
      project("ode:utils"),
      project("ode:dao-jpa"),
      AXIS2_ALL, ANNONGEN, BACKPORT, COMMONS.codec,
      COMMONS.collections, COMMONS.fileupload, COMMONS.httpclient,
      COMMONS.lang, COMMONS.pool, DERBY, DERBY_TOOLS,
      JAXEN,
      JAVAX.activation, JAVAX.ejb, JAVAX.javamail, JAVAX.connector, JAVAX.jms,
      JAVAX.persistence, JAVAX.transaction, JAVAX.stream, JENCKS, JIBX,
      GERONIMO.connector, GERONIMO.kernel, GERONIMO.transaction, LOG4J, OPENJPA, QUARTZ,
      SAXON, TRANQL, WOODSTOX, WSDL4J,
      WS_COMMONS.axiom, WS_COMMONS.neethi, WS_COMMONS.xml_schema,
      XALAN, XERCES, XMLBEANS

    package(:war).with(:libs=>libs).path("WEB-INF").tap do |web_inf|
      web_inf.merge project("ode:dao-jpa-ojpa-derby").package(:zip)
      web_inf.merge project("ode:dao-hibernate-db").package(:zip)
      web_inf.include project("ode:axis2").path_to("src/main/wsdl/*")
      web_inf.include project("ode:bpel-schemas").path_to("src/main/xsd/pmapi.xsd")
    end

    webserve.using(:war_path=>package(:war).name, :context_path=>"/ode",
      :process_alias=>{"HelloWorld2"=>"distro-axis2/src/examples/HelloWorld2",
                       "DynPartner"=>"distro-axis2/src/examples/DynPartner",
                       "MagicSession"=>"distro-axis2/src/examples/MagicSession"} )
  end

  desc "ODE APIs"
  define "bpel-api" do
    compile.with project("ode:utils"), project("ode:bpel-obj"),
      project("ode:bpel-schemas"), WSDL4J, COMMONS.logging
    package :jar
  end

  desc "ODE JCA connector"
  define "bpel-api-jca" do
    compile.with project("ode:bpel-api"), JAVAX.connector
    package :jar
  end

  desc "ODE BPEL Compiler"
  define "bpel-compiler" do
    compile.with project("ode:bpel-api"), project("ode:bpel-obj"),
      project("ode:bpel-schemas"), project("ode:bpel-scripts"),
      project("ode:utils"),
      COMMONS.logging, JAVAX.stream, JAXEN, SAXON, WSDL4J, XALAN, XERCES
    package :jar
  end

  desc "ODE JCA Connector Implementation"
  define "bpel-connector" do
    compile.with project("ode:bpel-api"), project("ode:bpel-api-jca"),
      project("ode:bpel-runtime"), project("ode:jca-ra"), project("ode:jca-server")
    package :jar
  end

  desc "ODE DAO Interfaces"
  define "bpel-dao" do
    compile.with project("ode:bpel-api")
    package :jar
  end

  desc "ODE Interface Layers Common"
  define "bpel-epr" do
    compile.with project("ode:utils"), project("ode:bpel-dao"),
      project("ode:bpel-api"),
      COMMONS.logging, DERBY, JAVAX.transaction, GERONIMO.transaction, GERONIMO.connector, TRANQL,
      JAVAX.connector
    package :jar
  end

  desc "ODE BPEL Object Model"
  define "bpel-obj" do
    compile.with project("ode:utils"), SAXON, WSDL4J
    package :jar
  end

  desc "ODE BPEL Query Language"
  define "bpel-ql" do
    jjtree_src = path_to(:src_dir, "main/jjtree")
    jjtree_out = path_to(:target_dir, "generated/jjtree")
    javacc_out = path_to(:target_dir, "generated/javacc")

    prepare Java::JavaCC.jjtree_task(path_to(jjtree_out, "org/apache/ode/ql/jcc")=>jjtree_src).using(:build_node_files=>false)
    prepare Java::JavaCC.javacc_task(path_to(javacc_out, "org/apache/ode/ql/jcc")=>jjtree_out)

    compile.with project("ode:bpel-api"), project("ode:bpel-compiler"),
      project("ode:bpel-obj"), project("ode:jacob"), project("ode:utils"),
      jjtree_out, javacc_out
    compile.from jjtree_out, javacc_out

    package :jar
  end

  desc "ODE Runtime Engine"
  define "bpel-runtime" do
    compile.with project("ode:bpel-api"), project("ode:bpel-compiler"), project("ode:bpel-dao"),
      project("ode:bpel-obj"), project("ode:bpel-schemas"), project("ode:bpel-store"),
      project("ode:jacob"), project("ode:jacob-ap"), project("ode:utils"),
      COMMONS.logging, COMMONS.collections,
      JAXEN, JAVAX.persistence, JAVAX.stream, SAXON,
      WSDL4J, XMLBEANS

    # Prepare before compile, but we use the same classpath,
    # so define this after compile.with.
    generated = path_to(:target_dir, "generated/apt")
    prepare Java.apt_task(generated=>path_to(:java_src_dir, "org/apache/ode/bpel/runtime/channels")).
      using(:classpath=>compile.classpath, :source=>compile.options.source)
    # Include the generated sources.
    compile.from generated

    tests.compile.with project("ode:bpel-scheduler-quartz"),
      project("ode:dao-jpa"),
      COMMONS.pool, COMMONS.lang, DERBY, JAVAX.connector, JAVAX.transaction,
      LOG4J, XERCES, Java::OpenJPA::REQUIRES, QUARTZ, XALAN
    tests.resources unzip(project("ode:dao-jpa-ojpa-derby").package(:zip)).into(path_to(:test_target_dir, "derby-db"))

    package :jar
  end

  desc "ODE Quartz Interface"
  define "bpel-scheduler-quartz" do
    compile.with project("ode:bpel-api"), project("ode:utils"),
      COMMONS.collections, COMMONS.logging, JAVAX.transaction, QUARTZ
    package :jar
  end

  desc "ODE Schemas"
  define "bpel-schemas" do
    schemas = [ path_to(:src_dir, "main/xsd/pmapi.xsdconfig"),
                path_to(:src_dir, "main/xsd/dd.xsdconfig"), path_to(:src_dir, "main/xsd") ]
    generated = path_to(:target_dir, "generated")
    prepare Java::XMLBeans.compile_task(generated=>schemas).
      using(:javasource=>compile.options.source, :classes=>path_to(:java_target_dir))

    compile.from generated
    compile.with JAVAX.stream, XMLBEANS
    package :jar
  end

  desc "ODE BPEL Test Script Files"
  define "bpel-scripts" do
    package :jar
  end

  desc "ODE Process Store"
  define "bpel-store" do
    compile.with project("ode:bpel-api"), project("ode:bpel-compiler"),
      project("ode:bpel-dao"), project("ode:bpel-obj"), project("ode:bpel-schemas"),
      project("ode:dao-hibernate"), project("ode:utils"),
      COMMONS.logging, JAVAX.persistence, JAVAX.stream,
      HIBERNATE, HSQLDB, XMLBEANS, XERCES, WSDL4J

    compile do |task|
      Java::OpenJPA.enhance(:output=>compile.target, :classpath=>compile.classpath,
        :properties=>path_to(:resources_dir, "META-INF/persistence.xml"))
    end

    tests.run.with DOM4J

    package :jar
  end

  desc "ODE BPEL Tests"
  define "bpel-test" do
    compile.with project("ode:bpel-api"), project("ode:bpel-compiler"),
      project("ode:bpel-dao"), project("ode:bpel-runtime"),
      project("ode:bpel-store"), project("ode:utils"),
      DERBY, WSDL4J

    tests.compile.with project("ode:dao-jpa"), JAVAX.persistence
    tests.run.with project("ode:bpel-obj"), project("ode:jacob"), project("ode:bpel-schemas"), project("ode:dao-jpa"),
      COMMONS.collections, COMMONS.lang, COMMONS.logging,
      JAVAX.connector, JAVAX.stream, JAVAX.transaction, JAXEN, HSQLDB, LOG4J, OPENJPA, SAXON, XERCES, XMLBEANS, XALAN

    package :jar
  end

  desc "ODE Hibernate DAO Implementation"
  define "dao-hibernate" do
    compile.with project("ode:bpel-api"), project("ode:bpel-dao"),
      project("ode:bpel-ql"), project("ode:utils"),
      COMMONS.lang, COMMONS.logging, JAVAX.transaction, HIBERNATE, DOM4J

    compile do
      Java::Hibernate.xdoclet :source=>compile.sources, :include=>"**/*.java",
        :target=>compile.target, :excludedtags=>"@version,@author,@todo"
    end

    package :jar
  end

  desc "ODE Hibernate Compatible Databases"
  define "dao-hibernate-db" do

    def predefined_for(name) ; file(path_to(:src_dir, "main", "sql", "tables_#{name}.sql")); end
    def properties_for(name) ; file(path_to(:src_dir, "main", "sql", "ode.#{name}.properties")); end

    dao_hibernate = project("ode:dao-hibernate").compile.target
    bpel_store = project("ode:bpel-store").compile.target

    export_task = Java::Hibernate.schemaexport_task
    export = lambda do |properties, sources, target|
      export_task.enhance([properties, sources]) do |task| 
        task.ant.schemaexport(:properties=>properties.to_s, :quiet=>"yes", :text=>"yes", :delimiter=>";",
          :drop=>"no", :create=>"yes", :output=>target.to_s) { fileset(:dir=>sources.to_s) { include :name=>"**/*.hbm.xml" } }
      end
      file_create(target.to_s) { export_task.invoke }
    end

    build file_create(path_to(:target_dir)) { |task| mkpath task.name }
    runtime_sql = export.call(properties_for(:derby), dao_hibernate, path_to(:target_dir, "runtime.sql")) 
    store_sql = export.call(properties_for(:derby), bpel_store, path_to(:target_dir, "store.sql")) 
    derby_sql = concat(path_to(:target_dir, "derby.sql")=>[ predefined_for(:derby), runtime_sql, store_sql ]) 
    %w{ firebird hsql postgres sqlserver }.each do |db|
      partial = export.call(properties_for(db), dao_hibernate, path_to(:target_dir, "partial.#{db}.sql"))
      build concat(path_to(:target_dir, "#{db}.sql")=>[ predefined_for(db), partial ])
    end

    build derby_db = Derby.create(path_to(:target_dir, "derby/hibdb")=>derby_sql)
    package :zip, :include=>derby_db
  end

  desc "ODE OpenJPA DAO Implementation"
  define "dao-jpa" do
    compile.with project("ode:bpel-api"), project("ode:bpel-dao"), project("ode:utils"),
      COMMONS.collections, COMMONS.logging, JAVAX.connector, JAVAX.persistence, JAVAX.transaction,
      OPENJPA, XERCES

    compile do |task|
      Java::OpenJPA.enhance :output=>compile.target, :classpath=>compile.classpath,
        :properties=>path_to(:resources_dir, "META-INF/persistence.xml" )
    end
    package :jar
  end

  desc "ODE OpenJPA Derby Database"
  define "dao-jpa-ojpa-derby" do
    # Create the Derby SQL file using the OpenJPA mapping tool, and
    # append the Quartz DDL at the end.
    derby_xml = path_to(:src_dir, "main/descriptors/persistence.derby.xml")
    quartz_sql = path_to(:src_dir, "main/scripts/quartz-derby.sql")
    derby_sql = file(path_to(:target_dir, "derby.sql")=>[derby_xml, quartz_sql]) do |task|
      mkpath path_to(:target_dir), :verbose=>false
      Java::OpenJPA.mapping_tool :properties=>derby_xml, :action=>"build", :sql=>task.name,
        :classpath=>artifacts( project("ode:bpel-store"), project("ode:dao-jpa"),
                      project("ode:bpel-api"), project("ode:bpel-dao"),
                      project("ode:utils") )
      File.open(task.name, "a") { |file| file.write File.read(quartz_sql) }
    end

    tests.compile.with project("ode:bpel-api"), project("ode:bpel-dao"),
      project("ode:bpel-obj"), project("ode:dao-jpa"), project("ode:utils"),
      COMMONS.collections, COMMONS.logging, JAVAX.persistence, JAVAX.transaction, 
      OPENJPA, XERCES, WSDL4J

    build derby_db = Derby.create(path_to(:target_dir, "derby/jpadb")=>derby_sql)
    package :zip, :include=>derby_db
  end

  desc "ODE Axis2 Based Distribution"
  define "distro-axis2" do
    resources(
      filter(["RELEASE_NOTES", "README", "LICENSE", "NOTICE", "DISCLAIMER"].map { |f| path_to("..", f) }).into(path_to(:target_dir, "stage")),
      filter(path_to(:src_dir, "examples")).into(path_to(:target_dir, "stage"))
    )

    returning(package(:zip, path_to(:target_dir, "apache-ode-#{VERSION_NUMBER}-incubating-war.zip"))) do |zip|
      zip.include path_to(:target_dir, "stage/*")
      zip.path("lib").include artifacts(COMMONS.logging, COMMONS.codec, COMMONS.httpclient,
        COMMONS.pool, COMMONS.collections, JAXEN,
        SAXON, LOG4J, WSDL4J)
      zip.include project("ode:axis2-war").package(:war), :as=>"ode.war"
      zip.merge project("ode:tools-bin").package(:zip)
      projects("ode:utils", "ode:tools", "ode:bpel-compiler",
               "ode:bpel-api", "ode:bpel-obj", "ode:bpel-schemas").
      #[ "ode:utils", "ode:tools", "ode:bpel-compiler",
      #  "ode:bpel-api", "ode:bpel-obj", "ode:bpel-schemas" ].
        map(&:packages).flatten.each do |pkg|
        zip.include(pkg.to_s, :as=>"#{pkg.id}.#{pkg.type}", :path=>"lib")
      end
    end
  end

  desc "ODE JAva Concurrent OBjects"
  define "jacob" do
    compile.with project("ode:utils"), project("ode:jacob-ap"),
      COMMONS.logging

    # Prepare before compile, but we use the same classpath,
    # so define this after compile.with.
    generated = path_to(:target_dir, "generated/apt")
    prepare Java.apt_task(generated=>path_to(:java_src_dir)).
      using(:classpath=>compile.classpath, :source=>compile.options.source)
    # Include the generated sources.
    compile.from generated

    package :jar
  end

  desc "ODE Jacob APR Code Generation"
  define "jacob-ap" do
    compile.with Java.tools
    package :jar
  end

  desc "ODE JBI Integration Layer"
  define "jbi" do
    compile.with project("ode:bpel-api"), project("ode:bpel-connector"),
      project("ode:bpel-dao"), project("ode:bpel-epr"), project("ode:bpel-obj"),
      project("ode:bpel-runtime"), project("ode:bpel-scheduler-quartz"),
      project("ode:bpel-schemas"), project("ode:bpel-store"),
      project("ode:utils"),
      COMMONS.logging, COMMONS.pool, JAVAX.transaction, JBI, LOG4J, WSDL4J, XERCES
    package :jar
  end

  desc "ODE JCA Resource Archive"
  define "jca-ra" do
    compile.with project("ode:utils"), JAVAX.connector
    package :jar
  end

  desc "ODE JCA Server"
  define "jca-server" do
    compile.with project("ode:jca-ra"), project("ode:utils"),
      COMMONS.logging
    package :jar
  end

  desc "ODE Tools"
  define "tools" do
    compile.with project("ode:bpel-compiler"), project("ode:utils"),
      ANT, COMMONS.httpclient, COMMONS.logging
    package :jar
  end

  desc "ODE Tools Binaries"
  define "tools-bin" do
    # Copy binary files over, set permissions on Linux files.
    bins = file(path_to(:target_dir, "bin")=>path_to(:src_dir, "main/dist/bin")) do |task|
      filter(task.prerequisites).into(File.dirname(task.name)).invoke
      chmod 0755, FileList[task.name + "/*"].collect.exclude("**/*.bat"), :verbose=>false
    end
    # Copy docs over.
    docs = filter(path_to(:src_dir, "main/dist/doc/*")).into(path_to(:target_dir, "doc"))

    build bins, docs
    package :zip, :include=>[path_to(:target_dir, "bin"), path_to(:target_dir, "doc")]
  end

  desc "ODE Utils"
  define "utils" do
    compile.with COMMONS.logging, COMMONS.pool, LOG4J, XERCES, JAVAX.stream
    package :jar
  end

end

desc "Deploys a process in the running Jetty daemon (started using jetty:bounce)."
task("jetty:process") do
  fail "A process should be provided by specifying PROCESS=/path/to/process." unless ENV['PROCESS']

  options = project("ode:axis2-war").task("jetty:bounce").options
  res = Jetty.jetty_call('/war', :get, options)
  case res
  when Net::HTTPSuccess
    # Copying process dir
    process_target = res.body.chomp + '/webapp/WEB-INF/processes'
    process_source = options[:process_alias][ENV['PROCESS']] || ENV['PROCESS']
    verbose { puts "Copying #{process_source} to #{process_target} " }
    cp_r(process_source, process_target)

    # Removing marker files to force redeploy
    rm Dir.glob("#{process_target}/*.deployed")

    puts "Process deployed."
  else
    puts "Unknown response from server: #{res}"
  end
end

# Lazy ass aliasing
task("jetty:bounce" => ["ode:axis2-war:jetty:bounce"])
task("jetty:shutdown" => ["ode:axis2-war:jetty:shutdown"])

