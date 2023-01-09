FROM eclipse-temurin:19-jre-alpine

COPY target/teiler.jar /app/

WORKDIR /app

CMD ["java", "-jar", "teiler.jar"]
