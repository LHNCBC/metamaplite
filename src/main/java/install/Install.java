
//
package install;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileFilter;
import java.io.File;

/**
 *
 */

public class Install {

  static class TemplateFileFilter implements FileFilter {
    public boolean accept(File pathname) {
      String[] fields = pathname.getName().split("\\.");
      String extension = fields[fields.length - 1];
      return extension.equals("in");
    }
  }

  static void generateFile(File templateFile, String basedir) 
    throws FileNotFoundException, IOException
  {
    String templateFilename = templateFile.getName();
    String outputFilename = templateFilename.substring(0,templateFilename.lastIndexOf('.'));
    System.out.println("generating " + outputFilename + " from " + templateFilename);
    BufferedReader br = new BufferedReader(new FileReader(templateFile));
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFilename)));
    String line;
    while ((line = br.readLine()) != null) {
      out.println(line.replaceAll("@@basedir@@", basedir));
    }
    out.close();
    br.close();
  }

  /**
   *
   * @param args - Arguments passed from the command line
   **/
  public static void main(String[] args)
    throws FileNotFoundException, IOException
  {
    FileFilter filter = new TemplateFileFilter();
    File basedirFile = new File(System.getProperty("user.dir"));
    for (File templateFile:  basedirFile.listFiles(filter)) {
      generateFile(templateFile, basedirFile.getName());
    }
  }
}
