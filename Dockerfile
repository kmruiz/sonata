# Generate fat-jar
FROM maven AS maven
WORKDIR /home/compiler
ADD ./src ./src
ADD ./pom.xml ./pom.xml
RUN mvn install -Dmaven.test.skip=true

# Generate a native-image
FROM oracle/graalvm-ce:latest AS graalvm
RUN gu install native-image
COPY --from=maven /home/compiler/target/lang-1.0-SNAPSHOT-jar-with-dependencies.jar ./snc.jar

RUN native-image \
    --no-server \
    -H:IncludeResources=.+.sn \
    -H:IncludeResourceBundles=net.sourceforge.argparse4j.internal.ArgumentParserImpl \
    --static \
    -jar snc.jar \
    snc

# Package into a scratch image
FROM scratch
WORKDIR /usr/bin
COPY --from=graalvm snc ./
WORKDIR /code
CMD /usr/bin/snc
