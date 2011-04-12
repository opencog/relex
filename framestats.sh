#!/bin/bash
export LANG=en_US.UTF-8

VM_OPTS="-Xmx1024m"
RELEX_OPTS="\
	-Djava.library.path=/usr/lib:/usr/lib/jni:/usr/local/lib:/usr/local/lib/jni \
	-Drelex.algpath=data/relex-semantic-algs.txt \
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
/usr/local/share/java/jwnl.jar:\
/usr/share/java/commons-logging.jar:\
/usr/share/java/gnu-getopt.jar:\
/usr/share/java/linkgrammar.jar:\
/usr/local/share/java/linkgrammar.jar:\
/usr/share/java/xercesImpl.jar:\
"
java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.frame.FrameStats $@

