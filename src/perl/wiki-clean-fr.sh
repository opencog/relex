#! /bin/sh
#
# Delete unwanted article types from the wikipedia article space.
# We won't be parsing these; they (mostly) don't contain any valid
# French-language sentences.
#
# Copyright (c) 2008, 2013 Linas Vepstas <linas@linas.org>

keep mediawiki

Catégorie
Fichier:
Modèle
Wikipédia

echo "Catégorie:"
find . -name 'Catégorie:*' -print | wc
find . -name 'MediaWiki:*' -print | wc
find . -name 'Help:*' -print | wc
echo "Fichier:"
find . -name 'Fichier:*' -print | wc
find . -name 'Image:*' -print | wc
echo "Modèle"
find . -name 'Modèle:*' -print | wc
find . -name 'Wikipédia:*' -print | wc
find . -name '"List of "*' -print | wc
find . -name '"Lists of "*' -print | wc

# Must use "find" to accomplish this, since using "rm Catégorie:*"
# leads to an overflow of the command line.

echo "Catégorie:"
time find . -name 'Catégorie:*' -exec rm {} \;
time find . -name 'MediaWiki:*' -exec rm {} \;
time find . -name 'Help:*' -exec rm {} \;
# Fichier: includes mp3's, ogg's, many different image types
echo "Fichier:"
time find . -name 'Fichier:*' -exec rm {} \;
time find . -name 'Image:*' -exec rm {} \;
echo "Modèle"
time find . -name 'Modèle:*' -exec rm {} \;
time find . -name 'Wikipédia:*' -exec rm {} \;
time find . -name '"List of "*' -exec rm {} \;
time find . -name '"Lists of "*' -exec rm {} \;
