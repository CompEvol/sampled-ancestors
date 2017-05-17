sampled-ancestors
=================

This BEAST2 package provides MCMC proposals and post-processing tools for samples of trees containing sampled ancestors. 
It relies on the support for sampled-ancestor trees built into the `beast.evolution.tree.Tree` class (https://github.com/CompEvol/beast2/blob/master/src/beast/evolution/tree/Tree.java).

Currently the sampled-ancestors package uses `beast.*` Java package namespace, which is now deprecated because the `beast.*` package namespace is reserved for the beast2 core (https://github.com/CompEvol/beast2).

The main Java packages and folders in the sampled-ancestors BEAST2 package are: 

beast.app.simulators
--------------------

Contains simulators for the fossilized birth-death (FBD) model and FBD-skyline model.

beast.app.tools
---------------

* Contains conversion tools between zero-branch-length sampled-ancestor trees and native sampled-ancestor trees
* Contains `SATreeTraceAnalysis` and various other post-processing tools and support
		
beast.evolution.operators
-------------------------
		
* Contains the operators that permit MCMC on sampled-ancestor trees

beast.evolution.speciation
--------------------------

Contains `SpeciesTreeDistribution` calculations for the FBD prior:

* `SABirthDeathModel` - A species tree probability density of FBD prior with multiple hard-coded parameterizations possible. 
* `ParameterizedSABirthDeathModel` -  A species tree probability density with multiple parameterizations implemented in an object-oriented way.

templates
---------

Contains a template called `FBD.xml` which provides a template for `SABirthDeathModel` and sampled-ancestor tree operators.
