FROM registry.cn-beijing.aliyuncs.com/itcast/openjdk:17-jdk-eclipse-temurin
LABEL maintainer="研究院研发组 <research-maint@itcast.cn>"
ENV JAVA_OPTS=""
# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 安装依赖
RUN apt-get update && \
    apt-get install -y \
    libstdc++6 \
    uuid-runtime && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
ADD app.jar /app/app.jar

ENTRYPOINT ["sh","-c","java  -jar $JAVA_OPTS /app/app.jar"]
