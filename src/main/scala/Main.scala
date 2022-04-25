import scrapper.FilmwebScrapper
import wvlet.log.LogSupport
import view.Index

object Main extends cask.MainRoutes with LogSupport {
  info(s"Application running on port: ${this.port}")

  @cask.staticResources("/assets")
  def staticResourceRoutes() = "assets"

  @cask.get("/")
  def homePage() = {
    Index.doc
  }

  @cask.post("/filmwebScrapper")
  def doScrapping(request: cask.Request) = {
    FilmwebScrapper.doScrapping(request)
  }

  initialize()
}
