FROM centos:7

RUN yum install -y java-1.8.0-openjdk-headless \
    && yum clean all -y \
    && mkdir -p /opt/lib \
    && mkdir -p  /data/home/my-attachment/config \
    && mkdir -p  /data/home/my-attachment/lib

COPY target/my-attachment-0.0.1-SNAPSHOT.jar /opt/lib

WORKDIR  /opt/lib

CMD ["java","-jar",\
            "-server",\
            "-Xmx300m",\
            "-Xms300m",\
            "-Xmn100m",\
            "-Dspring.config.location=/data/home/my-attachment/config/application.properties",\
            "-Dlogging.config=/data/home/my-attachment/config/logback.xml",\
            "/opt/lib/my-attachment-0.0.1-SNAPSHOT.jar"]

