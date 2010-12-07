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

module WSSecurity

  class << self
    def prepare_secure_processes_tests(test_dir, modules)
      task(test_dir.to_sym) do
        mkdir "#{test_dir}/modules" unless File.directory? "#{test_dir}/modules"
        Buildr.artifacts(modules).each(&:invoke)
        cp modules.map {|a| Buildr.repositories.locate(a)} , "#{test_dir}/modules"

        Dir.chdir(test_dir) do
          Dir['sample*-service.xml'].each do |service_file|
            sample_name = service_file.split('-').first
            proc_dir = "process-#{sample_name}"
            cp_r "process-template/.", proc_dir
            cp service_file, "#{proc_dir}/HelloService.axis2"
          end
        end
      end
    end
    def prepare_secure_services_tests(test_dir, file_pattern, modules)
      task(test_dir.to_sym) do
        # copy the required modules
        mkdir_p "#{test_dir}/modules" unless File.directory? "#{test_dir}/modules"
        Buildr.artifacts(modules).each(&:invoke)
        cp modules.map {|a| Buildr.repositories.locate(a)} , "#{test_dir}/modules"
        # generate one process per test
        Dir.chdir(test_dir) do
          Dir[file_pattern].each do |config_file|
            sample_name = File.basename(config_file, "."+config_file.split('.').last)
            # create process directory
            proc_dir = "process-#{sample_name}"
            mkdir proc_dir unless File.directory? proc_dir
            # copy files
            cp config_file, proc_dir
            # copy files from template and replace variable names
            Dir["process-template/*"].each do |file|
              lines = IO.readlines(file)
              # copy file and replace template values
              File.open("#{proc_dir}/#{File.basename(file)}", 'w') { |f|
                lines.each { |l|
                  l.gsub!("{sample.namespace}", "http://#{sample_name.gsub('-','.')}.samples.rampart.apache.org")
                  l.gsub!("{sample.service.name}", sample_name)
                  f<<l
                }
              }
            end
          end
        end
      end
    end
  end
end

