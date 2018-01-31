package models.form;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

public class FeedbackModel {

    private String name;

    @Email(message = "error.invalid.feedback.email")
    private String email;

    @NotBlank(message = "error.required.feedback.content")
    @Length(max = 500, message = "error.invalid.feedback.content")
    private String message;

    public String getName() {
        return name;
    }

    public FeedbackModel setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public FeedbackModel setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public FeedbackModel setMessage(String message) {
        this.message = message;
        return this;
    }
}
