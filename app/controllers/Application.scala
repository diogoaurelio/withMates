package controllers

import play.api.Play._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Messages, I18nSupport, MessagesApi}
import play.api.mvc._
import javax.inject._
import play.api.libs.mailer._
import extras.MailChimp
import play.api.libs.json.{JsValue, Json}
import play.twirl.api.HtmlFormat

class Application @Inject() (val messagesApi: MessagesApi, mailerClient: MailerClient) extends Controller with I18nSupport {

  val contactForm = Form(
    mapping(
      "email" -> email
    )(ContactData.apply)(ContactData.unapply)
  )

  def index = Action { implicit request =>
    println("Languages: " + request.acceptLanguages.map(_.code).mkString(", "))
    Ok(views.html.index(contactForm))
  }

  def formRepeat = Action { implicit request =>
    Ok(views.html.formRepeat(contactForm))
  }

  def addContact = Action { implicit request =>
    contactForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.formRepeat(formWithErrors)),
      ContactData => {
        // TODO: refactor for co-routines/async
        sendEmail("Awesome news boss,", ContactData.email) match {
          case true => subscribeMailchimp(ContactData.email) //Ok(views.html.messageSuccess(ContactData))
          case _ => Redirect(routes.Application.index()).flashing("error" -> Messages("form.email_error"))
        }

      }
    )
  }
  private def subscribeMailchimp(email:String) = {

    def errorAsJson(errors: JsValue) = Json.obj("errors" -> errors)
    def escape(s: String) = HtmlFormat.escape(s).toString()

    MailChimp.subscribe(email) match {
      case Right(msg) => Ok(Json.obj("success" -> escape(msg)))
      case Left(msg) => InternalServerError(errorAsJson(Json.obj("email" -> Json.arr(escape(msg)))))
    }
  }

  private def sendEmail(intro: String, clientEmail: String): Boolean = {
    val to = current.configuration.getString("play.mailer.recipients")
    val from = current.configuration.getString("play.mailer.from")
    val subject: String = "Good news " + Messages("global.companyname") + " Boss"
    val email = Email(
      subject,
      from.getOrElse("default value"),
      Seq(to.getOrElse("default value")),
      bodyText = Some(intro + " We have a new potential client: " + clientEmail),
      bodyHtml = Some("<html><body><p><b>" + intro + " </b></p>" +
        "<p> We have a new client! </p>" +
        "<p> Email s/he left: " + clientEmail + "</p>" +
        "<p> From your withMates Bot with byte love." +
        "</body></html>")
    )
    try {
      mailerClient.send(email)
      true
    } catch {
      case e: Exception => println(e); false
    }

  }

}
case class ContactData(email: String)