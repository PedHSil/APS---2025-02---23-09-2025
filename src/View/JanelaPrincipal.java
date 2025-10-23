package View;

import Controller.ClienteController;
import Model.Cliente;
import View.FormCliente;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.JOptionPane;

public class JanelaPrincipal extends Frame {
    private ClienteController controller = new ClienteController();
    private java.awt.List listaClientes = new java.awt.List();
    private TextField txtBusca = new TextField(20);

    public JanelaPrincipal() {
        setTitle("Clientes - Escritório de Arquitetura");
        setSize(600, 500);
        setLayout(new BorderLayout(10, 10));

        // Tema escuro
        Color corFundo = new Color(30, 30, 30);
        Color corTexto = Color.WHITE;
        Color corListaFundo = new Color(45, 45, 45);
        Color corBotaoFiltrar = new Color(70, 130, 180);

        setBackground(corFundo);

        Font fontePadrao = new Font("Century Gothic", Font.PLAIN, 14);
        setFont(fontePadrao);

        listaClientes.setBackground(corListaFundo);
        listaClientes.setForeground(corTexto);
        listaClientes.setFont(fontePadrao);

        txtBusca.setBackground(corListaFundo);
        txtBusca.setForeground(corTexto);
        txtBusca.setFont(fontePadrao);

        Panel painelBusca = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelBusca.setBackground(corFundo);
        Label lblBuscar = new Label("Buscar:");
        lblBuscar.setForeground(corTexto);
        lblBuscar.setFont(new Font("Century Gothic", Font.BOLD, 14));
        painelBusca.add(lblBuscar);
        painelBusca.add(txtBusca);
        Button btnBuscar = new Button("Filtrar");
        btnBuscar.setBackground(corBotaoFiltrar);
        btnBuscar.setForeground(corTexto);
        painelBusca.add(btnBuscar);
        add(painelBusca, BorderLayout.NORTH);

        Panel botoes = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        botoes.setBackground(corFundo);

        Button btnAdd = new Button("Adicionar");
        Button btnRemover = new Button("Remover");
        Button btnEditar = new Button("Editar");

        btnAdd.setForeground(Color.BLACK);
        btnRemover.setForeground(Color.BLACK);
        btnEditar.setForeground(Color.BLACK);

        botoes.add(btnAdd);
        botoes.add(btnRemover);
        botoes.add(btnEditar);

        add(listaClientes, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        atualizarLista();

        listaClientes.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {
        // e.getClickCount() == 2 => duplo clique
        if (e.getClickCount() == 2) {
            int idx = listaClientes.getSelectedIndex();
            if (idx >= 0) {
                // Obtem a string completa (não a versão truncada que a List mostra visualmente)
                // Aqui supondo que você montou a linha como id - nome | email | telefone | cpf
                String linhaCompleta = controller.listarClientes().get(idx).getId() + " - " 
                    + controller.listarClientes().get(idx).getNome() + "\n"
                    + "Email: " + controller.listarClientes().get(idx).getEmail() + "\n"
                    + "Telefone: " + controller.listarClientes().get(idx).getTelefone() + "\n"
                    + "CPF/CNPJ: " + controller.listarClientes().get(idx).getCpfCnpj();

                JOptionPane.showMessageDialog(null, linhaCompleta, "Detalhes do Cliente", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
});

        // Eventos
        btnAdd.addActionListener(e -> new FormCliente(this, controller, null));

        btnRemover.addActionListener(e -> {
            int idx = listaClientes.getSelectedIndex();
            if (idx >= 0) {
                String linha = listaClientes.getSelectedItem();
                try {
                    int id = Integer.parseInt(linha.split(" - ", 2)[0]);
                    controller.removerCliente(id);
                    atualizarLista();
                } catch (NumberFormatException ex) {
                    System.err.println("Erro ao converter id: '" + linha + "'");
                }
            }
        });

        btnEditar.addActionListener(e -> {
            int idx = listaClientes.getSelectedIndex();
            if (idx >= 0) {
                String linha = listaClientes.getSelectedItem();
                try {
                    int id = Integer.parseInt(linha.split(" - ", 2)[0]);
                    Cliente c = controller.buscarClientePorId(id);
                    new FormCliente(this, controller, c);
                } catch (NumberFormatException ex) {
                    System.err.println("Erro ao converter id: '" + linha + "'");
                }
            }
        });

        btnBuscar.addActionListener(e -> filtrarLista());
        txtBusca.addActionListener(e -> filtrarLista());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    // Atualiza listagem — monta string explicitamente e imprime debug
    public void atualizarLista() {
    listaClientes.removeAll();
    List<Cliente> clientes = controller.listarClientes();
    for (Cliente c : clientes) {
        String nome = c.getNome() == null ? "" : c.getNome();
        String email = c.getEmail() == null ? "" : c.getEmail();
        String telefone = c.getTelefone() == null ? "" : c.getTelefone();
        String cpf = c.getCpfCnpj() == null ? "" : c.getCpfCnpj();

        // Opcional: truncar campos longos (ex.: email) para manter a UI limpa
        String emailShort = email.length() > 20 ? email.substring(0, 17) + "..." : email;
        String telShort = telefone.length() > 15 ? telefone.substring(0, 12) + "..." : telefone;

        String linha = String.format("%d - %s | %s | %s", c.getId(), nome, emailShort, telShort, cpf);
        // Se quiser incluir cpf também:
        // String linha = String.format("%d - %s | %s | %s | %s", c.getId(), nome, emailShort, telShort, cpf);

        listaClientes.add(linha);
    }
}

    private void filtrarLista() {
    String termo = txtBusca.getText().trim();
    listaClientes.removeAll();
    List<Cliente> clientes;
    if (termo.isEmpty()) {
        clientes = controller.listarClientes();
    } else {
        clientes = controller.buscarClientes(termo);
    }
    for (Cliente c : clientes) {
        String nome = c.getNome() == null ? "" : c.getNome();
        String email = c.getEmail() == null ? "" : c.getEmail();
        String telefone = c.getTelefone() == null ? "" : c.getTelefone();

        String emailShort = email.length() > 20 ? email.substring(0, 17) + "..." : email;
        String telShort = telefone.length() > 15 ? telefone.substring(0, 12) + "..." : telefone;

        String linha = String.format("%d - %s | %s | %s", c.getId(), nome, emailShort, telShort);
        listaClientes.add(linha);
    }
}

    public static void main(String[] args) {
        new JanelaPrincipal();
    }
}
