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

namespace "check" do

  desc "Checks license headers."
  task("headers") do
    # Define license headers based on the filename extension.
    licenses = {}
    licenses[".java"] = <<EOF
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
EOF
    licenses[".xml"] = <<EOF
<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->
EOF
    licenses[".properties"] = <<EOF
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
EOF
    licenses[".bpel"] = licenses[".wsdl"] = licenses[".xsd"] = licenses[".soap"] =
    licenses['.usd'] = licenses['.xsl'] = licenses[".deploy"] = licenses[".xml"]
    licenses[".rake"] = licenses[".tdef"] = licenses[".properties"]
    # This also tells us which files to look at.
    extensions = licenses.keys.join(",")
    count = FileList["**/*{#{extensions}}"].inject(0) do |count, filename|
      if File.file?(filename) and  File.readlines(filename)[0..3].join !~ /Licensed to the Apache Software Foundation/
        when_writing "Missing header in #{filename}" do
          # Figure the license from the file, inject it into the file and rewrite it.
          license = licenses[filename.pathmap("%x")]
          if license
            content = File.read(filename)
            if (content =~ /<\?xml .*\?>/)
          modified = content.sub(/(<\?xml .*\?>\n?)(.*)/m) { "#{$1}#{license}#{$2}" }
            else
              modified = license + "\n" + content
            end
            File.open(filename, "w") { |file| file.write modified }
          else
            puts "Skipping unknown extension for file #{filename}"
          end
          count + 1
        end
      else
        count
      end
    end
    if count > 0
      warn "#{count} files found to have missing headers."
    else
      puts "All #{extensions} files checked and have the license in them."
    end
  end

end
