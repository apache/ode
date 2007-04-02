desc "Checks license headers."
task('headers') do
  puts Dir.pwd
  require 'tasks/check_license_headers'
  HeadersCheck.new.check_files('.')
end
