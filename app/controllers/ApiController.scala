package controllers;

import scala.concurrent.Future

import org.apache.commons.io.FileUtils

import com.github.ddth.commons.utils.HashUtils
import com.google.inject.Provider

import javax.inject.Inject
import modules.registry.IRegistry
import play.api.i18n.MessagesApi
import play.api.libs.json.JsArray
import play.api.libs.json.JsError
import play.api.libs.json.JsNull
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc._
import play.data.FormFactory
import utils.AppConstants

import collection.JavaConverters._
import bo.Rankings
import utils.AppUtils
import play.api.libs.json.JsObject
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsNumber
import play.Logger
import utils.AppGlobals

/**
 * Base controller to handle REST API requests.
 *
 * @author ThanhNB
 * @since 0.1.0
 */
class ApiController @Inject() (
        registry: Provider[IRegistry],
        formFactory: FormFactory,
        messagesApi: MessagesApi) extends BaseController(registry, formFactory, messagesApi) {

    protected def parseRequestContent(implicit request: Request[AnyContent]): JsValue = {
        val requestBody = request.body
        val textBody = requestBody.asText
        val jsonBody = requestBody.asJson
        val rawBody = requestBody.asRaw
        try {
            jsonBody.map { json => json }.getOrElse(
                textBody.map { text => Json.parse(text) }.getOrElse(
                    rawBody.map { x => Json.parse(FileUtils.readFileToString(x.asFile, AppConstants.UTF8)) }.getOrElse(JsNull)))
        } catch {
            case _: Throwable => JsNull
        }
    }

    protected val HEADER_REQUEST_TOKEN = "X-Request-Token";
    protected val HEADER_APP_ID = "X-App-Id";

    protected object PermissionCheckAction extends ActionBuilder[Request] {
        def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
            var validApiCall = AppUtils.isApiAuthDisabled
            if (!validApiCall) {
                val reqAppId = request.headers.get(HEADER_APP_ID).getOrElse(null)
                val reqToken = request.headers.get(HEADER_REQUEST_TOKEN).getOrElse(null)
                val timestamp = if (reqToken != null) reqToken.split("\\|", 2)(0) else "0"

                val app = registry.get.getPlayApplication
                val appConf = app.configuration
                val authAppId = appConf.getString("auth.app_id")
                val authApiKey = appConf.getString("auth.api_key")
                val calcToken = timestamp + "|" + HashUtils.md5(authApiKey + "|" + timestamp)

                validApiCall = reqAppId == (authAppId) &&
                    (if (app.isDev()) reqToken == (authApiKey) else reqToken == (calcToken))
            }

            if (validApiCall) {
                block(request)
            } else {
                Future.successful(doResponseJson(AppConstants.RESPONSE_ACCESS_DENIED, "Request validation failed (Invalid AppId or malformed RequestToken)"))
            }
        }
    }

    def dummy = PermissionCheckAction { implicit request: Request[AnyContent] =>
        doResponseJson(AppConstants.RESPONSE_OK, "Ok")
    }

    private def convertBigDecimal(bd: scala.math.BigDecimal): Object = {
        if (bd.isValidByte) bd.byteValue.asInstanceOf[Object]
        else if (bd.isValidShort) bd.shortValue.asInstanceOf[Object]
        else if (bd.isValidInt) bd.intValue.asInstanceOf[Object]
        else if (bd.isValidLong) bd.longValue.asInstanceOf[Object]
        else bd.doubleValue.asInstanceOf[Object]
    }

    private def toJavaMap(jsObj: JsObject): java.util.Map[String, Object] = {
        val result = new java.util.HashMap[String, Object]
        jsObj.fields foreach {
            case (key, value) => {
                value match {
                    case b: JsBoolean => result.put(key, b.value.asInstanceOf[Object])
                    case n: JsNumber  => result.put(key, convertBigDecimal(n.value).asInstanceOf[Object])
                    case s: JsString  => result.put(key, s.value.asInstanceOf[Object])
                    case _            => {}
                }
            }
        }
        return result
    }

    /**
     * Updates rankings data.
     *
     * <p>Inputs:</p>
     * <ul>
     * <li>{@code name}     : (String) name of the rankings</li>
     * <li>{@code timestamp}: (Int) timestamp/version of the ranking data</li>
     * <li>{@code rankings} : (Array) rankings data as an array of objects, sorted from the highest ranking to the lowest:
     *   <ul>
     *     <li>{@code id/key/k}: (String) the item's unique name/id</li>
     *     <li>{@code value/v} : (double) the item's value</li>
     *     <li>{@code info/i}  : (String) the item's metadata/extra info</li>
     *   </ul>
     * </li>
     * </ul>
     *
     * <p>Outputs: (int) number of items updated.</p>
     */
    def updateRankings = PermissionCheckAction { implicit request: Request[AnyContent] =>
        val requestJson: JsValue = parseRequestContent(request)
        (requestJson \ "name").validate[String] match {
            case e: JsError => doResponseJson(AppConstants.RESPONSE_CLIENT_ERROR, "Missing or invalid parameter [name]!")
            case n: JsSuccess[String] => {
                (requestJson \ "timestamp").validate[Int] match {
                    case e: JsError => doResponseJson(AppConstants.RESPONSE_CLIENT_ERROR, "Missing or invalid parameter [timestamp]!")
                    case t: JsSuccess[Int] => {
                        (requestJson \ "rankings").validate[JsArray] match {
                            case e: JsError => doResponseJson(AppConstants.RESPONSE_CLIENT_ERROR, "Missing or invalid parameter [rankings]!")
                            case r: JsSuccess[JsArray] => {
                                val items = r.get.value.map { x =>
                                    x.validate[JsObject] match {
                                        case e: JsError             => null
                                        case o: JsSuccess[JsObject] => o.get
                                    }
                                }.filter(_ != null).map { x => toJavaMap(x) }.asJava
                                val rankings = registry.get.getFrontendApi.updateRankings(n.get, t.get, items)
                                doResponseJson(AppConstants.RESPONSE_OK, "Ok", rankings.getItems.length.asInstanceOf[Object])
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets rankings data.
     *
     * <p>Inputs:</p>
     * <ul>
     * <li>{@code name}     : (String) name of the rankings</li>
     * <li>{@code timestamp}: (Int) timestamp</li>
     * </ul>
     *
     * <p>Outputs: (Array) rankings data as an array of object, sorted from the highest ranking to the lowest:</p>
     * <ul>
     *   <li>{@code id}   : (String) the item's unique name/id</li>
     *   <li>{@code value}: (double) the item's value</li>
     *   <li>{@code info} : (String) the item's metadata/extra info</li>
     * </ul>
     */
    def getRankings = PermissionCheckAction { implicit request: Request[AnyContent] =>
        val requestJson: JsValue = parseRequestContent(request)
        (requestJson \ "name").validate[String] match {
            case e: JsError => doResponseJson(AppConstants.RESPONSE_CLIENT_ERROR, "Missing or invalid parameter [name]!")
            case n: JsSuccess[String] => {
                (requestJson \ "timestamp").validate[Int] match {
                    case e: JsError => doResponseJson(AppConstants.RESPONSE_CLIENT_ERROR, "Missing or invalid parameter [timestamp]!")
                    case t: JsSuccess[Int] => {
                        val rankings = registry.get.getFrontendApi.getRankings(n.get, t.get)
                        val items = rankings.getItems.map { x => Map("id" -> x.getKey, "value" -> x.getValueAsDouble, "position" -> x.getPosition).asJava }
                        doResponseJson(AppConstants.RESPONSE_OK, "Ok", items.asInstanceOf[Object])
                    }
                }
            }
        }
    }
    
    /**
     * Gets ranking history data.
     *
     * <p>Inputs:</p>
     * <ul>
     *   <li>{@code name} : (String) name of the rankings</li>
     *   <li>{@code id}   : (String) id of the target to fetch history</li>
     *   <li>{@code start}: (Int) starting timestamp (inclusive)</li>
     *   <li>{@code end}  : (Int) ending timestamp (exclusive)</li>
     * </ul>
     *
     * <p>Outputs: (Array) ranking history data as an array of object, sorted by timestamp:</p>
     * <ul>
     *   <li>{@code timestamp}: (int)</li>
     *   <li>{@code position} : (int)</li>
     *   <li>{@code value}    : (double)</li>
     * </ul>
     */
    def getHistory = PermissionCheckAction { implicit request: Request[AnyContent] =>
        val requestJson: JsValue = parseRequestContent(request)
        (requestJson \ "name").validate[String] match {
            case e: JsError => doResponseJson(AppConstants.RESPONSE_CLIENT_ERROR, "Missing or invalid parameter [name]!")
            case n: JsSuccess[String] => {
                (requestJson \ "id").validate[String] match {
                    case e: JsError => doResponseJson(AppConstants.RESPONSE_CLIENT_ERROR, "Missing or invalid parameter [id]!")
                    case id: JsSuccess[String] => {
                        val start = (requestJson \ "start").asOpt[Int].getOrElse(Integer.MIN_VALUE)
                        val end = (requestJson \ "end").asOpt[Int].getOrElse(Integer.MAX_VALUE)
                        val history = registry.get.getFrontendApi.getHistory(n.get, id.get, start, end)
                        val items = history.getItems.map { x => Map("timestamp" -> x.getTimestamp, "value" -> x.getValueAsDouble, "position" -> x.getPosition).asJava }
                        doResponseJson(AppConstants.RESPONSE_OK, "Ok", items.asInstanceOf[Object])
                    }
                }
            }
        }
    }
}