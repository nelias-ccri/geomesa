<p align="center"><a href="http://geomesa.github.io"><img src="https://raw.githubusercontent.com/geomesa/geomesa.github.io/master/img/geomesa-2x.png"></img></a></p>

<a href="https://gitter.im/locationtech/geomesa?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge" target="_blank"><img src="https://badges.gitter.im/Join%20Chat.svg" alt="Join the chat at https://gitter.im/locationtech/geomesa"></img></a>
## GeoMesa

![Splash](http://www.geomesa.org/img/geomesa-overview-848x250.png)

GeoMesa is an open-source, distributed, spatio-temporal database built on top of the Apache Accumulo column family store. GeoMesa implements standard Geotools interfaces to provide geospatial functionality over very large data sets to application developers.  GeoMesa provides plugins for exposing geospatial data stored in Accumulo via standards-based OGC HTTP services and cluster monitoring and management tools within the GeoServer administrative interface.  

#### ![LocationTech](https://pbs.twimg.com/profile_images/2552421256/hv2oas84tv7n3maianiq_normal.png) GeoMesa is a member of the [LocationTech](http://www.locationtech.org) working group of the Eclipse Foundation.

## Versions and Downloads

**Latest release**: 1.1.0-rc.6 [Source](https://github.com/locationtech/geomesa/archive/geomesa-1.1.0-rc.6.tar.gz) [Release tarball](http://repo.locationtech.org/content/repositories/geomesa-releases/org/locationtech/geomesa/geomesa-assemble/1.1.0-rc.6/geomesa-assemble-1.1.0-rc.6-bin.tar.gz) [![Build Status](https://api.travis-ci.org/locationtech/geomesa.svg?branch=geomesa-1.1.0-rc.6)](https://travis-ci.org/locationtech/geomesa)

**Development version (source only)**: 1.1.0-rc.7-SNAPSHOT [Source](https://github.com/locationtech/geomesa/archive/master.tar.gz) [![Build Status](https://api.travis-ci.org/locationtech/geomesa.svg?branch=master)](https://travis-ci.org/locationtech/geomesa)

**1.0.x release**: geomesa-accumulo1.5-1.0.0-rc.7 [Source](https://github.com/locationtech/geomesa/releases/tag/geomesa-accumulo1.5-1.0.0-rc.7) [Release tarball](https://repo.locationtech.org/content/repositories/geomesa-releases/org/locationtech/geomesa/geomesa-assemble-accumulo1.5/1.0.0-rc.7/geomesa-assemble-accumulo1.5-1.0.0-rc.7-bin.tar.gz) [![Build Status](https://travis-ci.org/locationtech/geomesa.svg?branch=accumulo1.5.x%2F1.x)](https://travis-ci.org/locationtech/geomesa)  

<b>NOTE:</b> The current recommended version is `1.1.0-rc.6`. The most recent tar.gz assembly can be 
[downloaded here](http://repo.locationtech.org/content/repositories/geomesa-releases/org/locationtech/geomesa/geomesa-assemble/1.1.0-rc.6/geomesa-assemble-1.1.0-rc.6-bin.tar.gz) which contains the [Accumulo distributed runtime jar](geomesa-distributed-runtime), [GeoServer plugin](geomesa-plugin), and [command line tools](geomesa-tools).

GeoMesa artifacts can be downloaded from the [LocationTech Maven repository](https://repo.locationtech.org/content/repositories/geomesa-releases/)

Snapshots are available in the [LocationTech Snapshots Repository](https://repo.locationtech.org/content/repositories/geomesa-snapshots/)

### Upgrading

To upgrade between minor releases of GeoMesa, the versions of all GeoMesa components **must** match. 

This means that the version of the `geomesa-distributed-runtime` JAR installed on Accumulo tablet servers **must** match the version of the `geomesa-plugin` JAR installed in the WEB-INF/lib directory of GeoServer.

## Building from Source

Navigate to where you would like to download this project.

    git clone git@github.com:locationtech/geomesa.git
    cd geomesa
    build/mvn clean install

This project is managed by Maven, and builds using Maven with [Zinc](https://github.com/typesafehub/zinc).
From the root directory, the above will build each sub-project with its additional dependencies-included JAR.

## Documentation

* [Quick Start](http://www.geomesa.org/geomesa-quickstart/) on the main [documentation](http://www.geomesa.org/) site.
* [FAQ](http://www.geomesa.org/faq/)
* [Tutorials](http://www.geomesa.org/tutorials/)
* GeoMesa [Users](https://locationtech.org/mhonarc/lists/geomesa-users/) and [Dev](https://locationtech.org/mhonarc/lists/geomesa-dev/) mailing list archives
* READMEs are provided under most modules: [Tools](geomesa-tools), [Jobs](geomesa-jobs), etc

## GeoMesa Project Structure

* [**geomesa-accumulo**](geomesa-accumulo/geomesa-accumulo-datastore): This module contains the implementations of the core Accumulo indexing structures, Accumulo iterators, and the GeoTools interfaces for exposing the functionality as a `DataStore` to both application developers and GeoServer.
* **geomesa-assemble**: This module packages the GeoMesa distributed runtime, GeoMesa GeoServer plugin, and GeoMesa Tools. You can manually assemble using the `assemble.sh` script contained in the module.
* [**geomesa-compute**](geomesa-compute): This module contains utilities for working with distributed computing environments.  Currently, there are methods for instantiating an Apache Spark Resilient Distributed Dataset from a CQL query against data stored in GeoMesa.  Eventually, this project will contain bindings for traditional map-reduce processing, Scalding, and other environments.
* [**geomesa-convert**](geomesa-convert): This module is a configurable and extensible library for converting data into SimpleFeatures.
* **geomesa-distributed-runtime**: This module assembles a jar with dependencies that must be distributed to Accumulo tablet servers lib/ext directory or to an HDFS directory where Accumulo's VFSClassLoader can pick it up.
* **geomesa-examples**: This module includes Developer quickstart tutorials and examples for how to work with GeoMesa in Accumulo and Kafka.
* **geomesa-features**: This module includes code for serializing SimpleFeatures and custom SimpleFeature implementations designed for GeoMesa.
* **geomesa-filter**: This module is a library for manipulating and working with GeoTools Filters.
* **geomesa-hbase**: This module includes an implementation of GeoMesa on HBase and Google Cloud Bigtable.
* [**geomesa-jobs**](geomesa-jobs): This module contains map/reduce and scalding jobs for maintaining GeoMesa.
* [**geomesa-kafka**](geomesa-kafa/geomesa-kafka-datastore): This module contains an implementation of GeoMesa in Kafka for maintaining near-real-time caches of streaming data.
* [**geomesa-plugin**](geomesa-plugin): This module creates a plugin which provides WFS and WMS support.  The JAR named geomesa-plugin-<Version>-geoserver-plugin.jar is ready to be deployed in GeoServer by copying it into geoserver/WEB-INF/lib/
* [**geomesa-process**](geomesa-process): This module contains analytic processes optimized on GeoMesa data stores.
* [**geomesa-raster**](geomesa-raster): This module adds support for ingesting and working with geospatially-referenced raster data in GeoMesa.
* **geomesa-security**: This module adds support for managing security and authorization levels for data stored in GeoMesa. 
* [**geomesa-stream**](geomesa-stream): This module is a GeoMesa library that provides tools to process streams of `SimpleFeatures`.
* [**geomesa-tools**](geomesa-tools): This module contains a set of command line tools for managing features, ingesting and exporting data, configuring tables, and explaining queries in GeoMesa.
* [**geomesa-utils**](geomesa-utils): This module stores our GeoHash implementation and other general library functions unrelated to Accumulo. This sub-project contains any helper tools for geomesa.  Some of these tools such as the GeneralShapefileIngest have Map/Reduce components, so the geomesa-utils JAR lives on HDFS.
* **geomesa-web**: This module contains web services for accessing GeoMesa.
* **geomesa-z3**: This module contains the implementation of Z3, GeoMesa's space-filling Z-order curve.

## Scala console via scala-maven-plugin

To test and interact with core functionality, the Scala console can be invoked in a couple of ways.  From the root directory by specifying geomesa-accumulo-datastore 

    cd geomesa-accumulo
    mvn -pl geomesa-accumulo-datastore scala:console

By default, all of the project packages in `geomesa-accumulo-datastore` are loaded along with JavaConversions, JavaConverters.
