sampled-ancestors
=================

[![Build Status](https://github.com/CompEvol/sampled-ancestors/workflows/Unit%2Fintegration%20tests/badge.svg)](https://github.com/CompEvol/sampled-ancestors/actions?query=workflow%3A%22Unit%2Fintegration+tests%22)

This [BEAST 2](http://www.beast2.org) package provides MCMC proposals and post-processing tools for samples of trees containing sampled ancestors. 
It relies on the support for sampled-ancestor trees built into the `beast.evolution.tree.Tree` class (https://github.com/CompEvol/beast2/blob/master/src/beast/evolution/tree/Tree.java).

This archive contains the source code of the package and is therefore of
primary interest to programmers.

The paper describing this package is:

Alexandra Gavryushkina, David Welch, Tanja Stadler, Alexei J. Drummond (2014) Bayesian Inference of Sampled Ancestor Trees for Epidemiology and Fossil Calibration. _PLoS Computational Biology_ https://doi.org/10.1371/journal.pcbi.1003919

Currently the sampled-ancestors package uses `beast.*` Java package namespace, which is now deprecated because the `beast.*` package namespace is reserved for the beast2 core (https://github.com/CompEvol/beast2).

Building package from source
----------------------------

To build this package from source, ensure you have the following installed:

* Java JDK v1.8 
* Apache Ant v1.9 or later
* An internet connection

The internet connection is required since the build script downloads the most
recent version of the BEAST 2 source to build the package against.
Assuming both Java and Ant are on your execution path and your CWD is the root of
this archive, simply type "ant" from the command line to build the package.
This may take up to a minute due to the script fetching the BEAST source, and
the resulting binary will be left in the `/dist` directory.
To run the unit tests, use "ant test".


Archive Contents
----------------

* `README.md` : this file
* `build.xml` : Ant build script
* `/doc` : Contains additional documentation, currently a tutorial for using fossilized birth-death tree prior.
* `/examples` : Example analyses in beast2 xml format. See below for details.
* `/src` : source files. See below for details.
* `/templates` : BEAUti templates. See below for details.
* `version.xml` : BEAST package version file.

The main Java packages and folders in the sampled-ancestors BEAST2 package are: 

### `beast.app.simulators`

Contains simulators for the fossilized birth-death (FBD) model and FBD-skyline model.

### `beast.app.tools`

* Contains conversion tools between zero-branch-length sampled-ancestor trees and native sampled-ancestor trees
* Contains `SATreeTraceAnalysis` and various other post-processing tools and support
		
### `beast.evolution.operators`
		
* Contains the operators that permit MCMC on sampled-ancestor trees

### `beast.evolution.speciation`

Contains `SpeciesTreeDistribution` calculations for the FBD prior:

* `SABirthDeathModel` - A species tree probability density of FBD prior with multiple hard-coded parameterizations possible. 
* `ParameterizedSABirthDeathModel` -  A species tree probability density with multiple parameterizations implemented in an object-oriented way.

### `templates`

Contains a template called `FBD.xml` which provides a template for `SABirthDeathModel` and sampled-ancestor tree operators.

License
-------

This software is free (as in freedom). You are welcome to use it, modify it,
and distribute your modified versions provided you extend the same courtesy to
users of your modified version.  Specifically, it is made available under the
terms of the GNU General Public License version 3.

Acknowledgements
----------------

Work on this project was supported by:

* [The Royal Society of New Zealand's Marsden Fund](http://www.royalsociety.org.nz/programmes/funds/marsden/) grant contract UOA1324
* [The University of Auckland](http://auckland.ac.nz)
