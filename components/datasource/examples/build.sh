#!/bin/sh

BASE=`dirname $0`  # Directory containing this script. Not the same as $PWD.

$BASE/../../../build.sh -f $BASE/build.xml $@
