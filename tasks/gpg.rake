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

module Buildr
  module GPG
    extend self

    def sign_task(pkg)
      file(pkg.to_s + '.asc') do
        puts "GPG signing #{pkg.to_spec}"
        cmd = 'gpg',
               '--local-user', ENV['GPG_USER'],
               '--armor',
               '--output', pkg.to_s + '.asc'
        cmd += ['--passphrase', ENV['GPG_PASS']] if ENV['GPG_PASS']
        cmd += ['--detach-sig', pkg]
        #cmd << { :verbose => true }
        #sh *cmd
        system *cmd
      end
    end

    def sign_and_upload(pkg)
      task(:upload).enhance do
        artifact = Buildr.artifact(pkg.to_spec_hash.merge(:type => "#{pkg.type}.asc"))
        artifact.from sign_task(pkg)
        artifact.invoke
        artifact.upload
      end
    end
  
    def gpg_sign_before_upload
      self.enhance do 
        [self, self.projects].flatten.each { |prj|
          prj.packages.each { |pkg| sign_and_upload(pkg) }
          prj.packages.map {|pkg| pkg.pom }.uniq.each { |pom| sign_and_upload(pom) }
        }
      end
    end
  end
end

class Buildr::Project
  include Buildr::GPG
end
