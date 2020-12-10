#!/usr/bin/env bash

if [ ! -d "$CDP_USER_SOURCE_FOLDER_CONTAINER_PATH" ]; then
    >&2 echo "CDP_USER_SOURCE_FOLDER_CONTAINER_PATH ('$CDP_USER_SOURCE_FOLDER_CONTAINER_PATH') does not exist"
    exit 1
fi

GRADLEW_PATH="$CDP_USER_SOURCE_FOLDER_CONTAINER_PATH/gradlew"
if [ ! -f "$GRADLEW_PATH" ]; then
    >&2 echo "GRADLEW_PATH ('$GRADLEW_PATH') does not exist"
    exit 1
fi

export JAVA_JRE_7=/usr/lib/jvm/zulu-7-amd64/jre
export COLLECTD_HOME=/collectd-stage

if [ ! -d "$COLLECTD_HOME" ]; then
    >&2 echo "COLLECTD_HOME ('$COLLECTD_HOME') does not exist"
    ls -FAl /
    exit 1
fi

$GRADLEW_PATH -p "$CDP_USER_SOURCE_FOLDER_CONTAINER_PATH" --info --stacktrace -DisBuildServer=true --warning-mode=all :collectd:build
