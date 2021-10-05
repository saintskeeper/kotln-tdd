package com.ubertob.unlearnoop.exercises.chapter2.e07_invokable


data class EmailAddress(val raw: String)

data class Person(
    val firstName: String,
    val familyName: String,
    val email: EmailAddress
)


data class EmailSender(
    val msgTemplate: (Person) -> String
) {
    fun sendTo(aPerson: Person) {
        val msgText = msgTemplate(aPerson)
        send(aPerson.email, msgText)
    }

    private fun send(recipient: EmailAddress, text: String) {
        // connect to a mail server
        println("@$recipient\n$text")
    }
}

class EmailTemplate(fileName: String) : (Person) -> String {
    override fun invoke(aPerson: Person): String = TODO()
}