#!/bin/bash

sm2 --start AUTH OAUTH THIRD_PARTY_APPLICATION THIRD_PARTY_DELEGATED_AUTHORITY
sm2 --start PUSH_PULL_NOTIFICATIONS_API --appendArgs '{"PUSH_PULL_NOTIFICATIONS_API": ["-Dallowlisted.useragents.0=test-ppns-multibox"]}'

./run_local.sh
