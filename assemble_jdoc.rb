#!/bin/ruby
# Builds a new directory structure and a nice index that brings 
# together all the different javadocs generated for each module.
# The output is generated in a javadoc_site directory.

require 'fileutils'

TARGET = 'javadoc_site'
FileUtils.rm_r(TARGET) if (File.exist?(TARGET))
FileUtils.mkdir(TARGET)

index = <<BLOCK
<html>
  <body>
    <!-- If a good soul feels like adding a CSS someday. -->
    <div class='content' style='margin-right:10px;'>
      <h2>Ode's Complete Javadoc</h2>
      <p><ul>
BLOCK

Dir.new('.').each do |dir|
  next if ['..', '.', '.svn'].include?(dir) || !File.directory?(dir)
  next unless (File.exist?(source = "#{dir}/target/site/apidocs/."))
  FileUtils.mkdir(dest = TARGET + '/' + dir)
  FileUtils.cp_r(source, dest)
  index << "\r\n<li>Module <a href='#{dir}/index.html'>#{dir}</a> (sources available <a href='http://svn.apache.org/repos/asf/incubator/ode/trunk/#{dir}'>here</a>)</li>"
end

index << <<BLOCK 
      </ul></p>
    </div>
  </body>
</html>
BLOCK

File.new(TARGET + '/index.html', 'w').write(index)