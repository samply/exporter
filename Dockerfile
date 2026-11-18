FROM eclipse-temurin:25-jre

# Add pipx binary path to PATH environment variable
ENV PATH="$PATH:/root/.local/bin"

COPY target/exporter.jar /app/

WORKDIR /app

CMD ["sh", "-c", "java $JAVA_OPTS -jar exporter.jar"]
