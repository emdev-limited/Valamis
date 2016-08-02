package com.arcusys.valamis.web.configuration.ioc

import com.escalatesoft.subcut.inject.MutableBindingModule

object Configuration extends MutableBindingModule {
  this <~ new WebConfiguration
}