package View;

import Controller.*;
import Model.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class JanelaPrincipal extends Frame {
    private ClienteController controller = new ClienteController();
    private java.awt.List listaClientes = new java.awt.List();
    private TextField txtBusca = new TextField(20);

    public JanelaPrincipal() {
        setTitle("Clientes - Escritório de Arquitetura");
        setSize(600, 500);
        setLayout(new BorderLayout(10, 10));

        // === Tema escuro ===
        Color corFundo = new Color(30, 30, 30);       // cinza bem escuro
        Color corTexto = Color.WHITE;
        Color corListaFundo = new Color(45, 45, 45);  // fundo levemente mais claro
        Color corBotaoFiltrar = new Color(70, 130, 180); // azul suave

        setBackground(corFundo);

        // Fonte Century Gothic
        Font fontePadrao = new Font("Century Gothic", Font.PLAIN, 14);
        setFont(fontePadrao);

        // Lista de clientes escura
        listaClientes.setBackground(corListaFundo);
        listaClientes.setForeground(corTexto);
        listaClientes.setFont(fontePadrao);

        txtBusca.setBackground(corListaFundo);
        txtBusca.setForeground(corTexto);
        txtBusca.setFont(fontePadrao);

        // Painel superior com busca centralizada
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

        // Painel inferior com botões centralizados
        Panel botoes = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        botoes.setBackground(corFundo);

        Button btnAdd = new Button("Adicionar");
        Button btnRemover = new Button("Remover");
        Button btnEditar = new Button("Editar");

        // === Agora com texto preto (em vez de branco) ===
        btnAdd.setForeground(Color.BLACK);
        btnRemover.setForeground(Color.BLACK);
        btnEditar.setForeground(Color.BLACK);

        botoes.add(btnAdd);
        botoes.add(btnRemover);
        botoes.add(btnEditar);

        add(listaClientes, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        atualizarLista();

        // === Eventos ===
        btnAdd.addActionListener(e -> new FormCliente(this, controller, null));

        btnRemover.addActionListener(e -> {
            int idx = listaClientes.getSelectedIndex();
            if (idx >= 0) {
                String linha = listaClientes.getSelectedItem();
                int id = Integer.parseInt(linha.split(" - ")[0]);
                controller.removerCliente(id);
                atualizarLista();
            }
        });

        btnEditar.addActionListener(e -> {
            int idx = listaClientes.getSelectedIndex();
            if (idx >= 0) {
                String linha = listaClientes.getSelectedItem();
                int id = Integer.parseInt(linha.split(" - ")[0]);
                Cliente c = controller.buscarClientePorId(id);
                new FormCliente(this, controller, c);
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

    public void atualizarLista() {
        listaClientes.removeAll();
        List<Cliente> clientes = controller.listarClientes();
        for (Cliente c : clientes) {
            listaClientes.add(c.toString());
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
            listaClientes.add(c.toString());
        }
    }

    public static void main(String[] args) {
        new JanelaPrincipal();
    }
}
