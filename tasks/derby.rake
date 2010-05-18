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

module Derby

  REQUIRES = Buildr.group("derby", "derbytools", :under=>"org.apache.derby", :version=>"10.1.2.1")

  Java.classpath << REQUIRES

  class << self

    # Returns a task that will create a new Derby database. The task name is the path to
    # the derby database. The prerequisites are all the SQL files for inclusion in the database.
    #
    # For example:
    #   Derby.create "mydb"=>derby.sql
    def create(args)
      db, prereqs = args.keys.first, args.values.first
      file(File.expand_path(db)=>prereqs) do |task|
        rm_rf task.name if File.exist?(task.name)
        Buildr.ant("derby") do |ant|
          sqls = task.prerequisites.map(&:to_s)
          ant.sql :driver=>"org.apache.derby.jdbc.EmbeddedDriver", :url=>"jdbc:derby:#{task.to_s};create=true",
            :userid=>"sa", :password=>"", :autocommit=>"on" do
            sqls.each { |sql| ant.transaction :src=>sql }
          end
        end
        # Copy the SQL files into the database directory.
        Buildr.filter(prereqs).into(task.name).run
        touch task.name, :verbose=>false
      end
    end

  end

end
