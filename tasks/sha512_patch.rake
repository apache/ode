# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with this
# work for additional information regarding copyright ownership.  The ASF
# licenses this file to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.

raise 'Patch already applied' if Buildr::VERSION.to_s >= '1.5.5'

module URI
  class Generic
    def upload(source, options = nil)
      source = source.name if Rake::Task === source
      options ||= {}
      if String === source
        raise NotFoundError, 'No source file/directory to upload.' unless File.exist?(source)
        if File.directory?(source)
          Dir.glob("#{source}/**/*").reject { |file| File.directory?(file) }.each do |file|
            uri = self + (File.join(self.path, file.sub(source, '')))
            uri.upload file, {:digests=>[]}.merge(options)
          end
        else
          File.open(source, 'rb') { |input| upload input, options }
        end
      elsif source.respond_to?(:read)
        digests = (options[:digests] || [:md5, :sha1]).
          inject({}) { |hash, name| hash[name] = name.to_s == 'sha512' ? Digest::SHA2.new(512) : Digest.const_get(name.to_s.upcase).new ; hash}
        size = source.stat.size rescue nil
        write (options).merge(:progress=>verbose && size, :size=>size) do |bytes|
          source.read(bytes).tap do |chunk|
            digests.values.each { |digest| digest << chunk } if chunk
          end
        end
        digests.each do |key, digest|
          self.merge("#{self.path}.#{key}").write digest.hexdigest,
            (options).merge(:progress=>false)
        end
      else
        raise ArgumentError, 'Expecting source to be a file name (string, task) or any object that responds to read (file, pipe).'
      end
    end
  end
end
