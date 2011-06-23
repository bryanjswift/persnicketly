package com.persnicketly.web

case class JsonResponse(status: Int, data: Map[String, Any], errors: List[String])

object JsonResponse {
  def apply(status: Int) = new JsonResponse(status, Map[String, Any](), Nil)
  def apply(status: Int, data: Map[String, Any]) = new JsonResponse(status, data, Nil)
  def apply(status: Int, errors: List[String]) = new JsonResponse(status, Map[String, Any](), errors)
}
