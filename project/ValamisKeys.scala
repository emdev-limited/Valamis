import sbt._

object ValamisKeys {

  //!!!This task assumes that LRS and Assignments were already built before running this task!!!
  lazy val osgiFullLpkg = taskKey[Unit]("Generates OSGi bundles and collects dependencies for it and makes lpkg file, " +
    "including Valamis bundles, Valamis dependencies, LRS bundles, LRS dependencies, Assignments war")

  lazy val osgiFullLpkgNexus = taskKey[Unit]("Generates OSGi bundles and collects dependencies for it and makes lpkg file, " +
    "including Valamis bundles, Valamis dependencies, LRS bundles, LRS dependencies, Assignments war. LRS and Assignments " +
    "are downloaded from Nexus")

}