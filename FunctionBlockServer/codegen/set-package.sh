#!/bin/sh

if [ "$#" -ne 2 ]; then
	echo "Usage: $0 <NodeSet2.xml file> <package>"
else
	FILE=$1
	PACKAGE=$2
	if [ ! -f $FILE ]; then
		echo "File '$FILE' does not exist."	
	else
		PACKAGEFILE="${FILE%.*}.package"
		echo "Setting package for file '$FILE' to '$PACKAGE' in file '$PACKAGEFILE'."
		echo $PACKAGE > $PACKAGEFILE
	fi
fi

