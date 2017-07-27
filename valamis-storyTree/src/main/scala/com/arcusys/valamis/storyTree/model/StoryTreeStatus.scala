package com.arcusys.valamis.storyTree.model

import org.joda.time.DateTime


case class StoryTreeStatus(id: Long,
                           progress: Double,
                           lastDate: Option[DateTime],
                           title: String,
                           description: String,
                           nodes: Seq[StoryNodeStatus])

case class StoryNodeStatus(id: Long,
                           progress: Double,
                           lastDate: Option[DateTime],
                           title: String,
                           description: String,
                           relationComment: Option[String],
                           nodes: Seq[StoryNodeStatus],
                           packages: Seq[StoryPackageStatus])

case class StoryPackageStatus(id: Long,
                              packageId: Long,
                              progress: Double,
                              lastDate: Option[DateTime],
                              title: Option[String],
                              description: Option[String],
                              relationComment: Option[String],
                              topics: Seq[StoryTopicStatus])

case class StoryTopicStatus(id: Long,
                            progress: Double,
                            lastDate: Option[DateTime],
                            title: String)