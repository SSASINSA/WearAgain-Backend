# ---------- Build stage ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon --info dependencies || true

COPY src src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon \
      -Dorg.gradle.jvmargs="-Xmx2g" \
      -Dorg.gradle.caching=true -Dorg.gradle.parallel=true \
      clean bootJar -x test

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-alpine
ENV TZ=Asia/Seoul
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]
