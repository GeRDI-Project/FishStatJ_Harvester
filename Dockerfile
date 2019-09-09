# GeRDI Harvester Image for 'FishStatJ'

FROM jetty:9.4.7-alpine

# copy war file
COPY target/*.war $JETTY_BASE/webapps/fishstatj.war

# set Java system variable to indicate how the harvester is executed
ENV JAVA_OPTIONS="-DDEPLOYMENT_TYPE=docker"

# create log file folder with sufficient permissions
USER root
RUN mkdir -p /var/log/harvester
RUN chown jetty:jetty /var/log/harvester
USER jetty

EXPOSE 8080