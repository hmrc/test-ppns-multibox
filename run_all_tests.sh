#!/usr/bin/env bash
SBT_OPTS="-XX:MaxMetaspaceSize=400M" sbt clean compile coverage scalastyle test it:test coverageReport
