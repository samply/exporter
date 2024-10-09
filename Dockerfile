# For development. Image with Java, Python and the Opal Client:
#FROM docker.verbis.dkfz.de/ccp/exporter-base:latest
FROM eclipse-temurin:21-jre

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y --no-install-recommends \
        python3-venv \
        pipx \
        libcurl4-openssl-dev \
        libssl-dev && \
    pipx ensurepath && \
    pipx install obiba-opal && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Add pipx binary path to PATH environment variable
ENV PATH="$PATH:/root/.local/bin"

COPY target/exporter.jar /app/

WORKDIR /app

CMD ["sh", "-c", "java $JAVA_OPTS -jar exporter.jar"]
