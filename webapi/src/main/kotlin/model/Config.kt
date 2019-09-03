package com.geekorum.ttrss.webapi.model

import androidx.annotation.Keep
import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.makeNullable

/**
 * Request payload to get the configuration of the TtRss server.
 */
@Keep
@Serializable
class GetConfigRequestPayload : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "getConfig"
}

data class GetConfigResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    override val content: Content
) : ResponsePayload<GetConfigResponsePayload.Content>() {

    val iconsDir: String?
        get() = content.iconsDir

    val iconsUrl: String?
        get() = content.iconsUrl

    val numFeeds: Int?
        get() = content.numFeeds

    val daemonIsRunning: Boolean?
        get() = content.daemonIsRunning

    @Serializable
    data class Content(
        @SerialName("daemon_is_running")
        val daemonIsRunning: Boolean? = null,
        @SerialName("icons_dir")
        val iconsDir: String? = null,
        @SerialName("icons_url")
        val iconsUrl: String? = null,
        @SerialName("num_feeds")
        val numFeeds: Int? = null,
        override var error: Error? = null

    ) : BaseContent()

    companion object {
        fun serializer(): KSerializer<GetConfigResponsePayload> {
            return GetConfigResponsePayload.GetConfigResponsePayloadSerializer()
        }
    }


    @Serializer(GetConfigResponsePayload::class)
    class GetConfigResponsePayloadSerializer : KSerializer<GetConfigResponsePayload> {
        override fun serialize(encoder: Encoder, obj: GetConfigResponsePayload) {
            TODO("not implemented")
        }

        override fun deserialize(decoder: Decoder): GetConfigResponsePayload {
            val contentDecoder = decoder.beginStructure(descriptor)
            lateinit var content: Content
            var seq: Int? = null
            var status = 0
            loop@ while (true) {
                when (val i = contentDecoder.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> seq = contentDecoder.decodeNullableSerializableElement(descriptor, i,
                        makeNullable(
                            IntSerializer))
                    1 -> status = contentDecoder.decodeIntElement(descriptor, i)
                    2 -> {
                        val contentSerializer = Content.serializer()
                        content =
                            contentDecoder.decodeSerializableElement(contentSerializer.descriptor,
                                i,
                                contentSerializer)
                    }
                }
            }
            contentDecoder.endStructure(descriptor)
            return GetConfigResponsePayload(
                content = content,
                sequence = seq,
                status = status
            )
        }
    }
}
