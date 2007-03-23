require "open3"

module Derby
  VERSION   = "10.1.2.1"
  REQUIRES  = [ "org.apache.derby:derby:jar:#{VERSION}", "org.apache.derby:derbytools:jar:#{VERSION}" ]

  def self.create(args)
    db, prereqs = Rake.application.resolve_args(args)
    file(db=>prereqs) do |task|
      cmd = [ Java.path_to_bin, "-cp", artifacts(REQUIRES).join(File::PATH_SEPARATOR), "org.apache.derby.tools.ij" ]
      Open3.popen3(*cmd) do |stdin, stdout, stderr|
        # Shutdown so if a database already exists, we can remove it.
        stdin.puts "connect 'jdbc:derby:;shutdown=true';"
        rm_rf task.to_s if File.exist?(task.to_s)
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
      # Copy the SQL files into the database directory.
      filter(task.prerequisites).into(db).invoke
      # Tell other tasks we're refreshed, this also prevents running task
      # due to time differences between parent directory and db directory.
      touch task.to_s 
    end
  end
end      
