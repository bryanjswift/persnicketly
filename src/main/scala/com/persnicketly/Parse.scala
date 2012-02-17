package com.persnicketly

object Parse {
  def apply[T](f: Array[Byte] => T): Parse[T] = new Parse(f)
}

class Parse[T](f: Array[Byte] => T) {
  def apply(bytes: Array[Byte]): T = f(bytes)
}

