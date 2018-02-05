package controllers;

import models.Follower;
import play.libs.oauth.OAuth;
import play.libs.oauth.OAuth.ConsumerKey;
import play.libs.oauth.OAuth.OAuthCalculator;
import play.libs.oauth.OAuth.RequestToken;
import play.libs.oauth.OAuth.ServiceInfo;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;

import com.google.common.base.Strings;
import views.html.followers.listfollowers;
import views.html.welcome;
import views.html.followers.*;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class TwitterApiController extends Controller {

    public Result index() {
        return ok(views.html.index.render());
    }
    public Result welcome() {
        return ok(welcome.render("Welcome to Warehouse"));
    }
    static final ConsumerKey KEY = new ConsumerKey("dge7y4Q5AR8ufc9rxE2EsVm3v", "CHWXs1wRiN8wrOPrm8UIJW0Aa7UwcfESV3B4DuHWaikWBA6eki");

    private static final ServiceInfo SERVICE_INFO =
            new ServiceInfo("https://api.twitter.com/oauth/request_token",
                    "https://api.twitter.com/oauth/access_token",
                    "https://api.twitter.com/oauth/authorize",
                    KEY);

    private static final OAuth TWITTER = new OAuth(SERVICE_INFO);

    private final WSClient ws;

    @Inject
    public TwitterApiController(WSClient ws) {
        this.ws = ws;
    }


    public CompletionStage<Result> getLanguages() {
        Optional<RequestToken> sessionTokenPair = getSessionTokenPair();
        if (sessionTokenPair.isPresent()) {
            return ws.url("https://api.twitter.com/1.1/help/languages.json")
                    .sign(new OAuthCalculator(TwitterApiController.KEY, sessionTokenPair.get()))
                    .get()
                    .thenApply(result -> ok(result.asJson()));
        }
        return CompletableFuture.completedFuture(redirect(routes.TwitterApiController.auth()));
    }

    public CompletionStage<Result> getFollowers() {
        Optional<RequestToken> sessionTokenPair = getSessionTokenPair();
        if (sessionTokenPair.isPresent()) {
            return ws.url("    https://api.twitter.com/1.1/followers/list.json\n")
                    .sign(new OAuthCalculator(TwitterApiController.KEY, sessionTokenPair.get()))
                    .get()
                    .thenApply(result -> ok(result.asJson()));
        }
        return CompletableFuture.completedFuture(redirect(routes.TwitterApiController.auth()));
    }

    public Result auth() {
        String verifier = request().getQueryString("oauth_verifier");
        if (Strings.isNullOrEmpty(verifier)) {
            String url = routes.TwitterApiController.auth().absoluteURL(request());
            RequestToken requestToken = TWITTER.retrieveRequestToken(url);
            saveSessionTokenPair(requestToken);
            return redirect(TWITTER.redirectUrl(requestToken.token));
        } else {
            RequestToken requestToken = getSessionTokenPair().get();
            RequestToken accessToken = TWITTER.retrieveAccessToken(requestToken, verifier);
            saveSessionTokenPair(accessToken);
            return redirect(routes.TwitterApiController.welcome());
        }
    }

    private void saveSessionTokenPair(RequestToken requestToken) {
        session("token", requestToken.token);
        session("secret", requestToken.secret);
    }

    private Optional<RequestToken> getSessionTokenPair() {
        if (session().containsKey("token")) {
            return Optional.ofNullable(new RequestToken(session("token"), session("secret")));
        }
        return Optional.empty();
    }

    public Result getFollowersFromDb(){
        List<Follower> followers = Follower.find.all();
        return ok(listfollowers.render(followers));
    }

}
