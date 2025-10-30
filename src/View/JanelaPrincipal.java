package View;

import Controller.ClienteController;
import Model.Cliente;
import View.FormCliente;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class JanelaPrincipal extends Frame {
    private ClienteController controller = new ClienteController();
    private java.awt.List lista = new java.awt.List();
    private TextField txtBusca = new TextField(20);

    // mantém a lista atualmente exibida (com ou sem filtro)
    private List<Cliente> listaAtual = new ArrayList<>();

    public JanelaPrincipal() {
        setTitle("Clientes - Escritório de Arquitetura");
        setSize(600, 500);
        setLayout(new BorderLayout(10, 10));

        Color corFundo = new Color(30, 30, 30);
        Color corTexto = Color.WHITE;
        Color corListaFundo = new Color(45, 45, 45);
        Color corBotaoFiltrar = new Color(70, 130, 180);

        setBackground(corFundo);
        Font fontePadrao = new Font("Century Gothic", Font.PLAIN, 14);
        setFont(fontePadrao);

        lista.setBackground(corListaFundo);
        lista.setForeground(corTexto);
        lista.setFont(fontePadrao);

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

        add(lista, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        atualizarLista(); // carrega sem filtro

        // Duplo clique usa a lista que está NA TELA (listaAtual)
        lista.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = lista.getSelectedIndex();
                    if (idx >= 0 && idx < listaAtual.size()) {
                        Cliente c = listaAtual.get(idx);
                        String linhaCompleta = c.getId() + " - " + (c.getNome() == null ? "" : c.getNome())
                                + "\nEmail: " + (c.getEmail() == null ? "" : c.getEmail())
                                + "\nTelefone: " + (c.getTelefone() == null ? "" : c.getTelefone())
                                + "\nCPF/CNPJ: " + (c.getCpfCnpj() == null ? "" : c.getCpfCnpj());
                        JOptionPane.showMessageDialog(null, linhaCompleta, "Detalhes do Cliente", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        btnAdd.addActionListener(e -> new FormCliente(this, controller, null));

        btnRemover.addActionListener(e -> {
    int idx = lista.getSelectedIndex();
    if (idx >= 0 && idx < listaAtual.size()) {
        Cliente c = listaAtual.get(idx);
        try {
            controller.removerCliente(c.getId());
            atualizarLista();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao remover cliente:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
});

btnEditar.addActionListener(e -> {
    int idx = lista.getSelectedIndex();
    if (idx >= 0 && idx < listaAtual.size()) {
        Cliente c = listaAtual.get(idx);
        try {
            Cliente completo = controller.buscarClientePorId(c.getId());
            new FormCliente(this, controller, completo);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar cliente:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
});


        btnBuscar.addActionListener(e -> filtrarLista());
        txtBusca.addActionListener(e -> filtrarLista());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { dispose(); }
        });

        setVisible(true);
    }

   // Lista sem filtro
public void atualizarLista() {
    lista.removeAll();
    try {
        listaAtual = controller.listarClientes();
        for (Cliente c : listaAtual) adicionarLinha(c);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Erro ao listar clientes:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }
    validate();
    repaint();
}

// Lista com filtro (usa buscarPorTermoCompleto)
private void filtrarLista() {
    String termo = txtBusca.getText().trim();
    lista.removeAll();
    try {
        if (termo.isEmpty()) {
            listaAtual = controller.listarClientes();
        } else {
            listaAtual = controller.buscarClientes(termo);
        }
        for (Cliente c : listaAtual) adicionarLinha(c);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Erro ao buscar clientes:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }
    validate();
    repaint();
}


    private void adicionarLinha(Cliente c) {
        String nome = c.getNome() == null ? "" : c.getNome();
        String email = c.getEmail() == null ? "" : c.getEmail();
        String telefone = c.getTelefone() == null ? "" : c.getTelefone();
        String emailShort = email.length() > 20 ? email.substring(0, 17) + "..." : email;
        String telShort = telefone.length() > 15 ? telefone.substring(0, 12) + "..." : telefone;
        String linha = String.format("%d - %s | %s | %s", c.getId(), nome, emailShort, telShort);
        lista.add(linha);
    }

    public static void main(String[] args) { new JanelaPrincipal(); }
}
