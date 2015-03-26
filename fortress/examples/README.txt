


                          APACHE AVALON FORTRESS EXAMPLES

To Build:
   
   You will need Apache Maven to run these examples
   [http://maven.apache.org]

   1. Move to the root directory (fortress/examples)
   2. Run "maven jar:jar" to build
   3. This will build the following

target/avalon-fortress-examples-1.1-extended.jar  
target/avalon-fortress-examples-1.1-swing.jar
target/avalon-fortress-examples-1.1-viewer.jar
target/avalon-fortress-examples-1.1.jar
target/avalon-fortress-examples-1.1-servlet.war


The Examples:

   1. Extended Container 
      Main Class:  org.apache.avalon.fortress.examples.extended.Main
      Shows an extended container and excalibur lifecycle extensions
      Example results are recorded in a log file
      To Run:  maven run:extended

   2. Swing Container
      Main Class: org.apache.avalon.fortress.examples.swing.Main
      Shows a swing-based container.  Uses the translator component
      To Run:  maven run:swing

   3. Viewer Container
      Main Class:  org.apache.avalon.fortress.examples.viewer.Main
      Another swing example that does component lookups
      To Run:  maven run:viewer

   4. Fortress Servlet
      Shows an fortress container embedded in a servlet
      To Run:  launch the 'war' file in your favorite servlet
      container (ie- tomcat)

Notes:

   * For your own Fortress examples, please look at the project.xml
     and maven.xml.  This will show you what dependencies you need and
     how to generate the Fortress meta-data via maven.

   * For other examples see avalon-sandbox/examples
      