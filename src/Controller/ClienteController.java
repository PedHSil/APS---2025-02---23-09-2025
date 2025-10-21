package Controller;

import Model.Cliente;
import java.util.List;

public class ClienteController {
    private ClienteDAO dao = new ClienteDAO();

    // Mantive o método original (se você quiser mantê-lo)
    public void adicionarCliente(String nome) {
        Cliente c = new Cliente(0, nome);
        dao.salvar(c);
    }

    // Métodos que a View espera (salvar/atualizar/buscar por id)
    public void salvarCliente(Cliente c) {
        dao.salvar(c);
    }

    public void atualizarCliente(Cliente c) {
        dao.atualizar(c);
    }

    public List<Cliente> listarClientes() {
        return dao.listar();
    }

    public void removerCliente(int id) {
        dao.remover(id);
    }

    public List<Cliente> buscarClientes(String nome) {
        return dao.buscarPorNome(nome);
    }

    public Cliente buscarClientePorId(int id) {
        return dao.buscarPorId(id);
    }
}
