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

# This task creates a JBI package based on the component/bootstrap specification.
# It extends ZipTask, and all its over lovely options.
#
# The easiest way to use this task is through the Project#package method. For example:
#   package(:jbi).tap do |jbi|
#     jbi.component :type=>:service_engine=>"MyEngine", :description=>self.comment
#     jbi.component :class_name=>"com.example.MyComponent", :delegation=>:self, :libs=>libs
#     jbi.bootstrap :class_name=>"com.example.MyBootstrap", :delegation=>:parent, :libs=>libs
#   end
class JBITask < Buildr::ZipTask

  # Specifies the name of a jbi.xml file to use, or a Proc/Method returning
  # the contents of jbi.xml. Leave empty if you want to use #component and
  # bootstrap instead.
  attr_accessor :jbi_xml

  # Component specification.
  class Component
    # Component name.
    attr_accessor :name
    # Component type, e.g. :service_engine.
    attr_accessor :type
    # Description of component.
    attr_accessor :description
    # Delegation method. Default is :parent.
    attr_accessor :delegation
    # Component class name.
    attr_accessor :class_name
    # Array of libraries used by component.
    attr_accessor :libs

    def initialize()
      @libs = []
    end
  end

  # Bootstrap specification.
  class Bootstrap
    # Delegation method. Default is :parent.
    attr_accessor :delegation
    # Bootstrap class name.
    attr_accessor :class_name
    # Array of libraries used for bootstrapping.
    attr_accessor :libs

    def initialize()
      @libs = []
    end
  end

  def initialize(*args)
    super
    prepare { path("lib").include((component.libs + bootstrap.libs).flatten.uniq) }
      
    enhance do
      case jbi_xml
      when String
        path("META-INF").include jbi_xml.path, :as=>"jbi.xml" 
      when nil, true
        # Tempfiles gets deleted on garbage collection, so we're going to hold on to it
        # through instance variable not closure variable.
        Tempfile.open("MANIFEST.MF") { |@jbi_xml_tmp| @jbi_xml_tmp.write descriptor }
        path("META-INF").include @jbi_xml_tmp.path, :as=>"jbi.xml" 
      when Proc, Method
        Tempfile.open("MANIFEST.MF") { |@jbi_xml_tmp| @jbi_xml_tmp.write jbi_xml.call.to_s }
        path("META-INF").include @jbi_xml_tmp.path, :as=>"jbi.xml" 
      end
    end
  end

  def []=(key, value)
    case key.to_sym
    when :name, :description, :type
      self.component.send "#{name}=", value
    when :component, :bootstrap
      self.send key, value
    else
      super key, value
    end
    value
  end

  # Returns the component specification for this JBI package.
  # You can call accessor methods to configure the component
  # specification, you can also pass a hash of settings, for example:
  #   jbi.component :type=>:service_engine, :name=>"MyEngine"
  def component(args = nil)
    (@component ||= Component.new).tap do |component|
      args.each { |k, v| component.send "#{k}=", v } if args
    end
  end

  # Returns the bootstrap specification for this JBI package.
  # You can call accessor methods to configure the bootstrap
  # specification, you can also pass a hash of settings, for example:
  #   jbi.bootstrap :class_name=>"com.example.jbi.MyBootstrap", :libs=>libs
  def bootstrap(args = nil)
    (@bootstrap ||= Bootstrap.new).tap do |bootstrap|
      args.each { |k, v| bootstrap.send "#{k}=", v } if args
    end
  end

  # Create a JBI descriptor (jbi.xml) from the component/bootstrap specification.
  def descriptor()
    delegation = lambda { |key| "#{key || :parent}-first" }
    path_elements = lambda do |xml, libs|
      libs.each { |lib| xml.tag! "path-element", "lib/#{lib.to_s.pathmap('%f')}" }
    end
    xml = Builder::XmlMarkup.new(:indent=>2)
    xml.instruct!
    xml.jbi :xmlns=>"http://java.sun.com/xml/ns/jbi", :version=>"1.0" do
      xml.component :type=>component.type.to_s.sub("_", "-"),
        "component-class-loader-delegation"=>delegation[component.delegation],
        "bootstrap-class-loader-delegation"=>delegation[bootstrap.delegation] do
        xml.identification do
          xml.name component.name
          xml.description component.description
        end
        xml.tag!("component-class-name", component.class_name)
        xml.tag!("component-class-path") { path_elements[xml, component.libs] }
        xml.tag!("bootstrap-class-name", bootstrap.class_name)
        xml.tag!("bootstrap-class-path") { path_elements[xml, bootstrap.libs] }
      end
    end
  end

end

class Project


  def package_as_jbi(file_name) #:nodoc
    JBITask.define_task(file_name)
  end

  def package_as_jbi_spec(spec) #:nodoc
    spec.merge({ :type=>:zip })
  end

end
