#! /bin/bash
#
# Run the docker container. Stop any previously running copies.
#
./stop.sh relex-plain
docker run --rm --name="relex-plain" -p 3333:3333 \
   -w /home/Downloads/relex-master opencog/relex /bin/sh plain-text-server.sh

clear
