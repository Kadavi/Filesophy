# Filesophy

This code comprises a web application that can be built with Maven if you really wanted to, or check it out here http://filesophy.herokuapp.com

It is a rough proof-of-concept design that modernizes uploading by adding seamless HTML5 fault tolerance to swerve around errors and prevent the user from having to repeat the process if a disconnection happens.

Bottom line: if you disconnect your internet connection during an upload using Filesophy, it will wait for a signal and continue normally.

## Running the application locally

First build with:

    $mvn clean install

Then run it with:

    $java -jar target/dependency/webapp-runner.jar target/*.war

