package com.pedidofacil.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.store")
public class AppSettings {
    private String storeName = "Loja Exemplo Materiais";
    private String cnpj = "00.000.000/0000-00";
    private String address = "Rua Exemplo, 123 - Centro";
    private String phone = "(00) 0000-0000";

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
