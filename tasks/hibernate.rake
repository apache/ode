module Buildr
  module Java
    module Hibernate

      REQUIRES = OpenObject.new
      REQUIRES.collections  = "commons-collections:commons-collections:jar:3.1"
      REQUIRES.logging      = "commons-logging:commons-logging:jar:1.0.3"
      REQUIRES.dom4j        = "dom4j:dom4j:jar:1.6.1"
      REQUIRES.hibernate    = "org.hibernate:hibernate:jar:3.1.2"
      REQUIRES.xdoclet      = [ "xdoclet:xdoclet-hibernate-module:jar:1.2.3", "xdoclet:xdoclet-xdoclet-module:jar:1.2.3",
                                "xdoclet:xdoclet:jar:1.2.3", "xdoclet:xjavadoc:jar:1.1-j5" ]

      class << self

        def xdoclet(options)
          ant("hibernatedoclet") do |ant|
            ant.taskdef :name=>"hibernatedoclet", :classname=>"xdoclet.modules.hibernate.HibernateDocletTask", :classpath=>requires
            ant.hibernatedoclet :destdir=>options[:target].to_s, :excludedtags=>options[:excludedtags], :force=>"true" do
              hibernate :version=>"3.0"
              fileset :dir=>options[:source].to_s, :includes=>options[:include]
            end
          end
        end

        def schemaexport(name = "schemaexport")
          ant(name) do |ant|
            ant.taskdef :name=>"schemaexport", :classname=>"org.hibernate.tool.hbm2ddl.SchemaExportTask", :classpath=>requires
          end
        end

        def schemaexport_task(name = "schemaexport")
          task(name).tap do |task|
            class << task ; attr_accessor :ant ; end
            task.enhance { |task| task.ant = schemaexport(name) }
          end
        end

      protected

        def requires()
          @requires ||= artifacts(REQUIRES.to_hash.values).each(&:invoke).map(&:to_s).join(File::PATH_SEPARATOR)
        end

      end

    end
  end
end
