#!/bin/bash
#
# relation-extractor.sh: example relationship extractor.
# Parses a simple sentence about dinosaurs.
# This provides a basic demo of the RelEx abilities.
#
# Flags:
# RelationExtractor [-a (perform anaphora resolution)] 
# [-h (show this help)] 
# [-l (show parse links)]
# [-m (show parse metadata)] 
# [--maxParseSeconds N]
# [-n max number of parses to display] 
# [-o (show opencog scheme output)] 
# [--pa (show phrase-based lexical chunks)] 
# [--pb (show pattern-based lexical chunks)]
# [--pc (show relational lexical chunks)] 
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
# 	-Drelex.algpath=data/relex-semantic-algs.txt \
# 	-Dwordnet.configfile=data/wordnet/file_properties.xml \
# 	"

CLASSPATH="-classpath \
bin:\
/usr/local/share/java/jwnl.jar:\
/usr/local/share/java/jwnl-1.4rc2.jar:\
/usr/local/share/java/jwnl-1.3.3.jar:\
/usr/local/share/java/opennlp-tools-1.5.0.jar:\
/usr/local/share/java/opennlp-tools-1.4.3.jar:\
/usr/local/share/java/opennlp-tools-1.3.0.jar:\
/usr/local/share/java/maxent-3.0.0.jar:\
/usr/local/share/java/maxent-2.5.2.jar:\
/usr/local/share/java/maxent-2.4.0.jar:\
/usr/local/share/java/trove.jar:\
/usr/local/share/java/linkgrammar.jar:\
/usr/share/java/linkgrammar.jar:\
/usr/share/java/commons-logging.jar:\
/usr/share/java/gnu-getopt.jar:\
"

# Read a sentence from stdin:
#echo "Alice wrote a book about dinosaurs for the University of California in Berkeley." | \
#	java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -n 4 -l	-t -r-a
#/usr/lib/jvm/java-6-sun/bin/java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -n 4 -l -t -r -a -s "Alice ate the mushroom."
reset
# java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -n 4 -l -t -r -a -s "Alice ate the mushroom."
java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -n 4 -l -t -a --or --stanford --penn -s  "I saw the man with a telescope."

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
