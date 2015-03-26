#! /bin/sh

export RSYNC_RSH=ssh

rsync -azv --delete-after \
  target/base/target/docs/clover \
  target/base/target/docs/apidocs \
  \
  minotaur.apache.org:/www/excalibur.apache.org/
