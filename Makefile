#
# Makefile, used only for build the dist
#

DIST_FILES = \
	AUTHORS \
	ChangeLog \
	LICENSE \
	Makefile \
	README \
	build.xml \
	relation-extractor.sh \
	test-corpus.txt \
	data/frame/concept_vars.txt \
	data/frame/mapping_rules.txt \
	data/relex-semantic-algs.txt \
	data/sentence-detector/EnglishSD.bin.gz \
	data/wordnet/file_properties.xml \
	src/java/relex/README \
	src/java/relex/*.java
	src/java/relex/algs/*.java \
	src/java/relex/anaphora/test-corpus.txt \
	src/java/relex/anaphora/*.java \
	src/java/relex/anaphora/README \
	src/java/relex/chunk/*.java \
	src/java/relex/corpus/*.java \
	src/java/relex/entity/*.java \
	src/java/relex/feature/*.java \
	src/java/relex/frame/*.java \
	src/java/relex/morphy/*.java \
	src/java/relex/output/*.java \
	src/java/relex/output/README \
	src/java/relex/parser/*.java \
	src/java/relex/tree/*.java \
	src/java/relex/util/socket/*.java

all:

dist:
	tar --transform "s#^#relex-0.8.4/#" -zcvf relex-0.8.4.tar.gz ${DIST_FILES}
