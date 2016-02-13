package controllers

import play.api.Play._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import javax.inject._
import play.api.libs.mailer._


class Application @Inject() (val messagesApi: MessagesApi, mailerClient: MailerClient) extends Controller with I18nSupport {

  val contactForm = Form(
    mapping(
      "name" -> text,
      "email" -> nonEmptyText(minLength = 6)
    )(ContactData.apply)(ContactData.unapply)
  )

  def index = Action { implicit request =>
    Ok(views.html.index(contactForm))
  }

  def formRepeat = Action { implicit request =>
    Ok(views.html.formRepeat(contactForm))
  }

  def addContact = Action { implicit request =>
    contactForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.formRepeat(formWithErrors)),
      ContactData => {
        sendEmail("Awesome news boss,", ContactData.name, ContactData.email)
        Ok(views.html.messageSuccess(ContactData))
      }
    )
  }

  // http://stackoverflow.com/questions/30208339/play-framework-2-3-x-unable-to-send-emails-using-plugin-play-mailer
  def sendEmail(intro: String, clientName: String, clientEmail: String) {
    val from =  current.configuration.getString("play.mailer.from")
    val recepients = current.configuration.getString("play.mailer.recipients")
    val to = current.configuration.getString("play.mailer.to")
    val email = Email(
      "Good news boss.. - withMates",
      to.getOrElse("default value"),
      Seq(from.getOrElse("default value")),
      bodyText = Some(intro + " We have a new client: " + clientName + "Email he left:" + clientEmail),
      bodyHtml = Some("<html><body><p><b>" + intro + " </b></p>" +
        "<p> We have a new client: " + clientName + "</p>" +
        "<p> Email he left: " + clientEmail + "</p>" +
        "</body></html>")
    )
    mailerClient.send(email)
  }


}
case class ContactData(name: String, email: String)