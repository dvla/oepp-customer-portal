package logging;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.MDC;
import play.mvc.Controller;
import play.mvc.Http;
import session.SessionManager;
import uk.gov.dvla.domain.Offence;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class MDCInterceptor implements MethodInterceptor {

    @Inject
    private SessionManager sessionManager;

    private ContextHelper contextHelper;

    public MDCInterceptor() {
        this.contextHelper = new ContextHelper();
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        contextHelper.initiate(Controller.request(), sessionManager.existingSession());
        Object result = invocation.proceed();
        contextHelper.clear();
        return result;
    }

    class ContextHelper {

        void initiate(Http.Request request, Optional<SessionManager.Session> session) {
            Object caseNumber = null;
            Object vehicleRegistrationMark = null;

            Optional<Offence> offence = session.flatMap(SessionManager.Session::offence);
            if (offence.isPresent()) {
                Offence.Criteria criteria = offence.get().getCriteria();

                caseNumber = criteria.getCaseNumber();
                vehicleRegistrationMark = criteria.getVehicleRegistrationMark();
            } else {
                Map<String, String[]> formData = request.body().asFormUrlEncoded();

                if (formData != null) {
                    caseNumber = formData.getOrDefault("caseNumber", new String[]{null})[0];
                    vehicleRegistrationMark = formData.getOrDefault("vehicleRegistrationMark", new String[]{null})[0];
                }
            }

            MDC.put("PRN", valueOrDefault(caseNumber));
            MDC.put("VRM", valueOrDefault(vehicleRegistrationMark));
        }

        private String valueOrDefault(Object value) {
            if (value == null) {
                return "-";
            }

            String stringValue = value.toString();
            if (isBlank(stringValue)) {
                return "blank";
            }
            return stringValue;
        }

        void clear() {
            MDC.clear();
        }
    }

}
