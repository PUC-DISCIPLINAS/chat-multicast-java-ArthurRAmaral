package utils;

import enums.ResponseCode;

import java.io.Serializable;

public class ServerResponse implements Serializable {
    private ResponseCode code;
    private String mensagem;

    public ServerResponse(ResponseCode code) {
        this.code = code;
        this.mensagem = "";
    }

    public ServerResponse(String mensagem) {
        this.mensagem = mensagem;
        this.code = ResponseCode.KEEP_CONNECTION;
    }

    public ServerResponse(ResponseCode code, String mensagem) {
        this.code = code;
        this.mensagem = mensagem;
    }

    public ResponseCode getCode() {
        return code;
    }

    public void setCode(ResponseCode code) {
        this.code = code;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
