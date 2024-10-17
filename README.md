# zumultapi

This is the code from the ZuMult project (http://zumult.org), transferred from UHH's Gitlab to Github on 03 May 2022


## Building using maven

Install EXMARaLDA jar first and then package jar:

`mvn install:install-file -Dfile=lib/EXMARaLDA_WEB.jar -DgroupId=org.exmaralda -DartifactId=EXMARaLDA -Dversion=Preview-20220623 -Dpackaging=jar`

`mvn clean package`

Alternatively package a war:

`mvn clean package "-Dproject.packaging=war"`


## Deploying using docker

Build image (also builds war and deploys using tomcat):

`docker build . -t zumult`

Run the image in container:

`docker run --rm -p 8080:8080 --name zumult zumult:latest`

and vist http://localhost:8080/zumult/

Specifiying a different (local) folder as the corpusdata folder for container:

`docker run --rm -p 8080:8080 -v /your/local/path/to/data:/home/corpusdata --name zumult zumult:latest`
