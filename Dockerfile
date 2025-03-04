FROM registry.cn-beijing.aliyuncs.com/itcast/openjdk:17-jdk-alpine
LABEL maintainer="研究院研发组 <research-maint@itcast.cn>"
ENV JAVA_OPTS=""
# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 安装 glibc 兼容层
RUN apk add --no-cache --upgrade apk-tools && \
    apk add --no-cache --virtual .glibc \
    --repository https://dl-cdn.alpinelinux.org/alpine/edge/testing \
    gcompat \
    glibc \
    glibc-dev \
    libstdc++ \
    libuuid \
    libgcc

# 设置 glibc 库路径（关键！）
ENV LD_LIBRARY_PATH=/lib:/usr/glibc/compat/lib

WORKDIR /app
ADD app.jar /app/app.jar

ENTRYPOINT ["sh","-c","java  -jar $JAVA_OPTS /app/app.jar"]
