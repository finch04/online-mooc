FROM registry.cn-beijing.aliyuncs.com/itcast/openjdk:17-jdk-alpine
LABEL maintainer="研究院研发组 <research-maint@itcast.cn>"
ENV JAVA_OPTS=""
# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 安装 Alpine 的社区仓库依赖
RUN apk add --no-cache --upgrade apk-tools

# 安装 glibc 兼容层和 C++ 运行时库
RUN apk add --no-cache gcompat libstdc++ libgcc

WORKDIR /app
ADD app.jar /app/app.jar

ENTRYPOINT ["sh","-c","java  -jar $JAVA_OPTS /app/app.jar"]
