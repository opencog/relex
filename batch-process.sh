#!/bin/bash
#
# batch-process.sh: Example batch processing script. Unlike the other
# examples, this script outputs the so-called "compact format" which
# captures the full range of Link Grammar and RelEx output in a format
# that can be easily post-processed by other systems (typically by 
# using regex's). The src/perl/cff-to-opencog.pl perl script provides
# an example of post-processing: it converts this output format into 
# OpenCog hypergraphs.
#
# The idea behind the batch processing is that it is costly to parse
# large quantities of text: thus, it is convenient to parse the text
# once, save the results, and then perform post-processing at liesure,
# as needed.  Thus, the form of post-processing can be changed at will,
# without requiring texts to be re-processed over and over again.
#

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
/usr/local/share/java/maxent-3.0.0.jar:\
/usr/local/share/java/trove.jar:\
/usr/local/share/java/jwnl.jar:\
/usr/share/java/commons-logging.jar:\
/usr/share/java/gnu-getopt.jar:\
/usr/local/share/java/linkgrammar.jar:\
/usr/share/java/linkgrammar.jar:\
"

cat test-corpus.txt | \
java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.WebFormat  -n 4 -g 

exit 1;

# Here's a typical usage. It is assumed that the input is clean, 
# i.e. stripped of extraneous HTML markup, etc.

cat ../../data/voa_sentences-clean.txt | \
	java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.WebFormat  -n 4 \
	--url "voa_sentences-clean.txt" > ../../data/voa_sentences-parsed.xml


