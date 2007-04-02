module Buildr
  class JBITask < ZipTask

    class Component
      attr_accessor :name
      attr_accessor :type
      attr_accessor :description
      attr_accessor :delegation
      attr_accessor :class_name
      attr_writer   :libs
      def libs()
        @libs ||= []
      end
    end

    class Bootstrap
      attr_accessor :delegation
      attr_accessor :class_name
      attr_writer   :libs
      def libs()
        @libs ||= []
      end
    end

    def []=(key, value)
      case key.to_sym
      when :name, :description, :type
        self.component.send "#{name}=", value
      when :component
        self.component value
      when :bootstrap
        self.bootstrap value
      else
        super key, value
      end
      value
    end

    def component(args = nil)
      @component ||= Component.new
      args.each { |k, v| @component.send "#{k}=", v } if args
      @component
    end

    def bootstrap(args = nil)
      @bootstrap ||= Bootstrap.new
      args.each { |k, v| @bootstrap.send "#{k}=", v } if args
      @bootstrap
    end

  protected

    def create(zip)
      zip.mkdir "META-INF"
      zip.file.open("META-INF/jbi.xml", "w") { |output| output.write descriptor }
      path("lib").include component.libs.flatten, bootstrap.libs.flatten
      super zip
    end

    def descriptor()
      delegation = lambda { |key| "#{key || :parent}-first" }
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
          xml.tag! "component-class-name", component.class_name
          xml.tag! "component-class-path" do
            component.libs.each { |lib| xml.tag! "path-element", lib.to_s }
          end
          xml.tag! "bootstrap-class-name", bootstrap.class_name
          xml.tag! "bootstrap-class-path" do
            bootstrap.libs.each { |lib| xml.tag! "path-element", lib.to_s }
          end
        end
      end
    end

  end

  class Project

    def package_as_jbi(args)
      args[:type] = :zip
      file_name = args[:file] || path_to(:target_dir, Artifact.hash_to_file_name(args))
      unless Rake::Task.task_defined?(file_name)
        JBITask.define_task(file_name).tap { |task| package_extend task, args }
      end
      file(file_name).tap { |task| task.include args[:include] if args[:include] }
    end
  end
end

