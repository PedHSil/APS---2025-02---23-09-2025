package View;

import Controller.ClienteController;
import Model.Cliente;
import java.awt.*;
import java.awt.event.*;

/**
 * Formulário simples para adicionar/editar cliente (AWT).
 */
public class FormCliente extends Frame {
    private JanelaPrincipal parent;
    private ClienteController controller;
    private Cliente cliente;

    private TextField txtNome = new TextField(30);
    private Button btnSalvar = new Button("Salvar");
    private Button btnCancelar = new Button("Cancelar");

    public FormCliente(JanelaPrincipal parent, ClienteController controller, Cliente cliente) {
        super(cliente == null ? "Adicionar Cliente" : "Editar Cliente");
        this.parent = parent;
        this.controller = controller;
        this.cliente = cliente == null ? new Cliente() : cliente;

        setSize(420, 150);
        setLayout(new BorderLayout(10, 10));
        Panel centro = new Panel(new GridLayout(2, 1, 5, 5));
        Panel linhaNome = new Panel(new FlowLayout(FlowLayout.LEFT));
        linhaNome.add(new Label("Nome:"));
        txtNome.setText(this.cliente.getNome() == null ? "" : this.cliente.getNome());
        linhaNome.add(txtNome);
        centro.add(linhaNome);

        Panel botoes = new Panel(new FlowLayout(FlowLayout.CENTER));
        botoes.add(btnSalvar);
        botoes.add(btnCancelar);

        add(centro, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        btnSalvar.addActionListener(e -> salvar());
        btnCancelar.addActionListener(e -> fechar());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                fechar();
            }
        });

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void salvar() {
        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            Dialog d = new Dialog(this, "Erro", true);
            d.setLayout(new FlowLayout());
            d.add(new Label("Nome não pode ficar vazio"));
            Button ok = new Button("OK");
            ok.addActionListener(evt -> d.dispose());
            d.add(ok);
            d.setSize(220, 120);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
            return;
        }

        cliente.setNome(nome);
        if (cliente.getId() == 0) {
            controller.salvarCliente(cliente);
        } else {
            controller.atualizarCliente(cliente);
        }

        // Atualiza a lista na janela principal
        parent.atualizarLista();
        fechar();
    }

    private void fechar() {
        setVisible(false);
        dispose();
    }
}
