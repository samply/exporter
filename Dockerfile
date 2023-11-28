FROM eclipse-temurin:20-jre

COPY target/exporter.jar /app/

WORKDIR /app

RUN apt-get update && apt-get upgrade -y && apt-get install python3-pip -y &&  \
    apt-get install libcurl4-openssl-dev libssl-dev -y && \
    python3 -m pip install obiba-opal && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*


CMD ["sh", "-c", "java $JAVA_OPTS -jar exporter.jar"]
