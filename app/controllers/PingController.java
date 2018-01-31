package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class PingController extends Controller {

    public Result ping() {
        return ok("pong");
    }

}
