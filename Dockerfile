FROM eclipse-temurin:21-alpine
MAINTAINER zlatanov.xyz
COPY target/ravenscore.jar ravenscore.jar
EXPOSE 8080
ENTRYPOINT ["java","-Duser.timezone=UTC","-jar","/ravenscore.jar"]