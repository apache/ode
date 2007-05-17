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

module Clover

  REQUIRES = Buildr.artifacts("clover:clover:jar:1.3.11")
  DATABASE = "clover.db"

  task "clover" do
    Buildr.projects.each do |project|
      unless project.compile.sources.empty?
        instrumented = project.file("target/clover")
        instrumented.enhance project.compile.sources do |task|
          args = ["-i", DATABASE, "-d", task.to_s, "-jdk15"]
          args.concat task.prerequisites.each { |src| file(src).invoke }.
            map { |src| FileList[File.join(src.to_s, "**/*.java")] }.flatten
          args << "-verbose" if Rake.application.options.trace
          args << { :classpath=>REQUIRES }
          Buildr::Java.java "com.cenqua.clover.CloverInstr", *args
        end
        file DATABASE=>instrumented
        project.compile.sources = [instrumented]
        project.compile.with REQUIRES
        project.test.with REQUIRES
      end
    end
  end

  namespace "clover" do

    task "html"=>file("clover.db") do
      Buildr::Java.java "com.cenqua.clover.reporters.html.HtmlReporter", "-i", DATABASE, "-o", "clover", :classpath=>REQUIRES
    end

  end

end
