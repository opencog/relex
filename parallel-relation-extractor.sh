#!/bin/bash

export LANG=en_US.UTF-8

VM_OPTS="\
	-Xmx1024m \
	"

RELEX_OPTS="\
	-Drelex.algpath=data/relex-semantic-algs.txt \
	-Dwordnet.configfile=data/wordnet/file_properties.xml \
	"

CLASSPATH="-classpath \
bin:\
/usr/local/share/java/jwnl.jar:\
/usr/local/share/java/opennlp-tools-1.4.3.jar:\
/usr/local/share/java/maxent-2.5.2.jar:\
/usr/local/share/java/trove.jar:\
/usr/share/java/commons-logging.jar:\
/usr/share/java/gnu-getopt.jar:\
/usr/share/java/xercesImpl.jar:\
/opt/GATE-4.0/bin/gate.jar:\
/opt/GATE-4.0/lib/jdom.jar:\
/opt/GATE-4.0/lib/jasper-compiler-jdt.jar:\
/opt/GATE-4.0/lib/nekohtml-0.9.5.jar:\
/opt/GATE-4.0/lib/ontotext.jar:\
/opt/GATE-4.0/lib/PDFBox-0.7.2.jar\
"
java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.concurrent.ParallelRelationExtractor $1

