package View;

import Controller.ClienteController;
import Model.Cliente;
import View.FormCliente;
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

        System.out.println("DEBUG: atualizarLista() - clientes.size() = " + (clientes == null ? 0 : clientes.size()));

        if (clientes != null) {
            for (Cliente c : clientes) {
                // DEBUG: mostrar qual classe está sendo usada em runtime e toString()
                System.out.println("DEBUG: cliente.getClass().getName() = " + (c == null ? "null" : c.getClass().getName()));
                System.out.println("DEBUG: cliente.toString() = " + (c == null ? "null" : c.toString()));

                String nome = c.getNome() == null ? "" : c.getNome();
                String linha = c.getId() + " - " + nome; // formato fixo
                listaClientes.add(linha);
            }
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

        System.out.println("DEBUG: filtrarLista() - termo='" + termo + "', clientes.size()=" + (clientes == null ? 0 : clientes.size()));

        if (clientes != null) {
            for (Cliente c : clientes) {
                System.out.println("DEBUG: cliente.getClass().getName() = " + (c == null ? "null" : c.getClass().getName()));
                System.out.println("DEBUG: cliente.toString() = " + (c == null ? "null" : c.toString()));
                String nome = c.getNome() == null ? "" : c.getNome();
                String linha = c.getId() + " - " + nome;
                listaClientes.add(linha);
            }
        }
    }

    public static void main(String[] args) {
        new JanelaPrincipal();
    }
}
