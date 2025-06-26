FROM ubuntu:latest

RUN apt-get update && apt-get install -y \
    default-jdk \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
