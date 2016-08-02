package com.arcusys.valamis.lesson.generator.tincan.file

import java.io.{File, InputStream}

import com.arcusys.valamis.util.{FileSystemUtil, ZipBuilder}

import scala.xml.Elem

trait TinCanRevealJSPackageGeneratorContract {
  /** Returns path to zip file */
  def composePackage(filesToAdd: Seq[(String, InputStream)], rootActivityId: String, title: String, description: String): File
}

object TinCanRevealJSPackageGenerator extends TinCanRevealJSPackageGeneratorContract {
  private def composePackage(filesToAdd: Seq[(String, InputStream)], manifest: Elem): File = {

    val zipFile = FileSystemUtil.getTempFile("Package", "zip")

    val zip = new ZipBuilder(zipFile)
    zip.addEntry("tincan.xml", manifest.toString())
    filesToAdd.foreach { case (fileName, is) => zip.addFile(is, "data/" + fileName) }
    zip.close()
    zipFile
  }

  override def composePackage(filesToAdd: Seq[(String, InputStream)], rootActivityId: String, title: String, description: String) = {
    composePackage(filesToAdd, getDefaultManifest(rootActivityId, title, description))
  }

  def getDefaultManifest(rootActivityId: String, title: String, description: String) = {
    <tincan xmlns="http://projecttincan.com/tincan.xsd">
      <activities>
        <activity id={ rootActivityId } type="http://adlnet.gov/expapi/activities/course">
          <name>
            { title }
          </name>
          <description lang="en-US">
            { description }
          </description>
          <launch lang="en-us">data/index.html</launch>
        </activity>
      </activities>
    </tincan>
  }
}