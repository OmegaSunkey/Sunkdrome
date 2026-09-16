package omega.sunkey.sunkdrome.server

import io.javalin.Javalin
import omega.sunkey.sunkdrome.server.dataclasses.License
import omega.sunkey.sunkdrome.server.dataclasses.LicenseView
import omega.sunkey.sunkdrome.server.dataclasses.OpenSubsonicExtensionsPayload

fun Javalin.setBeforeHandlers() {
    this.before("/rest/*") { ctx ->
        //Here goes parameter checking
        /*Implement:
        - Query u: username -string
        - Query p: password -string
        - Query t: token -md5-string
        - Query s: salt -string
        - Validate all parameters and return if u & p are missing or are wrong
         */
        val u = ctx.queryParam("u")
        val p = ctx.queryParam("p")
        //val t: String? = ctx.queryParam("t")
        //val s: String? = ctx.queryParam("s")

        //TODO: actual user management
        if(u == "sunkey" && p == "sunkey") {
            ctx.attribute("currentUser", u)
        } else {
            reject(ctx, 40, "Wrong username or password")
        }
    }
}

fun Javalin.setPaths() {
    this.get("/rest/ping.view") { ctx ->
        success(ctx, null)
    }

    this.get("/rest/getLicense.view") { ctx ->
        success(ctx, LicenseView(License()))
    }

    this.get("/rest/getOpenSubsonicExtensions.view") { ctx ->
        success(ctx, OpenSubsonicExtensionsPayload(emptyList()))
    }
}