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

SETTINGS = "#{File.expand_path('.buildr', ENV['HOME'])}/settings.rb"

module NativeDB
#
  if File.exist? SETTINGS
    require SETTINGS
    Java.rjb.onload { Java.rjb.classpath << REQUIRES  }
  end

  class << self

    def create_dbs(buildr, base, orm)
      if File.exist? SETTINGS
        require SETTINGS

        settings().each do |name, dbprops|
          if dbprops[:db] != "derby"
            buildr.build create_hib_db(name, "#{base}/target/#{name}"=>dbprops) if orm == :hib and dbprops[:dao].downcase.include? "hib" 
            buildr.build create_jpa_db(base, name, "#{base}/target/#{name}"=>dbprops) if orm == :jpa and !dbprops[:dao].downcase.include? "hib"
          end
        end
      end
    end

    def create_hib_db(name, args)
      if File.exist? SETTINGS
        require SETTINGS
        db, dbprops = Rake.application.resolve_args(args)
        file(File.expand_path(db)) do |task|
          puts "Creating(preparing) database: #{db}."
          rm_rf task.name if File.exist?(task.name)
          Dir.mkdir(task.name)
          Buildr.ant(name) do |ant|
            ant.get :src=>"http://git.intalio.com/?p=integr.git;a=blob_plain;f=descriptors/package/#{dbprops[:db]}/ode_tables.sql;hb=#{dbprops[:integr_branch]}",
                    :dest=>"#{task.name}/ode_tables.sql"
            sqls = ["#{task.name}/ode_tables.sql"]
            
            # Apply the sql scripts to the database
            ant.sql :driver=>dbprops[:driver], :url=>dbprops[:url], :userid=>dbprops[:userid], :password=>dbprops[:password], :autocommit=>dbprops[:autocommit] do
              sqls.each { |sql| ant.transaction :src=>sql }
            end
            puts "Created(prepared) database: #{dbprops[:url]}."
          end
        end
      end
    end

    def create_jpa_db(base, name, args)
      if File.exist? SETTINGS
        require SETTINGS
        db, dbprops = Rake.application.resolve_args(args)
        file(File.expand_path(db)) do |task|
          puts "Creating(preparing) database: #{db}."
          rm_rf task.name if File.exist?(task.name)
          Dir.mkdir(task.name)
          Buildr.ant(name) do |ant|
            sqls = []
            if dbprops[:db] == "mysql"
              # create the drop table sql file from the create table sql
              create_tables = ""
              File.open("#{base}/target/#{dbprops[:db]}.sql", "r") do |f1|  
                while line = f1.gets
                  create_tables <<= line
                end
              end
            
              File.open("#{task.name}/drop-#{dbprops[:db]}.sql", "w") do |f2|
                create_tables.gsub(/CREATE TABLE (.*?)[\s\(].*?;/m) { |match|
                  f2.puts "DROP TABLE IF EXISTS " << $1 << ";\n"
                }
              end
              
              sqls |= ["#{task.name}/drop-#{dbprops[:db]}.sql"]
            end
            
            ant.copy :file=>"#{base}/target/#{dbprops[:db]}.sql", :todir=>task.name
            sqls |= ["#{task.name}/#{dbprops[:db]}.sql"]
            
            # Apply the sql scripts to the database
            ant.sql :driver=>dbprops[:driver], :url=>dbprops[:url], :userid=>dbprops[:userid], :password=>dbprops[:password], :autocommit=>dbprops[:autocommit] do
              sqls.each { |sql| ant.transaction :src=>sql }
            end
            puts "Created(prepared) database: #{dbprops[:url]}."
          end
        end
      end
    end

    def prepare_configs(test, base)
      test.setup task("prepare_configs") do |task| 
        if File.exist? SETTINGS
          require SETTINGS

          hibdbs = ""
          jpadbs = ""
          settings().each do |name, dbprops|
            dbs = (dbprops[:dao].downcase.include? "hib") ? hibdbs : jpadbs
            if dbprops[:db] == "derby"
              dbs <<= ", " if dbs.length > 0
              dbs <<= (dbs == jpadbs ? "<jpa>" : "<hib>")
            else
              test.with REQUIRES
  
              prepare_config(name, dbprops, "#{base}/target/conf.#{name}", "#{base}/src/main/webapp/WEB-INF/conf.template")
              dbs <<= ", " if dbs.length > 0
              dbs <<= "#{base}/target/conf.#{name}"
            end
          end
           test.options[:properties]["org.apache.ode.hibdbs"] = hibdbs
           test.options[:properties]["org.apache.ode.jpadbs"] =jpadbs
        end
      end
    end
    
    def prepare_config(name, dbprops, db, template)
      rm_rf db if File.exist?(db)
      Dir.mkdir(db)
      
      Buildr.ant(name) do |ant|
        ant.copy :todir=>db do
          ant.fileset :dir=>template
        end

        ant.replace :file=>"#{db}/ode-axis2.properties", :token=>"@connfactory@", :value=>dbprops[:dao]
        ant.replace :file=>"#{db}/ode-axis2.properties", :token=>"@driver@", :value=>dbprops[:driver]
        ant.replace :file=>"#{db}/ode-axis2.properties", :token=>"@url@", :value=>dbprops[:url]
        ant.replace :file=>"#{db}/ode-axis2.properties", :token=>"@userid@", :value=>dbprops[:userid]
        ant.replace :file=>"#{db}/ode-axis2.properties", :token=>"@password@", :value=>dbprops[:password]
        
        puts "Created config directory: #{db}."
      end
    end
    
  protected

    # This will download all the required artifacts before returning a classpath, and we want to do this only once.
    def requires()
      @requires ||= Buildr.artifacts(REQUIRES).each(&:invoke).map(&:to_s).join(File::PATH_SEPARATOR)
    end
  end
end
