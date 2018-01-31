package views;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class VRMFormatTest {

  @Test
  public void vrmFormat_shouldFormatVRMAccordingToRules() throws Exception {
      assertThat(VRM.format("AA88AAA"), is("AA88 AAA"));
      assertThat(VRM.format("A9"),      is("A 9     "));
      assertThat(VRM.format("A99"),     is("A 99    "));
      assertThat(VRM.format("A999"),    is("A 999   "));
      assertThat(VRM.format("A9999"),   is("A 9999  "));
      assertThat(VRM.format("AA9"),     is("AA 9    "));
      assertThat(VRM.format("AA99"),    is("AA 99   "));
      assertThat(VRM.format("AA999"),   is("AA 999  "));
      assertThat(VRM.format("AA9999"),  is("AA 9999 "));
      assertThat(VRM.format("AAA9"),    is("AAA 9   "));
      assertThat(VRM.format("AAA99"),   is("AAA 99  "));
      assertThat(VRM.format("AAA999"),  is("AAA 999 "));
      assertThat(VRM.format("AAA9999"), is("AAA 9999"));
      assertThat(VRM.format("AAA9Y"),   is("AAA 9Y  "));
      assertThat(VRM.format("AAA99Y"),  is("AAA 99Y "));
      assertThat(VRM.format("AAA999Y"), is("AAA 999Y"));
      assertThat(VRM.format("9A"),      is("9 A     "));
      assertThat(VRM.format("9AA"),     is("9 AA    "));
      assertThat(VRM.format("9AAA"),    is("9 AAA   "));
      assertThat(VRM.format("99A"),     is("99 A    "));
      assertThat(VRM.format("99AA"),    is("99 AA   "));
      assertThat(VRM.format("99AAA"),   is("99 AAA  "));
      assertThat(VRM.format("999A"),    is("999 A   "));
      assertThat(VRM.format("999AA"),   is("999 AA  "));
      assertThat(VRM.format("999AAA"),  is("999 AAA "));
      assertThat(VRM.format("9999A "),  is("9999 A  "));
      assertThat(VRM.format("9999AA"),  is("9999 AA "));
      assertThat(VRM.format("Y9AAA"),   is("Y9 AAA  "));
      assertThat(VRM.format("Y99AAA"),  is("Y99 AAA "));
      assertThat(VRM.format("Y999AAA"), is("Y999 AAA"));
  }
}
