#!/bin/bash
#
# opencog-server.sh: read from socket, generate opencog output.
#
# This script start a relex server that listens for plain-text input
# (English sentences) on port 4444. It then parses the text, and 
# returns opencog output. The end of the parse is demarcated with
# an ; END OF SENTENCE token.
#

export LANG=en_US.UTF-8

VM_OPTS="-Xmx1024m"

RELEX_OPTS="\
	-Drelex.algpath=data/relex-semantic-algs.txt \
	-Dwordnet.configfile=data/wordnet/file_properties.xml \
	"

CLASSPATH="-classpath \
bin:\
/usr/local/share/java/opennlp-tools-1.3.0.jar:\
/usr/local/share/java/maxent-2.4.0.jar:\
/usr/local/share/java/trove.jar:\
/usr/local/share/java/jwnl-1.4rc2.jar:\
/usr/share/java/commons-logging.jar:\
/usr/share/java/gnu-getopt.jar:\
/usr/share/java/linkgrammar.jar:\
/usr/local/share/java/linkgrammar.jar:\
/usr/share/java/xercesImpl.jar:\
"
java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.Server -n 4 -p 4444


