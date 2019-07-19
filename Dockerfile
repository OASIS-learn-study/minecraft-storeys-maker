FROM openjdk:8
# TODO FROM openjdk:11 AS builder
# TODO FROM registry.access.redhat.com/ubi8/ubi
# TODO RUN yum install java-11-openjdk-devel
COPY . .
# TODO RUN git reset --hard; git clean -d -f -x
RUN ./gradlew clean build

# FROM gcr.io/distroless/java:11
# COPY --from=builder web/build/libs/ .
