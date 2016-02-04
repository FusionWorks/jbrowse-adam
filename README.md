# Introduction
This project implements sample integration between JBrowse and Adam file format

###Preliminary preparations:

In folder resources/sample exists tracks for test purpose.
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