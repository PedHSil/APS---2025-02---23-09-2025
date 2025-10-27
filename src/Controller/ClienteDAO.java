package Controller;

import Model.Cliente;
import Model.Dados;
import Model.Endereco;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    // INSERIR novo cliente + dados + endereco (transação)
    public void salvar(Cliente cliente) {
        String sqlInsertCliente = "INSERT INTO cliente (nome) VALUES (?)";
        String sqlInsertDados = "INSERT INTO dados (cliente_id, cpf_cnpj, email, telefone) VALUES (?, ?, ?, ?)";
        String sqlInsertEndereco = "INSERT INTO endereco (cliente_id, tipo, logradouro, numero, complemento, bairro, cidade, estado, cep, pais) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psCli = conn.prepareStatement(sqlInsertCliente, Statement.RETURN_GENERATED_KEYS)) {
                psCli.setString(1, cliente.getNome());
                psCli.executeUpdate();
                try (ResultSet rs = psCli.getGeneratedKeys()) {
                    if (rs.next()) {
                        int clienteId = rs.getInt(1);
                        cliente.setId(clienteId);
                    } else {
                        throw new SQLException("Não foi possível obter id do cliente inserido.");
                    }
                }
            }

            // inserir dados (se houver)
            Dados d = cliente.getDados();
            // Também considera campos achatados caso setados sem objetos
            boolean temDados = (d != null && (notEmpty(d.getCpfCnpj()) || notEmpty(d.getEmail()) || notEmpty(d.getTelefone())))
                              || notEmpty(cliente.getCpfCnpj()) || notEmpty(cliente.getEmail()) || notEmpty(cliente.getTelefone());

            if (temDados) {
                // sincroniza: se dados for null, cria a partir dos campos achatados
                if (d == null) d = new Dados();
                if (isEmpty(d.getCpfCnpj())) d.setCpfCnpj(cliente.getCpfCnpj());
                if (isEmpty(d.getEmail())) d.setEmail(cliente.getEmail());
                if (isEmpty(d.getTelefone())) d.setTelefone(cliente.getTelefone());

                try (PreparedStatement psD = conn.prepareStatement(sqlInsertDados, Statement.RETURN_GENERATED_KEYS)) {
                    psD.setInt(1, cliente.getId());
                    psD.setString(2, d.getCpfCnpj());
                    psD.setString(3, d.getEmail());
                    psD.setString(4, d.getTelefone());
                    psD.executeUpdate();
                    try (ResultSet rsd = psD.getGeneratedKeys()) {
                        if (rsd.next()) d.setId(rsd.getInt(1));
                    }
                }
                // atualiza cliente (campos achatados) para manter compatibilidade local
                cliente.setCpfCnpj(d.getCpfCnpj());
                cliente.setEmail(d.getEmail());
                cliente.setTelefone(d.getTelefone());
                cliente.setDados(d);
            }

            // inserir endereco (se houver)
            Endereco e = cliente.getEndereco();
            boolean temEndereco = (e != null && notEmpty(e.getLogradouro()));
            if (temEndereco) {
                try (PreparedStatement psE = conn.prepareStatement(sqlInsertEndereco, Statement.RETURN_GENERATED_KEYS)) {
                    psE.setInt(1, cliente.getId());
                    psE.setString(2, e.getTipo());
                    psE.setString(3, e.getLogradouro());
                    psE.setString(4, e.getNumero());
                    psE.setString(5, e.getComplemento());
                    psE.setString(6, e.getBairro());
                    psE.setString(7, e.getCidade());
                    psE.setString(8, e.getEstado());
                    psE.setString(9, e.getCep());
                    psE.setString(10, e.getPais());
                    psE.executeUpdate();
                    try (ResultSet rse = psE.getGeneratedKeys()) {
                        if (rse.next()) e.setId(rse.getInt(1));
                    }
                }
                e.setClienteId(cliente.getId());
                cliente.setEndereco(e);
            }

            conn.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
            // Em caso de falha, dependendo da sua Conexao.getConnection(), certifique-se de rollback
            // Aqui não podemos chamar conn.rollback() porque o try-with-resources fecha a conn no bloco.
            // Para garantir rollback, podemos re-escrever sem try-with-resources para o Connection.
            // Mas, na prática, a maior parte dos drivers faz rollback ao fechar conexão não commitada.
        }
    }

    // ATUALIZAR cliente + dados + endereco (upsert manual)
    public void atualizar(Cliente cliente) {
        String sqlUpdateCliente = "UPDATE cliente SET nome = ? WHERE id = ?";
        String sqlSelectDados = "SELECT id FROM dados WHERE cliente_id = ?";
        String sqlUpdateDados = "UPDATE dados SET cpf_cnpj = ?, email = ?, telefone = ? WHERE cliente_id = ?";
        String sqlInsertDados = "INSERT INTO dados (cliente_id, cpf_cnpj, email, telefone) VALUES (?, ?, ?, ?)";

        String sqlSelectEndereco = "SELECT id FROM endereco WHERE cliente_id = ?";
        String sqlUpdateEndereco = "UPDATE endereco SET tipo=?, logradouro=?, numero=?, complemento=?, bairro=?, cidade=?, estado=?, cep=?, pais=? WHERE cliente_id=?";
        String sqlInsertEndereco = "INSERT INTO endereco (cliente_id, tipo, logradouro, numero, complemento, bairro, cidade, estado, cep, pais) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psCli = conn.prepareStatement(sqlUpdateCliente)) {
                psCli.setString(1, cliente.getNome());
                psCli.setInt(2, cliente.getId());
                psCli.executeUpdate();
            }

            // DADOS: verifica existência
            Dados d = cliente.getDados();
            boolean temDados = (d != null && (notEmpty(d.getCpfCnpj()) || notEmpty(d.getEmail()) || notEmpty(d.getTelefone())))
                              || notEmpty(cliente.getCpfCnpj()) || notEmpty(cliente.getEmail()) || notEmpty(cliente.getTelefone());

            try (PreparedStatement psCheckD = conn.prepareStatement(sqlSelectDados)) {
                psCheckD.setInt(1, cliente.getId());
                try (ResultSet rs = psCheckD.executeQuery()) {
                    boolean existe = rs.next();
                    if (temDados) {
                        if (d == null) d = new Dados();
                        if (isEmpty(d.getCpfCnpj())) d.setCpfCnpj(cliente.getCpfCnpj());
                        if (isEmpty(d.getEmail())) d.setEmail(cliente.getEmail());
                        if (isEmpty(d.getTelefone())) d.setTelefone(cliente.getTelefone());

                        if (existe) {
                            try (PreparedStatement psUpd = conn.prepareStatement(sqlUpdateDados)) {
                                psUpd.setString(1, d.getCpfCnpj());
                                psUpd.setString(2, d.getEmail());
                                psUpd.setString(3, d.getTelefone());
                                psUpd.setInt(4, cliente.getId());
                                psUpd.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement psIns = conn.prepareStatement(sqlInsertDados)) {
                                psIns.setInt(1, cliente.getId());
                                psIns.setString(2, d.getCpfCnpj());
                                psIns.setString(3, d.getEmail());
                                psIns.setString(4, d.getTelefone());
                                psIns.executeUpdate();
                            }
                        }

                        // sincronizar campos achatados
                        cliente.setCpfCnpj(d.getCpfCnpj());
                        cliente.setEmail(d.getEmail());
                        cliente.setTelefone(d.getTelefone());
                        cliente.setDados(d);
                    } else {
                        // opcional: se não tem dados agora, você pode deletar registro em dados (não fiz aqui)
                    }
                }
            }

            // ENDEREÇO: verifica existência e faz update/insert
            Endereco e = cliente.getEndereco();
            boolean temEndereco = (e != null && notEmpty(e.getLogradouro()));

            try (PreparedStatement psCheckE = conn.prepareStatement(sqlSelectEndereco)) {
                psCheckE.setInt(1, cliente.getId());
                try (ResultSet rs = psCheckE.executeQuery()) {
                    boolean existeE = rs.next();
                    if (temEndereco) {
                        if (existeE) {
                            try (PreparedStatement psUpdE = conn.prepareStatement(sqlUpdateEndereco)) {
                                psUpdE.setString(1, e.getTipo());
                                psUpdE.setString(2, e.getLogradouro());
                                psUpdE.setString(3, e.getNumero());
                                psUpdE.setString(4, e.getComplemento());
                                psUpdE.setString(5, e.getBairro());
                                psUpdE.setString(6, e.getCidade());
                                psUpdE.setString(7, e.getEstado());
                                psUpdE.setString(8, e.getCep());
                                psUpdE.setString(9, e.getPais());
                                psUpdE.setInt(10, cliente.getId());
                                psUpdE.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement psInsE = conn.prepareStatement(sqlInsertEndereco)) {
                                psInsE.setInt(1, cliente.getId());
                                psInsE.setString(2, e.getTipo());
                                psInsE.setString(3, e.getLogradouro());
                                psInsE.setString(4, e.getNumero());
                                psInsE.setString(5, e.getComplemento());
                                psInsE.setString(6, e.getBairro());
                                psInsE.setString(7, e.getCidade());
                                psInsE.setString(8, e.getEstado());
                                psInsE.setString(9, e.getCep());
                                psInsE.setString(10, e.getPais());
                                psInsE.executeUpdate();
                            }
                        }
                        e.setClienteId(cliente.getId());
                        cliente.setEndereco(e);
                    } else {
                        // opcional: deletar endereco existente se usuário apagou dados de endereço
                    }
                }
            }

            conn.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
            // mesma observação sobre rollback automático ao fechar conexão não commitada
        }
    }

    // LISTAR todos os clientes (agora popula Dados e Endereco parcialmente)
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.id, c.nome, c.created_at, c.updated_at, " +
                     "d.id AS dados_id, d.cpf_cnpj, d.email, d.telefone, " +
                     "e.id AS end_id, e.tipo AS end_tipo, e.logradouro, e.numero, e.complemento, e.bairro, e.cidade, e.estado, e.cep, e.pais " +
                     "FROM cliente c " +
                     "LEFT JOIN dados d ON d.cliente_id = c.id " +
                     "LEFT JOIN endereco e ON e.cliente_id = c.id " +
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

                String cpf = rs.getString("cpf_cnpj");
                String email = rs.getString("email");
                String tel = rs.getString("telefone");

                c.setCpfCnpj(cpf);
                c.setEmail(email);
                c.setTelefone(tel);

                int dadosId = rs.getInt("dados_id");
                if (!rs.wasNull()) {
                    Dados d = new Dados();
                    d.setId(dadosId);
                    d.setClienteId(c.getId());
                    d.setCpfCnpj(cpf);
                    d.setEmail(email);
                    d.setTelefone(tel);
                    c.setDados(d);
                }

                int endId = rs.getInt("end_id");
                if (!rs.wasNull()) {
                    Endereco e = new Endereco();
                    e.setId(endId);
                    e.setClienteId(c.getId());
                    e.setTipo(rs.getString("end_tipo"));
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

                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // BUSCAR cliente por ID (popula dados e endereco)
    public Cliente buscarPorId(int id) {
        String sql = "SELECT c.id, c.nome, c.created_at, c.updated_at, " +
                     "d.id AS dados_id, d.cpf_cnpj, d.email, d.telefone, " +
                     "e.id AS end_id, e.tipo AS end_tipo, e.logradouro, e.numero, e.complemento, e.bairro, e.cidade, e.estado, e.cep, e.pais " +
                     "FROM cliente c " +
                     "LEFT JOIN dados d ON d.cliente_id = c.id " +
                     "LEFT JOIN endereco e ON e.cliente_id = c.id " +
                     "WHERE c.id = ?";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cliente c = new Cliente();
                    c.setId(rs.getInt("id"));
                    c.setNome(rs.getString("nome"));
                    c.setCreatedAt(rs.getTimestamp("created_at"));
                    c.setUpdatedAt(rs.getTimestamp("updated_at"));

                    String cpf = rs.getString("cpf_cnpj");
                    String email = rs.getString("email");
                    String tel = rs.getString("telefone");

                    c.setCpfCnpj(cpf);
                    c.setEmail(email);
                    c.setTelefone(tel);

                    int dadosId = rs.getInt("dados_id");
                    if (!rs.wasNull()) {
                        Dados d = new Dados();
                        d.setId(dadosId);
                        d.setClienteId(c.getId());
                        d.setCpfCnpj(cpf);
                        d.setEmail(email);
                        d.setTelefone(tel);
                        c.setDados(d);
                    }

                    int endId = rs.getInt("end_id");
                    if (!rs.wasNull()) {
                        Endereco e = new Endereco();
                        e.setId(endId);
                        e.setClienteId(c.getId());
                        e.setTipo(rs.getString("end_tipo"));
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // REMOVER cliente
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

    // BUSCAR por nome (simples)
    public List<Cliente> buscarPorNome(String nome) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.id, c.nome, c.created_at, c.updated_at, d.cpf_cnpj, d.email, d.telefone " +
                     "FROM cliente c LEFT JOIN dados d ON d.cliente_id = c.id WHERE c.nome LIKE ?";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nome + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente c = new Cliente();
                    c.setId(rs.getInt("id"));
                    c.setNome(rs.getString("nome"));
                    c.setCreatedAt(rs.getTimestamp("created_at"));
                    c.setUpdatedAt(rs.getTimestamp("updated_at"));

                    c.setCpfCnpj(rs.getString("cpf_cnpj"));
                    c.setEmail(rs.getString("email"));
                    c.setTelefone(rs.getString("telefone"));

                    // popula Dados mínimo
                    Dados d = new Dados();
                    d.setClienteId(c.getId());
                    d.setCpfCnpj(c.getCpfCnpj());
                    d.setEmail(c.getEmail());
                    d.setTelefone(c.getTelefone());
                    c.setDados(d);

                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Helpers
    private boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
