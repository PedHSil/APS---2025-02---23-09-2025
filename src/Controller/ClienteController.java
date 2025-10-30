package Controller;

import Model.Cliente;
import Model.Dados;
import Model.Endereco;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller atualizado com validações e tratamento de exceções do DAO.
 * Regras:
 *  - lança IllegalArgumentException para erros de validação (regras de negócio)
 *  - lança Exception (envolvendo SQLException) para problemas de persistência
 */
public class ClienteController {
    private ClienteDAO dao = new ClienteDAO();

    public void adicionarCliente(String nome) throws Exception {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode estar vazio.");
        }
        Cliente c = new Cliente(0, nome.trim());
        try {
            dao.salvar(c);
        } catch (SQLException ex) {
            throw new Exception("Erro ao salvar cliente: " + ex.getMessage(), ex);
        }
    }

    public void salvarCliente(Cliente c) throws Exception {
        validarClienteParaSalvar(c);
        try {
            dao.salvar(c);
        } catch (SQLException ex) {
            throw new Exception("Erro ao salvar cliente no banco: " + ex.getMessage(), ex);
        }
    }

    public void atualizarCliente(Cliente c) throws Exception {
        if (c == null || c.getId() <= 0) {
            throw new IllegalArgumentException("Cliente inválido para atualizar.");
        }
        validarClienteParaSalvar(c); // mesma validação básica
        try {
            dao.atualizar(c);
        } catch (SQLException ex) {
            throw new Exception("Erro ao atualizar cliente no banco: " + ex.getMessage(), ex);
        }
    }

    public void removerCliente(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido para remoção.");
        }
        try {
            dao.remover(id);
        } catch (SQLException ex) {
            throw new Exception("Erro ao remover cliente: " + ex.getMessage(), ex);
        }
    }

    public List<Cliente> listarClientes() throws Exception {
        try {
            return dao.listar();
        } catch (SQLException ex) {
            throw new Exception("Erro ao listar clientes: " + ex.getMessage(), ex);
        }
    }

    // busca genérica (nome/email/telefone/cpf/endereço)
    public List<Cliente> buscarClientes(String termo) throws Exception {
        try {
            return dao.buscarPorTermoCompleto(termo);
        } catch (SQLException ex) {
            throw new Exception("Erro ao buscar clientes: " + ex.getMessage(), ex);
        }
    }

    public Cliente buscarClientePorId(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido para busca.");
        }
        try {
            return dao.buscarPorId(id);
        } catch (SQLException ex) {
            throw new Exception("Erro ao buscar cliente por ID: " + ex.getMessage(), ex);
        }
    }

    /**
     * Validações básicas de negócio antes de salvar/atualizar.
     * Ajuste as regras conforme necessidade (ex.: CPF/CNPJ, email, duplicidade).
     */
    private void validarClienteParaSalvar(Cliente c) {
    if (c == null) throw new IllegalArgumentException("Cliente não pode ser nulo.");
    if (c.getNome() == null || c.getNome().trim().isEmpty())
        throw new IllegalArgumentException("Nome é obrigatório.");
    Dados d = c.getDados();
    if (d == null) throw new IllegalArgumentException("Dados do cliente são obrigatórios.");
    String cpf = d.getCpfCnpj() == null ? "" : d.getCpfCnpj().replaceAll("\\D","");
    if (cpf.length() != 11 && cpf.length() != 14)
        throw new IllegalArgumentException("CPF/CNPJ inválido.");
    if (d.getEmail() == null || !d.getEmail().matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
        throw new IllegalArgumentException("Email inválido.");
    if (d.getTelefone() == null || d.getTelefone().replaceAll("\\D","").length() < 8)
        throw new IllegalArgumentException("Telefone inválido.");

    Endereco e = c.getEndereco();
    if (e == null) throw new IllegalArgumentException("Endereço é obrigatório.");
    if (e.getTipo() == null || e.getTipo().trim().isEmpty()) throw new IllegalArgumentException("Tipo de endereço obrigatório.");
    if (e.getLogradouro() == null || e.getLogradouro().trim().isEmpty()) throw new IllegalArgumentException("Logradouro obrigatório.");
    if (e.getNumero() == null || e.getNumero().trim().isEmpty()) throw new IllegalArgumentException("Número obrigatório.");
    if (e.getComplemento() == null || e.getComplemento().trim().isEmpty()) throw new IllegalArgumentException("Complemento obrigatório.");
    if (e.getBairro() == null || e.getBairro().trim().isEmpty()) throw new IllegalArgumentException("Bairro obrigatório.");
    if (e.getCidade() == null || e.getCidade().trim().isEmpty()) throw new IllegalArgumentException("Cidade obrigatória.");
    if (e.getEstado() == null || e.getEstado().trim().isEmpty()) throw new IllegalArgumentException("Estado obrigatório.");
    String cep = e.getCep() == null ? "" : e.getCep().replaceAll("\\D","");
    if (cep.length() < 7) throw new IllegalArgumentException("CEP inválido.");
    if (e.getPais() == null || e.getPais().trim().isEmpty()) throw new IllegalArgumentException("País obrigatório.");
}

}
