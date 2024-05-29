FROM amazoncorretto:17-alpine-jdk
ENV JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
CMD apt-get update -y
ENTRYPOINT ["java", "-Xmx2048M", "-jar", "/application.jar"]
