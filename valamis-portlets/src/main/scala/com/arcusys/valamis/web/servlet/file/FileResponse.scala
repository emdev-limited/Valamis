package com.arcusys.valamis.web.servlet.file

case class FileResponse(id: Long,
                        contentType: String,
                        name: String,
                        url: String,
                        customData: String="")