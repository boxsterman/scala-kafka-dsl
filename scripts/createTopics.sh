#!/usr/bin/env bash

kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 8 --config retention.ms=-1 --topic topic-in
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 8 --config retention.ms=-1 --topic topic-in-2
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 8 --config retention.ms=-1 --topic topic-out

# Example for compaction
#kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 8 --config cleanup.policy=compact --topic XXX
