package com.arcusys.valamis.storyTree.model

case class Story(
    id: Option[Long],
    courseId: Long,
    title: String,
    description: String,
    logo: Option[String],
    published: Boolean,
    isDefault: Boolean = false
)


case class StoryNode(
    id: Option[Long],
    parentId: Option[Long],
    treeId: Long,
    title: String,
    description: String,
    //Comment for relation to parent
    comment: Option[String])

case class StoryPackageItem(
    id: Option[Long],
    nodeId: Long,
    treeId: Long,
    packageId: Long,
    comment: Option[String])
