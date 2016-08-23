#!/bin/bash

export LANG=en_US.UTF-8

VM_OPTS="\
	-Xmx1024m \
	"

RELEX_OPTS="\
	-Drelex.algpath=data/relex-semantic.algs \
	-Dwordnet.configfile=data/wordnet/file_properties.xml \
	"

CLASSPATH="-classpath \
bin:\
/usr/local/share/java/jwnl.jar:\
/usr/local/share/java/opennlp-tools-1.5.3.jar:\
/usr/local/share/java/maxent-3.0.3.jar:\
/usr/local/share/java/opennlp-tools-1.5.0.jar:\
/usr/local/share/java/maxent-3.0.0.jar:\
/usr/local/share/java/maxent-2.5.2.jar:\
/usr/local/share/java/trove.jar:\
/usr/share/java/commons-logging.jar:\
/usr/share/java/gnu-getopt.jar:\
"
java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.concurrent.ParallelRelationExtractor $1

