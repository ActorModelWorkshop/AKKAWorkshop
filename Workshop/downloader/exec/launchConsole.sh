#!/bin/sh
java -javaagent:extra/aspectjweaver.jar -Dorg.aspectj.tracing.factory=default -Djava.library.path=extra/ -jar downloader-1.0-SNAPSHOT.jar