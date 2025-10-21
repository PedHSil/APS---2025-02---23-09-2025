package View;

import Controller.ClienteController;
import Model.Cliente;

import java.awt.*;
import java.awt.event.*;

public class FormCliente extends Dialog {
    private TextField txtNome = new TextField(20);
    private TextField txtEmail = new TextField(20);
    private TextField txtTelefone = new TextField(15);
    private Cliente clienteEdicao = null;
    private ClienteController controller;
    private Frame parent;

    // agora aceita (Frame, controller, Cliente) — JanelaPrincipal usa esse formato
    public FormCliente(Frame parent, ClienteController controller, Cliente cliente) {
        super(parent, (cliente == null ? "Novo Cliente" : "Editar Cliente"), true);
        this.parent = parent;
        this.controller = controller;
        this.clienteEdicao = cliente;

        setSize(300, 200);
        setLayout(new GridLayout(4, 2));

        // Campos do formulário
        add(new Label("Nome:"));
        add(txtNome);
        add(new Label("Email:"));
        add(txtEmail);
        add(new Label("Telefone:"));
        add(txtTelefone);

        // Botão salvar
        Button btnSalvar = new Button("Salvar");
        add(new Label("")); // espaço vazio
        add(btnSalvar);

        // Se vier um cliente para editar, carrega os dados
        if (clienteEdicao != null) {
            carregarCliente(clienteEdicao);
        }

        // ActionListener do botão Salvar
        btnSalvar.addActionListener(e -> {
            if (clienteEdicao == null) {
                // Novo cliente — usa o construtor com id = 0
                Cliente novo = new Cliente(0,
                        txtNome.getText(),
                        txtEmail.getText(),
                        txtTelefone.getText()
                );
                controller.salvarCliente(novo);
            } else {
                // Editando cliente existente
                clienteEdicao.setNome(txtNome.getText());
                clienteEdicao.setEmail(txtEmail.getText());
                clienteEdicao.setTelefone(txtTelefone.getText());
                controller.atualizarCliente(clienteEdicao);
            }
            // Atualiza a lista na JanelaPrincipal
            ((JanelaPrincipal) parent).atualizarLista();
            dispose();
        });

        // Fechar diálogo
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    // Método para carregar cliente existente (edição)
    public void carregarCliente(Cliente c) {
        this.clienteEdicao = c;
        txtNome.setText(c.getNome());
        txtEmail.setText(c.getEmail());
        txtTelefone.setText(c.getTelefone());
    }
}
