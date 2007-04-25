require "open3"

module Derby

  REQUIRES = Buildr.group("derby", "derbytools", :under=>"org.apache.derby", :version=>"10.1.2.1")

  class << self

    # Returns a task that will create a new Derby database. The task name is the path to
    # the derby database. The prerequisites are all the SQL files for inclusion in the database.
    #
    # For example:
    #   Derby.create "mydb"=>derby.sql
    def create(args)
      db, prereqs = Rake.application.resolve_args(args)
      # Copy the SQL files into the database directory.
      file(File.expand_path(db)=>prereqs) do |task|
        cmd = [ Java.path_to_bin('java'), "-cp", requires, "org.apache.derby.tools.ij" ]
        Open3.popen3(*cmd) do |stdin, stdout, stderr|
          # Shutdown so if a database already exists, we can remove it.
          stdin.puts "connect 'jdbc:derby:;shutdown=true';"
          rm_rf task.name if File.exist?(task.name)
          # Create a new database, and load all the prerequisites.
          stdin.puts "connect 'jdbc:derby:#{task.to_s};create=true;user=sa'"
          stdin.puts "set schema sa"
          stdin.puts "autocommit on;"
          task.prerequisites.each { |prereq| stdin.write File.read(prereq.to_s) }
          # Exiting will shutdown the database so we can copy the files around.
          stdin.puts "exit"
          stdin.close
          # Helps when dignosing SQL errors.
          stdout.read.tap { |output| puts output if Rake.application.options.trace }
        end
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
