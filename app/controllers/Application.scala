package controllers

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


}
case class ContactData(name: String, email: String)