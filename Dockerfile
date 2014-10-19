# docker build -t $USER/relex-master .

FROM ubuntu:14.04
MAINTAINER dhart@opencog.org

EXPOSE 4444
ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64
WORKDIR /home/Downloads/

# Install relex dependencies
COPY install-scripts/install-ubuntu-dependencies.sh /tmp/
RUN sudo locale-gen en_US.UTF-8
RUN /tmp/install-ubuntu-dependencies.sh

WORKDIR /home/dev/
