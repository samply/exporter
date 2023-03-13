FROM eclipse-temurin:19-jre-focal

COPY target/exporter.jar /app/

WORKDIR /app

RUN apt-get update && apt-get upgrade -y && apt-get install python3-pip -y &&  \
    apt-get install libcurl4-openssl-dev libssl-dev -y && \
    python3 -m pip install obiba-opal


CMD ["java", "-jar", "exporter.jar"]
