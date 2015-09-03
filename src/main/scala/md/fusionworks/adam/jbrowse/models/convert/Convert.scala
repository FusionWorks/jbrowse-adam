package md.fusionworks.adam.jbrowse.models.convert
import org.apache.spark.{SparkConf, SparkContext}
import org.bdgenomics.adam.rdd.{ADAMContext, ADAMRDDFunctions}

class Convert {
  def fasta(link: String){
    println("link to out file: ")
    var out = readLine()
    if(!out.contains(".adam"))
      out+=".adam"
    val conf = new SparkConf().setAppName("Simple Application").setMaster("local[2]")
    val sc = new SparkContext(conf)
    val ac = new ADAMContext(sc)
    val filt = ac.loadSequence(link)
    val save = new ADAMRDDFunctions(filt)
    save.adamParquetSave(out)
  }

  def vcf(link: String){
    println("link to out file: ")
    var out = readLine()
    if(!out.contains(".adam"))
    out+=".adam"
    val conf = new SparkConf().setAppName("Simple Application").setMaster("local[2]")
    val sc = new SparkContext(conf)
    val ac = new ADAMContext(sc)
    val filt = ac.loadVariants(link)
    val save = new ADAMRDDFunctions(filt)
    save.adamParquetSave(out)
  }
}

object Reads extends Convert {
  def main(args: Array[String]) {
    var line = ""
    while(line.equalsIgnoreCase("example") || line.equalsIgnoreCase("")) {
      print("\nyou want to design a file (yes or no) or example: ")
      line = readLine()
      if(line.contains("example")){
        println("")
        println("  - When the line: \"path to the file:\" \nYou can write \n\"/home/user/files/file.vcf\" \n or\n\"/home/user/files/file.fasta\"")
        println("  - When the line: \"path to the file:\" \nYou can write \n\"/home/user/files/saveFile\"")
      }
    }
    while (line.equalsIgnoreCase("yes")) {

        print("path to the file: ")
        line = readLine()
        println(s"you score $line")
          if(line.contains("fasta"))
            fasta(line)
          if(line.contains("vcf"))
            vcf(line)
          else println("error")
      print("\nyou want to design a file (yes or no): ")
      line = readLine()
    }
  }
}