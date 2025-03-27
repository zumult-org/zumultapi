#########################################
# Dockerfile for setting up the ZuMult Application.
# This installs dependencies to the system, 
# then compiles the code using maven
# and in the second stepsinstalls it in 
# a Tomcat application server.
#########################################
#
# Build stage
#
FROM maven:3.6.3-openjdk-17-slim AS build
ENV HOME=/usr/app
RUN mkdir -p $HOME
ADD . $HOME
WORKDIR $HOME
RUN mvn clean package "-Dproject.packaging=war"
COPY target/zumultapi.war /usr/app/target/zumultapi.war
#
# Deploy stage
#
FROM tomcat:9-jdk17
ARG WAR_FILE=/usr/app/target/zumultapi.war
LABEL org.opencontainers.image.source=https://github.com/zumult-org/zumultapi

ENV CATALINA_WEBAPP=${CATALINA_HOME}/webapps

USER root:root

RUN apt-get update \
    && apt-get install --no-install-recommends -y unzip ffmpeg git git-lfs praat
ENV HOME=/usr/app
ENV ZUMULT_CONFIG_PATH=/usr/app/Configuration.xml
ENV CORPUSDATA=/home/corpusdata
ARG GITHUB_PAT
ARG HTTPS_CERT=false
ENV JAVA_TOOL_OPTIONS=-Djavax.xml.transform.TransformerFactory="net.sf.saxon.TransformerFactoryImpl"

VOLUME ["/home/corpusdata"]

RUN mkdir -p $HOME
WORKDIR $HOME
COPY --from=build $WAR_FILE /usr/app/zumultapi.war
COPY --from=build $HOME/docker-entrypoint.sh /usr/app/docker-entrypoint.sh
COPY --from=build $HOME/docker-config/server.xml /usr/app/server.xml
COPY --from=build $HOME/docker-config/Configuration.xm[l] /usr/app/Configuration.xml
COPY --from=build $HOME/docker-config/Configuration_UDE.xml /usr/app/Configuration_UDE.xml
COPY --from=build $HOME/docker-config/web.xml /usr/app/web.xml
COPY --from=build $HOME/docker-config/server.[p]12 /usr/app/server.p12
RUN mkdir -p $CORPUSDATA
RUN git clone --depth 1 https://$GITHUB_PAT@github.com/zumult-org/exmaraldademocorpus.git
WORKDIR exmaraldademocorpus
RUN git lfs install && git lfs pull
RUN git status
WORKDIR $HOME
RUN mv exmaraldademocorpus/src/main/java/data/* $CORPUSDATA
RUN if [ "$HTTPS_CERT" = "true" ] ; then \
    mv -f /usr/app/Configuration_UDE.xml /usr/app/Configuration.xml; \
    fi 

EXPOSE 8080
EXPOSE 8443
# configuration and deployment as tomcat webapp   
RUN cp -f /usr/app/server.xml ${CATALINA_HOME}/conf/server.xml \
    && unzip -q /usr/app/zumultapi.war -d ${CATALINA_WEBAPP}/zumultapi \
    && mkdir -p ${CATALINA_WEBAPP}/zumultapi/downloads \
    && rm -Rf ${CATALINA_WEBAPP}/ROOT \
    && chmod 755 /usr/app/docker-entrypoint.sh \
    && cp -f /usr/app/web.xml ${CATALINA_WEBAPP}/zumultapi/WEB-INF/web.xml 
RUN if [ "$HTTPS_CERT" = "true" ] ; then \
    mkdir -p ${CATALINA_HOME}/ssl \
    && cp -f /usr/app/server.p12 ${CATALINA_HOME}/ssl/server.p12; \
    fi 


HEALTHCHECK CMD curl --fail http://localhost:8080/zumultapi || exit 1

