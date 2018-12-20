# GeRDI Harvester Image for 'FishStatJ'

FROM jetty:9.4.7-alpine

COPY \/target\/*.war $JETTY_BASE\/webapps\/fishstatj.war

EXPOSE 8080