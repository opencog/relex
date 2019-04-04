#!/bin/bash
#
# RelEx install script for Ubuntu/Debian systems
# Tested on Ubuntu 12.04 LTS (Precise Pangolin)
# Tested on Ubuntu 14.04 LTS (Trusty Tahr)
#
# Usage:
#   git clone https://github.com/opencog/relex
#   cd relex
#   install-scripts/install-ubuntu-dependencies.sh
#     (note: will ask for sudo password)
#   ant run
#
# Authors: David Hart, Cosmo Harrigan

## Ubuntu/Debian Packages
sudo apt-get -y update 
sudo apt-get -y install build-essential python-dev swig zlib1g-dev unzip wget
sudo apt-get -y install wordnet-dev wordnet-sense-index
sudo apt-get -y install openjdk-9-jdk 
sudo apt-get -y install maven

# Link Grammar
wget -r --no-parent --no-check-certificate -nH --cut-dirs=3 http://www.abisource.com/downloads/link-grammar/current/
tar -xvf link-grammar-5.*.tar.gz
rm link-grammar-5.*.tar.gz*
rm -rf index.html* robots.txt
pushd link-grammar-5.*
JAVA_HOME=/usr/lib/jvm/java-9-openjdk-amd64  ./configure 
make -j6
sudo make install
sudo ln -v -s /usr/local/lib/liblink-grammar.so.5 /usr/lib/liblink-grammar.so.5
popd
sudo ldconfig

MVN_USER=`whoami`
LINKGRAMMAR_JAR=`find ./link-grammar* -name linkgrammar*.jar`
LINKGRAMMAR_VERSION=`echo $LINKGRAMMAR_JAR | grep -oP '(?<=-)\d+\.\d+\.\d+(?=\.)'`

if grep -q '^vagrant:' /etc/passwd; then
    cd /home/vagrant/relex
    MVN_USER=vagrant
fi

sudo -u $MVN_USER mvn install:install-file \
   -Dfile=$LINKGRAMMAR_JAR \
   -DgroupId=org.opencog \
   -DartifactId=linkgrammar \
   -Dversion=$LINKGRAMMAR_VERSION \
   -Dpackaging=jar

# RelEx
sudo -u $MVN_USER mvn package
sudo -u $MVN_USER mvn install
