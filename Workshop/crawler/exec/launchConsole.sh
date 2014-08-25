#!/bin/sh
java -javaagent:extra/aspectjweaver.jar -Dorg.aspectj.tracing.factory=default -Djava.library.path=extra/ -jar crawler-1.0-SNAPSHOT.jar