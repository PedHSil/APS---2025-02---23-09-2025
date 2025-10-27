package View;

import Controller.ClienteController;
import Model.Cliente;
import Model.Dados;
import Model.Endereco;

import java.awt.*;
import java.awt.event.*;

/**
 * Formulário simples para adicionar/editar cliente (AWT).
 * Agora inclui campos de dados (cpf/cnpj, email, telefone) e um endereço simples.
 */
public class FormCliente extends Frame {
    private JanelaPrincipal parent;
    private ClienteController controller;
    private Cliente cliente;

    private TextField txtNome = new TextField(30);
    private TextField txtCpfCnpj = new TextField(20);
    private TextField txtEmail = new TextField(30);
    private TextField txtTelefone = new TextField(20);

    // Endereço simples
    private Choice cbTipo = new Choice(); // residencial, comercial, entrega, outro
    private TextField txtLogradouro = new TextField(30);
    private TextField txtNumero = new TextField(8);
    private TextField txtComplemento = new TextField(20);
    private TextField txtBairro = new TextField(20);
    private TextField txtCidade = new TextField(20);
    private TextField txtEstado = new TextField(10);
    private TextField txtCep = new TextField(12);
    private TextField txtPais = new TextField(20);

    private Button btnSalvar = new Button("Salvar");
    private Button btnCancelar = new Button("Cancelar");

    public FormCliente(JanelaPrincipal parent, ClienteController controller, Cliente cliente) {
        super(cliente == null ? "Adicionar Cliente" : "Editar Cliente");
        this.parent = parent;
        this.controller = controller;
        this.cliente = cliente == null ? new Cliente() : cliente;

        setSize(800, 480);
        setLayout(new BorderLayout(8, 8));

        Panel center = new Panel(new GridLayout(3, 1, 4, 6));

        // Linha: nome
        Panel pNome = new Panel(new FlowLayout(FlowLayout.LEFT));
        pNome.add(new Label("Nome:"));
        txtNome.setText(this.cliente.getNome() == null ? "" : this.cliente.getNome());
        pNome.add(txtNome);
        center.add(pNome);

        // Linha: dados (cpf/email/telefone)
        Panel pDados = new Panel(new FlowLayout(FlowLayout.LEFT));
        pDados.add(new Label("CPF/CNPJ:"));
        txtCpfCnpj.setText(this.cliente.getDados() == null ? "" : safe(this.cliente.getDados().getCpfCnpj()));
        pDados.add(txtCpfCnpj);
        pDados.add(new Label("Email:"));
        txtEmail.setText(this.cliente.getDados() == null ? "" : safe(this.cliente.getDados().getEmail()));
        pDados.add(txtEmail);
        pDados.add(new Label("Telefone:"));
        txtTelefone.setText(this.cliente.getDados() == null ? "" : safe(this.cliente.getDados().getTelefone()));
        pDados.add(txtTelefone);
        center.add(pDados);

        // Bloco: endereço (vários campos)
        Panel pEndereco = new Panel(new GridLayout(3, 1));
        Panel pTipo = new Panel(new FlowLayout(FlowLayout.LEFT));
        pTipo.add(new Label("Tipo:"));
        cbTipo.add("residencial"); cbTipo.add("comercial"); cbTipo.add("entrega"); cbTipo.add("outro");
        String tipoAtual = this.cliente.getEndereco() == null ? "residencial" : safe(this.cliente.getEndereco().getTipo());
        cbTipo.select(tipoAtual);
        pTipo.add(cbTipo);
        pTipo.add(new Label("Logradouro:"));
        txtLogradouro.setText(this.cliente.getEndereco() == null ? "" : safe(this.cliente.getEndereco().getLogradouro()));
        pTipo.add(txtLogradouro);
        pTipo.add(new Label("Nº:"));
        txtNumero.setText(this.cliente.getEndereco() == null ? "" : safe(this.cliente.getEndereco().getNumero()));
        pTipo.add(txtNumero);
        pEndereco.add(pTipo);

        Panel pLinha2 = new Panel(new FlowLayout(FlowLayout.LEFT));
        pLinha2.add(new Label("Complemento:"));
        txtComplemento.setText(this.cliente.getEndereco() == null ? "" : safe(this.cliente.getEndereco().getComplemento()));
        pLinha2.add(txtComplemento);
        pLinha2.add(new Label("Bairro:"));
        txtBairro.setText(this.cliente.getEndereco() == null ? "" : safe(this.cliente.getEndereco().getBairro()));
        pLinha2.add(txtBairro);
        pEndereco.add(pLinha2);

        Panel pLinha3 = new Panel(new FlowLayout(FlowLayout.LEFT));
        pLinha3.add(new Label("Cidade:"));
        txtCidade.setText(this.cliente.getEndereco() == null ? "" : safe(this.cliente.getEndereco().getCidade()));
        pLinha3.add(txtCidade);
        pLinha3.add(new Label("Estado:"));
        txtEstado.setText(this.cliente.getEndereco() == null ? "" : safe(this.cliente.getEndereco().getEstado()));
        pLinha3.add(txtEstado);
        pLinha3.add(new Label("CEP:"));
        txtCep.setText(this.cliente.getEndereco() == null ? "" : safe(this.cliente.getEndereco().getCep()));
        pLinha3.add(txtCep);
        pLinha3.add(new Label("País:"));
        txtPais.setText(this.cliente.getEndereco() == null ? "Brasil" : safe(this.cliente.getEndereco().getPais()));
        pLinha3.add(txtPais);
        pEndereco.add(pLinha3);

        center.add(pEndereco);

        Panel botoes = new Panel(new FlowLayout(FlowLayout.CENTER));
        botoes.add(btnSalvar);
        botoes.add(btnCancelar);

        add(center, BorderLayout.CENTER);
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

    // Helper para evitar nulls em setText
    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void salvar() {
        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            mostrarErro("Nome não pode ficar vazio");
            return;
        }

        // Preenche cliente
        cliente.setNome(nome);

        // Preenche dados
        Dados d = cliente.getDados();
        if (d == null) d = new Dados();
        d.setCpfCnpj(txtCpfCnpj.getText().trim());
        d.setEmail(txtEmail.getText().trim());
        d.setTelefone(txtTelefone.getText().trim());
        cliente.setDados(d);

        // Preenche endereco (um endereço principal)
        Endereco e = cliente.getEndereco();
        if (e == null) e = new Endereco();
        e.setTipo(cbTipo.getSelectedItem());
        e.setLogradouro(txtLogradouro.getText().trim());
        e.setNumero(txtNumero.getText().trim());
        e.setComplemento(txtComplemento.getText().trim());
        e.setBairro(txtBairro.getText().trim());
        e.setCidade(txtCidade.getText().trim());
        e.setEstado(txtEstado.getText().trim());
        e.setCep(txtCep.getText().trim());
        e.setPais(txtPais.getText().trim());
        cliente.setEndereco(e);

        try {
            if (cliente.getId() == 0) {
                controller.salvarCliente(cliente); // deve inserir cliente, dados, endereco em transação
            } else {
                controller.atualizarCliente(cliente); // atualizar nas 3 tabelas
            }
            parent.atualizarLista();
            fechar();
        } catch (Exception ex) {
            mostrarErro("Erro ao salvar: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void mostrarErro(String msg) {
        Dialog d = new Dialog(this, "Erro", true);
        d.setLayout(new FlowLayout());
        d.add(new Label(msg));
        Button ok = new Button("OK");
        ok.addActionListener(evt -> d.dispose());
        d.add(ok);
        d.setSize(320, 120);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void fechar() {
        setVisible(false);
        dispose();
    }
}
