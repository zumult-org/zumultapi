# zumultapi

This is the code from the ZuMult project (http://zumult.org), transferred from UHH's Gitlab to Github on 03 May 2022.


## maven

       <repository>
            <id>github</id>
            <url>https://maven.pkg.github.com/zumult-org/zumultapi</url>
        </repository>   

        <dependency>
            <groupId>org.zumult</groupId>
            <artifactId>zumultapi</artifactId>
            <version>0.0.6-alpha</version>
        </dependency>     


## Deploying using docker

**To be updated soon**

Build image (also builds war and deploys using tomcat):

`docker build . -t zumult`

Run the image in container:

`docker run --rm -p 8080:8080 --name zumult zumult:latest`

and vist http://localhost:8080/zumult/

Specifiying a different (local) folder as the corpusdata folder for container:

`docker run --rm -p 8080:8080 -v /your/local/path/to/data:/home/corpusdata --name zumult zumult:latest`
