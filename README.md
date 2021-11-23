
# RelEx Semantic Relation Extractor
### Version 1.6.3  circa 2016

RelEx is a dependency parser for the English language.  It extracts
dependency relations from Link Grammar, and adds some shallow semantic
analysis.  The primary use of RelEx is as a language input front-end
to the OpenCog artificial general intelligence system.

There are multiple inter-related parts to RelEx. The core component
extracts the dependency relationships. An experimental module provides
some simple anaphora resolution suggestions.  Output is provided in
various formats, including one format suitable for later batch
post-processing, another format suitable for input to OpenCog, and an
W3C OWL format. There are also a small assortment of perl scripts for
cleaning up web and wiki pages, &c.

The main RelEx website is at

   > http://wiki.opencog.org/w/RelEx

It provides an overview of the project, as well as detailed documentation.

The source code management system is at

   > http://github.com/opencog/relex

Source tarballs may be downloaded from either of two locations:

   > https://launchpad.net/relex/+download

   > http://www.abisource.com/downloads/link-grammar/relex/

Build and install of the core package is discussed below.


Running the Relex Servers
-------------

### Run via Docker

The easiest way to run RelEx is with Docker. The Docker
system allows sandboxed containers to be easily created and deployed;
the typical use of a container is to run some server.  See the
http://www.docker.io website for more info and tutorials.

Opencog has prebuilt images for relex available with the image tag: opencog/relex

#### Running the Plain-text Server
To have docker run the plain text server, type into a terminal:
```
   $ docker run -it -p 3333:3333 opencog/relex /bin/sh plain-text-server.sh
```

To test the plain text server via telnet, type into another terminal:
```
   telnet localhost 3333
   This is a test sentence!
```
The server will return a plain-text analysis of the input
sentence and disconnect the session.

#### Running the OpenCog-format Server
To have docker run the OpenCog format server, type:
```
   $ docker run -it -p 4444:4444 opencog/relex /bin/sh opencog-server.sh

```
To test the OpenCog format server via telnet, type into another terminal:

```
   telnet localhost 4444
   This is a test sentence!
```
The server will return an OpenCog/Scheme version of the parse and disconnect the session.

#### Running the raw Link Grammar Server
To have docker run the raw link-grammar JSON-format server, type:
```
   $ docker run -it -p 9000:9000 opencog/relex /bin/sh link-grammar-server.sh
```
You can now access the relex server with telnet.

The raw link-grammar server expects a JSON-formatted input, begining
with the 5 letters `text:` it returns a JSON-formatted response.

To test the link-grammar JSON format server via telnet, type into another terminal:

```
   telnet localhost 9000
   text:This is a test sentence!
```

This will return a JSON formatted parse and then disconnect the session.


#### Docker Cheat-Sheet
A docker cheat-sheet:
```
docker ps
docker ps -a
docker rm
docker images
docker rmi
```

Installation
-------------

### Installing on Ubuntu/Debian

An installation script for Ubuntu/Debian is provided in the [install-scripts](./install-scripts) directory.

### Installing on all other systems

For other systems, follow the instructions below.
To build and use RelEx, the following packages are required to be
installed:

 - libgetopt-java (GNU getopt)
 - Link Parser
 - WordNet 3.0
 - JWNL Java wordnet library
 - OpenNLP tools (optional, but recommended)
 - W3C OWL (optional)

Pre-requisite dependencies
--------------------------
The following packages are required pre-requisites for building RelEx.

- ***Link Grammar Parser***.
	Compile and install the Link Grammar Parser. This parser is
	described at
	
	> http://abisource.com/projects/link-grammar/
	
	and sources	are available for download at
	
	> http://www.abisource.com/projects/link-grammar/#download

	Link-grammar version 5.2.1 or later is needed to obtain a variety
	of required fixes.

	The Link Grammar Parser is the underlying engine, providing
	the core sentence parsing ability.

	If the parser is not installed in the default location,
	be sure to modify `-Djava.library.path` appropriately in
	`relation-extractor.sh` and other shell scripts.

- ***GNU getopt***.
	This is a standard command-line option parsing library.
	For Ubuntu, install the `libgetopt-java` package.

- ***Wordnet***.
	Wordnet is used by RelEx to provide basic English morphology
	analysis, such as singular versions of (plural) nouns, base forms
	(lemmas) of adjectives, adverbs and infinitive forms of verbs.

	Download, unpack and install WordNet 3.0.  The install directory
	needs to be specified in `data/wordnet/file_properties.xml`, with
	the `name="dictionary_path"` property in this file.

	Some typical install locations are:
	
	- `/opt/WordNet-3.0/data` for RedHat and SuSE
	- `/usr/share/wordnet` for Ubuntu and Debian
	- `C:\Program Files\WordNet\3.0\data` for Windows

	The `relex/Morphy/Morphy.java` class provides a simple, easy-to-use
	wrapper around wordnet, providing the needed word morphology info.

The following packages are required pre-requisites for building RelEx.
Note, that they are automatically installed if Maven system is used.

- ***didion.jwnl***.
	The didion JWNL is the "Java WordNet Library", and provides the
	Java programming API to access the wordnet data files.
	Its home page is at

	> http://sourceforge.net/projects/jwordnet

	and can be downloaded from

	> http://sourceforge.net/project/showfiles.php?group_id=33824

	Verify that the final installed location of `jwnl.jar` is correctly
	specified in the `build.xml` file. Note that GATE also provides a
	`jwnl.jar`, but the GATE version of `jwnl.jar` is not compatible
	(welcome to java DLL hell).

	When copying `jwnl.jar`: verify the file permisions! Be sure to issue
	the following command: `chmod 644 jwnl.jar`, as otherwise, you'll
	get strange "java cannot unzip jar" error messages.

- ***Apache Commons Logging***.
	The JWNL package requires that the Apache commons logging
	jar file be installed. In Debian/Ubuntu, this is supplied by
	the `libcommons-logging-java` package. In RedHat/CentOS systems,
	the package name is `jakarta-commons-logging`.

- ***SLF4J and Logback***.
    RelEx uses SLF4J as a facade for the Logback logging framework.
	SLF4J home pages is at

	> https://www.slf4j.org

	and can be downloaded from

	> https://www.slf4j.org/download.html

	Logback home pages is at

	> https://logback.qos.ch

	and can be downloaded from

	> https://logback.qos.ch/download.html


Optional packages
-----------------
The following packages are optional. If they are found, then
additional parts of RelEx will be built, enabling additional
function.

If you use Maven, these dependencies are already managed.

- ***OpenNLP***.
	RelEx uses OpenNLP for sentence detection, giving RelEx the ability
	to find sentence boundaries in free text. If OpenNLP is not found,
	then the (far) less accurate `java.text.BreakIterator` class is used.
	Although Oracle documentation states that "Sentence boundary analysis
	allows selection with correct interpretation of periods within numbers
	and abbreviations", this is patently false, as it incorrectly breaks
	the sentence "Dr. Smith is late." into two sentences.  Thus, OpenNLP
	is recommended.

	The OpenNLP home page is at

	> http://opennlp.sourceforge.net/

	Download and install OpenNLP tools, and verify that the
	installed files are correctly identified in both `build.xml`
	and in `relation-extractor.sh`.

	OpenNLP also requires the installation of maxent from

	> http://maxent.sourceforge.net/

	You'll need `maxent-3.0.0.jar` and `opennlp-tools-1.5.3.jar`.

	The OpenNLP package is used solely in corpus/DocSplitter.java,
	which provides a simple, easy-to-use wrapper for splitting a
	document into sentences. Replace this file if an alternate
	sentence detector is desired.

- ***Trove***.
	Some users may require the GNU Trove to enable OpenNLP, although
	this depends on the JDK installed.  GNU Trove is an implementation
	of the java.util class hierarchy, which may or may not be included
	in the installed JDK.  If needed, download trove from:

	> http://trove4j.sourceforge.net/

	Since trove is optimized, using it may improve performance and/or
	decrease memory usage, as compared to the standard Sun JDK
	implementation of the java.util hierarchy.

	***IMPORTANT*** OpenNLP expects Gnu Trove version 1.0, and will not
	work with version 2.0 !!


Building
--------

### With Maven

Maven manages almost all of dependencies automatically. Only exception is Link Grammar library which should be added into local maven repository manually, using:
```
mvn install:install-file \
    -Dfile=<linkgrammar-jar-folder/linkgrammar.jar> \
    -DgroupId=org.opencog \
    -DartifactId=linkgrammar \
    -Dversion=<linkgrammar.version> \
    -Dpackaging=jar
```

Then you can build and install relex.jar using:
```
mvn install
```

Using RelEx
-----------
It is assumed that RelEx will be used in one of two different ways.
These are in a "batch processing" mode, and a "custom Java development"
mode.

In the "batch processing mode", RelEx is run once over a large text,
and its output is saved to a file.  This output can then be
post-processed at a later time, to extract desired info. The goal here
is to avoid the heavy CPU overhead of re-parsing a large text over and
over.  Example post-processing scripts are included (described below).

In the "custom Java development" mode, it is assumed that a capable
Java programmer can write new code to interface RelEx to meet their needs.
A good place to start is to review the workings of the output code in
`src/java/relex/output/*.java`.

The standard RelEx demo output is NOT SUITABLE for post-processing. It
is meant to be a human-readable example of what the system generates;
it does not include all required output. For example, if the same word
appears in a sentence twice, the demo output will not distinguish between
these two words.

This release of RelEx includes an experimental Stanford-parser
compatibility mode.  In this mode, RelEx will generate the same
dependency relations as the Stanford parser. This mode is technically
interesting for comparing output; RelEx is more than three time faster
than the lexicalized (factored) Stanford parser, although it is slower
than the PCFG parser. This is described in greater detail in the file
`README-Stanford`.

This release of RelEx includes an optional Penn Treebank style part of
speech tagger.  The tagger is experimental, and has not been evaluated
for accuracy. It is probable that the accuracy is low, primarily because
it has not been well tested. Because the tagging is based on the syntactic
parse, in principle the accuracy could be very high, once fully debugged.


Running RelEx
-------------
Several example unix shell scripts and MS Windows batch files are
included to show sample usage. These files (`*.sh` in unix, or `*.bat`,
in Windows) define the required system properties, classpath and JVM
options.

If there are any ClassNotFound exceptions, please verify the paths
and values in these files.


### `relation-extractor.sh`

The primary usage example is the `relation-extractor.sh` file.
Running this will display:

 - The link parser output.
 - The detected persons, organizations and locations.
 - The dependency relations found.
 - Anaphora resolutions.
 - Parse ranking info.
 - (Optionally) Stanford and Penn Treebank output.

Output is controlled by command-line flags that are set in the shell
script.  The `-h` flag will print a list of all of the available
command-line options.


### `batch-process.sh`

The `batch-process.sh` script is an example batch processing script.
This script outputs the so-called "compact (cff) format" which captures
the full range of Link Grammar and RelEx output in a format that can be
easily post-processed by other systems (typically by using regex's).

The idea behind the batch processing is that it is costly to parse
large quantities of text: thus, it is convenient to parse the text
once, save the results, and then perform post-processing at leisure,
as needed.  Thus, the form of post-processing can be changed at will,
without requiring texts to be re-processed over and over again.


### `src/perl/cff-to-opencog.pl`

This perl script provides an example of post-processing: it converts
the "cff" batch output format into OpenCog hypergraphs, which can
then be processed by OpenCog.


### `opencog-server.sh`

This script starts a relex server that  listens for plain-text input
(English sentences) on port 4444. It then parses the text, and returns
opencog output on the same socket.  This server is meant to serve the
OpenCog chatbot directly; it is not intended for general, manual use.


### `doc-splitter.sh`

The `doc-splitter.sh` file is a simple command-line utility to reformat
a free-form text into sentences, one per line.


### `src/perl/wiki-scrub.pl`

Ad-hoc script to scrub Wikipedia xml dumps, outputting only valid
English-language sentences.  This  script removes wiki markup, URL's
tables, images, & etc.  It currently seems to be pretty darned
bullet-proof, although it might handle multi-line refs incorrectly.

### `relexd`, `relexd-relex`, `relexd-link`

If you built RelEx with Maven, these scripts can be used.
They accept additional arguments to be passed to `relex.Server`.

1. `sh target/appassembler/bin/relexd`, which runs `java relex.Server ...`
2. `sh target/appassembler/bin/relexd-relex`, which runs `java relex.Server --relex ...`
3. `sh target/appassembler/bin/relexd-link`, which runs `relex.Server --link --relex --verbose ...`


Using RelEx in custom code
--------------------------
The primary output of RelEx is the set of semantic relationships of a
sentence. To obtain the list of these relationships, make a copy of
`src/java/relex/output/SimpleView.java`, and customize it to provide
the relationships that you wish, in the format that you wish.

The class `src/java/relex/RelationExtractor.java` should be considered
to be a large example program illustrating all of the various features
of RelEx.  For custom applications, this class should be copied and
modified as desired to fit the application.


Speed test results
------------------
Performance comparison of RelEx-1.2.0 vs. Stanford-1.6.1, run 11 Oct 2009.
Test corpus: first 150 sentences (including preface boilerplate) from
Project Gutenberg "Pride and Prejudice".  Due to differences in sentence
detection, Stanford and RelEx disagree on the sentence count. Due to
differences in counting punctuation, the splitting of possessives and
contractions, the two disagree on the word count as well.

Since these tests were run, the performance of link-grammar has been
improved by a factor of 2x-3x. This update should have a significant
effect on relex speeds.

The unix command `wc` counts 2609 words in 148 sentences, for
2609/148 = 17.6 words/sent.

### Stanford, w/ englishFactored.ser.gz , w/unix `time` command:

      real	10m4.882s
      user	10m1.974s
      sys	0m4.208s

   Actual: 2609/605= 4.31 words/sec

### Stanford, w/ englishPCFG.ser.gz , w/unix `time` command:

      real	2m21.690s
      user	2m23.165s
      sys	0m1.056s

   Actual: 2609/143 = 18.24 words/sec

### Stanford, w/ wsjFactored.ser.gz , w/unix `time` command:

      real	10m5.972s
      user	10m3.802s
      sys	0m4.516s

### Stanford, w/ wsjPCFG.ser.gz , w/unix `time` command:

      real	2m11.154s
      user	2m14.312s
      sys	0m1.144s

   Actual: 2609/134 = 19.47 words/sec

### RelEx, w/unix `time` command:

      real	2m59.739s
      user	2m36.342s
      sys	0m22.137s

   Actual: 2609/180 = 14.50 words/sec

      Ratio: Stanford-englishFactored/RelEx = 605sec/180sc = 3.36x (faster)
      Ratio: Stanford-wsjFactored/RelEx = 606sec/180sec = 3.36x (faster)
      Ratio: Stanford-englishPCFG/RelEx = 143sec/180sec = 0.79x (slower)
      Ratio: Stanford-wsjPCFG/RelEx = 134sec/180sec = 0.74x (slower)


TODO
----

### TODO - Comparatives

RelEx is buggy when it comes to handling comparative sentences.
This needs fixing.

### TODO - Wordnet Install

Windows users consistently have trouble installing Wordnet correctly.
In particular, dictionary location appears to be totally random. Try
to find some work-around for this.


Bugs
----
Sentence splitter:
The sentence splitter fails to split the following:

"In such cases, a woman has not often much beauty to think of."  "But,
my dear, you must indeed go and see Mr. Bingley when he comes into the neighbourhood."

todo

 - write paper on wsd by pos-lookup
 - write paper on relex overview

Notes
-----

Lexical Chunking
----------------

Key ideas:
 * Lexis is the basis of language.
 * Language consists of grammaticalized lexis, not lexicalized grammar.

See: Olga Moudraia, "Lexical Approach to Second Language Teaching"
http://www.cal.org/resources/digest/0102lexical.html

Alternate names: "gambits", "lexical phrases", "lexical units",
"lexicalized stems", "speech formulae".

Definition of Lexical Chunks
----------------------------
Lexis may be single words, and also the word combinations that are a
basis of one's mental lexicon.  That is, language consists of meaningful
chunks that, when combined, produce continuous coherent text; only a
minority of spoken sentences are entirely novel creations.

Types of lexical chunks:
 * Words (e.g., book, pen)
 * Phrasal verbs (e.g. switch off, talk to ... about ...)
 * Polywords (e.g., by the way, upside down)
 * Collocations, or word partnerships (e.g., community service,
   absolutely convinced)
 * Idioms (e.g. break a leg, back in the day)
 * Institutionalized utterances (e.g., I'll get it; We'll see;
   That'll do; If I were you . . .; Would you like a cup of coffee?)
 * Sentence frames and heads (e.g., That is not as . . . as you think;
   The fact/suggestion/problem/danger was . . .)
 * Text frames (e.g., In this paper we explore . . .; Firstly . . .;
   Secondly . . .; Finally . . .)

(Taken from Lewis, M. (1997b). "Pedagogical implications of the lexical
approach." In J. Coady & T.  Huckin (Eds.), "Second language vocabulary
acquisition: A rationale for pedagogy" (pp.  255-270). Cambridge:
Cambridge University Press.)
