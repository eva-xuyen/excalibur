
Avalon Components
-----------------

The components repository is based on the restructuring of the Cornerstone 
project.  The restructuring incorporates the seperation of each component 
into its own directory including isolation of of api relative to 
implemetation.

Components subprojects include:

   connection
   datasources
   scheduler
   sockets
   store
   threads

The default target will build, jar and install the complete component 
suite into your local repository.

  $ mvn clean install

