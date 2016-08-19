cs-actions
=============

Java actions to be used by operations.

[![Build Status](https://travis-ci.org/CloudSlang/cs-actions.svg?branch=master)](https://travis-ci.org/CloudSlang/cs-actions)

Build instructions for cs-powershell:
   * Download VMWare Web client SDK for vim25.jar: From http://developercenter.vmware.com/web/sdk/60/web-client, you need to download this SDK and get vim25.jar from it.
   * Install it using: mvn install:install-file -Dfile="path-to-vim25.jar" -DgroupId=com.vmware -DartifactId=vim25 -Dversion=1.0 -Dpackaging=jar
   * From the home directory of this project (cs-actions), run "mvn -DskipTests=true install"
   * cd cs-powershell; mvn -DskipTests=true package
   * You will find cs-powershell-*-jar-with-dependencies.jar in target directory

You can add this jar to dependencies of your project.
