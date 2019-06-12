FROM openjdk:8

RUN mkdir /opt/jackrabbit

COPY repository.xml /opt/jackrabbit

RUN mkdir /usr/src/microcontentrepository

COPY micro-contentrepository.jar /usr/src/microcontentrepository

WORKDIR /usr/src/microcontentrepository

CMD ["java", "-jar", "micro-contentrepository.jar"]