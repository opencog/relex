#!/bin/bash
#
# plain-text-server.sh: read from socket, generate plain-text demo output.
#
# This script starts a RelEx server that listens for plain-text input
# (English sentences) on port 3333. It then parses the text, and
# returns a plain-text output on the same socket. The end of the parse is
# demarcated with an ; END OF SENTENCE token.
#
# It is intended that this server be used only for live web demos,
# rather than for anything serious.
#
# Example usage:
#    ./opencog-server.sh &
#    telnet localhost 3333
#    This is a test
#    ^]q
#
export LANG=en_US.UTF-8

VM_OPTS="-Xmx1024m"

RELEX_OPTS="\
	-Djava.library.path=/usr/lib:/usr/lib/jni:/usr/local/lib:/usr/local/lib/jni \
	"

CLASSPATH="-classpath \
bin:\
/usr/local/share/java/opennlp-tools-1.5.0.jar:\
/usr/local/share/java/opennlp-tools-1.4.3.jar:\
/usr/local/share/java/opennlp-tools-1.3.0.jar:\
/usr/local/share/java/maxent-3.0.0.jar:\
/usr/local/share/java/maxent-2.5.2.jar:\
/usr/local/share/java/maxent-2.4.0.jar:\
/usr/local/share/java/trove.jar:\
/usr/local/share/java/jwnl-1.4rc2.jar:\
/usr/local/share/java/jwnl.jar:\
/usr/share/java/commons-logging.jar:\
/usr/share/java/gnu-getopt.jar:\
/usr/share/java/linkgrammar.jar:\
/usr/local/share/java/linkgrammar.jar:\
/usr/share/java/xercesImpl.jar:\
"
# java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.PlainTextServer --link --phrase --relex --stanford --port 3333
java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.PlainTextServer --port 3333


