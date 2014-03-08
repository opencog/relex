#!/bin/bash
#
# opencog-server.sh: read from socket, generate opencog output.
#
# This script starts a RelEx server that listens for plain-text input
# (English sentences) on port 4444. It then parses the text, and
# returns opencog output on the same socket. The end of the parse is
# demarcated with an ; END OF SENTENCE token.
#
# It is intended that this server be used entirely from within OpenCog
# (primarily by the OpenCog chatbot), to parse text. It is not intended
# for general, manual use.
#
# Example usage:
#    ./opencog-server.sh &
#    telnet localhost 4444
#    This is a test
#    ^]q
#
export LANG=en_US.UTF-8

VM_OPTS="-Xmx1024m"

RELEX_OPTS="\
	-Djava.library.path=/usr/lib:/usr/lib/jni:/usr/local/lib:/usr/local/lib/jni \
	-Drelex.algpath=data/relex-semantic.algs \
	-Dwordnet.configfile=data/wordnet/file_properties.xml \
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
# java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.Server --link --relex --anaphora --verbose

# This will return parsed text on the input socket.
java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.Server --relex --anaphora --port 4444

# This will send the parsed output to an opencog server on localhost at port 17001
# java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.Server --relex --port 4444 --host localhost:17001

# This will send the RelEx OpenCog output and the RelEx2Logic function calls to
# an opencog server on localhost at port 17001
# java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.Server --logic --port 4444 --host localhost:17001


