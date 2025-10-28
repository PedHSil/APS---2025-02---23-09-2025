package Controller;

import Model.Cliente;
import Model.Dados;
import Model.Endereco;

import java.sql.*;
import java.util.*;

public class ClienteDAO {

    public void salvar(Cliente cliente) {
        String sql = "INSERT INTO cliente (nome) VALUES (?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cliente.getNome());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) cliente.setId(keys.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.id, c.nome, c.created_at, c.updated_at, " +
                     "d.cpf_cnpj, d.email, d.telefone " +
                     "FROM cliente c LEFT JOIN dados d ON d.cliente_id = c.id " +
                     "ORDER BY c.id DESC";
        try (Connection conn = Conexao.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("id"));
                c.setNome(rs.getString("nome"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setUpdatedAt(rs.getTimestamp("updated_at"));
                c.setCpfCnpj(rs.getString("cpf_cnpj"));
                c.setEmail(rs.getString("email"));
                c.setTelefone(rs.getString("telefone"));
                lista.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public void remover(int id) {
        String sql = "DELETE FROM cliente WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

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
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    // Busca por QUALQUER termo (nome/email/telefone/cpf_cnpj e endereço) retornando cliente completo
    public List<Cliente> buscarPorTermoCompleto(String termo) {
        String sql = """
            SELECT DISTINCT
                c.id AS c_id, c.nome AS c_nome, c.created_at AS c_created_at, c.updated_at AS c_updated_at,
                d.id AS d_id, d.cpf_cnpj AS d_cpf_cnpj, d.email AS d_email, d.telefone AS d_telefone,
                e.id AS e_id, e.tipo AS e_tipo, e.logradouro AS e_logradouro, e.numero AS e_numero,
                e.complemento AS e_complemento, e.bairro AS e_bairro, e.cidade AS e_cidade,
                e.estado AS e_estado, e.cep AS e_cep, e.pais AS e_pais
            FROM cliente c
            LEFT JOIN dados d    ON d.cliente_id = c.id
            LEFT JOIN endereco e ON e.cliente_id = c.id
            WHERE c.nome       LIKE ?
               OR d.email      LIKE ?
               OR d.telefone   LIKE ?
               OR d.cpf_cnpj   LIKE ?
               OR e.logradouro LIKE ?
               OR e.numero     LIKE ?
               OR e.complemento LIKE ?
               OR e.bairro     LIKE ?
               OR e.cidade     LIKE ?
               OR e.estado     LIKE ?
               OR e.cep        LIKE ?
               OR e.pais       LIKE ?
            ORDER BY c.nome, c.id
        """;

        Map<Integer, Cliente> mapa = new LinkedHashMap<>();
        String like = "%" + termo + "%";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 12; i++) stmt.setString(i, like);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("c_id");
                    Cliente cli = mapa.get(id);
                    if (cli == null) {
                        cli = new Cliente();
                        cli.setId(id);
                        cli.setNome(rs.getString("c_nome"));
                        cli.setCreatedAt(rs.getTimestamp("c_created_at"));
                        cli.setUpdatedAt(rs.getTimestamp("c_updated_at"));

                        Integer dId = (Integer) rs.getObject("d_id");
                        if (dId != null) {
                            Dados d = new Dados();
                            d.setId(dId);
                            d.setCpfCnpj(rs.getString("d_cpf_cnpj"));
                            d.setEmail(rs.getString("d_email"));
                            d.setTelefone(rs.getString("d_telefone"));
                            cli.setDados(d);
                            // espelha para os campos que a UI usa
                            cli.setCpfCnpj(d.getCpfCnpj());
                            cli.setEmail(d.getEmail());
                            cli.setTelefone(d.getTelefone());
                        }
                        mapa.put(id, cli);
                    }

                    Integer eId = (Integer) rs.getObject("e_id");
                    if (eId != null) {
                        Endereco e = new Endereco();
                        e.setId(eId);
                        e.setTipo(rs.getString("e_tipo"));
                        e.setLogradouro(rs.getString("e_logradouro"));
                        e.setNumero(rs.getString("e_numero"));
                        e.setComplemento(rs.getString("e_complemento"));
                        e.setBairro(rs.getString("e_bairro"));
                        e.setCidade(rs.getString("e_cidade"));
                        e.setEstado(rs.getString("e_estado"));
                        e.setCep(rs.getString("e_cep"));
                        e.setPais(rs.getString("e_pais"));

                        boolean exists = false;
                        for (Endereco ex : mapa.get(id).getEnderecos()) {
                            if (Objects.equals(ex.getId(), e.getId())) { exists = true; break; }
                        }
                        if (!exists) mapa.get(id).getEnderecos().add(e);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return new ArrayList<>(mapa.values());
    }

    public void atualizar(Cliente c) {
        String sql = "UPDATE cliente SET nome = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getNome());
            stmt.setInt(2, c.getId());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Cliente buscarPorId(int id) {
        String sql = "SELECT c.id, c.nome, c.created_at, c.updated_at, " +
                     "d.cpf_cnpj, d.email, d.telefone " +
                     "FROM cliente c LEFT JOIN dados d ON d.cliente_id = c.id WHERE c.id = ?";
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
                    c.setCpfCnpj(rs.getString("cpf_cnpj"));
                    c.setEmail(rs.getString("email"));
                    c.setTelefone(rs.getString("telefone"));
                    return c;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
