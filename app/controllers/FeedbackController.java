package controllers;

import configuration.inject.ConfigurationValue;
import models.form.FeedbackModel;
import org.apache.commons.validator.routines.EmailValidator;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import services.email.EmailServiceClient;
import services.email.model.AddressType;
import services.email.model.Email;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

public class FeedbackController extends Controller {

    private final String feedbackSender;
    private final String feedbackRecipient;

    @Inject
    private FormFactory formFactory;
    @Inject
    private EmailServiceClient emailServiceClient;

    @Inject
    public FeedbackController(@ConfigurationValue(key = "email.feedback.sender") String feedbackSender,
                              @ConfigurationValue(key = "email.feedback.recipient") String feedbackRecipient) {
        checkArgument(!isNullOrEmpty(feedbackSender), "Feedback sender is required");
        checkArgument(!isNullOrEmpty(feedbackRecipient), "Feedback recipient is required");

        EmailValidator emailValidator = EmailValidator.getInstance(true);
        checkArgument(emailValidator.isValid(feedbackSender), "Feedback sender is invalid");
        checkArgument(emailValidator.isValid(feedbackRecipient), "Feedback recipient is invalid");

        this.feedbackSender = feedbackSender;
        this.feedbackRecipient = feedbackRecipient;
    }

    // GET
    public Result displayFeedbackForm() {
        Logger.debug("Displaying feedback form");
        return ok(views.html.pages.support.feedback.render(formFactory.form(FeedbackModel.class)));
    }

    // POST
    public Result submitFeedback() {
        Form<FeedbackModel> form = formFactory.form(FeedbackModel.class).bindFromRequest();

        if (form.hasErrors()) {
            Logger.debug("Displaying feedback form with validation errors");
            return (badRequest((Html) views.html.pages.support.feedback.render(form)));
        }

        try {
            emailServiceClient.sendEmail(new Email.Builder()
                    .fromSender(feedbackSender)
                    .toRecipient(AddressType.TO, feedbackRecipient)
                    .withSubject(Messages.get("email.feedback.subject"))
                    .withTextBody(feedbackTextBody(form.get()))
                    .create()
            );

            Logger.debug("Redirecting to feedback submitted page");
            return redirect(routes.FeedbackController.displayFeedbackSubmittedPage());
        } catch (Exception ex) {
            Logger.error("Cannot send feedback email", ex);
            return redirect(routes.FeedbackController.displayFeedbackNotSubmittedPage());
        }
    }

    private String feedbackTextBody(FeedbackModel model) {
        return views.txt.email.feedback.render(model).body();
    }

    public Result displayFeedbackSubmittedPage() {
        Logger.debug("Displaying feedback submitted page");
        return ok(views.html.pages.support.feedbackSubmitted.render());
    }

    public Result displayFeedbackNotSubmittedPage() {
        Logger.debug("Displaying feedback not submitted page");
        return ok(views.html.pages.support.feedbackNotSubmitted.render());
    }

}
