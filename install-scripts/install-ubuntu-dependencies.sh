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
sudo apt-get -y install ant libcommons-logging-java libgetopt-java

# Link Grammar
wget -r --no-parent --no-check-certificate -nH --cut-dirs=3 http://www.abisource.com/downloads/link-grammar/current/
tar -xvf link-grammar-5.*.tar.gz
rm link-grammar-5.*.tar.gz*
rm index.html*
cd link-grammar-5.*
JAVA_HOME=/usr/lib/jvm/java-9-openjdk-amd64  ./configure 
make -j6
sudo make install
sudo ln -v -s /usr/local/lib/liblink-grammar.so.5 /usr/lib/liblink-grammar.so.5
cd ..
sudo ldconfig

# Java WordNet Library
wget http://downloads.sourceforge.net/project/jwordnet/jwnl/JWNL%201.4/jwnl14-rc2.zip
unzip jwnl14-rc2.zip jwnl14-rc2/jwnl.jar
sudo mv -v jwnl14-rc2/jwnl.jar /usr/local/share/java/
rm -v jwnl14-rc2.zip && rmdir jwnl14-rc2
sudo chmod -v 0644 /usr/local/share/java/jwnl.jar 

cd ..

# RelEx
if grep -q '^vagrant:' /etc/passwd; then
    cd /home/vagrant/relex
    sudo -u vagrant ant build
else
    ant build
fi

sudo ant install
