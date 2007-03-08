require "buildr/lib/buildr.rb"
require "open3"

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
BACKPORT            = "backport-util-concurrent:backport-util-concurrent:jar:2.1"
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
  :kernel           =>"geronimo:geronimo-kernel:jar:1.1",
  :transaction      =>"geronimo:geronimo-transaction:jar:1.1"
)
HIBERNATE           = "org.hibernate:hibernate:jar:3.1.2"
HSQLDB              = "hsqldb:hsqldb:jar:1.8.0.7"
JAVAX               = OpenStruct.new(
  :activation       =>"javax.activation:activation:jar:1.1",
  #:activation       =>"geronimo-spec:geronimo-spec-activation:jar:1.0.2-rc4",
  :connector        =>"org.apache.geronimo.specs:geronimo-j2ee-connector_1.5_spec:jar:1.0",
  :javamail         =>"geronimo-spec:geronimo-spec-javamail:jar:1.3.1-rc5",
  :jms              =>"geronimo-spec:geronimo-spec-jms:jar:1.1-rc4",
  :persistence      =>"javax.persistence:persistence-api:jar:1.0",
  :servlet          =>"org.apache.geronimo.specs:geronimo-servlet_2.4_spec:jar:1.0",
  :stream           =>"stax:stax-api:jar:1.0",
  :transaction      =>"org.apache.geronimo.specs:geronimo-jta_1.0.1B_spec:jar:1.0"
)
JAXEN               = "jaxen:jaxen:jar:1.1-beta-8"
JENCKS              = "org.jencks:jencks:jar:all:1.3"
JIBX                = "jibx:jibx-run:jar:1.1-beta3"
JOTM                = [ "jotm:jotm:jar:2.0.10", "jotm:jotm_jrmp_stubs:jar:2.0.10",
                        "org.objectweb.carol:carol:jar:2.0.5", "howl:howl-logger:jar:0.1.11" ]
LOG4J               = "log4j:log4j:jar:1.2.13"
OPENJPA             = ["org.apache.openjpa:openjpa-all:jar:0.9.7-incubating-SNAPSHOT", 
                       "net.sourceforge.serp:serp:jar:1.12.0"]
QUARTZ              = "quartz:quartz:jar:1.5.2"
SAXON               = group("saxon", "saxon-xpath", "saxon-dom", :under=>"net.sf.saxon", :version=>"8.7")
WOODSTOX            = "woodstox:wstx-asl:jar:3.0.1"
WSDL4J              = "wsdl4j:wsdl4j:jar:1.5.2"
XALAN               = "org.apache.ode:xalan:jar:2.7.0"
XERCES              = "xerces:xercesImpl:jar:2.8.0"
XSTREAM             = "xstream:xstream:jar:1.2"
WS_COMMONS          = OpenStruct.new(
  :axiom            =>AXIOM,
  :neethi           =>"org.apache.ws.commons.neethi:neethi:jar:2.0",
  :xml_schema       =>"org.apache.ws.commons.schema:XmlSchema:jar:1.2"
)
XMLBEANS            = "xmlbeans:xbean:jar:2.2.0"


repositories.remote[:central] = "http://pxe.intalio.org/public/maven2"
repositories.remote[:apache_incubator]="http://people.apache.org/repo/m2-incubating-repository"
repositories.remote[:maven_central]="http://repo1.maven.org/maven2"
repositories.deploy_to[:url] ||= "sftp://ode.intalio.org/var/www/public/maven2"

define "ode", :group=>"org.apache.ode", :version=>VERSION_NUMBER do

  compile.options.source = "1.5"
  compile.options.target = "1.5"

  define "axis2" do
    compile.with project("ode:bpel-api"), project("ode:bpel-connector"),
      project("ode:bpel-dao"), project("ode:bpel-epr"),
      project("ode:bpel-runtime"), project("ode:bpel-scheduler-quartz"),
      project("ode:bpel-schemas"), project("ode:bpel-store"),
      project("ode:minerva"), project("ode:utils"),
      AXIOM, AXIS2, COMMONS.logging, COMMONS.collections,
      DERBY, GERONIMO.kernel, GERONIMO.transaction,
      JAVAX.activation, JAVAX.servlet, JAVAX.stream,
      JAVAX.transaction, JENCKS, JOTM, WSDL4J, XMLBEANS

    tests.compile.with *compile.classpath
    tests.compile.with project("ode:tools")
    
    package :jar
  end

  define "axis2-war" do
    libs = project("ode:axis2"), project("ode:bpel-api"),
      project("ode:bpel-compiler"), project("ode:bpel-dao"), 
      project("ode:bpel-epr"), project("ode:bpel-obj"),
      project("ode:bpel-ql"), project("ode:bpel-runtime"),
      project("ode:bpel-scheduler-quartz"), project("ode:bpel-schemas"),
      project("ode:bpel-store"),
      project("ode:dao-hibernate"), project("ode:jacob"),
      project("ode:minerva"), project("ode:utils"),
      project("ode:dao-jpa"), project("ode:dao-jpa-ojpa-derby"),
      AXIS2_ALL, ANNONGEN, BACKPORT, COMMONS.codec,
      COMMONS.collections, COMMONS.fileupload, COMMONS.httpclient, 
      COMMONS.lang, DERBY, DERBY_TOOLS,
      JAVAX.activation, JAVAX.javamail, JAVAX.connector, JAVAX.jms,
      JAVAX.persistence, JAVAX.transaction, JAVAX.stream, JENCKS, JIBX,
      JOTM, GERONIMO.kernel, GERONIMO.transaction, OPENJPA, QUARTZ, WOODSTOX, WSDL4J,
      WS_COMMONS.axiom, WS_COMMONS.neethi, WS_COMMONS.xml_schema,
      XALAN, XERCES

    resources do |task|
      if compile.compiled?
        unzip(artifact("#{group}:ode-dao-jpa-ojpa-derby:zip:#{version}")).
          into(path_to(:target_dir, "resources")).invoke
      end
      #untar(artifact("#{group}:ode-dao-hibernate-db-derby:tar:#{version}")).
      #  into(path_to(:target_dir, "resources")),
    end
    resources(
      filter(path_to(:base_dir, "../axis2/src/main/wsdl/*")).into(path_to(:target_dir, "resources")),
      filter(path_to(:base_dir, "../bpel-schemas/src/main/xsd/pmapi.xsd")).into(path_to(:target_dir, "resources"))
    )
    
    package(:war).with(:libs=>libs, :manifest=>false).
      path("WEB-INF").include(path_to(:target_dir, "resources/*"))
  end

  define "bpel-api" do
    compile.with project("ode:utils"), project("ode:bpel-obj"),
      project("ode:bpel-schemas"), WSDL4J
    package :jar
  end

  define "bpel-api-jca" do
    compile.with project("ode:bpel-api"), JAVAX.connector
    package :jar
  end

  define "bpel-compiler" do
    compile.with project("ode:bpel-api"), project("ode:bpel-obj"),
      project("ode:bpel-schemas"), project("ode:bpel-scripts"),
      project("ode:utils"),
      COMMONS.logging, JAXEN, SAXON, WSDL4J, XALAN, XERCES
    package :jar
  end

  define "bpel-connector" do
    compile.with project("ode:bpel-api"), project("ode:bpel-api-jca"),
      project("ode:bpel-runtime"), project("ode:jca-ra"), project("ode:jca-server")
    package :jar
  end

  define "bpel-dao" do
    compile.with project("ode:bpel-api")
    package :jar
  end

  define "bpel-epr" do
    compile.with project("ode:utils"), project("ode:bpel-dao"),
      project("ode:bpel-api"), project("ode:minerva"),
      COMMONS.logging, DERBY, JAVAX.transaction
    package :jar
  end

  define "bpel-obj" do
    compile.with project("ode:utils"), SAXON, WSDL4J
    package :jar
  end

  define "bpel-ql" do
    jjtree_src = path_to(:src_dir, "main/jjtree") 
    jjtree_out = path_to(:target_dir, "generated/jjtree")
    javacc_out = path_to(:target_dir, "generated/javacc")

    prepare JavaCC.jjtree_task(path_to(jjtree_out, "org/apache/ode/ql/jcc")=>jjtree_src).using(:build_node_files=>false)
    prepare JavaCC.javacc_task(path_to(javacc_out, "org/apache/ode/ql/jcc")=>jjtree_out)

    compile.with project("ode:bpel-api"), project("ode:bpel-compiler"),
      project("ode:bpel-obj"), project("ode:jacob"), project("ode:utils"),
    compile.sources << jjtree_out << javacc_out
      
    package :jar
  end

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
    compile.sources << generated

    tests.resources do |task| 
      #rm_rf path_to(:test_target_dir, "derby-db")
      if tests.compile.compiled?
        unzip(artifact("#{group}:ode-dao-jpa-ojpa-derby:zip:#{version}")).into(path_to(:test_target_dir, "derby-db")).invoke
      end
    end
    tests.compile.with *compile.classpath
    tests.compile.with project("ode:bpel-scheduler-quartz"),
      project("ode:dao-jpa"), project("ode:minerva"),
      COMMONS.pool, COMMONS.lang, DERBY, JAVAX.connector, JAVAX.transaction,
      JOTM, LOG4J, XERCES, XSTREAM, OpenJPA::REQUIRES, QUARTZ, XALAN

    package :jar
  end

  define "bpel-scheduler-quartz" do
    compile.with project("ode:bpel-api"), project("ode:utils"),
      COMMONS.collections, COMMONS.logging, JAVAX.transaction, QUARTZ
    package :jar
  end

  define "bpel-schemas" do
    schemas = [ path_to(:src_dir, "main/xsd/pmapi.xsdconfig"),
                path_to(:src_dir, "main/xsd/dd.xsdconfig"), path_to(:src_dir, "main/xsd") ]
    prepare XMLBeans.compile_task(path_to(:target_dir, "generated")=>schemas).
      using(:javasource=>compile.options.source,
            :classes=>path_to(:java_target_dir),
            :jar=>path_to(:target_dir, "xmlbeans.jar"))
            
    package :jar
  end

  define "bpel-scripts" do
    package :jar
  end

  define "bpel-store" do
    compile.with project("ode:bpel-api"), project("ode:bpel-compiler"),
      project("ode:bpel-dao"), project("ode:bpel-obj"), project("ode:bpel-schemas"),
      project("ode:dao-hibernate"), project("ode:utils"),
      COMMONS.logging, JAVAX.persistence, JAVAX.stream,
      HIBERNATE, HSQLDB, XMLBEANS, XERCES, WSDL4J

    build do |task|
      # Only enhance if any class files were compiled.
      if compile.compiled?
        OpenJPA.enhance(:output=>compile.target, :classpath=>compile.classpath,
          :properties=>path_to(:resources_dir, "META-INF/persistence.xml"))
      end
    end

    package :jar
  end

  define "bpel-test" do
    compile.with project("ode:bpel-api"), project("ode:bpel-runtime"),
      project("ode:bpel-store"), project("ode:utils"),
      DERBY, WSDL4J
    package :jar
  end

  define "dao-hibernate" do
    compile.with project("ode:bpel-api"), project("ode:bpel-dao"),
      project("ode:bpel-ql"), project("ode:utils"),
      COMMONS.lang, COMMONS.logging, JAVAX.transaction, HIBERNATE, DOM4J
    package :jar
  end

  define "dao-jpa" do
    compile.with project("ode:bpel-api"), project("ode:bpel-dao"), project("ode:utils"),
      COMMONS.logging, JAVAX.persistence, JAVAX.transaction, OPENJPA
    package :jar
  end

  define "dao-jpa-ojpa" do
    compile.with project("ode:bpel-api"), project("ode:dao-jpa"), project("ode:bpel-dao"),
      COMMONS.collections, JAVAX.persistence, JAVAX.transaction,
      OPENJPA, XERCES

    build do |task|
      if compile.compiled?
        OpenJPA.enhance :output=>compile.target, :classpath=>compile.classpath,
          :properties=>path_to(:resources_dir, "META-INF/persistence.xml" )
      end
    end
    package :jar
  end

  define "dao-jpa-ojpa-derby" do
    # Create the Derby SQL file using the OpenJPA mapping tool, and
    # append the Quartz DDL at the end.
    derby_xml = path_to(:src_dir, "main/descriptors/persistence.derby.xml")
    quartz_sql = path_to(:src_dir, "main/scripts/quartz-derby.sql")
    derby_sql = file(path_to(:target_dir, "derby.sql")=>[derby_xml, quartz_sql]) do |task|
      mkpath path_to(:target_dir)
      OpenJPA.mapping_tool :properties=>derby_xml, :action=>"build", :sql=>task.name,
        :classpath=>[ project("ode:bpel-store"), project("ode:dao-jpa"),
                      project("ode:bpel-api"), project("ode:bpel-dao"),
                      project("ode:utils") ]
      File.open(task.name, "a") { |file| file.write File.read(quartz_sql) }
    end

    # Generate the derby database using the DDL.
    derby_db = file(path_to(:target_dir, "derby")=>derby_sql) do |task|
      cmd = [ Java.path_to_bin("java") ]
      cmd << "-cp" << artifacts(DERBY, DERBY_TOOLS).join(File::PATH_SEPARATOR)
      cmd << "org.apache.derby.tools.ij"
      Open3.popen3(*cmd) do |stdin, stdout, stderr|
        # Shutdown so if a database already exists, we can remove it.
        stdin.puts "connect 'jdbc:derby:;shutdown=true';"
        rm_rf path_to(:target, "derby") rescue nil
        # Create a new database, and load all the prerequisites.
        stdin.puts "connect 'jdbc:derby:#{task.name}/jpadb;create=true;user=sa'"
        stdin.puts "set schema sa"
        stdin.puts "autocommit on;"
        #puts "Going to write prereqs: #{prereq.to_s}"
        task.prerequisites.each { |prereq| stdin.write File.read(prereq.to_s) }
        # Exiting will shutdown the database so we can copy the files around.
        stdin.puts "exit"
        stdin.close
        # Helps when dignosing SQL errors.
        returning(stdout.read) { |output| puts output if Rake.application.options.trace }
      end
      # Copy the SQL files into the database directory.
      filter(task.prerequisites).into("#{task.name}/jpadb").invoke
      # Tell other tasks we're refreshed, this also prevents running task
      # due to time differences between derby and jpadb directories.
      touch task.name 
    end

    build derby_db
    package :zip, :include=>path_to(:target_dir, "derby/*")
  end

  define "distro-axis2" do
    resources(
      filter(["LICENSE", "NOTICE", "DISCLAIMER"].map { |f| path_to("..", f) }).into(path_to(:target_dir, "stage")),
      filter(path_to(:src_dir, "examples")).into(path_to(:target_dir, "stage"))
    )

    package(:zip).include(path_to(:target_dir, "stage/*"))
    package(:zip).include(COMMONS.logging, COMMONS.codec, COMMONS.httpclient,
      COMMONS.pool, COMMONS.collections, JAXEN,
      SAXON, LOG4J, WSDL4J, :path=>"lib")
    package(:zip).include("#{group}:ode-axis2-war:war:#{version}", :as=>"ode.war")
    package(:zip).merge("#{group}:ode-tools-bin:zip:#{version}")
    [ "ode-utils", "ode-tools", "ode-bpel-compiler",
      "ode-bpel-api", "ode-bpel-obj", "ode-bpel-schemas" ].each do |name|
      artifact = artifact("#{group}:#{name}:jar:#{version}")
      package(:zip).include(artifact, :as=>"#{artifact.id}.#{artifact.type}", :path=>"lib")
    end
  end

  define "jacob" do
    compile.with project("ode:utils"), project("ode:jacob-ap"),
      COMMONS.logging, XSTREAM

    # Prepare before compile, but we use the same classpath,
    # so define this after compile.with.
    generated = path_to(:target_dir, "generated/apt")
    prepare Java.apt_task(generated=>path_to(:java_src_dir)).
      using(:classpath=>compile.classpath, :source=>compile.options.source)
    # Include the generated sources.
    compile.sources << generated

    package :jar
  end

  define "jacob-ap" do
    compile.with File.join(ENV['JAVA_HOME'], "lib/tools.jar")
    package :jar
  end

  define "jca-ra" do
    compile.with project("ode:utils"), JAVAX.connector
    package :jar
  end

  define "jca-server" do
    compile.with project("ode:jca-ra"), project("ode:utils"),
      COMMONS.logging
    package :jar
  end

  define "minerva" do
    compile.with COMMONS.logging, JAVAX.connector, JAVAX.transaction
    package :jar
  end

  define "tools" do
    compile.with project("ode:bpel-compiler"), project("ode:utils"),
      ANT, COMMONS.httpclient, COMMONS.logging
    package :jar
  end

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

  define "utils" do
    compile.with COMMONS.logging, COMMONS.pool, LOG4J, XERCES
    package :jar
  end

end
