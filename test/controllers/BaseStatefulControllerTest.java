package controllers;

import com.google.inject.AbstractModule;
import org.junit.Before;
import session.SessionManager;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseStatefulControllerTest extends BaseControllerTest {

    protected static final String SESSION_ID = "194f2741-4161";

    protected final SessionManager.Session session;

    protected BaseStatefulControllerTest() {
        session = mock(SessionManager.Session.class);

        SessionManager sessionManager = mock(SessionManager.class);
        when(sessionManager.session()).thenReturn(session);
        when(sessionManager.existingSession()).thenReturn(Optional.of(session));

        this.builder = builder
                .overrides(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(SessionManager.class).toInstance(sessionManager);
                    }
                });
    }

    @Before
    public void init() {
        when(session.id()).thenReturn(SESSION_ID);
        when(session.offence()).thenReturn(Optional.empty());
    }

}
