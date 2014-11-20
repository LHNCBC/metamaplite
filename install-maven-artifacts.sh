
# You could install an artifact on a specific local repository by
# setting the localRepositoryPath parameter when installing.
#
# mvn install:install-file  -Dfile=path-to-your-artifact-jar \
#                          -DgroupId=your.groupId \
#                          -DartifactId=your-artifactId \
#                          -Dversion=version \
#                          -Dpackaging=jar \
#                          -DlocalRepositoryPath=path-to-specific-local-repo (optional)

mvn install:install-file  -Dfile=/usr/local/pub/nlp/BioC/BioC_Java_1.0.1/lib/biolemmatizer-core-1.1-jar-with-dependencies.jar \
                          -DgroupId=edu.ucdenver.ccp \
                          -DartifactId=biolemmatizer-core \
                          -Dversion=1.2 \
                          -Dpackaging=jar 


mvn install:install-file  -Dfile=/usr/local/pub/nlp/BioC/BioC_Java_1.0.1/lib/bioc.jar \
                          -DgroupId=ncbi.nlm.nih.gov \
                          -DartifactId=bioc \
                          -Dversion=1.0.1 \
                          -Dpackaging=jar 


