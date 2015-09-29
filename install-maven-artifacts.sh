
# You could install an artifact on a specific local repository by
# setting the localRepositoryPath parameter when installing.
#
# mvn install:install-file  -Dfile=path-to-your-artifact-jar \
#                          -DgroupId=your.groupId \
#                          -DartifactId=your-artifactId \
#                          -Dversion=version \
#                          -Dpackaging=jar \
#                          -DlocalRepositoryPath=path-to-specific-local-repo (optional)

mvn install:install-file  -Dfile=lib/biolemmatizer-core-1.2.jar \
                          -DgroupId=edu.ucdenver.ccp \
                          -DartifactId=biolemmatizer-core \
                          -Dversion=1.2 \
                          -Dpackaging=jar 


mvn install:install-file  -Dfile=lib/bioc-1.0.1.jar \
                          -DgroupId=bioc \
                          -DartifactId=bioc \
                          -Dversion=1.0.1 \
                          -Dpackaging=jar 

mvn install:install-file  -Dfile=lib/context-2012.jar \
                          -DgroupId=context \
                          -DartifactId=context \
                          -Dversion=2012 \
                          -Dpackaging=jar 

mvn install:install-file  -Dfile=lib/nlp-2.4.C.jar \
                          -DgroupId=gov.nih.nlm.nls \
                          -DartifactId=nlp \
                          -Dversion=2.4.C \
                          -Dpackaging=jar

mvn install:install-file  -Dfile=lib/irutils-2.0-SNAPSHOT.jar \
                          -DgroupId=irutils \
                          -DartifactId=irutils \
                          -Dversion=2.0-SNAPSHOT \
                          -Dpackaging=jar
