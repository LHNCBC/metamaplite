// /export/home/wjrogers/projects/metamaplite/src/main/java/install/Install.java, Wed Sep 23 17:30:27 2015, edit by Will Rogers

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
    String templateFilename = templateFile.getPath();
    if (templateFilename.lastIndexOf(".in") > 3) {
      String outputFilename = templateFilename.substring(0,templateFilename.lastIndexOf(".in"));
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
    System.out.println("basedir: " + basedirFile.getPath());
    /* generate any file having a template with extension ".in" in base and base/config directories */
    for (File templateFile:  basedirFile.listFiles(filter)) {
      generateFile(templateFile, basedirFile.getPath());
    }
    File configdirFile = new File(basedirFile.getPath() + "/config");
    System.out.println("configdir: " + configdirFile.getPath());
    for (File templateFile:  configdirFile.listFiles(filter)) {
      generateFile(templateFile, basedirFile.getPath());
    }

  }
}
