#!/bin/bash

export LANG=en_US.UTF-8

VM_OPTS="\
	-Xmx1024m \
	"

RELEX_OPTS="\
	-Drelex.algpath=data/relex-semantic.algs \
	-Dwordnet.configfile=data/wordnet/file_properties.xml \
	"

CLASSPATH='-classpath ./target/classes:./target/lib/*'
java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.concurrent.ParallelRelationExtractor $1
