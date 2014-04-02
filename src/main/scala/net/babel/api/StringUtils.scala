package net.babel.api

import org.apache.commons.lang.StringEscapeUtils.escapeJava
import com.fasterxml.jackson.databind.{ DeserializationContext, JsonDeserializer, SerializerProvider, JsonSerializer }
import com.fasterxml.jackson.core.{ Version, JsonParser, JsonGenerator }
import com.fasterxml.jackson.databind.module.SimpleModule
import java.io.StringWriter

object StringUtils {

  def escape(input: Option[String]): String = {
    input match {
      case Some(x) => escapeJava(x)
      case None => ""
    }
  }

  object CustomJson extends com.codahale.jerkson.Json {
    val module = new SimpleModule("CustomJson", Version.unknownVersion())
    mapper.registerModule(module)
  }

  def toJerksonJson(s: Any) = CustomJson.generate(s)
  def fromJerksonJson[A: Manifest](s: String) = CustomJson.parse[A](s)

}