# Introduction
Project **jbrowse-adam** implements sample integration between [JBrowse](http://jbrowse.org/ "JBrowse") and [ADAM file formats](https://github.com/bigdatagenomics/adam "ADAM").

##Preliminary preparations:

To run `jbrowse-adam` we need some files with genomic data. Sample files is attached to project, but physically located on at Git LFS storage ([https://git-lfs.github.com/]()). Please, before clone project, install this extension to property download them. File `local.conf` already configured to use this genomic data files in `local-mode`.

Alternatively we can convert full data (`tutorial_files.zip` at `ftp://gsapubftp-anonymous@ftp.broadinstitute.org`) in ADAM format, how to do it, it is written below.

##Launch application

###To run ``jbrowse-adam`` in "local-mode":

Before start, we need to install latest versions of `Java`, `Scala` and `SBT`, if they are not already installed.

In `jbrowse-adam` folder type `sbt "run local"` or launch `sbt` and type `re-start local` or in file `application.conf` set `config.path = "local"` and type `sbt run`.

###To run ``jbrowse-adam`` in "cluster-mode":

By default nothing no need to change in `application.conf`, when `config.path = "cluster"`

But, need to correct paths in file `cluster.conf`, see in example below.

###Example of launch on Amazon EMR Cluster

####Cluster creation

We tested project on these settings of cluster (when create cluster, switch to `Advanced options`):

**Software and Steps**:
* Vendor: `Amazon`
* Release: `emr-4.3.0`
* Check `Hadoop 2.7.1` and `Spark 1.6.0`, uncheck others

**Hardware**:
* Master: 1x `m3.xlarge`
* Core: 2x `m3.xlarge`
* Task: 10x `m3.xlarge`

We also tested big data at `r3.xlarge` EC2 instances and received a boost in performance when process really big data.

**General cluster Settings:**
* Uncheck termination protection

**Security**:
* `EC2 Key Pair` - need to be created by EC2 admin and specified here. This pair need for ssh access.

Press ***Create Cluster*** button

Now need to wait (about 7 min).

####Access to cluster via SSH and web browser

* In `Cluster details` search `Master public DNS` and press `SSH`
* Copy string for access to cluster from the console:

    ```ssh -i ~/you-key-pair.pem hadoop@ec2-XX-XX-XXX-XXX.us-west-1.compute.amazonaws.com```

    It is meant that the key pairs file `you-key-pair.pem` are in the user's root directory, e.g. `/home/user` and have access rights 600 (`chmod 600 ~/you-key-pair.pem`)
* SSH into EMR master instance with command above.
* To see work of cluster in browser - press `Enable web connection` and follow instructions, details see below.

####Preparing data and code base

* Upload genomic data to S3 bucket. In order to reduce delays, the S3 bucket should be located in the same region as the EMR cluster.
* Install `git` and `sbt` on cluster:
```
    sudo yum install git
    curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
    sudo yum install sbt
```
* Clone code with:

    `git clone --recursive https://github.com/FusionWorks/jbrowse-adam.git`
* ```cd jbrowse-adam```
* Edit paths to genomic data:

    `nano src/main/resources/cluster.conf`

    Change all `filePath` to yours paths at S3 bucket (s3n://...)

    Ctrl+O - save changes, Ctrl+X - exit
* Assembly code with:
``
    sbt assembly
``

    Until the project is assembling, you can drink tea. It is a long process.
* Launch `jbrowse-adam` with command:
```
    spark-submit \
    --master yarn-client \
    --num-executors 50 \
    --executor-memory 8g \
    --packages org.bdgenomics.adam:adam-core:0.16.0 \
    --class md.fusionworks.adam.jbrowse.Boot target/scala-2.10/jbrowse-adam-assembly-0.1.jar
```

This command works for extreme big genomic files (35+ Gb). You may decrease or remove at all (use default values): `--num-executors`, `--executor-memory`, `--driver-memory`.

####See results in browser:

Assume, that we have master public DNS: `ec2-XX-XXX-XXX-XXX.us-west-1.compute.amazonaws.com`. In apperas in `Cluster details`.

When web connection is enabled, we can access some interesting addresses:

* JBrowse: `http://ec2-XX-XXX-XXX-XXX.us-west-1.compute.amazonaws.com:8080`
* Spark jobs: `http://ec2-XX-XXX-XXX-XXX.us-west-1.compute.amazonaws.com:4040`
* Alternatively, we can see Spark jobs with CSS styles in `Cluster details` -> `Resource Manager` -> `Application master`.

####Terminate cluster job:

* Ctrl+C

###Convert genomic data to ADAM format (local example):
```
cd jbrowse-adam
sbt console
import md.fusionworks.adam.jbrowse.tools._
AdamConverter.vcfToADAM("file:///path/to/genetic/file_data.vcf", "file:///path/to/genetic/file_data.vcf.adam")
```

Available operations:
* fastaToADAM
* vcfToADAM
* bam_samToADAM

If we got `Out of memory errors`, we should give to JVM more memory. For example:

`sbt console -J-XX:-UseGCOverheadLimit  -J-Xms1024M -J-Xmx2048M -J-XX:+PrintFlagsFinal`

###Convert genomic data to ADAM format (EMR/S3 example):
```
cd jbrowse-adam

spark-submit \
--master yarn-client \
--num-executors 50 \
--conf spark.executor.memory=8g \
--driver-memory=8g \
--packages org.bdgenomics.adam:adam-core:0.16.0 \
--class md.fusionworks.adam.jbrowse.tools.ConvertToAdam \
target/scala-2.10/jbrowse-adam-assembly-0.1.jar \
s3n://path/to/legacy/genetic/file/_data.bam \
s3n://path/to/new/adam/genetic/file_data.bam.adam
```
This example works for extreme big files (35+ Gb). You may decrease or remove at all (use default values): `--num-executors`, `--conf spark.executor.memory`, `--driver-memory`.