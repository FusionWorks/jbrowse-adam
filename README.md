# Introduction
This project implements sample integration between JBrowse and Adam file format

###Preliminary preparations:

In folder resources/sample exists tracks for test purpose. dedupped_20.bam.adam don't contain all data. Only first 100 Mb of 550.

File local.conf already configured to use them in local-mode.

For cluster-mode please, edit file cluster.conf

###To run ``jbrowse-adam`` in "local-mode":

`sbt run local` or in sbt-console type `re-start local` or in application.conf set config.path = "local"

###To run ``jbrowse-adam`` in "cluster-mode":

By default nothing no need to change in application.conf, when config.path = "cluster"

* ssh into emr master instance

* clone code with:
```
git clone --recursive https://github.com/FusionWorks/jbrowse-adam.git
```

* ```cd jbrowse-adam```

* assembly code with:
```
sbt assembly
```

* submit app:
```
spark-submit \
--master yarn-client \
--num-executors 50 \
--packages org.bdgenomics.adam:adam-core:0.16.0 \
--class md.fusionworks.adam.jbrowse.Boot target/scala-2.10/jbrowse-adam-assembly-0.1.jar
```

###To convert genomic data to ADAM format (local example):
```
cd jbrowse-adam
sbt console
import md.fusionworks.adam.jbrowse.tools._
AdamConverter.vcfToADAM("/path/to/file/generic_data.vcf", "/path/to/file/generic_data.vcf.adam")
```

Allowed operations:
* fastaToADAM
* vcfToADAM
* bam_samToADAM

###To convert genomic data to ADAM format (EMR/S3 example):

This example works for extreme big files (35+ Gb). You may decrease or remove at all (use default values): num-executors, spark.executor.memory, driver-memory.

```
cd jbrowse-adam

spark-submit \
--master yarn-client \
--num-executors 50 \
--conf spark.executor.memory=10g \
--driver-memory=10g \
--packages org.bdgenomics.adam:adam-core:0.16.0 \
--class md.fusionworks.adam.jbrowse.tools.ConvertToAdam \
target/scala-2.10/jbrowse-adam-assembly-0.1.jar \
s3n://path/to/file/generic_data.bam \
s3n://path/to/file/generic_data.bam.adam
```