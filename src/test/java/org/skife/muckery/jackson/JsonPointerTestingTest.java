package org.skife.muckery.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPointerTestingTest
{
    @Test
    public void testFoo() throws Exception
    {
        String json = "{" +
                      "  \"names\" : [ \"Henning\", \"Xinxin\" ]," +
                      "  \"stuff\" : {" +
                      "    \"glasses\" : {" +
                      "      \"color\" : \"black\"," +
                      "      \"style\" : \"vanhoe\"" +
                      "    }" +
                      "  }" +
                      "}";


        JsonNode root = new ObjectMapper().readTree(json);
        assertThat(root.at("/names/0").textValue()).isEqualTo("Henning");
        assertThat(root.at("/names/1").textValue()).isEqualTo("Xinxin");
        assertThat(root.at("/stuff").isObject()).isTrue();
        assertThat(root.at("/stuff/glasses/color").textValue()).isEqualTo("black");
    }
}
