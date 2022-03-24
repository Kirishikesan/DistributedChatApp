# DistributedChatApp


## Instructions to build executable jar file using IntelliJ IDEA
1. open the "DistributedChatApp" Java project using IntelliJ IDEA
2. Open File -> Project Structure -> Project Setting -> Modules -> Press + button(add dependency)
3. Select JAR or directories
4. Add all the JAR files that located in Libraries folder. Those are,
   1. json-simple-1.1.jar
   2. mysql-connector-java-8.0.28.jar
   3. protobuf-java-3.11.4.jar
5. Click OK
6. Open File -> Project Structure -> Project Setting -> Artifact -> Press + button
7. Select JAR -> From module with dependencies -> Pick Main class as  App.java
8. Click OK
9. Open Build -> Build Artifacts - DistributedChatApp.jar -> Build
10. Find the DistributedChatApp.jar file in "DistributedChatApp\out\artifacts\DistributedChatApp_jar" directory


## Instructions to Run the Jar
1.connect MYSQL local server
   1.1 run mysql in local server(localhost) using 3306 port
   1.2 setup a user in mysql in this way
        username = "root"
        password = ""

2.setup database and table
   2.1 create a database as "chatApp"
   2.2 create table as "activeViews"(run following command in SQL tab) ->
        create table activeViews(server_id VARCHAR(5), view_list VARCHAR(255));

3.run the jar file
   3.1 open the terminal inside the DistributedChatApp.jar file location
   3.2 build the executable jar file using IntelliJ IDEA as above mention
   3.3 run jar file using following command in terminal
        java -jar DistributedChatApp.jar [server_name] "[location of server config file]"
        eg: java -jar DistributedChatApp.jar s1 server_config.txt