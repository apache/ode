module Jdepend

  REQUIRES = ["jdepend:jdepend:jar:2.9.1"]

  task "jdepend" do
    paths = projects(:in=>self).map { |prj| prj.path_to("target/classes") }.each { |path| file(path).invoke }.
      select { |path| File.exist?(path) }
    java "jdepend.swingui.JDepend", paths, :classpath=>REQUIRES
  end

end
