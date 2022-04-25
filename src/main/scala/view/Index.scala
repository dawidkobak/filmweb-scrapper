package view

import scalatags.Text.all._

object Index {

  val bootstrap = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"

  val doc = {
    doctype("html")(
      html(lang := "pl")(
        head(
          link(rel := "stylesheet", `type` := "text/css", href := bootstrap),
          link(rel := "stylesheet", `type` := "text/css", href := "/assets/style.css"),
          script(src := "/assets/jquery-3.6.0.min.js"),
          script(src := "/assets/main.js"),
        ),
        body(
          div(id := "container")(
            form(id := "scrapForm")(
              p("Podaj frazy filmów lub seriali do scrapowania z ", a(href := "https://www.filmweb.pl", "Filmweb")),
              input(
                `type` := "text",
                placeholder := "Fraza",
                onfocus := "this.placeholder=''",
                onblur := "this.placeholder='Fraza'",
                name := "phrase"
              ),
              div(id := "anotherPhrases"),
              div(id := "withIcon")(
                img(
                  src := "assets/plus-icon.jpg",
                  id := "plusIcon"
                )
              ),
              input(
                `type` := "button",
                value := "Scrap",
                id := "submitButton"
              )
            ),
            div(id := "result")
          )
        )
      )
    )
  }
}
