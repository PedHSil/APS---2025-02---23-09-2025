package Model;

import java.sql.Timestamp;

public class Cliente {
    private int id;
    private String nome;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // campos achatados (mantidos)
    private String cpfCnpj;
    private String email;
    private String telefone;

    // novos objetos (mantidos para modelagem 1:1)
    private Dados dados;
    private Endereco endereco;

    public Cliente() {}

    public Cliente(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // Getters e Setters (mantidos conforme solicitado)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    // --- Novos getters/setters para objetos Dados e Endereco ---
    /**
     * Retorna o objeto Dados. Se estiver null, cria um novo Dados
     * preenchendo-o a partir dos campos achatados (cpfCnpj, email, telefone)
     * para manter compatibilidade retroativa.
     */
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

    /**
     * Define o objeto Dados e sincroniza os campos achatados (cpfCnpj, email, telefone)
     * para que código legado que use os getters achatados continue funcionando.
     */
    public void setDados(Dados dados) {
        this.dados = dados;
        if (dados != null) {
            this.cpfCnpj = dados.getCpfCnpj();
            this.email = dados.getEmail();
            this.telefone = dados.getTelefone();
        }
    }

    public Endereco getEndereco() {
        if (endereco == null) endereco = new Endereco();
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    @Override
    public String toString() {
        return id + " - " + (nome != null ? nome : "");
    }
}
