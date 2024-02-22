FROM amazoncorretto:17 as build

WORKDIR /workspace/app

COPY gradle gradle
COPY build.gradle settings.gradle gradlew ./
COPY src src

# 빌드 진행
RUN ./gradlew bootJar
RUN mkdir -p build/libs/dependency && (cd build/libs/dependency; jar -xf ../*.jar) # dependency 추출

FROM amazoncorretto:17

VOLUME /tmp
ARG DEPENDENCY=/workspace/app/build/libs/dependency

#추출된 dependency 카피하기
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","com.greenroom.server.api.ServerApplication"]