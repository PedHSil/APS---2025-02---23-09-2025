package Model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Cliente {
    private int id;
    private String nome;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // campos "achatados" de Dados (para UI/compat)
    private String cpfCnpj;
    private String email;
    private String telefone;

    // relação 1:1 (Dados)
    private Dados dados;

    //Compat: ainda mantemos um "endereco principal" para a UI atual
    private Endereco endereco;

    //Novo: relação 1:N (vários endereços)
    private List<Endereco> enderecos = new ArrayList<>();

    public Cliente() {}

    public Cliente(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // --- básicos ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // --- achatados (Dados) ---
    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    // --- Dados (1:1) com sincronização nos achatados ---
    public Dados getDados() {
        if (dados == null) {
            Dados d = new Dados();
            d.setCpfCnpj(this.cpfCnpj);
            d.setEmail(this.email);
            d.setTelefone(this.telefone);
            this.dados = d;
        }
        return dados;
    }

    public void setDados(Dados dados) {
        this.dados = dados;
        if (dados != null) {
            this.cpfCnpj = dados.getCpfCnpj();
            this.email = dados.getEmail();
            this.telefone = dados.getTelefone();
        }
    }

    // --- Endereços (1:N) ---
    public List<Endereco> getEnderecos() {
        if (enderecos == null) enderecos = new ArrayList<>();
        return enderecos;
    }

    public void setEnderecos(List<Endereco> enderecos) {
        this.enderecos = (enderecos == null) ? new ArrayList<>() : enderecos;
        // mantém compat com UI: reflete o primeiro como "principal"
        if (this.enderecos.isEmpty()) {
            this.endereco = null;
        } else {
            this.endereco = this.enderecos.get(0);
        }
    }

    /** Conveniência: adiciona ao final da lista e, se for o primeiro, vira principal. */
    public void addEndereco(Endereco e) {
        if (e == null) return;
        getEnderecos().add(e);
        if (this.endereco == null) this.endereco = e;
    }

    /** Conveniência: define/atualiza o endereço principal (índice 0). */
    public void setEnderecoPrincipal(Endereco e) {
        if (e == null) {
            this.endereco = null;
            if (enderecos != null && !enderecos.isEmpty()) enderecos.set(0, null);
            return;
        }
        if (getEnderecos().isEmpty()) {
            enderecos.add(e);
        } else {
            enderecos.set(0, e);
        }
        this.endereco = e;
    }

    // --- Compat com UI antiga (usa um único endereço) ---
    /** Retorna o endereço "principal" (primeiro da lista). */
    public Endereco getEndereco() {
        if (endereco != null) return endereco;
        if (enderecos != null && !enderecos.isEmpty()) return enderecos.get(0);
        return new Endereco(); // não adiciona, só evita NullPointer em telas
    }

    /** Define o endereço "principal" e sincroniza com a lista. */
    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
        if (endereco == null) {
            // zera também a posição 0, se existir
            if (enderecos != null && !enderecos.isEmpty()) {
                enderecos.set(0, null);
            }
            return;
        }
        if (enderecos == null) enderecos = new ArrayList<>();
        if (enderecos.isEmpty()) {
            enderecos.add(endereco);
        } else {
            enderecos.set(0, endereco);
        }
    }

    @Override
    public String toString() {
        return id + " - " + (nome != null ? nome : "");
    }
}
