#!/bin/bash

echo '<?xml version="1.0" encoding="UTF-8"?>' > cleanup.setup
echo '<setup:Project xsi:type="setup:CompoundTask" name="Converted preferences"' >> cleanup.setup
echo '  xmi:version="2.0"'  >> cleanup.setup
echo '  xmlns:setup="http://www.eclipse.org/oomph/setup/1.0"' >> cleanup.setup
echo '  xmlns:xmi="http://www.omg.org/XMI"' >> cleanup.setup
echo '  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' >> cleanup.setup
echo '  xsi:schemaLocation="http://www.eclipse.org/oomph/setup/git/1.0 http://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/models/Git.ecore http://www.eclipse.org/oomph/setup/jdt/1.0 http://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/models/JDT.ecore http://www.eclipse.org/oomph/setup/launching/1.0 http://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/models/Launching.ecore http://www.eclipse.org/oomph/setup/pde/1.0 http://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/models/PDE.ecore http://www.eclipse.org/oomph/setup/projects/1.0 http://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/models/Projects.ecore"'  >> cleanup.setup
echo '>' >> cleanup.setup

perl -ne 'chomp; my ($key, $value) = split(/=/); print "<setupTask xsi:type=\"setup:PreferenceTask\" key=\"/instance/org.eclipse.jdt.ui/$key\" value=\"$value\" />\n"' < cleanup.txt >> cleanup.setup

echo '</setup:Project>' >> cleanup.setup
