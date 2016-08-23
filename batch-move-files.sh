#!/bin/bash
#
# batch-move-files.sh -- move parsed files from one directory to another.
# Moves a fixed number of files per run.
#
# Copyright (c) 2011 Linas Vepstas

# IFS=$(echo -en "\n\b")

FILES=parsed/*/*

mkdir enwiki-20101011
cd enwiki-20101011
mkdir parsed
cd parsed
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

cd ../..


cnt=0;
for fpath in $FILES
do
	f=${fpath##*/}
	
	# echo  "$fpath"
	mv "${fpath}" "enwiki-20101011/${fpath}"
	let cnt=cnt+1

	if [ $cnt -gt 40000 ];
	then
		break
	fi
	# echo $cnt
done
