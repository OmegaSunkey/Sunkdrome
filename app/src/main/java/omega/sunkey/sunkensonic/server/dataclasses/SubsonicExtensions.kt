package omega.sunkey.sunkensonic.server.dataclasses

data class OpenSubsonicExtension(
    val name: String,
    val versions: List<Int>
)

data class OpenSubsonicExtensionsPayload(
    val openSubsonicExtensions: List<OpenSubsonicExtension>?
)