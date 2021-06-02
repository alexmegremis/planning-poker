FROM openjdk:11-jdk-oracle
EXPOSE 8080
RUN "ls -lA"
RUN "find ."
ADD planning-poker/target/*.jar app.jar
ENTRYPOINT [“java”,"-jar","/app.jar"]
