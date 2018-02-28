# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with this
# work for additional information regarding copyright ownership. The ASF
# licenses this file to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

# https://lists.apache.org/thread.html/b9043f64b8610504cc6b2dfc873dda20b99722bc412d85627ddf6d1a@%3Cusers.buildr.apache.org%3E
# We dont need this file once an addon is avaialble with the buildr release. So keeping this here until it's made avaialbe.
#
require 'digest'

module Buildr

  module Sha512
    class << self

      def sha512_checksum(pkg)
        filename = pkg.to_s + '.sha512'
        file(filename) do
          bytes = File.open(pkg.to_s, 'rb') {|file| file.read}
          File.open(filename, 'w') {|file| file.write Digest::SHA2.new(512).hexdigest(bytes)}
        end
      end

      def checksum_and_upload(project, pkg)
        project.task(:upload).enhance do
          artifact = Buildr.artifact(pkg.to_spec_hash.merge(:type => "#{pkg.type}.sha512"))
          artifact.from(sha512_checksum(pkg))
          artifact.invoke
          artifact.upload
        end
      end

      def checksum_and_upload_all_packages(project)
        project.packages.each {|pkg| Buildr::Sha512.checksum_and_upload(project, pkg)}
        project.packages.select {|pkg| pkg.respond_to?(:pom)}.map {|pkg| pkg.pom}.compact.uniq.each {|pom| Buildr::Sha512.checksum_and_upload(project, pom)}
      end
    end

    module ProjectExtension
      include Extension

      attr_writer :sha512_checksum

      def sha512_checksum?
        @sha512_checksum.nil? ? true : !!@sha512_checksum
      end

      after_define do |project|
        Buildr::Sha512.checksum_and_upload_all_packages(project) if project.sha512_checksum?
      end
    end
  end
end

class Buildr::Project
  include Buildr::Sha512::ProjectExtension
end
