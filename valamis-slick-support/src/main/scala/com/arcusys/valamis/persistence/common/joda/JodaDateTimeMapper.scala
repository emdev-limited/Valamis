/*
 * Copyright 2013 Toshiyuki Takahashi
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.arcusys.valamis.persistence.common.joda

import java.sql._

import com.arcusys.valamis.persistence.common.joda.converter.JodaDateTimeSqlTimestampConverter

import scala.slick.driver.{JdbcDriver, MySQLDriver}
import org.joda.time._
import slick.profile.RelationalProfile


class JodaDateTimeMapper(val driver: JdbcDriver) {

  import driver._

  val typeMapper =
    new DriverJdbcType[DateTime] with JodaDateTimeSqlTimestampConverter {
      def zero = new DateTime(0L)
      def sqlType = java.sql.Types.TIMESTAMP
      override def sqlTypeName(size: Option[RelationalProfile.ColumnOption.Length]) =  driver match {
        case driver: MySQLDriver => "DATETIME"
        case _ => columnTypes.timestampJdbcType.sqlTypeName(size)
      }
      override def setValue(v: DateTime, p: PreparedStatement, idx: Int): Unit =
        p.setTimestamp(idx, toSqlType(v))
      override def getValue(r: ResultSet, idx: Int): DateTime =
        fromSqlType(r.getTimestamp(idx))
      override def updateValue(v: DateTime, r: ResultSet, idx: Int): Unit =
        r.updateTimestamp(idx, toSqlType(v))
      override def valueToSQLLiteral(value: DateTime) = columnTypes.timestampJdbcType.valueToSQLLiteral(toSqlType(value))
    }
}

