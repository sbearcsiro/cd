#!/bin/sh
sbt assembly
rsync -avz --progress --partial target/scala-2.11/cd-assembly-1.0.jar volunteer-dev.ala.org.au:

#scp target/scala-2.11/cd-assembly-1.0.jar volunteer-dev.ala.org.au:

