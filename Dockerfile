FROM oracle/graalvm-ce:latest AS build-env

RUN gu install native-image
ADD target/lang-1.0-SNAPSHOT-jar-with-dependencies.jar snc.jar

RUN native-image \
    -H:IncludeResourceBundles=net.sourceforge.argparse4j.internal.ArgumentParserImpl \
    --static \
    -jar snc.jar \
    snc

FROM scratch
WORKDIR /usr/bin
COPY --from=build-env snc ./

CMD snc
