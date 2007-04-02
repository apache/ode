class HeadersCheck

  def check_files(dir)
    count = FileList["**/*.{java,xml,bpel,wsdl}"].inject(0) do |count, filename|
        match = nil
        File.open(filename) do |f|
          # Checking for the Apache header in the 4 first lines
          4.times { match ||= (/Licensed to the Apache Software Foundation/ =~ f.readline) rescue nil }
        end
        when_writing("Missing header in #{filename}") { add_header(filename); count+= 1 }
        count
    end

    puts "#{count} files don't have been checked."
  end

  def add_header(filename)
    ext = /\.([^\.]*)$/.match(filename[1..-1])[1]
    header, content = HEADERS[ext], ''
    File.open(filename, 'r') { |file| content = file.read }
    if content[0..4] == '<?xml'
      content = content[0..content.index("\n")] + header + content[(content.index("\n") + 1)..-1]
    else
      content = header + content
    end
    File.open(filename, 'w') { |file| file.write(content) }
  end

end

JAVA_HEADER = <<JAVA
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
JAVA

XML_HEADER = <<XML
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
XML

HEADERS = {
  'java' => JAVA_HEADER,
  'c' => JAVA_HEADER,
  'cpp' => JAVA_HEADER,
  'xml' => XML_HEADER,
  'bpel' => XML_HEADER,
  'wsdl' => XML_HEADER
}

# if ['-h', '--help', 'help'].include? ARGV[0]
#  puts "Scans the current directory for files with missing Apache "
#  puts "license headers."
#  puts "   ruby check_license_headers.rb      # list files"
#  puts "   ruby check_license_headers.rb add  # add headers automatically"
# else
#  HeadersCheck.new.check_files('.', ARGV[0] != 'add')
# end
