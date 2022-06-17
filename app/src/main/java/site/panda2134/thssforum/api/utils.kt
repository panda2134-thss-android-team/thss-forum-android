package site.panda2134.thssforum.api

import com.google.gson.*
import io.gsonfire.DateSerializationPolicy
import io.gsonfire.GsonFireBuilder
import java.lang.reflect.Type
import java.time.Instant


val gsonFireBuilder: GsonFireBuilder = GsonFireBuilder()
    .dateSerializationPolicy(DateSerializationPolicy.rfc3339)
val gsonBuilder = gsonFireBuilder.createGsonBuilder()
    .registerTypeAdapter(Instant::class.java, object: JsonDeserializer<Instant>,
        JsonSerializer<Instant> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Instant {
            if (json !is JsonPrimitive || ! json.isString) {
                throw IllegalStateException("parsing Instant requires a string")
            }
            return Instant.parse(json.asString)
        }

        override fun serialize(
            src: Instant?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return if (src == null) {
                JsonNull.INSTANCE
            } else {
                JsonPrimitive(src.toString())
            }
        }

    })
val gsonFireObject = gsonBuilder.create()