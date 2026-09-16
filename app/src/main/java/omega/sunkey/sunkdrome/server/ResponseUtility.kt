package omega.sunkey.sunkdrome.server

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.javalin.http.Context


fun reject(ctx: Context, code: Int, message: String) {
    val response = FailedResponse(
        error = Error(code, message)
    )
    val root = Root(response)
    val f = ctx.queryParam("f")
    ctx.status(200)

    if(f == "json") {
        ctx.contentType("application/json")
        ctx.json(root)
    } else {
        ctx.contentType("application/xml")
        val xml = XmlMapper()
        ctx.result(xml.writeValueAsString(root))
    }
}

fun <T> success(ctx: Context, payload: T? = null) {
    val response = Response(
        status = "ok",
        payload = payload
    )
    val root = Root(response)
    val f = ctx.queryParam("f")
    ctx.status(200)

    if (f == "json") {
        ctx.contentType("application/json")
        ctx.json(root)
    } else {
        ctx.contentType("application/xml")
        val xml = XmlMapper()
        ctx.result(xml.writeValueAsString(root))
    }
}