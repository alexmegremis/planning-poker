#FROM openjdk:11-jdk-oracle
FROM adoptopenjdk/openjdk11:x86_64-debian-jdk-11.0.10_9
EXPOSE 8080
RUN ls
RUN "find ."
ADD planning-poker/target/*.jar app.jar
ENTRYPOINT [“java”,"-jar","/app.jar"]
