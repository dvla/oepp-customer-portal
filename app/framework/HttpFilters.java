package framework;

import framework.filters.AccessLogFilter;
import framework.filters.HttpsSecurityHeadersFilter;
import play.mvc.EssentialFilter;
import play.filters.csrf.CSRFFilter;
import play.filters.headers.SecurityHeadersFilter;

import javax.inject.Inject;

public class HttpFilters implements play.http.HttpFilters {

    @Inject
    private CSRFFilter crossSiteRequestForgeryFilter;
    @Inject
    private SecurityHeadersFilter securityHeadersFilter;
    @Inject
    private HttpsSecurityHeadersFilter httpsSecurityHeadersFilter;
    @Inject
    private AccessLogFilter accessLogFilter;

    @Override
    public EssentialFilter[] filters() {
        return new EssentialFilter[]{
                crossSiteRequestForgeryFilter.asJava(),
                securityHeadersFilter.asJava(),
                httpsSecurityHeadersFilter,
                accessLogFilter
        };
    }
}