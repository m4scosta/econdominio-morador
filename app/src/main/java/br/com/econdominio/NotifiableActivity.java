package br.com.econdominio;

import org.json.JSONObject;

public interface NotifiableActivity {

    boolean notify(JSONObject notification);
}
