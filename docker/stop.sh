#! /bin/bash
#
# Stop any running containers previously started with 'run.sh'
#
NAME=$1
if test x"$1" == x; then
   NAME=relex
fi
TAINER=`docker ps |grep $NAME |cut -f1 -d" "`
if test x"$TAINER" != x; then
	echo -n 'Stopping leftover container.. '
	docker stop -t 1 $TAINER
fi
TAINER=`docker ps -a |grep $NAME |cut -f1 -d" "`
if test x"$TAINER" != x; then
	echo -n 'Removing.. '
	docker rm $TAINER
fi
