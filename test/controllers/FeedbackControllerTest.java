package controllers;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import services.email.EmailServiceClient;
import services.email.model.AddressType;
import services.email.model.Email;

import java.io.IOException;

import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class FeedbackControllerTest extends BaseControllerTest {

    private EmailServiceClient emailServiceClient;

    public FeedbackControllerTest() {
        emailServiceClient = mock(EmailServiceClient.class);

        builder = builder
                .configure(ImmutableMap.<String, Object>builder()
                        .put("email.feedback.sender", "noreply@dvla.gov.uk")
                        .put("email.feedback.recipient", "feedback@dvla.gov.uk")
                        .build()
                ).overrides(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(EmailServiceClient.class).toInstance(emailServiceClient);
                    }
                });
    }

    @Test
    public void feedbackFormShouldExist() {
        makeRequest(HTTP.get("/support/feedback"), (result) -> assertThat(result.status(), is(200)));
    }

    @Test
    public void sendFeedback_shouldReturn400AndDisplayFeedbackFormWhenValidationErrorsAreFound() {
        Request request = HTTP.post("/support/feedback")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(400));
            assertThat(result.redirectLocation().isPresent(), is(false));
        });
    }

    @Test
    public void sendFeedback_shouldSendFeedbackEmailWithEnglishSubject() throws IOException {
        Email actualEmail = setupMocksAndSendEmail("en");
        assertCorrectEmailWasSent(actualEmail, "Feedback on Failure to Tax Vehicle Penalty Service");
    }

    @Test
    public void sendFeedback_shouldSendFeedbackEmailWithEnglishSubjectWhenLanguageIsWelsh() throws IOException {
        Email actualEmail = setupMocksAndSendEmail("cy");
        assertCorrectEmailWasSent(actualEmail, "Feedback on Failure to Tax Vehicle Penalty Service");
    }

    private Email setupMocksAndSendEmail(String language) throws IOException {
        Request request = HTTP.post("/support/feedback")
                .withLanguage(language)
                .withCSRFToken("secret")
                .withFormParameter("message", "Some non blank message")
                .withFormParameter("name", "Gerald H. Kendrick")
                .withFormParameter("email", "g.kendrick@examle.com");

        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        makeRequest(request, (result) -> verify(emailServiceClient).sendEmail(captor.capture()));

        return captor.getValue();
    }

    private void assertCorrectEmailWasSent(Email email, String expectedSubject) throws IOException {
        assertThat(email.getSender(), is("noreply@dvla.gov.uk"));
        assertThat(email.getRecipients().get(AddressType.TO), Matchers.hasItem("feedback@dvla.gov.uk"));
        assertThat(email.getSubject(), is(expectedSubject));
        assertThat(email.getTextBody().get(), equalToIgnoringWhiteSpace(exemplarFeedbackEmailBody()));
    }

    private String exemplarFeedbackEmailBody() throws IOException {
        return Resources.toString(getClass().getResource("/feedback-email.txt"), defaultCharset());
    }

    @Test
    public void sendFeedback_shouldReturn303AndRedirectToFeedbackSubmittedPageWhenSubmittedSucceeded() {
        Request request = HTTP.post("/support/feedback")
                .withFormParameter("message", "Some non blank message");

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.FeedbackController.displayFeedbackSubmittedPage().url()));
        });
    }

    @Test
    public void sendFeedback_shouldReturn303AndRedirectToFeedbackNotSubmittedPageWhenSubmitFailed() {
        when(emailServiceClient.sendEmail(any())).thenThrow(new RuntimeException());

        Request request = HTTP.post("/support/feedback")
                .withFormParameter("message", "Some non blank message");

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.FeedbackController.displayFeedbackNotSubmittedPage().url()));
        });
    }
}
