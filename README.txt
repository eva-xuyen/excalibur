

                          APACHE AVALON FORTRESS

What is it?

  Fortress is a 'light weight' embeddable Avalon container.  It
  replaces the older Excalibur Component Manager (ECM) container and
  provides backwards support for ECM components.  For more
  information, see http://avalon.apache.org/excalibur/fortress

Layout

  bean              - A Fortress Bean for easier embedding.
  cli               - command line bootloader for Fortress (empty)
  container-api     - Fortress container API source code
  container-impl    - Fortress container Implementation source code
  container-test    - Fortress container unittests source code
  examples          - Several Fortress examples
  site              - Fortress documentation
  platform	        - Fortress installation/platform utilities (empty)
  servlet           - Fortress servlet environment (empty)
  meta              - Fortress meta-builder tools and ant tasks

To Build:

  You will need Apache Maven [http://maven.apache.org]

  2. run 'maven multiproject:install' 


Please send questions or comments to the Avalon user list, users@avalon.apache.org.
