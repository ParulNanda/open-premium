FROM dockering/oracle-java8

ARG APP_NAME
ARG APP_VERSION

ADD target/${APP_NAME}-${APP_VERSION}.jar app.jar
RUN sh -c 'touch /app.jar'

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]
