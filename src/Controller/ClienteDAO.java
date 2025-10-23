package Controller;

import Model.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    // ✅ INSERIR novo cliente
    public void salvar(Cliente cliente) {
        String sql = "INSERT INTO cliente (nome) VALUES (?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cliente.getNome());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    cliente.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ LISTAR todos os clientes
   public List<Cliente> listar() {
    List<Cliente> lista = new ArrayList<>();
    String sql = "SELECT id, nome, created_at, updated_at FROM cliente ORDER BY id DESC";
    System.out.println("DEBUG: Executando listar() -> SQL: " + sql);
    try (Connection conn = Conexao.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        int contador = 0;
        while (rs.next()) {
            contador++;
            int id = rs.getInt("id");
            String nome = rs.getString("nome");
            Timestamp created = rs.getTimestamp("created_at");
            Timestamp updated = rs.getTimestamp("updated_at");

            // DEBUG: imprimir os valores recebidos do banco
            System.out.println("ROW: id=" + id + ", nome=" + nome + ", created_at=" + created + ", updated_at=" + updated);

            Cliente c = new Cliente();
            c.setId(id);
            c.setNome(nome);
            c.setCreatedAt(created);
            c.setUpdatedAt(updated);
            lista.add(c);
        }
        System.out.println("DEBUG: listar() terminou. linhas lidas = " + contador);
    } catch (SQLException e) {
        System.err.println("ERROR: listar() - SQLException:");
        e.printStackTrace();
    }
    return lista;
}
    // ✅ REMOVER cliente (dados e endereços são removidos automaticamente por ON DELETE CASCADE)
    public void remover(int id) {
        String sql = "DELETE FROM cliente WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ BUSCAR cliente(s) por nome
    public List<Cliente> buscarPorNome(String nome) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id, nome, created_at, updated_at FROM cliente WHERE nome LIKE ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nome + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Cliente c = new Cliente();
                    c.setId(rs.getInt("id"));
                    c.setNome(rs.getString("nome"));
                    c.setCreatedAt(rs.getTimestamp("created_at"));
                    c.setUpdatedAt(rs.getTimestamp("updated_at"));
                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ✅ ATUALIZAR nome de um cliente
    public void atualizar(Cliente c) {
        String sql = "UPDATE cliente SET nome = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getNome());
            stmt.setInt(2, c.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ BUSCAR cliente por ID
    public Cliente buscarPorId(int id) {
        String sql = "SELECT id, nome, created_at, updated_at FROM cliente WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Cliente c = new Cliente();
                    c.setId(rs.getInt("id"));
                    c.setNome(rs.getString("nome"));
                    c.setCreatedAt(rs.getTimestamp("created_at"));
                    c.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
