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

module H2

  REQUIRES = "com.h2database:h2:jar:1.1.117"

  #Java.classpath << REQUIRES

  class << self

    # Returns a task that will create a new Derby database. The task name is the path to
    # the derby database. The prerequisites are all the SQL files for inclusion in the database.
    #
    # For example:
    #   H2.create "mydb"=>derby.sql
    def create(dbname, args)
      db, prereqs = args.keys.first, args.values.first
      targetDir=File.expand_path(db)
      file(targetDir=>prereqs) do |task|
        rm_rf dbname if File.exist?(dbname)
        Java::Commands.java "org.h2.tools.RunScript", "-url", "jdbc:h2:file:"+targetDir+File::Separator+dbname+";DB_CLOSE_ON_EXIT=false;user=sa", "-showResults", "-script", prereqs, :classpath => REQUIRES 
        #Buildr.filter(prereqs).into(dbname).run
        #touch task.name, :verbose=>false
      end
    end
  end
end
