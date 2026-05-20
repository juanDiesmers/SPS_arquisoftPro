package com.sps.sns.dto;

public class PurchaseWebhookRequest {

    private Long compraId;
    private String resultado;

    public PurchaseWebhookRequest() {
    }

    public PurchaseWebhookRequest(Long compraId, String resultado) {
        this.compraId = compraId;
        this.resultado = resultado;
    }

    public Long getCompraId() {
        return compraId;
    }

    public void setCompraId(Long compraId) {
        this.compraId = compraId;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
}
