#!/bin/zsh

# Constants
GROUP_IDS=("org.apache.commons" "org.apache.commons" "org.javatuples" "org.xerial")
ARTIFACT_IDS=("commons-compress" "commons-exec" "javatuples" "sqlite-jdbc")
MODULE_NAMES=("org.apache.commons.compress" "commons.exec" "javatuples" "org.xerial.sqlitejdbc")
MODULE_VERSIONS=("1.21" "1.3" "1.2" "3.36.0.3")
MODULE_JARS=("commons-compress-1.21.jar" "commons-exec-1.3.jar" "javatuples-1.2.jar" "sqlite-jdbc-3.36.0.3.jar")

# Get local Maven repository
repositoryDir=$(mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout)

# Iterate through each one of the problematic modules
for i in {1..4}; do
  # Get the module name and version
  groupID=${GROUP_IDS[$i]}
  artifactID=${ARTIFACT_IDS[$i]}
  moduleName=${MODULE_NAMES[$i]}
  moduleVersion=${MODULE_VERSIONS[$i]}
  moduleJar=${MODULE_JARS[$i]}

  # Get the module's JAR file
  moduleDir="${repositoryDir}/${groupID//.//}/${artifactID}/${moduleVersion}"
  moduleJarPath="${moduleDir}/${moduleJar}"

  # Navigate into that directory
  cd "${moduleDir}"

  # Generate the module-info.java file
  jdeps --generate-module-info . ${moduleJar}
  if [ $? -ne 0 ]; then
    # Try again, but this time ignoring dependencies
    jdeps --ignore-missing-deps --generate-module-info . ${moduleJar}

    if [ $? -ne 0 ]; then
      # If we still can't generate the module-info.java file, then we're done
      echo "Failed to generate module-info.java for ${artifactID} (${moduleVersion})"
      continue # Skip to the next module
    fi
  fi

  # Patch module
  javac --patch-module ${moduleName}=${moduleJar} ${moduleName}/module-info.java

  # Recompile jar
  jar uf ${moduleJar} -C ${moduleName} module-info.class

  # Report that we have done the updating
  echo "Updated ${artifactID} (${moduleVersion})"
done
