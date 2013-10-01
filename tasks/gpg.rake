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
    module ProjectExtension
      def gpg_sign_before_upload
        self.enhance do
          [self, self.projects].flatten.each { |prj|
            prj.packages.each { |pkg| GPG.sign_and_upload(self, pkg) }
            prj.packages.map {|pkg| pkg.pom }.uniq.each { |pom| GPG.sign_and_upload(self, pom) }
          }
        end
      end
    end
  end
end
