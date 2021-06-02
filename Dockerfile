FROM openjdk:8-jdk-alpine
EXPOSE 8080
RUN "ls -lA"
RUN "find ."
ADD planning-poker/target/*.jar app.jar
ENTRYPOINT [“java”,"-jar","/app.jar"]
