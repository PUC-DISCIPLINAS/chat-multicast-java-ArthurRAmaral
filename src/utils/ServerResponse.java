package utils;

import enums.ResponseCode;

import java.io.Serializable;

public class ServerResponse implements Serializable {
    private ResponseCode code = ResponseCode.KEEP_CONNECTION;
    private String mensagem = "";
    private Object body = null;

    public ServerResponse(ResponseCode code) {
        this.code = code;
    }

    public ServerResponse(String mensagem) {
        this.mensagem = mensagem;
    }

    public ServerResponse(ResponseCode code, Object body) {
        this.code = code;
        this.body = body;
    }

    public ServerResponse(ResponseCode code, String mensagem) {
        this.code = code;
        this.mensagem = mensagem;
    }

    public ServerResponse(ResponseCode code, String mensagem, Object body) {
        this.code = code;
        this.mensagem = mensagem;
        this.body = body;
    }

    public Object getBody() {
        return body;
    }

    public ResponseCode getCode() {
        return code;
    }

    public String getMensagem() {
        return mensagem;
    }
}
