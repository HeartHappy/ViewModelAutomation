package com.hearthappy.annotations


class ContentType {

    /**
     * Represents a pattern application / * to match any application content type.
     */
    object Application {
        const val Any = "Content-Type:application/*"
        const val Atom = "Content-Type:application/atom+xml"
        const val Cbor = "Content-Type:application/cbor"
        const val Json = "Content-Type:application/json"
        const val HalJson = "Content-Type:application/hal+json"
        const val JavaScript = "Content-Type:application/javascript"
        const val OctetStream = "Content-Type:application/octet-stream"
        const val FontWoff = "Content-Type:application/font-woff"
        const val Rss = "Content-Type:application/rss+xml"
        const val Xml = "Content-Type:application/xml"
        const val Xml_Dtd = "Content-Type:application/xml-dtd"
        const val Zip = "Content-Type:application/zip"
        const val GZip = "Content-Type:application/gzip"
        const val FormUrlEncoded = "Content-Type:application/x-www-form-urlencoded"
        const val Pdf = "Content-Type:application/pdf"
        const val ProtoBuf = "Content-Type:application/protobuf"
        const val Wasm = "Content-Type:application/wasm"
        const val ProblemJson = "Content-Type:application/problem+json"
        const val ProblemXml = "Content-Type:application/problem+xml"
    }

    /**
     * Provides a list of standard subtypes of an audio content type.
     */
    object Audio {
        const val Any = "Content-Type:audio/*"
        const val MP4 = "Content-Type:audio/mp4"
        const val MPEG = "Content-Type:audio/mpeg"
        const val OGG = "Content-Type:audio/ogg"
    }

    /**
     * Provides a list of standard subtypes of an image content type.
     */
    object Image {
        const val Any = "Content-Type:image/*"
        const val GIF = "Content-Type:image/gif"
        const val JPEG = "Content-Type:image/jpeg"
        const val PNG = "Content-Type:image/png"
        const val SVG = "Content-Type:image/svg+xml"
        const val XIcon = "Content-Type:image/x-icon"
    }

    /**
     * Provides a list of standard subtypes of a message content type.
     */
    object Message {
        const val Any = "Content-Type:message/*"
        const val Http = "Content-Type:message/http"
    }

    /**
     * Provides a list of standard subtypes of a multipart content type.
     */
    object MultiPart {
        const val Any = "Content-Type:multipart/*"
        const val Mixed = "Content-Type:multipart/mixed"
        const val Alternative = "Content-Type:multipart/alternative"
        const val Related = "Content-Type:multipart/related"
        const val FormData = "Content-Type:multipart/form-data"
        const val Signed = "Content-Type:multipart/signed"
        const val Encrypted = "Content-Type:multipart/encrypted"
        const val ByteRanges = "Content-Type:multipart/byteranges"
    }

    /**
     * Provides a list of standard subtypes of a text content type.
     */
    object Text {
        const val Any = "Content-Type:text/*"
        const val Plain = "Content-Type:text/plain"
        const val CSS = "Content-Type:text/css"
        const val CSV = "Content-Type:text/csv"
        const val Html = "Content-Type:text/html"
        const val JavaScript = "Content-Type:text/javascript"
        const val VCard = "Content-Type:text/vcard"
        const val Xml = "Content-Type:text/xml"
        const val EventStream = "Content-Type:text/event-stream"
    }

    /**
     * Provides a list of standard subtypes of a video content type.
     */
    object Video {
        const val Any = "Content-Type:video/*"
        const val MPEG = "Content-Type:video/mpeg"
        const val MP4 = "Content-Type:video/mp4"
        const val OGG = "Content-Type:video/ogg"
        const val QuickTime = "Content-Type:video/quicktime"
    }
}

