package com.hearthappy.processor

import com.hearthappy.annotations.ContentType

const val CONTENT_TYPE = "Content-Type"

const val Application_Any = "HttpHeaders.ContentType, ContentType.Application.Any"
const val Application_Atom = "HttpHeaders.ContentType, ContentType.Application.Atom"
const val Application_Cbor = "HttpHeaders.ContentType, ContentType.Application.Cbor"
const val Application_Json = "HttpHeaders.ContentType, ContentType.Application.Json"
const val Application_HalJson = "HttpHeaders.ContentType, ContentType.Application.HalJson"
const val Application_JavaScript = "HttpHeaders.ContentType, ContentType.Application.JavaScript"
const val Application_OctetStream = "HttpHeaders.ContentType, ContentType.Application.OctetStream"
const val Application_FontWoff = "HttpHeaders.ContentType, ContentType.Application.FontWoff"
const val Application_Rss = "HttpHeaders.ContentType, ContentType.Application.Rss"
const val Application_Xml = "HttpHeaders.ContentType, ContentType.Application.Xml"
const val Application_Xml_Dtd = "HttpHeaders.ContentType, ContentType.Application.Xml_Dtd"
const val Application_Zip = "HttpHeaders.ContentType, ContentType.Application.Zip"
const val Application_GZip = "HttpHeaders.ContentType, ContentType.Application.GZip"
const val Application_FormUrlEncoded =
    "HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded"
const val Application_Pdf = "HttpHeaders.ContentType, ContentType.Application.Pdf"
const val Application_ProtoBuf = "HttpHeaders.ContentType, ContentType.Application.ProtoBuf"
const val Application_Wasm = "HttpHeaders.ContentType, ContentType.Application.Wasm"
const val Application_ProblemJson = "HttpHeaders.ContentType, ContentType.Application.ProblemJson"
const val Application_ProblemXml = "HttpHeaders.ContentType, ContentType.Application.ProblemXml"

const val Audio_Any = "HttpHeaders.ContentType, ContentType.Audio.Any"
const val Audio_MP4 = "HttpHeaders.ContentType, ContentType.Audio.MP4"
const val Audio_MPEG = "HttpHeaders.ContentType, ContentType.Audio.MPEG"
const val Audio_OGG = "HttpHeaders.ContentType, ContentType.Audio.OGG"

const val Image_Any = "HttpHeaders.ContentType, ContentType.Image.Any"
const val Image_GIF = "HttpHeaders.ContentType, ContentType.Image.GIF"
const val Image_JPEG = "HttpHeaders.ContentType, ContentType.Image.JPEG"
const val Image_PNG = "HttpHeaders.ContentType, ContentType.Image.PNG"
const val Image_SVG = "HttpHeaders.ContentType, ContentType.Image.SVG"
const val Image_XIcon = "HttpHeaders.ContentType, ContentType.Image.XIcon"

const val Message_Any = "HttpHeaders.ContentType, ContentType.Message.Any"
const val Message_Http = "HttpHeaders.ContentType, ContentType.Message.Http"

const val MultiPart_Any = "HttpHeaders.ContentType, ContentType.MultiPart.Any"
const val MultiPart_Mixed = "HttpHeaders.ContentType, ContentType.MultiPart.Mixed"
const val MultiPart_Alternative = "HttpHeaders.ContentType, ContentType.MultiPart.Alternative"
const val MultiPart_Related = "HttpHeaders.ContentType, ContentType.MultiPart.Related"
const val MultiPart_FormData = "HttpHeaders.ContentType, ContentType.MultiPart.FormData"
const val MultiPart_Signed = "HttpHeaders.ContentType, ContentType.MultiPart.Signed"
const val MultiPart_Encrypted = "HttpHeaders.ContentType, ContentType.MultiPart.Encrypted"
const val MultiPart_ByteRanges = "HttpHeaders.ContentType, ContentType.MultiPart.ByteRanges"

const val Text_Any = "HttpHeaders.ContentType, ContentType.Text.Any"
const val Text_Plain = "HttpHeaders.ContentType, ContentType.Text.Plain"
const val Text_CSS = "HttpHeaders.ContentType, ContentType.Text.CSS"
const val Text_CSV = "HttpHeaders.ContentType, ContentType.Text.CSV"
const val Text_Html = "HttpHeaders.ContentType, ContentType.Text.Html"
const val Text_JavaScript = "HttpHeaders.ContentType, ContentType.Text.JavaScript"
const val Text_VCard = "HttpHeaders.ContentType, ContentType.Text.VCard"
const val Text_Xml = "HttpHeaders.ContentType, ContentType.Text.Xml"
const val Text_EventStream = "HttpHeaders.ContentType, ContentType.Text.EventStream"

const val Video_Any = "HttpHeaders.ContentType, ContentType.Video.Any"
const val Video_MPEG = "HttpHeaders.ContentType, ContentType.Video.MPEG"
const val Video_MP4 = "HttpHeaders.ContentType, ContentType.Video.MP4"
const val Video_QuickTime = "HttpHeaders.ContentType, ContentType.Video.QuickTime"


/**
 * 获取固定头
 * @receiver String
 * @return String
 */
internal fun String?.asFixedHeader(): String {
    val key = this?.split(":")?.get(0)
    if (key == CONTENT_TYPE) {
        return when (this) {
            ContentType.Application.Any -> Application_Any
            ContentType.Application.Atom -> Application_Atom
            ContentType.Application.Cbor -> Application_Cbor
            ContentType.Application.Json -> Application_Json
            ContentType.Application.HalJson -> Application_HalJson
            ContentType.Application.JavaScript -> Application_JavaScript
            ContentType.Application.OctetStream -> Application_OctetStream
            ContentType.Application.FontWoff -> Application_FontWoff
            ContentType.Application.Rss -> Application_Rss
            ContentType.Application.Xml -> Application_Xml
            ContentType.Application.Xml_Dtd -> Application_Xml_Dtd
            ContentType.Application.Zip -> Application_Zip
            ContentType.Application.GZip -> Application_GZip
            ContentType.Application.FormUrlEncoded -> Application_FormUrlEncoded
            ContentType.Application.Pdf -> Application_Pdf
            ContentType.Application.ProtoBuf -> Application_ProtoBuf
            ContentType.Application.Wasm -> Application_Wasm
            ContentType.Application.ProblemJson -> Application_ProblemJson
            ContentType.Application.ProblemXml -> Application_ProblemXml
            ContentType.Audio.Any -> Audio_Any
            ContentType.Audio.MP4 -> Audio_MP4
            ContentType.Audio.MPEG -> Audio_MPEG
            ContentType.Audio.OGG -> Audio_OGG
            ContentType.Image.Any -> Image_Any
            ContentType.Image.GIF -> Image_GIF
            ContentType.Image.JPEG -> Image_JPEG
            ContentType.Image.PNG -> Image_PNG
            ContentType.Image.SVG -> Image_SVG
            ContentType.Image.XIcon -> Image_XIcon
            ContentType.Message.Any -> Message_Any
            ContentType.Message.Http -> Message_Http
            ContentType.MultiPart.Any -> MultiPart_Any
            ContentType.MultiPart.Mixed -> MultiPart_Mixed
            ContentType.MultiPart.Alternative -> MultiPart_Alternative
            ContentType.MultiPart.Related -> MultiPart_Related
            ContentType.MultiPart.FormData -> MultiPart_FormData
            ContentType.MultiPart.Signed -> MultiPart_Signed
            ContentType.MultiPart.Encrypted -> MultiPart_Encrypted
            ContentType.MultiPart.ByteRanges -> MultiPart_ByteRanges
            ContentType.Text.Any -> Text_Any
            ContentType.Text.Plain -> Text_Plain
            ContentType.Text.CSS -> Text_CSS
            ContentType.Text.CSV -> Text_CSV
            ContentType.Text.Html -> Text_Html
            ContentType.Text.JavaScript -> Text_JavaScript
            ContentType.Text.VCard -> Text_VCard
            ContentType.Text.Xml -> Text_Xml
            ContentType.Text.EventStream -> Text_EventStream
            ContentType.Video.Any -> Video_Any
            ContentType.Video.MPEG -> Video_MPEG
            ContentType.Video.MP4 -> Video_MP4
            ContentType.Video.QuickTime -> Video_QuickTime
            else -> Application_Json
        }
    }
    return Application_Json
}