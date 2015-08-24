package md.fusionworks.adam.jbrowse

/**
 * Created by sergiu on 7/22/15.
 */


import md.fusionworks.adam.jbrowse.models._
import org.specs2.matcher.ShouldMatchers
import org.specs2.mutable._

class TestListSpray extends Specification with ShouldMatchers{

/*

    "Features" should {
      "getFeatures" in {
       var get = JbrowseUtil.getFeatures(342725, 342826)
       get.features must contain(Feature(
       "HWI-ST1023:119:D0L67ACXX:3:1101:8361:100830",
       "TTTTCCCAAGGACCCTTTATTACCTCCGTTTCCATTTTCATTTCTCTCATTTAATTCTCCGGCACTAATTTCGTTTAGTGGTTCTTTCTCTTCGCTACCAC",
       342725,
       342826,
       "101M"
       ))
        //println(get)
      }
    }

*/


    "Global" should {
      "getGlobal" in {
        val get = JBrowseUtil.getGlobal
        //get must contain(Global(0.02,234235,87,87,42,2.1))
       // get must assert(Global(0.02,234235,87,87,42,2.1))
       // get must Global(0.02,234235,87,87,42,2.1)
        get mustEqual Global(0.02,234235,87,87,42,2.1)
       /* assert(get.featureDensity === 0.02)
        assert(get.featureCount === 234235)
        assert(get.scoreMin === 87)
        assert(get.scoreMax === 87)
        assert(get.scoreMean === 42)
        assert(get.scoreStdDev === 2.1)*/
        //println(get)
      }
    }


  "RefSeqs" should {
    "getRefSeqs" in {
      val get = JBrowseUtil.getRefSeqs
      get must contain(RefSeqs("ctgA",1523375,1242))
      get must contain(RefSeqs("ctgB",66, 0))
      // println(get)
    }
  }


  "TrakList" should {
    "getTrackList:" in {
      val get = JBrowseUtil.getTrackList
     get.tracks must contain(Track(
        "mygene_track",
        "My ADAM Genes",
        "JBrowse/View/Track/HTMLFeatures",
        "JBrowse/Store/SeqFeature/REST",
        "http://localhost:8080/data"))
     get.tracks must contain(Track(
        "my_sequence_track",
        "DNA",
        "JBrowse/View/Track/Sequence",
        "JBrowse/Store/SeqFeature/REST",
        "http://localhost:8080/data"))

      //println(get)
    }
  }


}






