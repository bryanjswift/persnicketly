#!/bin/sh

if [ -x project/java_args.sh ]; then
  PERSNICKETLY_JAVA_ARGS=`./project/java_args.sh`
else
  echo "To add to JAVA_ARGS for this project create ./project/java_args.sh"
  echo "and make sure ./project/java_args.sh is executable."
  PERSNICKETLY_JAVA_ARGS=""
fi

java $PERSNICKETLY_JAVA_ARGS -jar ./project/sbt-launch-0.7.7.jar "$@"
