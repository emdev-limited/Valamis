package com.arcusys.valamis.storyTree.model

/**
 * Created by mminin on 16.06.15.
 */
case class StoryTree(id: Long,
                     title: String,
                     description: String,
                     nodes: Seq[StoryTreeNode])

case class StoryTreeNode(id: Long,
                         title: String,
                         description: String,
                         nodes: Seq[StoryTreeNode],
                         relationComment: Option[String],
                         packages: Seq[StoryTreePackageItem])

case class StoryTreePackageItem(id: Long,
                                packageId: Long,
                                title: Option[String],
                                description: Option[String],
                                topics: Seq[String],
                                relationComment: Option[String])
