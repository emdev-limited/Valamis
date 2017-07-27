package com.arcusys.valamis.storyTree.service

import com.arcusys.valamis.storyTree.model.StoryTreeStatus

/**
 * Created by mminin on 15.06.15.
 */
trait StoryTreeStatusService {
  def get(treeId: Long, userId: Long): StoryTreeStatus
}