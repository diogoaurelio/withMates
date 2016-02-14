package forms

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by diogo
  * Convenience Object for initial phase -> be notified for when we launch
  */
object notifyMeForm {

  val contactForm = Form(
    mapping(
      "name" -> text,
      "email" -> email
    )(ContactData.apply)(ContactData.unapply)
  )
}
case class ContactData(name: String, email: String)
