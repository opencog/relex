#! /bin/sh
#
# Create subdirectories for alphabetized wikipedia articles,
# Move articles from main dir into subdirs.
#
# Copyright (c) 2008, 2013 Linas Vepstas <linas@linas.org>

mkdir A
mkdir B
mkdir C
mkdir D
mkdir E
mkdir F
mkdir G
mkdir H
mkdir I
mkdir J
mkdir K
mkdir L
mkdir M
mkdir N
mkdir O
mkdir P
mkdir Q
mkdir R
mkdir S
mkdir T
mkdir U
mkdir V
mkdir W
mkdir X
mkdir Y
mkdir Z
mkdir num
mkdir misc

# Must use find to do this, since "mv dir/A* otherdir/A"
# leads to an overflow on the shell command line.
echo "start A"
time find ../wiki-stripped -name 'A*' -exec mv {} A \;
time find ../wiki-stripped -name 'B*' -exec mv {} B \;
time find ../wiki-stripped -name 'C*' -exec mv {} C \;
time find ../wiki-stripped -name 'D*' -exec mv {} D \;
echo "start E"
time find ../wiki-stripped -name 'E*' -exec mv {} E \;
time find ../wiki-stripped -name 'F*' -exec mv {} F \;
time find ../wiki-stripped -name 'G*' -exec mv {} G \;
time find ../wiki-stripped -name 'H*' -exec mv {} H \;
echo "start I"
time find ../wiki-stripped -name 'I*' -exec mv {} I \;
time find ../wiki-stripped -name 'J*' -exec mv {} J \;
time find ../wiki-stripped -name 'K*' -exec mv {} K \;
time find ../wiki-stripped -name 'L*' -exec mv {} L \;
echo "start M"
time find ../wiki-stripped -name 'M*' -exec mv {} M \;
time find ../wiki-stripped -name 'N*' -exec mv {} N \;
time find ../wiki-stripped -name 'O*' -exec mv {} O \;
time find ../wiki-stripped -name 'P*' -exec mv {} P \;
time find ../wiki-stripped -name 'Q*' -exec mv {} Q \;
echo "start R"
time find ../wiki-stripped -name 'R*' -exec mv {} R \;
time find ../wiki-stripped -name 'S*' -exec mv {} S \;
echo "start T"
time find ../wiki-stripped -name 'T*' -exec mv {} T \;
time find ../wiki-stripped -name 'U*' -exec mv {} U \;
time find ../wiki-stripped -name 'V*' -exec mv {} V \;
time find ../wiki-stripped -name 'W*' -exec mv {} W \;
echo "start X"
time find ../wiki-stripped -name 'X*' -exec mv {} X \;
time find ../wiki-stripped -name 'Y*' -exec mv {} Y \;
time find ../wiki-stripped -name 'Z*' -exec mv {} Z \;
time find ../wiki-stripped -name '[0-9]*' -exec mv {} num \;
time find ../wiki-stripped -name '*' -type f -exec mv {} misc \;

