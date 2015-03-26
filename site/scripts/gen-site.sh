# clean up
rm -Rf target

# create site
#maven site

# create merged source tree
mkdir -p target/base/src
find .. -path '*/src' ! -path '*site*' -exec echo \{\}/* \; | cat > target/src.txt
rsync -r --exclude='*.svn*' `cat target/src.txt` target/base/src/

# create merged project.xml
ruby scripts/aggregate-dependencies.rb
mv project-all-deps.xml target/base/project.xml

cd target/base

# run merged tests
# run merged clover
# create merged javadocs
maven site

# copy merged data into main tree
#cd ../..
#mkdir -p target/docs
#cp -r target/base/target/docs/apidocs target/docs
#cp -r target/base/target/docs/clover target/docs
