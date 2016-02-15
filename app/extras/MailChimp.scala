package extras

import play.api.{Play, Logger}
import play.api.i18n.{Messages, I18nSupport, MessagesApi}
import com.ecwid.mailchimp.{MailChimpException, MailChimpClient}
import com.ecwid.mailchimp.method.list.{ListSubscribeMethod, ListsMethodFilters, ListsMethod}
import scala.collection.JavaConversions._

/**
  * Created by diogo
  */
object MailChimp {

  val successMessage = Messages("mailchimp.subscribed")
  val errorMessage = Messages("mailchimp.failed")

  val apiKey = configString("mailchimp.apikey")
  val listName = configString("mailchimp.listname")

  private lazy val mailChimpClient = new MailChimpClient()

  def subscribe(email: String): Either[String, String] = {
    try {
      listInfo match {
        case Some(info) => {
          MailChimp.subscribeForList(info.id, email)
          Right(successMessage)
        }
        case _ => {
          Left(errorMessage)
        }
      }
    } catch {
      case ex: MailChimpException => {
        Logger.error(errorMessage, ex)
        ex.code match {
          case 502 => Left("Valid email required")
          case _ => Left(errorMessage)
        }
      }
    }
  }

  private lazy val listInfo = {
    val listsMethod = new ListsMethod()
    listsMethod.apikey = apiKey
    listsMethod.filters = new ListsMethodFilters()
    listsMethod.filters.list_name = listName
    listsMethod.filters.exact = true
    listsMethod.start = 0
    listsMethod.limit = 1
    try {
      mailChimpClient.execute(listsMethod).data.headOption match {
        case None => {
          Logger.error("Cannot find list named '" + listName + "'")
          None
        }
        case whatever => whatever
      }
    } catch {
      case e: Throwable => {
        Logger.error("Cannot load info for List '" + listInfo + "'")
        None
      }
    }
  }

  private def subscribeForList(listId: String, email: String): Unit = {
    val listSubscribeMethod = new ListSubscribeMethod()
    listSubscribeMethod.apikey = apiKey
    listSubscribeMethod.id = listId
    listSubscribeMethod.email_address = email
    listSubscribeMethod.double_optin = false
    listSubscribeMethod.update_existing = true
    mailChimpClient.execute(listSubscribeMethod)
  }

  private def configString(key: String): String = {
    Play.current.configuration.getString(key).getOrElse("-UNKNOWN-")
  }


}