#!/bin/sh
rm *.class
javac AuthorizationInfo.java
jar cvfm auth.jar manifest *.class
