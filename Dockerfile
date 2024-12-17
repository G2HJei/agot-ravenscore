FROM eclipse-temurin:21-alpine
MAINTAINER zlatanov.xyz
COPY target/ravenscore.jar ravenscore.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/ravenscore.jar"]