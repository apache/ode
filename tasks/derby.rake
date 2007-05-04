module Derby

  REQUIRES = Buildr.group("derby", "derbytools", :under=>"org.apache.derby", :version=>"10.1.2.1")

  Java.rjb.onload { Java.rjb.classpath << REQUIRES  }

  class << self

    # Returns a task that will create a new Derby database. The task name is the path to
    # the derby database. The prerequisites are all the SQL files for inclusion in the database.
    #
    # For example:
    #   Derby.create "mydb"=>derby.sql
    def create(args)
      db, prereqs = Rake.application.resolve_args(args)
      file(File.expand_path(db)=>prereqs) do |task|
        rm_rf task.name if File.exist?(task.name)
        Ant.executable("derby") do |ant|
          sqls = task.prerequisites.map(&:to_s)
          ant.sql :driver=>"org.apache.derby.jdbc.EmbeddedDriver", :url=>"jdbc:derby:#{task.to_s};create=true",
            :userid=>"sa", :password=>"", :autocommit=>"on" do
            sqls.each { |sql| transaction :src=>sql }
          end
        end
        # Copy the SQL files into the database directory.
        Buildr.filter(prereqs).into(task.name).run
        touch task.name, :verbose=>false
      end
    end

  protected

    # This will download all the required artifacts before returning a classpath, and we want to do this only once.
    def requires()
      @requires ||= Buildr.artifacts(REQUIRES).each(&:invoke).map(&:to_s).join(File::PATH_SEPARATOR)
    end
  end
end
