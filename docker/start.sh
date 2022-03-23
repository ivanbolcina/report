#!/bin/sh
export JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Xmx1512m -XX:+UseZGC"

exec java $JAVA_OPTS -jar report-1.0.0.jar