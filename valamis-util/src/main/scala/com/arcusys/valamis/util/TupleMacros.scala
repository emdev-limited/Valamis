package com.arcusys.valamis.util

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

/**
  * Created by pkornilov on 15.03.16.
  */

trait ToTuple[Z, T] {
  def toTuple(z: Z): T
}

trait FromTuple[Z, T] {
  def fromTuple(t: T): Z
}

object TupleHelpers extends Common {
  def toTupleWithFilter[Z: ({type ToTuple_[Z] = ToTuple[Z, T]})#ToTuple_, T](z: Z): T =
    implicitly[ToTuple[Z, T]].toTuple(z)

  def fromTupleWithFilter[Z: ({type FromTuple_[Z] = FromTuple[Z, T]})#FromTuple_, T](t: T): Z =
    implicitly[FromTuple[Z, T]].fromTuple(t)
}

trait Common {

  //TODO pass this filter from outside
  val fieldFilter: FieldFilter = {
    case (name, tpe) =>
      !(name.equalsIgnoreCase("id") && (tpe == "Long" || tpe == "Option[Long]"))
  }


  def getFieldNamesAndTypes(c: Context)(tpe: c.universe.Type):
  Iterable[(c.universe.Name, c.universe.Type)] = {
    import c.universe._

    object CaseField {
      def unapply(trmSym: TermSymbol): Option[(Name, Type)] = {
        if (trmSym.isVal && trmSym.isCaseAccessor)
          Some((TermName(trmSym.name.toString.trim), trmSym.typeSignature))
        else
          None
      }
    }

    val res = tpe.decls.collect {
      case CaseField(nme, tpe) if fieldFilter(nme.toTermName.decodedName.toString, tpe.toString) =>
        (nme, tpe)
    }
    res
  }
}

object ToTuple extends Common {

  implicit def toTupleMacro[Z, T]: ToTuple[Z, T] = macro toTupleMacroImpl[Z, T]

  def toTupleMacroImpl[Z: c.WeakTypeTag, T](c: Context): c.Expr[ToTuple[Z, T]] = {
    import c.universe._

    val tpe: Type = weakTypeOf[Z]

    val (names, types) = getFieldNamesAndTypes(c)(tpe).unzip

    val fldSels: Iterable[Tree] = names.map { nme =>
      q"""
         z.${nme.toTermName}
        """
    }

    val toTuple: Tree =
      q"""
         new ToTuple[$tpe, (..$types)] {
           def toTuple(z: $tpe) =  (..$fldSels)
         }
        """

    val res = c.Expr[ToTuple[Z, T]](toTuple)
    println("Generated toTuple method for: " + tpe.toString)
    res
  }
}

object FromTuple extends Common {
  implicit def fromTupleMacro[Z, T]: FromTuple[Z, T] = macro fromTupleMacroImpl[Z, T]

  def fromTupleMacroImpl[Z: c.WeakTypeTag, T](c: Context): c.Expr[FromTuple[Z, T]] = {
    import c.universe._

    val tpe: Type = weakTypeOf[Z]

    val (nmes, tpes) = getFieldNamesAndTypes(c)(tpe).unzip

    def prj(i: Int): TermName = newTermName("_" + i)

    val prjs: Seq[Tree] = (1 to nmes.toSeq.size).map { i =>
      q"""
         t.${prj(i)}
        """
    }

    val tpeSym: Symbol = tpe.typeSymbol.companionSymbol

    val fromTuple: Tree =
      q"""
         new FromTuple[$tpe, (..$tpes)] {
           def fromTuple(t: (..$tpes)) = ${tpeSym}(..$prjs)
         }
        """

    val res = c.Expr[FromTuple[Z, T]](fromTuple)
    println("Generated toTuple method for: " + tpe.toString)
    res
  }
}






