#!/bin/bash
#
# relation-extractor.sh: example relationship extractor.
# Parses a simple sentence about dinosaurs.
# This provides a basic demo of the RelEx abilities.
#
# Flags:
# RelationExtractor
# [-g (generate link graph - requires graphviz)]
# [-h (show this help)]
# [-l (show parse links)]
# [-m (show parse metadata)]
# [--maxParseSeconds N]
# [-n max number of parses to display]
# [-o (show opencog scheme output)]
# [--or (show opencog rule-based scheme output)]
# [--prolog (show prolog output)]
# [-q (do NOT show relations)]
# [-r (show raw output)]
# [-s Sentence (in quotes)]
# [--stanford (generate stanford-compatible output)]
# [-t (show parse tree)]
# [-v (verbose, full graph output)]


export LANG=en_US.UTF-8

# Remote debugging
# VM_OPTS="-Xdebug -Xnoagent -Djava.compiler=none -Xrunjdwp:transport=dt_socket,server=y,suspend=y -Xmx1024m -Djava.library.path=/usr/lib:/usr/local/lib"

VM_OPTS="-Xmx1024m \
	-Djava.library.path=/usr/lib:/usr/lib/jni:/usr/local/lib:/usr/local/lib/jni"

# By default, these resources are read from the relex jar file.
# Alternately, they are taken from the default paths, which are the
# same as those immediate below.
# RELEX_OPTS="\
# 	-Drelex.algpath=data/relex-semantic.algs \
# 	-Dwordnet.configfile=data/wordnet/file_properties.xml \
# 	"

CLASSPATH='-classpath ./target/classes:./target/lib/*'

# Read a sentence from stdin:
#echo "Alice wrote a book about dinosaurs for the University of California in Berkeley." | \
#	java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -n 4 -l	-t -r-a
#/usr/lib/jvm/java-6-sun/bin/java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -n 4 -l -t -r -a -s "Alice ate the mushroom."

# java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -n 4 -l -t -r -a -s "Alice ate the mushroom."
java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -n 4 -l -t -a --stanford --penn -s "Alice ate the mushroom."

# Generate a graph of the links produced by link-grammar
# Requires graphviz (http://www.graphviz.org/)
# java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -g -s "Alice ate the mushroom."

# Alternately, the sentence can be specified on the command line:
# java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -n 4 -l -t \
#	-s "Alice wrote a book about dinosaurs for the University of California in Berkeley."

# Alternately, a collection of sentences can be read from a file:
# cat trivial-corpus.txt | \
#	java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -n 4 -l	-t -a

# A collection of sentences can be read from a file and sent to the
# opencog server (assumed to be at port 17001 on localhost).
# cat trivial-corpus.txt | \
#	java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -o | \
#	telnet localhost 17001
