#
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

module Hibernate

  REQUIRES = Buildr.struct(
    :collections  => "commons-collections:commons-collections:jar:3.1",
    :logging      => "commons-logging:commons-logging:jar:1.0.3",
    :dom4j        => "dom4j:dom4j:jar:1.6.1",
    :hibernate    => "org.hibernate:hibernate:jar:3.1.2",
    :xdoclet      => Buildr.group("xdoclet", "xdoclet-xdoclet-module", "xdoclet-hibernate-module",
                                :under=>"xdoclet", :version=>"1.2.3") + ["xdoclet:xjavadoc:jar:1.1-j5"]
  )

  class << self
    include Buildr::Ant

    # Uses XDoclet to generate HBM files form annotated source files.
    # Options include:
    # * :sources -- Directory (or directories) containing source files.
    # * :target -- The target directory.
    # * :excludetags -- Tags to exclude (see HibernateDocletTask)
    #
    # For example:
    #  Java::Hibernate.xdoclet :sources=>compile.sources,
    #    :target=>compile.target, :excludedtags=>"@version,@author,@todo"
    def xdoclet(options)
      ant("hibernatedoclet") do |doclet|
        doclet.taskdef :name=>"hibernatedoclet", :classname=>"xdoclet.modules.hibernate.HibernateDocletTask", :classpath=>requires
        doclet.hibernatedoclet :destdir=>options[:target].to_s, :excludedtags=>options[:excludedtags], :force=>"true" do
          hibernate :version=>"3.0"
          options[:sources].to_a.each do |source|
            fileset :dir=>source.to_s, :includes=>"**/*.java"
          end
        end
      end
    end

    # Returns a new AntProject that supports the schemaexport task.
    def schemaexport(name = "schemaexport")
      ant(name) do |export|
        export.taskdef :name=>"schemaexport", :classname=>"org.hibernate.tool.hbm2ddl.SchemaExportTask", :classpath=>requires
      end
    end

    # Returns an new task with an accessor (ant) to an AntProject that supports
    # the schemaexport task.
    #
    # For example:
    #   Java::Hibernate.schemaexport_task.enhance do |task|
    #     task.ant.schemaexport :properties=>"derby.properties", :output=>"derby.sql",
    #       :delimiter=>";", :drop=>"no", :create=>"yes" do
    #       fileset(:dir=>path_to(:java_src_dir)) { include :name=>"**/*.hbm.xml" } }
    #     end
    #   end
    def schemaexport_task(name = "schemaexport")
      unless Rake::Task.task_defined?(name)
        class << task(name) ; attr_accessor :ant ; end
        task(name).enhance { |task| task.ant = schemaexport(name) }
      end
      task(name)
    end

  protected

    # This will download all the required artifacts before returning a classpath, and we want to do this only once.
    def requires()
      @requires ||= Buildr.artifacts(REQUIRES.to_a).each(&:invoke).map(&:to_s).join(File::PATH_SEPARATOR)
    end

  end

end

class Project

  def hibernate_doclet(options = {})
    if options[:package]
      depends = compile.sources.map { |src| FileList[File.join(src.to_s, options[:package].gsub(".", "/"), "*.java")] }.flatten
    else
      depends = compile.sources.map { |src| FileList[File.join(src.to_s, "**/*.java")] }.flatten
    end
    file("target/hbm.timestamp"=>depends) do |task|
      Hibernate.xdoclet({ :sources=>compile.sources, :target=>compile.target }.merge(options))
      write task.name
    end
  end

end
