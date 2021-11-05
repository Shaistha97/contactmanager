# Pull base image 
From tomcat:8-jre8 

# Maintainer 
MAINTAINER "Shaistha Seema" 
COPY ./webapp.war /usr/local/tomcat/webapps
