
REM You could install an artifact on a specific local repository by
REM setting the localRepositoryPath parameter when installing.
REM
REM mvn install:install-file  -Dfile=path-to-your-artifact-jar ^
REM                          -DgroupId=your.groupId ^
REM                          -DartifactId=your-artifactId ^
REM                          -Dversion=version ^
REM                          -Dpackaging=jar ^
REM                          -DlocalRepositoryPath=path-to-specific-local-repo (optional)

mvn install:install-file  -Dfile=lib/biolemmatizer-core-1.2.jar ^
                          -DgroupId=edu.ucdenver.ccp ^
                          -DartifactId=biolemmatizer-core ^
                          -Dversion=1.2 ^
                          -Dpackaging=jar 

mvn install:install-file  -Dfile=lib/bioc-1.0.1.jar ^
                          -DgroupId=bioc ^
                          -DartifactId=bioc ^
                          -Dversion=1.0.1 ^
                          -Dpackaging=jar 

mvn install:install-file  -Dfile=lib/context-2012.jar ^
                          -DgroupId=context ^
                          -DartifactId=context ^
                          -Dversion=2012 ^
                          -Dpackaging=jar 

mvn install:install-file  -Dfile=lib/nlp-2.4.C.jar ^
                          -DgroupId=gov.nih.nlm.nls ^
                          -DartifactId=nlp ^
                          -Dversion=2.4.C ^
                          -Dpackaging=jar

mvn install:install-file  -Dfile=lib/lvgdist-2020.0.jar ^
                          -DgroupId=gov.nih.nlm.nls.lvg ^
                          -DartifactId=lvgdist ^
                          -Dversion=2020.0 ^
                          -Dpackaging=jar


