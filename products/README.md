# RPM build configuration

In order to sign RPM's (which is default) a change in the file `~/.rpmmacros` is
required.

Add the following entries:
```
%_gpg_name <your-gpg-key-id>
%__gpg_check_password_cmd /bin/true
%__gpg_sign_cmd %{__gpg} gpg --batch --no-verbose --no-armor --use-agent --no-secmem-warning -u "%{_gpg_name}" -sbo %{__signature_filename} %{__plaintext_filename}
```

Be sure to fill in <your-gpg-key-id> with the key you want to use for signing!

# DEB build configuration

Add the following entries to your maven settings file (`~/.m2/settings.xml`):
```
<properties>
   <jdeb.keyring><!-- path to key ring: e.g. /home/user/.gnupg/secring.gpg --></jdeb.keyring>
   <jdeb.key><!-- your key id --></jdeb.key>
   <jdeb.passphrase><!-- your key password --></jdeb.passphrase>
</properties>
```

Be sure to fill out the gaps!

# Running the build

Also see: https://wiki.eclipse.org/PackageDrone/ReleaseBuild

First run:

    pushd runtime ; mvn clean package ; popd
    mvn clean integration-test -Pjdeb-sign -Prpm -Dtycho.localArtifacts=ignore
    pushd secondary ; mvn install ; popd
    pushd testing/server-test ; mvn clean test; popd