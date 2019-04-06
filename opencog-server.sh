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

VM_OPTS="-Xmx2048m"

RELEX_OPTS="\
	-Djava.library.path=/usr/lib:/usr/lib/jni:/usr/local/lib:/usr/local/lib/jni \
	-Drelex.algpath=data/relex-semantic.algs \
	-Dwordnet.configfile=data/wordnet/file_properties.xml \
	"

CLASSPATH='-classpath ./target/classes:./target/lib/*'

# Return with Link Grammar and Relex output on default port 4444.
java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.Server --link --relex

# This will return parsed text on the input socket.
# java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.Server --relex

# Return Link Grammar and Relex output.
# java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.Server --link --relex --verbose

# Like the above, but listens on a non-default port
# java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.Server --port 4242

# Like the above, but sents the output to a different host, instead of
# replying on the same socket.
# java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.Server --port 4242 --host somewhere.com:17001
