# Sampled Ancestors

[![Build Status](https://github.com/CompEvol/sampled-ancestors/workflows/Unit%2Fintegration%20tests/badge.svg)](https://github.com/CompEvol/sampled-ancestors/actions?query=workflow%3A%22Unit%2Fintegration+tests%22)

This [BEAST 3](https://github.com/CompEvol/beast3) package provides MCMC proposals and post-processing tools for samples of trees containing sampled ancestors.
It relies on the support for sampled-ancestor trees built into the `beast.base.evolution.tree.Tree` class.

The paper describing this package is:

Alexandra Gavryushkina, David Welch, Tanja Stadler, Alexei J. Drummond (2014) Bayesian Inference of Sampled Ancestor Trees for Epidemiology and Fossil Calibration. _PLoS Computational Biology_ https://doi.org/10.1371/journal.pcbi.1003919

## Building from source

Requirements:

* JDK 25 or later
* Apache Maven

```sh
mvn compile
```

To run an example:

```sh
mvn -pl beast-sampled-ancestors exec:exec -Dbeast.args="examples/bears.xml"
```

## Project structure

This is a multi-module Maven project with two modules:

* **beast-sampled-ancestors** — core module (no JavaFX dependency, runs headless on clusters)
* **beast-sampled-ancestors-fx** — BEAUti input editors and JavaFX-dependent tools

### `sa.app.simulators`

Simulators for the fossilized birth-death (FBD) model and FBD-skyline model.

### `sa.app.tools`

* Conversion tools between zero-branch-length sampled-ancestor trees and native sampled-ancestor trees
* `SATreeTraceAnalysis` and various other post-processing tools (in `-fx` module)

### `sa.evolution.operators`

Operators that permit MCMC on sampled-ancestor trees.

### `sa.evolution.speciation`

`SpeciesTreeDistribution` calculations for the FBD prior:

* `SABirthDeathModel` — FBD prior with multiple hard-coded parameterizations
* `ParameterizedSABirthDeathModel` — FBD prior with object-oriented parameterizations

### `sa.beauti` (in `-fx` module)

BEAUti input editors for sampled-ancestor priors.

### `fxtemplates`

Contains `FBD.xml`, a BEAUti template for `SABirthDeathModel` and sampled-ancestor tree operators.

## License

This software is free (as in freedom). With your modified versions provided you extend the same courtesy to
users of your modified version. Specifically, it is made available under the
terms of the GNU General Public License version 3.

## Acknowledgements

Work on this project was supported by:

* [The Royal Society of New Zealand's Marsden Fund](http://www.royalsociety.org.nz/programmes/funds/marsden/) grant contract UOA1324
* [The University of Auckland](http://auckland.ac.nz)
