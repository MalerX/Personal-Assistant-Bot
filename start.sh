#!/bin/bash
./gradlew clean test shadowJar
echo $JAVA_OPTS
java $JAVA_OPTS -jar build/libs/personalAssistentBot-0.0.1-all.jar
