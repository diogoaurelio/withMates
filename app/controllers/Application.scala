package controllers

import play.api.Play._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Messages, I18nSupport, MessagesApi}
import play.api.mvc._
import javax.inject._
import play.api.libs.mailer._


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
        sendEmail("Awesome news boss,", ContactData.email) match {
          case true => Ok(views.html.messageSuccess(ContactData))
          case _ => Redirect(routes.Application.index()).flashing("error" -> Messages("form.email_error"))
        }
      }
    )
  }

  // http://stackoverflow.com/questions/30208339/play-framework-2-3-x-unable-to-send-emails-using-plugin-play-mailer
  def sendEmail(intro: String, clientEmail: String): Boolean = {
    val from =  current.configuration.getString("play.mailer.from")
    val recepients = current.configuration.getString("play.mailer.recipients")
    val to = current.configuration.getString("play.mailer.to")
    val email = Email(
      "Good news " + Messages("global.companyname") + " Boss",
      to.getOrElse("default value"),
      Seq(from.getOrElse("default value")),
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