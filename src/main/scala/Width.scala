/*
 Copyright (c) 2011, 2012, 2013, 2014 The Regents of the University of
 California (Regents). All Rights Reserved.  Redistribution and use in
 source and binary forms, with or without modification, are permitted
 provided that the following conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the following
      two paragraphs of disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      two paragraphs of disclaimer in the documentation and/or other materials
      provided with the distribution.
    * Neither the name of the Regents nor the names of its contributors
      may be used to endorse or promote products derived from this
      software without specific prior written permission.

 IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF
 ANY, PROVIDED HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION
 TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 MODIFICATIONS.
*/

package Chisel

import Node._
import ChiselError._

object Width {
  def apply(w: Int = -1, throwUnset: Boolean = true): Width = {
    if (w < -1) {
      ChiselError.warning("Width:apply < -1")
      if (throwUnset) {
        throw new Exception("Width:apply < -1");
      }
    }
    val neww = new Width(w, throwUnset)
    neww
  }

}

class Width(_width: Int, _throwIfUnset: Boolean) extends Ordered[Width] {
  type WidthType = Option[Int]  // The internal representation of width
  val unSet: WidthType = None	// The internal "unset" value
  val unSetVal: Int = -1	// The external "unset" value
	// Construction: initialize internal state
  private var widthVal: WidthType = if (_width > -1) Some(_width) else unSet
  val throwIfUnsetRef = _throwIfUnset
  val debug: Boolean = false

  if (debug) {
    ChiselError.warning("Width: w " + _width + ", throw " + _throwIfUnset)
  }

  // Caller requires a single, integer value (legacy code).
  def width: Int = if (isKnown) widthVal.get else unSetVal

  //  Set the width of a Node.
  def setWidth(w: Int) = {
    assert(w > -2, ChiselError.error("Width.setWidth: setting width to " + w))
    if (w < 0) {
      if (throwIfUnsetRef) {
        throw new Exception("Width:setWidth < -1");
      }
      widthVal = unSet
    } else {
      widthVal = Some(w)
    }
    if (debug) {
      println("Width(" + this + "):setWidth(" + w + ")")
    }
  }

  // Syntactic sugar - an attempt to set "width" to an integer.
  def width_=(w: Int) {
    setWidth(w)
  }

  // Indicate whether width is actually known(set) or not.
  def isKnown: Boolean = widthVal != unSet

  // Return an "known" integer value or raise an exception
  //  if called when the width is unknown.
  def needWidth(): Int = {
    if (! isKnown ) {
      ChiselError.warning("needWidth but width not set")
      if (throwIfUnsetRef) {
        ChiselError.report()
        throw new Exception("uninitialized width");
      }
    }
    width
  }

  // Return either the width or the specificed value if the width isn't set.
  def widthOrValue(v: Int): Int = {
    if (widthVal != unSet) widthVal.get else v
  }

  // Compare two (known) widths.
  def compare(that: Width) = this.needWidth() - that.needWidth()

  // Print a string representation of width
  override def toString: String = widthVal.toString

  def copy(w: Int = this.width, t: Boolean = this.throwIfUnsetRef): Width = {
    val neww = new Width(w, t)
    neww
  }

  override def clone(): Width = {
    val w = new Width(this.width, this.throwIfUnsetRef)
    w
  }
  
  // Define the arithmetic operations so we can deal with unspecified widths
  // (Currently unused - mostly a template for how to do this.
  def BinaryOp(op: String, operand: Width): Width = {
    if (!this.isKnown) {
      this
    } else if (! operand.isKnown) {
      operand
    } else {
      op match {
        case "+" => Width(this.width + operand.width)
        case "-" => {
          assert(this.width >= operand.width)
          Width(this.width - operand.width)
        }
      }
    }
  }
  
  // Define addition of two widths
  def +(w: Width): Width = BinaryOp("+", w)
  
  // Define subtraction of one Width from another
  def -(w: Width): Width = BinaryOp("-", w)
  
  // Define addition of an Int to a Width
  def +(i: Int): Width = BinaryOp("+", Width(i))
  
  // Define subtraction of an Int from a Width
  def -(i: Int): Width = BinaryOp("-", Width(i))
  
  implicit def fromInt(i: Int): Width = Width(i)
}