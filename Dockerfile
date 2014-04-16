FROM base
MAINTAINER dbrian

EXPOSE 9000

ENV JAVA_HOME /usr/lib/jvm/java-7-oracle/
WORKDIR /home/Downloads/

RUN sudo locale-gen en_US.UTF-8
RUN (cd /home; mkdir Downloads)

RUN apt-get -y install vim curl git unzip screen telnet
RUN apt-get -y install software-properties-common

# GCC etc.
RUN apt-get -y install gcc g++ make

# Java
RUN add-apt-repository -y ppa:webupd8team/java
RUN apt-get -y update
RUN echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
RUN echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
RUN apt-get -y install oracle-java7-installer
RUN apt-get -y install ant
RUN apt-get -y install libcommons-logging-java

# Wordnet
RUN apt-get -y install wordnet
RUN apt-get -y install wordnet-dev
RUN apt-get -y install wordnet-sense-index

# JWNL
RUN (curl -O http://tcpdiag.dl.sourceforge.net/project/jwordnet/jwnl/JWNL%201.4/jwnl14-rc2.zip)
RUN (unzip jwnl14-rc2.zip; cd jwnl14-rc2; cp jwnl.jar /usr/share/java/; chmod 777 /usr/share/java/jwnl.jar)

# Link Parser
RUN (curl -O http://www.abisource.com/downloads/link-grammar/5.0.3/link-grammar-5.0.3.tar.gz)
RUN (tar zxvf link-grammar-5.0.3.tar.gz; cd link-grammar-5.0.3/; ./configure; make; sudo make install; ldconfig)

# OpenNPL
RUN (curl -O http://www.gaidso.com/apache//opennlp/opennlp-1.5.3/apache-opennlp-1.5.3-bin.tar.gz)
RUN (tar zxvf apache-opennlp-1.5.3-bin.tar.gz; cd apache-opennlp-1.5.3; cp lib/*.jar /usr/local/share/java/; cp lib/*.jar /usr/share/java/; cp lib/opennlp-tools-1.5.3.jar /usr/local/share/java/opennlp-tools-1.5.0.jar)

# Relex
RUN (git clone https://github.com/opencog/relex.git; cd relex; ant)

