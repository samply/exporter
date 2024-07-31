FROM maven:3-eclipse-temurin-20 AS build

WORKDIR /app
COPY . ./
RUN mvn clean install -U

FROM bellsoft/liberica-openjre-alpine:20
RUN apk add --no-cache python3 py3-pip && python3 -m pip install obiba-opal
#TODO  Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'converterManager' defined in URL [jar:nested:/app/exporter.jar/!BOOT-INF/classes/!/de/samply/converter/ConverterManager.class]: Failed to instantiate [de.samply.converter.ConverterManager]: Constructor threw exception

WORKDIR /app
COPY --from=build /app/target/exporter*.jar /app/exporter.jar

CMD ["java", "-jar", "exporter.jar"]
