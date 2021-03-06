package org.clulab.aske.automates.scienceparse

import java.io.File
import java.nio.file.Path
import com.typesafe.config.Config
import ai.lum.common.ConfigUtils._
import ai.lum.common.FileUtils._

object ScienceParseClient {

  def fromConfig(config: Config): ScienceParseClient = {
    val domain = config[String]("domain")
    val port = config[String]("port")
    new ScienceParseClient(domain, port)
  }
  //------------------------------------------------------
  //     Methods for creating ScienceParseDocuments
  //------------------------------------------------------

  def mkDocument(file: File): ScienceParseDocument = {
    val json = ujson.read(file.readString())
    mkDocument(json)
  }

  def mkDocument(json: ujson.Js): ScienceParseDocument = {
    val id = json("id").str
    val title = json("title").str
    val year = json("year").num.toInt
    val authors = json("authors").arr.map(mkAuthor).toVector
    val abstractText = json("abstractText").str
    val sections = json("sections").arr.map(mkSection).toVector
    val references = json("references").arr.map(mkReference).toVector
    ScienceParseDocument(id, title, year, authors, abstractText, sections, references)
  }

  def mkAuthor(json: ujson.Js): Author = {
    val name = json("name").str
    val affiliations = json("affiliations").arr.map(_.str).toVector
    Author(name, affiliations)
  }

  def mkSection(json: ujson.Js): Section = {
    val heading = json.obj.get("heading").map(_.str)
    val text = json("text").str
    Section(heading, text)
  }

  def mkReference(json: ujson.Js): Reference = {
    val title = json("title").str
    val authors = json("authors").arr.map(_.str).toVector
    val venue = json("venue").str
    val year = json("year").num.toInt
    Reference(title, authors, venue, year)
  }



}

class ScienceParseClient(
    val domain: String,
    val port: String
) {

  import ScienceParseClient._

  val url = s"http://$domain:$port/v1"
  val headers = Map("Content-type" -> "application/pdf")

  /* Parse to ScienceParseDocument */

  def parsePdf(filename: String): ScienceParseDocument = {
    parsePdf(new File(filename))
  }

  def parsePdf(file: File): ScienceParseDocument = {
    val response = requests.post(url, headers = headers, data = file)
    val json = ujson.read(response.text)
    mkDocument(json)
  }

  def parsePdf(path: Path): ScienceParseDocument = {
    val response = requests.post(url, headers = headers, data = path)
    val json = ujson.read(response.text)
    mkDocument(json)
  }

  def parsePdf(bytes: Array[Byte]): ScienceParseDocument = {
    val response = requests.post(url, headers = headers, data = bytes)
    val json = ujson.read(response.text)
    mkDocument(json)
  }

  /* Parse to json String */

  def parsePdfToJson(filename: String): String = {
    parsePdfToJson(new File(filename))
  }

  def parsePdfToJson(file: File): String = {
    val response = requests.post(url, headers = headers, data = file)
    response.text
  }

  def parsePdfToJson(path: Path): String = {
    val response = requests.post(url, headers = headers, data = path)
    response.text
  }

  def parsePdfToJson(bytes: Array[Byte]): String = {
    val response = requests.post(url, headers = headers, data = bytes)
    response.text
  }

}
