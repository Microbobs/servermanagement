# servermanagement
This is a JavaFx 8 application for developers who struggle with managing multiple webservers (Tomcat, Wildfly) for multiple projects. Once you have registered a server, you can see it in an overview of all registered servers. The overview gives you information about the status of the server, the deployed webapps and enables you to directly start/stop the server. You also get an easy access to configure files within the registered server and an fast access to the server in the default file explorer.
Many functions are unimplemented for wildfly servers. The implementation for the functionality for tomcat servers is very strict and makes a lot of assumptions. For example that your standard way of start/stopping the server is executing the startup/shutdown.sh/.bat. The tool does currently not support webservers which are run as a service.

#How to Run
Simply run the class "de.microbob.MainApplication"

#How to Build
To build the application you may simply call "mvn clean install"
