#FROM docker.verbis.dkfz.de/ccp/exporter-base:latest # For development. Image with Java, Python and the Opal Client
FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get upgrade -y && apt-get install python3-pip -y &&  \
    apt-get install libcurl4-openssl-dev libssl-dev -y && \
    python3 -m pip install obiba-opal && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY target/exporter.jar /app/

WORKDIR /app

CMD ["sh", "-c", "java $JAVA_OPTS -jar exporter.jar"]
