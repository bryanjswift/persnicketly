#!/bin/sh
OUT=/home/bryanjswift/backups/mongo
DATE=`date "+%Y-%m-%dT%H%M"`
mongodump --host 127.0.0.1 --port 27017 --db persnicketly --out $OUT
mv $OUT/persnicketly $OUT/persnicketly-$DATE
cd $OUT
tar cfz persnicketly-$DATE.tar.gz persnicketly-$DATE
cd -
rm -rf $OUT/persnicketly-$DATE
