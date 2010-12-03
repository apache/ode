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
    artifact = Buildr.artifact(pkg.to_spec_hash.merge(:type => "#{pkg.type}.asc"))
    artifact.from sign_task(pkg)
    task(:upload).enhance [artifact.upload_task]
  end
end
