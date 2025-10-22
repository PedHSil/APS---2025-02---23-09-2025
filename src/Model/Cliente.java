package Model;

import java.sql.Timestamp;

public class Cliente {
    private int id;
    private String nome;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String email; // <-- novo campo

    public Cliente() { }

    public Cliente(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // Getters e setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getEmail() { return email; }  // <-- getter
    public void setEmail(String email) { this.email = email; } // <-- setter

    @Override
    public String toString() {
        return id + " - " + nome + (email != null ? " (" + email + ")" : "");
    }
}
