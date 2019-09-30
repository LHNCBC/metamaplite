package gov.nih.nlm.nls.metamap.lite.mapdb;

import org.mapdb.*;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.nio.charset.Charset;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

// import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Describe class BuildDictionaries here.
 *
 *
 * Created: Wed May 22 15:50:02 2019
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class BuildDictionaries {

  static Charset charset = Charset.forName("utf-8");
  DB db;
  
  /**
   * Creates a new <code>BuildDictionaries</code> instance.
   *
   */
  public BuildDictionaries() {
    
  }
  public BuildDictionaries(Charset charset) {
    this.charset = charset;
  }

  public static class Record {
    private String filename;
    private String[] columnNames;
    private String[] columnTypes;
    private String[] keyColumns;
    /** input line*/
    String line;
    Record(String line) {
      this.line = line;
    }
    /** @return input line*/
    String getLine() { return this.line; }
    /** @return line separated into fields */
    String[] getFields() { return this.line.split("\\|");}
    public void setFile(String filename) { this.filename = filename; }
    public void setColumns(String[] columns) { this.columnNames = columns; }
    public void setColtypes(String[] columns) { this.columnTypes = columns; }
    public void setKeycols(String[] keycols) {
      this.keyColumns = keycols;
    }
  }

  /**
   * Load Table
   * @param tablefilename name of file containing table of records with pipe-separated fields.
   * @param charset charset to use usually ASCII or UTF-8
   * @return list of records instances.
   * @throws FileNotFoundException thrown if file or directory is not found
   * @throws IOException general i/o exception
   */
  public static List<Record> loadTable(String tablefilename, Charset charset) 
    throws FileNotFoundException, IOException {
    List<Record> newList = new ArrayList<Record>();
    BufferedReader br =
      new BufferedReader(new InputStreamReader(new FileInputStream(tablefilename), charset));
    String line;
    while ((line = br.readLine()) != null) {
      newList.add(new Record(line));
    }
    return newList;
  }

  /**
   * Load Table to Map
   * @param map HTreeMap to fill
   * @param tablefilename name of file containing table of records with pipe-separated fields.
   * @param columnIndex index of column to use keys
   * @param charset charset to use usually ASCII or UTF-8
   * @throws FileNotFoundException thrown if file or directory is not found
   * @throws IOException general i/o exception
   */
  public static void loadTableToDB(HTreeMap map, String tablefilename, int columnIndex, Charset charset) 
    throws FileNotFoundException, IOException {
    System.out.println("Loading table: " + tablefilename + ", column: " + columnIndex);

    BufferedReader br =
      new BufferedReader(new InputStreamReader(new FileInputStream(tablefilename), charset));
    String line;
    while ((line = br.readLine()) != null) {
      String[] fields = line.split("\\|");
      String key = fields[columnIndex].toLowerCase();
      if (map.containsKey(key)) {
	List<String[]> recordList = (List<String[]>)map.get(key);
	recordList.add(fields);
      } else {
	List<String[]> recordList = new ArrayList<String[]>();
	recordList.add(fields);
	map.put(key, recordList);
      }
    }
  }

  public static void loadMap(DB db, String mapname, String tablePath, int columnIndex)
    throws FileNotFoundException, IOException {
    HTreeMap tableMap = db.hashMap(mapname).createOrOpen();
    loadTableToDB(tableMap, tablePath, columnIndex, charset);
  }

  public static void createDb(String directoryName)
    throws FileNotFoundException, IOException {
    Charset charset = Charset.forName("utf-8");
    String dbFilename = directoryName + "/thedataset.db";
    DB db = DBMaker.fileDB(dbFilename).make();
    // skip yaml configuration part
    //this.configMap = db.hashMap("config").openOrCreate();
    
    HTreeMap meshTcRelaxedMeshMap = db.hashMap("meshtcrelaxedmap.mesh").createOrOpen();
    
    loadTableToDB(meshTcRelaxedMeshMap, directoryName + "/mesh_tc_relaxed.txt", 0, charset);
    db.commit();

    HTreeMap varsTermMap = db.hashMap("vars.term").createOrOpen();
    loadTableToDB(varsTermMap, directoryName + "/vars.txt", 0, charset);
    db.commit();

    HTreeMap map = db.hashMap("cuiconcept.cui").createOrOpen();
    loadTableToDB(map, directoryName + "/cuiconcept.txt", 0, charset);
    db.commit();

    HTreeMap cuiSemanticTypeCuiMap = db.hashMap("cuisemantictype.cui").createOrOpen();
    loadTableToDB(cuiSemanticTypeCuiMap, directoryName + "/cuist.txt", 0, charset);
    db.commit();

    HTreeMap cuiSourceInfoStrMap = db.hashMap("cuisourceinfo.str").createOrOpen();
    loadTableToDB(cuiSourceInfoStrMap, directoryName + "/cuisourceinfo.txt", 3, charset);
    db.commit();

    HTreeMap cuiSourceInfoCuiMap = db.hashMap("cuisourceinfo.cui").createOrOpen();
    loadTableToDB(cuiSourceInfoCuiMap, directoryName + "/cuisourceinfo.txt", 0, charset);
    db.commit();

    System.out.println("closing db");
    db.close();
  }
  
  /**
   * @param args command line arguments
   * @throws FileNotFoundException thrown if file or directory is not found
   * @throws IOException general i/o exception
   */
  public static void main(String[] args)
    throws FileNotFoundException, IOException {

    ArgumentParser parser = ArgumentParsers.newFor("BuildDictionaries").build()
      .defaultHelp(true)
      .description("Build MapDb Dictionaries.");
    parser.addArgument("directory")
     	.help("directory containing tables and configfile");
    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }
    System.out.println(ns);
    String dirName = ns.<String> get("directory");
    createDb(dirName);
    /* String dbDir =
       "/net/lhcdevfiler/vol/cgsb5/ind/II_Group_WorkArea/wjrogers/Projects/metamaplite/data/mapdb/pubchem-mesh";*/
    // ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    // try {
    //   Record record = mapper.readValue(new File(dbDir + "/ifconfig.yaml"), Record.class);
    //   System.out.println(ReflectionToStringBuilder.toString(user,ToStringStyle.MULTI_LINE_STYLE));
    // } catch (Exception e) {
    //   // TODO Auto-generated catch block
    //   e.printStackTrace();

    // }
    /*createDb(dbDir);*/
  }


}
