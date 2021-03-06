package io.starteos.jeos.net.response;

import java.util.List;

public class RequiredKeysResponse extends BaseResponse {
    private List<String> available_keys;

    public RequiredKeysResponse(List<String> available_keys) {
        this.available_keys = available_keys;
    }

    public List<String> getAvailable_keys() {
        return available_keys;
    }

    public void setAvailable_keys(List<String> available_keys) {
        this.available_keys = available_keys;
    }
}
