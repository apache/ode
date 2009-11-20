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

# This task creates an OSGi bundle package using the bnd tool. 
#
# The classpath and path to a bnd configuration file are required. 
# Additional properties can be specified using the bnd.properties
# hash. Refer to the bnd documentation (http://www.aqute.biz/Code/Bnd)
# for details on the supported properties. 
#
# The easiest way to use this task is through the Project#package method. 
# For example:
#   package(:bundle).tap do |bnd|
#     bnd.bnd_file = 'conf/foo.bnd'
#     bnd.classpath = artifacts(...)
#     bnd.properties['foo'] = 'bar'
#   end
class BndTask < Rake::FileTask

  BND = "biz.aQute:bnd:jar:0.0.379"

  # Classpath string for building the bundle
  attr_accessor :classpath

  # Path to bnd file
  attr_accessor :bnd_file
  
  # Hash of properties passed to bnd
  attr_accessor :properties

  def initialize(*args)
    super
    @properties = {}

    # Make sure bnd tool is available
    Buildr.artifact(BND).invoke

    enhance do 
      Buildr.ant('bnd') do |project|
        
        # pass properties to bnd as ant properties
        properties.each do |key, value|
          project.property(:name=>key, :value=>value)
        end
        
        project.taskdef :name=>'bnd', :classname=>'aQute.bnd.ant.BndTask', :classpath=>Buildr.artifact(BND)
        project.bnd(:classpath=>classpath, :files=>File.expand_path(bnd_file), :output=>name, 
                    :eclipse=>false, :failok=>false, :exceptions=>true) 
      end
    end
  end
  
  
end


class Project

  def package_as_bundle(file_name) #:nodoc
    BndTask.define_task(file_name)
  end
  
  def package_as_bundle_spec(spec) #:nodoc
    spec.merge({ :type=>:jar, :classifier=>'bundle' })
  end
  
end
