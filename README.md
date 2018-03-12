# OPC UA Function Blocks
This repository contains models and files that are accompanying the paper "Tool and technology independent function interfaces by using a generic OPC UA representation". 
This is just a demo application, it comes with no warranty and is just for showing the concepts of the paper.

## The OPC UA Information Model
The folder 'model' contains OPC UA Nodeset files, which are describing the model of the paper and in the namespace mxautomation a very easy example function block is modeled.

## The java example function block server
The folder 'FunctionBlockServer' contains java files for creating a very basic OPC UA Server which contains function block types and the MoveLinearRelative function block as example.

### Setup
For getting the server running you need to request a (evaluation) licence from the Prosys OPC UA SDK. And include the jar into the build-path. We used version 2.2.6-708. 
Additionally you need the OPC UA Java Stack. It normally comes with the Prosys library. We used Stack-1.02.337.10.

### Run the example server
Run the application by running FunctionBlockServer.java. 
The server should start up and in the console log you should get a message, that by pressing any button, you can instanciate the Function Block. This should create an instance
of MoveLinearRelative in the OPC UA objects folder. There you can parametrize the input ports and then call the Start-Method. E.g. by using UaExpert.