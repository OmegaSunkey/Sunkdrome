package omega.sunkey.sunkdrome.server.dataclasses

data class License(
    val valid: Boolean = true,
    val email: String = "",
)

data class LicenseView(
    val license: License
)