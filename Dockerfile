FROM eclipse-temurin:19-jre-alpine

COPY target/teiler.jar /app/

WORKDIR /app

RUN apk upgrade

CMD ["java", "-jar", "teiler.jar"]
