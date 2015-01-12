#
# Docker file that builds RelEx and starts the RelEx server.
#
# To build:
#    docker build -t $USER/relex-master .
#
# To start:
#    docker run $USER/relex-master
#
# To demo:
#    telnet localhost 4444
#    > "This is a test sentence."
#

FROM ubuntu:14.04
MAINTAINER dhart@opencog.org

ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64
WORKDIR /home/Downloads/

RUN apt-get -y update
RUN apt-get -y upgrade

RUN apt-get -y install vim unzip screen telnet netcat-openbsd
# RUN apt-get -y install software-properties-common

# GCC and basic build tools
RUN apt-get -y install gcc g++ make

# Java
RUN apt-get -y install openjdk-7-jdk
RUN apt-get -y install ant
RUN apt-get -y install libcommons-logging-java

# Wordnet
RUN apt-get -y install wordnet
RUN apt-get -y install wordnet-dev
# RUN apt-get -y install wordnet-sense-index

# There are UTF8 chars in the Java sources, and the RelEx build will
# break if build in a C environment.
RUN sudo locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# Link Parser
ADD http://www.abisource.com/downloads/link-grammar/5.2.3/link-grammar-5.2.3.tar.gz /home/Downloads/link-grammar-5.2.3.tar.gz
RUN (tar zxvf link-grammar-5.2.3.tar.gz; cd link-grammar-5.2.3/; ./configure; make -j6; sudo make install; ldconfig)

# JWNL
ADD http://downloads.sourceforge.net/project/jwordnet/jwnl/JWNL%201.4/jwnl14-rc2.zip /home/Downloads/jwnl14-rc2.zip
RUN (unzip jwnl14-rc2.zip; cd jwnl14-rc2; cp jwnl.jar /usr/share/java/; chmod 777 /usr/share/java/jwnl.jar)

# OpenNLP
ADD http://www.motorlogy.com/apache/opennlp/opennlp-1.5.3/apache-opennlp-1.5.3-bin.zip /home/Downloads/apache-opennlp-1.5.3-bin.zip
RUN unzip apache-opennlp-1.5.3-bin.zip
RUN (cd apache-opennlp-1.5.3; cp lib/*.jar /usr/local/share/java/; cp lib/*.jar /usr/share/java/; cp lib/opennlp-tools-1.5.3.jar /usr/local/share/java/opennlp-tools-1.5.0.jar)

# Relex
ADD http://github.com/opencog/relex/archive/master.zip /home/Downloads/relex-master.zip
RUN (unzip relex-master.zip; cd relex-master; ant)

# Punch out ports

EXPOSE 9000
EXPOSE 4444

CD relex-master
CMD opencog-server.sh
