FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/tpathcache.jar /tpathcache/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/tpathcache/app.jar"]
