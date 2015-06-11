# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with this
# work for additional information regarding copyright ownership. The ASF
# licenses this file to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

module Buildr

    #
    # Module to add project dependencies to our artifact's pom files. 
    # Inspired by BUILDR-486 and https://github.com/jvshahid/buildr-dependency-extensions.
    #
    module PomWithDependencies
        include Extension

    # We have to add the dependencies to the monkey patched POM before the dependencies are
    # changed in the compile, test and run after_define
    after_define(:compile) do |project|
        project.package.pom.dependencies =
        [project.compile.dependencies.select {|dep| dep.respond_to?(:to_spec) && dep.respond_to?(:to_hash)}.map { |a| a.to_hash.merge(:scope => 'compile') },
            project.test.dependencies.select {|dep| dep.respond_to?(:to_spec) && dep.respond_to?(:to_hash) && !project.compile.dependencies.include?(dep)}.map { |a| a.to_hash.merge(:scope => 'test') },
            project.run.classpath.select {|dep| dep.respond_to?(:to_spec) && dep.respond_to?(:to_hash) && !project.compile.dependencies.include?(dep)}.map { |a| a.to_hash.merge(:scope => 'runtime') }
            ].flatten
        end
    end

    module ActsAsArtifact
        # monkey patch Buildr's the pom xml generation
        def pom_xml
            Proc.new do
                xml = Builder::XmlMarkup.new(:indent=>2)
                xml.instruct!
                xml.project do
                    xml.modelVersion  '4.0.0'
                    xml.groupId       group
                    xml.artifactId    id
                    xml.version       version
                    xml.classifier    classifier if classifier
                    unless @dependencies.nil? || @dependencies.empty?
                        xml.dependencies do
                            @dependencies.uniq.each do |art|
                                xml.dependency do
                                    xml.groupId       art[:group]
                                    xml.artifactId    art[:id]
                                    xml.version       art[:version]
                                    xml.classifier    art[:classifier] if art.has_key? :classifier
                                    xml.scope         art[:scope] if art.has_key? :scope
                                end
                            end
                        end
                    end
                end
            end
        end

        # make ActAsArtifac dependency aware
        def dependencies=(dependencies)
            @dependencies = dependencies
        end

        def dependencies
            @dependencies ||= POM.load(self).dependencies.map { |spec| artifact(spec) } if self.is_a? Artifact
            @dependencies ||= []
        end

    end
end

# use this module for all projects.
class Buildr::Project
    include Buildr::PomWithDependencies
end
