# Imagem da aplicação. Gere o JAR antes: ./mvnw -DskipTests package
FROM eclipse-temurin:21-jre
RUN useradd --system --uid 1001 sor
WORKDIR /app
COPY target/sistema-organizacao-recursos-*.jar app.jar
USER sor
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
