ARG PROJECT_DIRNAME="/web-application-server"

FROM maven:3.8.3-openjdk-17 AS builder
ARG PROJECT_DIRNAME
RUN ["mkdir", "-p", "${PROJECT_DIRNAME}"]
WORKDIR ${PROJECT_DIRNAME}
COPY ./pom.xml ./pom.xml
RUN ["mvn", "clean"]
RUN ["mvn", "dependency:go-offline"]
COPY ./src ./src
RUN ["mvn", "package"]

FROM openjdk:17-alpine
ARG PROJECT_DIRNAME
RUN ["mkdir", "-p", "${PROJECT_DIRNAME}"]
WORKDIR ${PROJECT_DIRNAME}
COPY --from=builder "${PROJECT_DIRNAME}/target" ${PROJECT_DIRNAME}
CMD ["java", "-jar", "web-application-server.jar"]
