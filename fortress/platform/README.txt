Excalibur Fortress Platform

 To Build
 --------
 * Check out Excalibur from svn
 * Run maven multiproject:install to build Excalibur
 * Move to the Platform directory
 * Run "maven dist" on this project to get the distribution
 * (if you get issues about gpg, you'll still get a binary distribution)

 To Run
 ------
 * Download a copy of this distribution
 * Unzip into your favorite directory
 * From the distribution root, run:
    - For Windows: bin/fortress.bat console
    - For Linux: bin/fortress.sh console
    - For OS X or other Unix: bin/run.sh (you may need to modify this script)

 What's Included
 ---------------
 * All required libraries and dependencies for Fortress 1.2
 * Example startup scripts for running Fortress as a daemon or NT Service
   - For this, we take advantage of the Java Service Wrapper
 * An example Fortress application with included configuration files

 NOTE: this software bundle is currently in ALPHA state.  Please report any issues.
