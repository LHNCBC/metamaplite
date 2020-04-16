# Compiling MetaMapLite with GraalVM

To Generate native image of the comand line version of MetaMapLite
using GraalVM's native-image using MetaMapLite standalone jar

From MetaMapLite toplevel directory:

    $ GRAALVM=<directory where grallvm is installed>
    $ M2_REPO=<maven repository directory (usually $HOME/.m2/repository)>
    $ mvn package
    $ bash graalvm/native-compile-metamaplite.sh

For more information see also:
 
1. "Configuring Graal Native AOT for reflection": https://blog.frankel.ch/configuring-graal-native-aot-reflection/
2. GraalVM main website: https://www.graalvm.org
3. GraalVM Github page: https://github.com/oracle/graal
4. GrallVM Community Edition (What I used for this): https://github.com/oracle/graal/releases

