package controllers

import play.api.mvc._
import scrapper.FilmwebScrapper

import javax.inject._

@Singleton
class FilmwebScrapperController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def scrapFilmweb() = Action { implicit request: Request[AnyContent] =>
    Ok(FilmwebScrapper.doScrapping())
  }
}
