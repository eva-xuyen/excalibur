def strip( xml )
  result = xml.gsub( /<[^>]+>[^<>]*<\/[^>]+>/m, '' )
  result.gsub!( /<[^>]+>/m, '' )
  result.gsub!( /[\n\t ]/, '' )
  
  return result
end

def extractArtifactId( dependency )
    id = ''
    if dependency.include? "artifactId" then
      id = dependency.gsub( /\<artifactId\>([^<>]*)\<\/artifactId\>/, '\1' )
    else
      id = dependency.gsub( /\<id\>([^<>]*)\<\/id\>/, '\1' )
    end
    
    return strip( id )
end

def extractGroupId( dependency )
    id = ''
    if dependency.include? "groupId" then
      id = dependency.gsub( /\<groupId\>([^<>]*)\<\/groupId\>/, '\1' )
    else
      id = dependency.gsub( /\<id\>([^<>]*)\<\/id\>/, '\1' )
    end
    
    return strip( id )
end

def extractVersion( dependency )
    version = dependency.gsub( /\<version\>([^<>]*)\<\/version\>/, '\1' )
    return strip( version )
end

def getDepList( project, depList )
    dependencies = project[/\<dependencies\>.*\<\/dependencies\>/m]
    
    if ! dependencies
      return depList
    end
    
    dependencies.gsub!( /\<\!--.*?--\>/m, '' )
    
    dependencies.split( /\<\/dependency\>/ ).each { |dependency|
    
      artifact = extractArtifactId( dependency )
      group = extractGroupId( dependency )
      version = extractVersion( dependency )

      #puts "found artifact " + artifact
      #puts "current artifacts: " + result
      #puts ""
      
      if artifact.empty?
        next
      end

      if depList.include? artifact
        next
      end
     
      curr = "    <dependency>\n" +
        "        <id>" + artifact + "</id>\n" +
        "        <version>" + version + "</version>\n"

      if( artifact != group ) then
        curr = curr  +
          "        <groupId>" + group + "</groupId>\n"
      end

      curr = curr + "    </dependency>\n"

      depList = depList + curr + "\n"
    }
    
    return depList
end

def processFile( projectFile, dest, depList )
    puts "processing " + projectFile
    
    source = File.open( projectFile, 'r' )
    project = source.read
    
    newDepList = getDepList( project, depList )
    
    return newDepList
end

def processAll( files )
    count = 0
    depFile = "project-all-deps.xml"
    if FileTest.exist?( depFile )
      depFile = depFile + ".new"
    end
    dest = File.open( depFile, 'w' )
    dest.puts "<project>
    <extend>${basedir}/../../../buildsystem/project-common.xml</extend>

    <name>Apache Excalibur</name>
    <id>excalibur-javadocs</id>

    <shortDescription>Apache Excalibur</shortDescription>
    <description>Apache Excalibur</description>

    <reports>
      <report>maven-clover-plugin</report>
      <report>maven-junit-report-plugin</report>
      <report>maven-javadoc-plugin</report>
    </reports>

    <dependencies>
"
    depList = ''

    files.each { |path|
      projectFile = path.gsub( /\n/, "" )
      #projectFile.gsub!( /\.\//, "" )
      
      depList = processFile( projectFile, dest, depList )
      count = count + 1
    }

    dest.puts depList
    dest.puts "
    </dependencies>
</project>
"

    return count
end

 # runs the processor
 def main()
     files = `find .. -type f -name 'project.xml' ! -path '*site*' ! -path '*bean*' -maxdepth 4`
     count = processAll( files )
 
     puts "transformed #{count} files.\n"
 end
 
 main()
 
