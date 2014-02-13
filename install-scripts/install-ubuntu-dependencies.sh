#!/bin/bash
#
# RelEx install script for Ubuntu/Debian systems
# Tested on Ubuntu 12.04 LTS (Precise Pangolin)
#
# Usage:
#   git clone https://github.com/opencog/relex
#   cd relex
#   sudo ./install-scripts/install-ubuntu-dependencies.sh
#
# Authors: David Hart, Cosmo Harrigan

# Dependencies
apt-get -y install build-essential 
apt-get -y install wordnet-dev wordnet-sense-index
apt-get -y install openjdk-7-jdk 
apt-get -y install ant libcommons-logging-java

# link-grammar
wget http://www.abisource.com/downloads/link-grammar/4.8.6/link-grammar-4.8.6.tar.gz
tar -xvf link-grammar-4.8.6.tar.gz
rm link-grammar-4.8.6.tar.gz
cd link-grammar-4.8.6
JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64  ./configure 
make 
make install
ln -v -s /usr/local/lib/liblink-grammar.so.4 /usr/lib/liblink-grammar.so.4
cd ..

# jwnl
wget http://downloads.sourceforge.net/project/jwordnet/jwnl/JWNL%201.4/jwnl14-rc2.zip
unzip jwnl14-rc2.zip
rm jwnl14-rc2.zip
cp -v jwnl14-rc2/jwnl.jar /usr/local/share/java/
chmod 0644 /usr/local/share/java/jwnl.jar 

# getopt
wget http://download.java.net/maven/2/gnu/getopt/java-getopt/1.0.13/java-getopt-1.0.13.jar
cp -v java-getopt-1.0.13.jar /usr/share/java/gnu-getopt.jar && chmod 0644 /usr/share/java/gnu-getopt.jar

# test relex
JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64 ant run
