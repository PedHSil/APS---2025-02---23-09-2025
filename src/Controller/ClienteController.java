package Controller;

import Model.Cliente;
import java.util.List;

public class ClienteController {
    private ClienteDAO dao = new ClienteDAO();

    public void adicionarCliente(String nome) {
        Cliente c = new Cliente(0, nome);
        dao.salvar(c);
    }

    public void salvarCliente(Cliente c) { dao.salvar(c); }
    public void atualizarCliente(Cliente c) { dao.atualizar(c); }
    public void removerCliente(int id) { dao.remover(id); }

    public List<Cliente> listarClientes() { return dao.listar(); }

    // busca genérica (nome/email/telefone/cpf/endereço)
    public List<Cliente> buscarClientes(String termo) {
        return dao.buscarPorTermoCompleto(termo);
    }

    public Cliente buscarClientePorId(int id) { return dao.buscarPorId(id); }
}
