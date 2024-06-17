FROM openjdk:17-slim
RUN apt update \
&& DEBIAN_FRONTEND=noninteractive \
apt-get install -y maven libxext6 libxrender1 libxtst6 \
&& apt-get clean \
&& rm -rf /var/lib/apt/lists/* 
WORKDIR /app
COPY . /app/
CMD mvn compile && mvn exec:java -Dexec.mainClass=${FILE}
