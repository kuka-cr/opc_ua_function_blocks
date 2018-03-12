# Java SDK source code generator

This tool reduces manual workload when using custom information models in OPC UA applications. It creates Java classes based on given 
information models (in the NodeSet2.xml format).

Also see `ReadmeSample.md`, which shows you by hand how to generate a sample model and integrate it into the sample applications.

(This file is written in markdown format. Although the files are very well readable as text, there are also various markdown viewers 
available, which can display the text in final document style. For example the 
[Markdown Editor plugin for Eclipse](http://www.winterwell.com/software/markdown-editor.php) has a Markdown HTML Preview.)

## 0. Install

The code generator can be used stand-alone, but it may be most convenient to copy the codegen directory to the SDK directory or your 
project directory.

Note that in the evaluation version of the SDK, the codegen is limiting generation to 10 types.

## 1. Configuration

Initial setup of the code generator with the `codegen.properties` file:

1. Insert the path to your information model file(s) in NodeSet2.xml format to the `codegen.properties.targets` property.
2. Insert the Java package of your information model(s) to `codegen.properties.package` property. Alternatively, you can specify a 
  package for each model with the included `set-package` scripts.
3. Optionally select the templates you want to use for code generation.
4. Optionally customize the output root location of the code generator with the `codegen.properties.output.intf` and `codegen.properties.output.impl` 
   properties. The default locations are `../generated-intf` and `../generated-impl`. Note that folders corresponding to the Java packages are created 
   automatically.

### 1.1 Notes about naming methods in information models

The default templates will generate a setter and getter for each variable, e.g. `getVar()` and `setVar(...)`. If you define an UA method 
with the same name and argument type, e.g. `getVar` or `setVar` you will get a name clash. You can override this problem by not using 
those kinds of names or modifying the templates (See 4.) to prepend the method implementations differently, e.g. `doGetVar` or `callSetVar`.  

## 2. Usage

The code generator is invoked with [Ant](http://ant.apache.org/) task, on command line:

	ant

or run the task with your favourite IDE Ant integration (on the build.xml).

You can integrate the command to your build task, so that if any template, target file or library file changes, the code is regenerated.

You can also specify an alternate properties file by running from the command line as

	ant -propertyfile your.codegen.properties 

where `your.codegen.properties` is the name of your properties file.

### 2.1 Up-to-date check

The generator will make an up-to-date check for the input files and will not generate if the source files have not changed. 
In case you wish to force the generation, you can run 

    ant -Dforce=1
    
This will be necessary if you modify the properties file and provide it as a command line argument, for example.

Another option is to remove the hidden '.codegen' file from the 'generated-intf' directory.

### 2.2 Usage without Ant

If you do not wish to use Ant, you can execute the code generator directly with

    java -jar codegen-x.x.x-standalone.jar
    
(where x.x.x is the current version)

Running the above will give the exact syntax. You will need to provide the same arguments that are defined in the properties file for it.

## 3. Integration to application

The code generator generates interface types into the defined generation folder. The interfaces can be used in both client and server applications. 

The code generator generates also base classes and the actual implementation classes for both the server and client. 

Base classes and interfaces are regenerated whenever the code generator runs (to the directory defined by `codegen.properties.output-intf`) 
and they should not be edited manually. The actual classes are generated only once (to `codegen.properties.output-impl`) and they 
can be modified and stored in version control systems. If the interface of the base class changes, you may need to modify the actual class 
to make it compile again.

### 3.1 Server

The server implementation classes are generated in 'server' sub-packages under the defined generation folders.

If your information models contain any methods, you will need to implement these as well in the implementation nodes. 
Check your generated source (Refresh the project in Eclipse first, for example) and see if it gives any errors for these.

To use the nodes then in your applications:

1. Add the generation target directories to your project source path.
2. Register generated classes with `UaServer.registerModel(CodegenModel model)`. You can use the generated server-side InformationModel class 
   for the registration, for example `server.registerModel(com.mycompany.ua.types.server.InformationModel.MODEL)`.
3. Load information model XML with `server.getAddressSpace().loadModel(URI path)`.
4. Create instances with `NodeManagerUaNode.createInstance(Class class, String name)`.
5. Write method implementations. If your types define methods, the generated implementations will throw a Bad_NotImplemented StatusException 
   by default. You must write the actual implementation to the generated implementation ('impl') classes.

### 3.2 Client

The client implementation classes are generated in 'client' sub-packages under the defined generation folders.

To use the nodes in your applications:

1. Add the generation target directories to your project source path.
2. Register generated classes with `UaClient.registerModel(CodegenModel model)`. You can use the generated client-side InformationModel class 
   for the registration, for example `client.registerModel(com.mycompany.ua.types.client.InformationModel.MODEL)`.
3. Read instances with `client.getAddressSpace().getNode(NodeId id)`. You have to cast the result to the correct generated type. Note that only getters
   work on client-side (at the moment).

## 4. Templates

Code generation is based on [Mustache](http://mustache.github.io/) templates.

The code generator is shipped with three template sets: `common`, `client` and `server`. The `common` template set includes interfaces 
that both `client` and `server` template sets use. In addition, the `common` set has templates for Structures and Enumerations.

The `client` template set generates code for client-side and the `server` template set generates code for server-side.

Each template starts with parameter definitions, followed by line with `---` and then the actual template. The following parameters can 
be defined:

* `:targets`: which types does the template use? Available values: `:ObjectType`, `:VariableType`, `:Structure` and `:Enumeration`. 
  Multiple targets can be defined.
* `:package`: which package is template code generated to? The parameter is used as a suffix for the original package defined in 
  `codegen.properties` or in a model-specific package file. Default value: `""`.
* `:name`: what names will the generated code have? The parameter is used as a suffix for the type name. Default value: `""`.
* `:overwrite?`: will the generator overwrite existing classes? Default value: `true`.
* `:many-to-one?`: is only one class generated for all the types? Default value: `false`.

### 4.1 Partials

Partials can be used when a part of a template is repeated many times in a template. All partials are read from the `partials` folder 
inside the template folder.

Partials are used with the standard Mustache syntax: `{{> partial}}`. A partial with file name `^variable.mustache` is used capitalized: 
`{{> Variable}}`.

## 5. Further reading

[Java source code generation from OPC UA information models](https://aaltodoc.aalto.fi/handle/123456789/12030)