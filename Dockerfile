FROM eclipse-temurin:17-jre-alpine

COPY target/teiler.jar /app/

WORKDIR /app

CMD ["java", "-jar", "teiler.jar"]
