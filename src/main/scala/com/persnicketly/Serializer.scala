package com.persnicketly

trait Serializer {
  def toByteArray: Array[Byte] = {
    val bos = new java.io.ByteArrayOutputStream()
    val out = new java.io.ObjectOutputStream(bos)
    out.writeObject(this)
    bos.toByteArray
  }
}

object Serializer {
  def apply[T](bytes: Array[Byte]): T = {
    val bis = new java.io.ByteArrayInputStream(bytes)
    val in = new java.io.ObjectInputStream(bis)
    in.readObject.asInstanceOf[T]
  }
}
