package omega.sunkey.sunkdrome.server

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.javalin.Javalin
import io.javalin.http.Context

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
}