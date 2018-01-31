package models.form;

import java.util.Objects;

import com.amazonaws.util.StringUtils;
import com.google.common.base.MoreObjects;
import javax.validation.constraints.NotNull;
import java.util.*;
import play.data.validation.ValidationError;
import java.lang.Object;


public class ConfirmationUntaxedModel {
    @NotNull(message = "error.required.whatNextDecision")
    private String whatNextDecision;

    public ConfirmationUntaxedModel() {}

    public ConfirmationUntaxedModel(String whatNextDecision) {
        this.whatNextDecision = whatNextDecision;
    }

    public String getWhatNextDecision() {
        return whatNextDecision;
    }

    public void setWhatNextDecision(String whatNextDecision) {
        this.whatNextDecision = whatNextDecision;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("whatNextDecision", whatNextDecision)
                .toString();
    }

}
