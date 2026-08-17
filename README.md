# Sampled Ancestors

[![CI & Publish](https://github.com/CompEvol/sampled-ancestors/actions/workflows/ci-publish.yml/badge.svg)](https://github.com/CompEvol/sampled-ancestors/actions/workflows/ci-publish.yml)

This [BEAST 3](https://github.com/CompEvol/beast3) package provides MCMC proposals and post-processing tools for samples of trees containing sampled ancestors.
It relies on the support for sampled-ancestor trees built into the `beast.base.evolution.tree.Tree` class.

Single Maven artifact: `io.github.compevol:sampled-ancestors`. JPMS module: `sampled.ancestors`.

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
mvn exec:exec -Dbeast.args="examples/bears.xml"
```

To launch BEAUti:

```sh
mvn exec:exec -Dbeast.module=beast.fx -Dbeast.main=beastfx.app.beauti.Beauti
```

## Project structure

### `sa.app.simulators`

Simulators for the fossilized birth-death (FBD) model and FBD-skyline model.

### `sa.app.tools`

Conversion tools between zero-branch-length sampled-ancestor trees and native sampled-ancestor trees.

### `sa.app.tools.fx`

`SATreeTraceAnalysis` and various other post-processing tools.

### `sa.evolution.operators`

Operators that permit MCMC on sampled-ancestor trees.

### `sa.evolution.speciation`

`SpeciesTreeDistribution` calculations for the FBD prior:

* `SABirthDeathModel` -- FBD prior with multiple hard-coded parameterizations
* `ParameterizedSABirthDeathModel` -- FBD prior with object-oriented parameterizations

### `sa.beauti`

BEAUti input editors for sampled-ancestor priors.

### `fxtemplates`

Contains `FBD.xml`, a BEAUti template for `SABirthDeathModel` and sampled-ancestor tree operators.
Located at `src/main/resources/sampled.ancestors/fxtemplates/`.

## Release

### 1. Maven Central release (JARs)

Push a `v*` tag to trigger `.github/workflows/ci-publish.yml`, which sets the Maven
version from the tag, builds, tests, GPG-signs, and publishes to Maven Central:

```bash
git tag v2.3.0
git push origin v2.3.0
```

Monitor the run at:
https://github.com/CompEvol/sampled-ancestors/actions/workflows/ci-publish.yml

### 2. GitHub release (BEAST package ZIP)

First remove `-SNAPSHOT` from `<version>` in `pom.xml` so it matches the release
(e.g. `2.3.0-SNAPSHOT` -> `2.3.0`), and snyc the `version.xml`, then build the installable BEAST package ZIP locally:

```bash
mvn clean package -DskipTests
```

**Note:** if you skip the manual edit above, the build still succeeds, but the module
jar bundled inside the ZIP (`lib/model-selection-<version>.jar`) will carry the
`-SNAPSHOT` suffix — that's meant for dev/testing builds, not an official release.
Alternatively, instead of hand-editing `pom.xml`, run:

```bash
mvn versions:set -DnewVersion=<version> -DgenerateBackupPoms=false
```

The ZIP is written to `target/MODEL_SELECTION.v<version>.zip`. Then manually:

1. Go to https://github.com/CompEvol/sampled-ancestors/releases
2. Choose the matching tag (e.g. `v2.3.0`), fill in the release title/notes
3. Upload `target/MODEL_SELECTION.v<version>.zip` as a release asset
4. Publish


## License

This software is free (as in freedom). With your modified versions provided you extend the same courtesy to
users of your modified version. Specifically, it is made available under the
terms of the GNU General Public License version 3.

## Acknowledgements

Work on this project was supported by:

* [The Royal Society of New Zealand's Marsden Fund](http://www.royalsociety.org.nz/programmes/funds/marsden/) grant contract UOA1324
* [The University of Auckland](http://auckland.ac.nz)
