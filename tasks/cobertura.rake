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

module Cobertura

  class << self

    REQUIRES = ["cobertura:cobertura:jar:1.8", "log4j:log4j:jar:1.2.9", "asm:asm:jar:2.2.1", "oro:oro:jar:2.0.8"]

    def requires()
      @requires ||= Buildr.artifacts(REQUIRES).each(&:invoke).map(&:to_s)
    end

    def ant_project()
      @ant_project ||= Buildr::Ant.executable("cobertura") { |ant|
        ant.taskdef(:classpath=>requires.join(File::PATH_SEPARATOR), :resource=>"tasks.properties" ) }
    end

    def report_to(file = nil)
      File.expand_path(File.join(*["cobertura", file.to_s].compact))
    end

    def data_file()
      File.expand_path("cobertura.ser")
    end

  end

  namespace "cobertura" do

    task "instrument" do
      Buildr.projects.each do |project|
        unless project.compile.sources.empty?
          # Instrumented bytecode goes in a different directory. This task creates before running the test
          # cases and monitors for changes in the generate bytecode.
          instrumented = project.file(project.project.path_to(:target, "instrumented")=>project.compile.target) do |task|
            ant_project.send "cobertura-instrument", :todir=>task.to_s, :datafile=>data_file do
              fileset(:dir=>project.compile.target.to_s) { include :name=>"**/*.class" }
            end
            touch task.to_s, :verbose=>false
          end
          # We now have two target directories with bytecode. It would make sense to remove compile.target
          # and add instrumented instead, but apparently Cobertura only creates some of the classes, so
          # we need both directories and instrumented must come first.
          project.test.junit.classpath.unshift file(instrumented)
          project.test.junit.with requires
          project.clean { rm_rf instrumented.to_s, :verbose=>false }
        end
      end
    end

    desc "Run the test cases and produce code coverage reports in #{report_to(:html)}"
    task "html"=>["instrument", "build"] do
      puts "Creating test coverage reports in #{report_to(:html)}"
      projects = Buildr.projects
      ant_project.send "cobertura-report", :destdir=>report_to(:html), :format=>"html", :datafile=>data_file do
        projects.map(&:compile).map(&:sources).flatten.each do |src|
          fileset(:dir=>src.to_s) { include :name=>"**/*.java" } if File.exist?(src.to_s)
        end
      end
    end

    desc "Run the test cases and produce code coverage reports in #{report_to(:xml)}"
    task "xml"=>["instrument", "build"] do
      puts "Creating test coverage reports in #{report_to(:xml)}"
      projects = Buildr.projects
      ant_project.send "cobertura-report", :destdir=>report_to(:xml), :format=>"xml", :datafile=>data_file do
        projects.map(&:compile).map(&:sources).flatten.each do |src|
          fileset :dir=>src.to_s if File.exist?(src.to_s)
        end
      end
    end

    task "clean" do
      rm_rf [report_to, data_file], :verbose=>false
    end
  end

  task "clean" do
    task("cobertura:clean").invoke if Dir.pwd == Rake.application.original_dir
  end

end
