package omega.sunkey.sunkensonic.server.dataclasses

data class License(
    val valid: Boolean = true,
    val email: String = "",
)

data class LicenseView(
    val license: License
)