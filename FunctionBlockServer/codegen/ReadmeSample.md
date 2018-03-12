# Sample configuration for code generation

The code generator is used to generate code for the ValveType in the SampleConsoleServer sample application. 

The information model is in `SampleTypes.xml` and its namespace is 
`http://ua.prosysopc.com/SampleTypes`.
 
The code will be generated to `../samples`, into the Java package `com.prosysopc.ua.samples.types`.

## 1. Configuration files

You will need to modify the following files:

- codegen.properties

### 1.1 codegen.properties

Modify the codegen.properties to include the `SampleTypes.xml` information model, `com.prosysopc.ua.samples.types` package and the `samples` output directory:

    # Comma separated list of target files from which code is generated. Insert your information model here!
    # The given file path is relative to this folder.
    # e.g. single model: targets=myModel.xml
    # e.g. multiple models: targets=myModel1.xml, myModel2.xml
    codegen.properties.targets=SampleTypes.xml

	# The Java package where the code is generated to. Alternatively, you can leave this blank and specify
	# the package with set-package script. The script stores the package to a file next to the information model file.
	# Note: If you define several models, you must use set-package to define the packages. Here you can only define one package.
    codegen.properties.package=com.prosysopc.ua.samples.types

    # Template sets to use (folders under templates/-folder)
    codegen.properties.templates=common, client, server

	# Output folder for the INTERFACE files that are generated even if a file already exists.
	# Generated code in this folder should not be edited manually.
	# The given file path is relative to this folder.
    codegen.properties.output-intf=../samples

	# Output folder for IMPLEMENTATION files that are generated only if they don't exist and may be edited to define
	# additional functionality or internal logic.
	# Generated code in this folder can be edited manually.
	# The given file path is relative to this folder.
	codegen.properties.output-impl=../samples

### 1.2 set-package

Instead of defining the package in codegen.properties (as above), you can define the package of the model in a "package" 
file. The simplest way to generate the file is to use the set-package command. You need to run it from the command line as:

	(Windows)> set-package.bat SampleTypes.xml com.prosysopc.ua.samples.types 
	(Linux/OSX)$ ./set-package.sh SampleTypes.xml com.prosysopc.ua.samples.types
	
## 2. Generation
 
You can now run the code generation by running build.xml as an Ant Build within your IDE or from the command line simply as
 
	ant
   
Alternatively, you can define properties in a custom properties file, e.g.

	ant -propertyfile sampleTypes.codegen.properties 
  
### 2.1 Regeneration

If you need to regenerate the types, you can just run 'ant' again. However, it will not generate the files
if the source file (SampleTypes.xml) hasn't changed. Also, it will never overwrite the implementation
files; only the interface files are overwritten. You are expected to modify the implementation files to your needs.

You can force regeneration as described in the 'up-to-date' section of the Readme.md, if necessary, though.

## 3. Usage in applications

The generated types are in the Java package that you defined in the `codegen.properties`. The generator also
generates an `InformationModel` class into each package, which contains all the classes that are defined in that package.
You can use that class to get them registered in the SDK classes, after which they will be used automatically,
when new instances are created.

The generator creates interfaces and base classes for the types and also implementation classes separately 
for the client and server. 

### 3.1 SampleConsoleServer

To use the generated types in SampleConsoleServer, modify the `loadInformationModels()` method as follows:

#### 3.1.1 Register Classes

	protected void loadInformationModels() {
		...

		// ADD THESE TO USE THE SAMPLE TYPES		
		server.registerModel(com.prosysopc.ua.samples.types.server.InformationModel.MODEL);
		try {
			server.getAddressSpace().loadModel(new File(System.getProperty("user.dir"),
							"codegen/SampleTypes.xml").toURI());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

#### 3.1.2 Create Instances

You can now instantiate new ValveType objects in the server, for example add the following lines of code 
to the end of `MyNodeManager.createAddressSpace()`:

	// A sample generated ValveType
	try {
		ValveType myValve = createInstance(ValveType.class, "MyValve");
		myObjectsFolder.addComponent(myValve);
		myValve.setFlow(0.0);
		myValve.setIsOpen(true);
		myValve.setBestBefore(DateTime.parseDateTime("2020-01-01T00:00:00Z"));
	} catch (Exception e) {
		printException(e);
	}

You will need to add the following import as well:

	import com.prosysopc.ua.samples.types.ValveType;

NOTE: By default, createInstance() uses the name as the NodeId, BrowseName and DisplayName. You can use the
other overloaded versions of it, to provide your own values for each, if you need. Or you can use the NodeBuilder directly. 
See the Javadoc of the method for an example.

#### 3.1.3 Implement the method

The sample ValveType contains one method `ChangeState`. You must add your own implementation, for example in `ValveTypeNode.java`:

	public class ValveTypeNode extends ValveTypeNodeBase {
		...	
		@Override
		protected void onChangeState(ServiceContext serviceContext, ValveState newState) throws StatusException {
			setState(newState);
		}
	}
 

### 3.2 Client application

The new type can also be used on the client side.  It is not that useful with the SampleConsoleClient, but
you can try it in your own application as follows.

#### 3.2.1 Register Classes

After the client is connected, you can register the classes

	client.connect();
	client.registerModel(com.prosysopc.ua.samples.types.client.InformationModel.MODEL);
 
#### 3.2.2 Create Instances
 
To read the Valve1 from the server, you will need to find the NodeId for it, for example by browsing the server.
Or, when we know that it's generated as above into MyNodeManager, we can get it with the following code:        

	int ns = client.getNamespaceTable().getIndex("http://www.prosysopc.com/OPCUA/SampleAddressSpace");
	NodeId myValveNodeId = new NodeId(ns, "MyValve");
		
Now, we can create an instance of the complete structure in the client as:

    ValveType myValve = (ValveType) client.getAddressSpace().getNode(myValveNodeId);
		