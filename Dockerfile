FROM openjdk:8-jdk-alpine
EXPOSE 8080
ADD planning-poker/target/*.jar app.jar
ENTRYPOINT [“java”,"-jar","/app.jar"]
