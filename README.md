# Introduction
This project implements sample integration between JBrowse and Adam file format

to run ``jbrowse-adam``:

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