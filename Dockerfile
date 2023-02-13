FROM eclipse-temurin:19-jre-alpine

COPY target/exporter.jar /app/

WORKDIR /app

RUN apk upgrade

CMD ["java", "-jar", "exporter.jar"]
