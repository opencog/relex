#! /bin/sh
#
# Delete unwanted article types from the wikipedia article space.
# We won't be parsing these; they (mostly) don't contain any valid
# Polish-language sentences.
#
# Copyright (c) 2008, 2013 Linas Vepstas <linas@linas.org>

Keep media, wiki --- 

Pilk:
Lista:
Obraz:
Pomoc:

echo "Kategoria:"
find . -name 'Kategoria:*' -print | wc
find . -name 'MediaWiki:*' -print | wc
find . -name 'Help:*' -print | wc
echo "File:"
find . -name 'File:*' -print | wc
find . -name 'Image:*' -print | wc
echo "Szablon"
find . -name 'Szablon:*' -print | wc
find . -name 'Wikipedia:*' -print | wc
find . -name '"List of "*' -print | wc
find . -name '"Lists of "*' -print | wc

# Must use "find" to accomplish this, since using "rm Kategoria:*"
# leads to an overflow of the command line.

echo "Kategoria:"
time find . -name 'Kategoria:*' -exec rm {} \;
time find . -name 'MediaWiki:*' -exec rm {} \;
time find . -name 'Help:*' -exec rm {} \;
# File: includes mp3's, ogg's, many different image types
echo "File:"
time find . -name 'File:*' -exec rm {} \;
time find . -name 'Image:*' -exec rm {} \;
echo "Szablon"
time find . -name 'Szablon:*' -exec rm {} \;
time find . -name 'Wikipedia:*' -exec rm {} \;
time find . -name '"List of "*' -exec rm {} \;
time find . -name '"Lists of "*' -exec rm {} \;
