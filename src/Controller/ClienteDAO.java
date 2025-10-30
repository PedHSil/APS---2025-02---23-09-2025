package Controller;

import Model.Cliente;
import Model.Dados;
import Model.Endereco;

import java.sql.*;
import java.util.*;

/**
 * ClienteDAO atualizado: métodos declaram throws SQLException
 * e operação de salvar realiza transação (cliente + dados + endereco).
 */
public class ClienteDAO {

    public void salvar(Cliente cliente) throws SQLException {
        String sqlCliente = "INSERT INTO cliente (nome) VALUES (?)";
        String sqlDados = "INSERT INTO dados (cliente_id, cpf_cnpj, email, telefone) VALUES (?, ?, ?, ?)";
        String sqlEndereco = "INSERT INTO endereco (cliente_id, tipo, logradouro, numero, complemento, bairro, cidade, estado, cep, pais) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = Conexao.getConnection();
            conn.setAutoCommit(false); // inicia transação

            // inserir cliente
            try (PreparedStatement stmt = conn.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, cliente.getNome());
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        cliente.setId(keys.getInt(1));
                    } else {
                        throw new SQLException("Falha ao obter ID gerado para cliente.");
                    }
                }
            }

            Dados d = cliente.getDados();
            if (d != null) {
                try (PreparedStatement stmtDados = conn.prepareStatement(sqlDados, Statement.RETURN_GENERATED_KEYS)) {
                    stmtDados.setInt(1, cliente.getId());
                    stmtDados.setString(2, d.getCpfCnpj());
                    stmtDados.setString(3, d.getEmail());
                    stmtDados.setString(4, d.getTelefone());
                    stmtDados.executeUpdate();
                    try (ResultSet keys = stmtDados.getGeneratedKeys()) {
                        if (keys.next())
                            d.setId(keys.getInt(1));
                    }
                }
            }

            Endereco e = cliente.getEndereco();
            if (e != null) {
                try (PreparedStatement stmtEnd = conn.prepareStatement(sqlEndereco, Statement.RETURN_GENERATED_KEYS)) {
                    stmtEnd.setInt(1, cliente.getId());
                    stmtEnd.setString(2, e.getTipo());
                    stmtEnd.setString(3, e.getLogradouro());
                    stmtEnd.setString(4, e.getNumero());
                    stmtEnd.setString(5, e.getComplemento());
                    stmtEnd.setString(6, e.getBairro());
                    stmtEnd.setString(7, e.getCidade());
                    stmtEnd.setString(8, e.getEstado());
                    stmtEnd.setString(9, e.getCep());
                    stmtEnd.setString(10, e.getPais());
                    stmtEnd.executeUpdate();
                    try (ResultSet keys = stmtEnd.getGeneratedKeys()) {
                        if (keys.next())
                            e.setId(keys.getInt(1));
                    }
                }
            }

            conn.commit();
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbe) {
                    /* fallback: nada */ }
            }
            throw ex; // relança para controller tratar
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public List<Cliente> listar() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.id, c.nome, c.created_at, c.updated_at, " +
                "d.cpf_cnpj, d.email, d.telefone, " +
                "e.id AS e_id, e.tipo AS e_tipo, e.logradouro AS e_logradouro, e.numero AS e_numero, " +
                "e.complemento AS e_complemento, e.bairro AS e_bairro, e.cidade AS e_cidade, " +
                "e.estado AS e_estado, e.cep AS e_cep, e.pais AS e_pais " +
                "FROM cliente c " +
                "LEFT JOIN dados d ON d.cliente_id = c.id " +
                // junta o primeiro endereco (se existir). ORDER BY id LIMIT 1 pode variar por
                // SGBD; funciona em MySQL/Postgres.
                "LEFT JOIN endereco e ON e.id = (SELECT id FROM endereco WHERE cliente_id = c.id ORDER BY id LIMIT 1) "
                +
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

                Dados d = new Dados();
                d.setCpfCnpj(rs.getString("cpf_cnpj"));
                d.setEmail(rs.getString("email"));
                d.setTelefone(rs.getString("telefone"));
                c.setDados(d);

                c.setCpfCnpj(d.getCpfCnpj());
                c.setEmail(d.getEmail());
                c.setTelefone(d.getTelefone());

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
                    c.setEndereco(e);
                }

                lista.add(c);
            }
        }
        return lista;
    }

    public void remover(int id) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Cliente> buscarPorNome(String nome) throws SQLException {
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
        }
        return lista;
    }

    public void atualizarClienteCompleto(Cliente cliente) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexao.getConnection();
            conn.setAutoCommit(false);

            // Atualizar cliente
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE cliente SET nome = ? WHERE id = ?")) {
                stmt.setString(1, cliente.getNome());
                stmt.setInt(2, cliente.getId());
                stmt.executeUpdate();
            }

            // Atualizar dados
            Dados d = cliente.getDados();
            if (d != null) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE dados SET cpf_cnpj = ?, email = ?, telefone = ? WHERE cliente_id = ?")) {
                    stmt.setString(1, d.getCpfCnpj());
                    stmt.setString(2, d.getEmail());
                    stmt.setString(3, d.getTelefone());
                    stmt.setInt(4, cliente.getId());
                    stmt.executeUpdate();
                }
            }

            // Atualizar endereço
            Endereco e = cliente.getEndereco();
            if (e != null) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE endereco SET tipo = ?, logradouro = ?, numero = ?, complemento = ?, bairro = ?, cidade = ?, estado = ?, cep = ?, pais = ? WHERE id = ?")) {
                    stmt.setString(1, e.getTipo());
                    stmt.setString(2, e.getLogradouro());
                    stmt.setString(3, e.getNumero());
                    stmt.setString(4, e.getComplemento());
                    stmt.setString(5, e.getBairro());
                    stmt.setString(6, e.getCidade());
                    stmt.setString(7, e.getEstado());
                    stmt.setString(8, e.getCep());
                    stmt.setString(9, e.getPais());
                    stmt.setInt(10, e.getId());
                    stmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException ex) {
            if (conn != null)
                conn.rollback();
            throw ex;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // Busca por termo (nome/email/telefone/cpf_cnpj e endereço)
    public List<Cliente> buscarPorTermoCompleto(String termo) throws SQLException {
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

            for (int i = 1; i <= 12; i++)
                stmt.setString(i, like);

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
                            if (Objects.equals(ex.getId(), e.getId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists)
                            mapa.get(id).getEnderecos().add(e);
                    }
                }
            }
        }
        return new ArrayList<>(mapa.values());
    }

    public void atualizar(Cliente c) throws SQLException {
        String sql = "UPDATE cliente SET nome = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getNome());
            stmt.setInt(2, c.getId());
            stmt.executeUpdate();
        }
    }

    public Cliente buscarPorId(int id) throws SQLException {
        String sql = "SELECT c.id, c.nome, c.created_at, c.updated_at, " +
                "d.cpf_cnpj, d.email, d.telefone, " +
                "e.id AS e_id, e.tipo, e.logradouro, e.numero, " +
                "e.complemento, e.bairro, e.cidade, e.estado, e.cep, e.pais " +
                "FROM cliente c " +
                "LEFT JOIN dados d ON d.cliente_id = c.id " +
                "LEFT JOIN endereco e ON e.cliente_id = c.id " +
                "WHERE c.id = ?";

        try (Connection conn = Conexao.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Cliente c = new Cliente();
                    c.setId(rs.getInt("id"));
                    c.setNome(rs.getString("nome"));
                    // preencher Dados
                    Dados d = new Dados();
                    d.setCpfCnpj(rs.getString("cpf_cnpj"));
                    d.setEmail(rs.getString("email"));
                    d.setTelefone(rs.getString("telefone"));
                    c.setDados(d);

                    // preencher Endereço
                    Integer eId = (Integer) rs.getObject("e_id");
                    if (eId != null) {
                        Endereco e = new Endereco();
                        e.setId(eId);
                        e.setTipo(rs.getString("tipo"));
                        e.setLogradouro(rs.getString("logradouro"));
                        e.setNumero(rs.getString("numero"));
                        e.setComplemento(rs.getString("complemento"));
                        e.setBairro(rs.getString("bairro"));
                        e.setCidade(rs.getString("cidade"));
                        e.setEstado(rs.getString("estado"));
                        e.setCep(rs.getString("cep"));
                        e.setPais(rs.getString("pais"));
                        c.setEndereco(e);
                    }

                    return c;
                }
            }
        }
        return null;
    }

}
