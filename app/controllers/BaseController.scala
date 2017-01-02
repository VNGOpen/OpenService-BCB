package controllers

import java.util.Collections

import org.apache.commons.lang3.StringUtils

import com.github.ddth.commons.utils.SerializationUtils
import com.google.inject.Provider

import javax.inject.Inject
import modules.registry.IRegistry
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContent
import play.api.mvc.Call
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.data.FormFactory
import play.twirl.api.Html
import collection.JavaConverters._

object BaseController {
    private val EMPTY_FORM_DATA = Collections.EMPTY_MAP.asInstanceOf[java.util.Map[String, String]]

    def parseRequestAsJavaForm = { implicit request: Request[AnyContent] =>
        val scalaFormData = request.body.asFormUrlEncoded.getOrElse(null)
        if (scalaFormData != null) scalaFormData.mapValues { v => v(0) }.asJava else EMPTY_FORM_DATA
    }
}

class BaseController @Inject() (registry: Provider[IRegistry],
                                formFactory: FormFactory,
                                messagesApi: MessagesApi) extends Controller {

    protected def doResponseJson(status: Int): Result = {
        return doResponseJson(status, null, null)
    }

    protected def doResponseJson(status: Int, message: String): Result = {
        return doResponseJson(status, message, null)
    }

    protected def doResponseJson(status: Int, messsage: String, data: Object): Result = {
        val dataMap = Map("status" -> status, "message" -> messsage, "data" -> data)
        doResponseJson(dataMap.filter(_._2 != null).asJava)
    }

    /**
     * Responses a JSON string to client.
     *
     * @param data
     * @return
     */
    protected def doResponseJson(data: Object): Result = {
        return Ok(SerializationUtils.toJsonString(data)).as("application/json")
    }

    /**
     * Returns a redirect response to client.
     */
    protected def doRedirect(url: String): Result = { Redirect(url) }

    /**
     * Returns a redirect response to client.
     */
    protected def doRedirect(call: Call): Result = { Redirect(call) }

    /**
     * Sets the flash message and returns a redirect response to client.
     */
    protected def doRedirect(url: String, flashKey: String, flashMsg: String): Result = {
        if (StringUtils.isBlank(flashKey) || StringUtils.isBlank(flashMsg)) {
            Redirect(url)
        } else {
            Redirect(url).flashing(flashKey -> flashMsg)
        }
    }

    /**
     * Sets the flash message and returns a redirect response to client.
     */
    protected def doRedirect(call: Call, flashKey: String, flashMsg: String): Result = {
        if (StringUtils.isBlank(flashKey) || StringUtils.isBlank(flashMsg)) {
            Redirect(call)
        } else {
            Redirect(call).flashing(flashKey -> flashMsg)
        }
    }

    protected def calcMessages(implicit requestHeader: RequestHeader) = {
        messagesApi.preferred(requestHeader)
    }

    protected def render(view: String, params: Object*)(implicit requestHeader: RequestHeader): Html = {
        val clazzName = "views.html." + view
        val clazz = Class.forName(clazzName)
        for (method <- clazz.getMethods) {
            if (method.getName.equals("render")) {
                val messages = calcMessages
                return method.invoke(null, (params :+ messages): _*).asInstanceOf[Html]
            }
        }
        return null
    }
}
