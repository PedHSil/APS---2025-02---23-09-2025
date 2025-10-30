package View;

import Controller.ClienteController;
import Model.Cliente;
import Model.Endereco;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class JanelaPrincipal extends JFrame {

    private final ClienteController controller = new ClienteController();
    private final JTextField txtBusca = new JTextField(24);

    private final String[] COLUNAS = new String[]{
            "ID", "Nome", "Email", "Telefone", "CPF/CNPJ",
            "Tipo", "Logradouro", "Número", "Complemento",
            "Bairro", "Cidade", "Estado", "CEP", "País"
    };

    private final DefaultTableModel model = new DefaultTableModel(COLUNAS, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabela = new JTable(model);

    // mantém a lista atualmente exibida (com ou sem filtro)
    private List<Cliente> listaAtual = new ArrayList<>();

    public JanelaPrincipal() {
        super("Clientes - Escritório de Arquitetura");

        // ====== Tema/estética (opcional, pode tirar se quiser) ======
        Color corFundo = new Color(30, 30, 30);
        Color corTexto = Color.WHITE;
        Color corListaFundo = new Color(45, 45, 45);
        Color corBotaoFiltrar = new Color(70, 130, 180);
        Font fontePadrao = new Font("Century Gothic", Font.PLAIN, 14);

        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBackground(corFundo);
        setContentPane(raiz);
        setFont(fontePadrao);

        // ====== Barra de busca ======
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelBusca.setBackground(corFundo);

        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setForeground(corTexto);
        lblBuscar.setFont(new Font("Century Gothic", Font.BOLD, 14));

        txtBusca.setBackground(corListaFundo);
        txtBusca.setForeground(corTexto);
        txtBusca.setFont(fontePadrao);

        JButton btnBuscar = new JButton("Filtrar");
        btnBuscar.setBackground(corBotaoFiltrar);
        btnBuscar.setForeground(corTexto);

        painelBusca.add(lblBuscar);
        painelBusca.add(txtBusca);
        painelBusca.add(btnBuscar);
        raiz.add(painelBusca, BorderLayout.NORTH);

        // ====== Tabela ======
        tabela.setFont(fontePadrao);
        tabela.setRowHeight(22);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // não encolhe colunas
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Larguras iniciais (ajuste à vontade)
        int[] w = {50, 160, 220, 120, 120, 90, 220, 80, 140, 140, 140, 80, 100, 100};
        for (int i = 0; i < w.length && i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.getViewport().setBackground(corListaFundo);
        raiz.add(scroll, BorderLayout.CENTER);

        // ====== Botões ======
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        botoes.setBackground(corFundo);

        JButton btnAdd = new JButton("Adicionar");
        JButton btnRemover = new JButton("Remover");
        JButton btnEditar = new JButton("Editar");

        botoes.add(btnAdd);
        botoes.add(btnRemover);
        botoes.add(btnEditar);
        raiz.add(botoes, BorderLayout.SOUTH);

        // ====== Ações ======
        btnBuscar.addActionListener(e -> filtrarLista());
        txtBusca.addActionListener(e -> filtrarLista());

        btnAdd.addActionListener(e -> new FormCliente(this, controller, null)); // cria e ao salvar chama parent.atualizarLista()

        btnRemover.addActionListener(e -> {
            int idx = tabela.getSelectedRow();
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
            int idx = tabela.getSelectedRow();
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

        // Duplo clique: mostra detalhes com todas as infos (comportamento similar ao antigo)
        tabela.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = tabela.getSelectedRow();
                    if (idx >= 0 && idx < listaAtual.size()) {
                        Cliente c = listaAtual.get(idx);
                        Endereco ePrincipal = c.getEndereco();
                        String linhaCompleta =
                                c.getId() + " - " + n(c.getNome()) +
                                "\nEmail: " + n(c.getEmail()) +
                                "\nTelefone: " + n(c.getTelefone()) +
                                "\nCPF/CNPJ: " + n(c.getCpfCnpj()) +
                                "\nEndereço: " +
                                n(ePrincipal.getTipo()) + " | " +
                                n(ePrincipal.getLogradouro()) + ", " + n(ePrincipal.getNumero()) +
                                " - " + n(ePrincipal.getBairro()) +
                                " - " + n(ePrincipal.getCidade()) + "/" + n(ePrincipal.getEstado()) +
                                " - CEP " + n(ePrincipal.getCep()) +
                                " - " + n(ePrincipal.getPais());
                        JOptionPane.showMessageDialog(JanelaPrincipal.this, linhaCompleta,
                                "Detalhes do Cliente", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        // ====== Janela ======
        setSize(1100, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // carrega sem filtro
        atualizarLista();

        setVisible(true);
    }

    private static String n(String s) { return (s == null) ? "" : s; }

    public void atualizarLista() {
        try {
            listaAtual = controller.listarClientes();
            preencherTabela(listaAtual);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao listar clientes:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrarLista() {
        String termo = txtBusca.getText().trim();
        try {
            if (termo.isEmpty()) {
                listaAtual = controller.listarClientes();
            } else {
                listaAtual = controller.buscarClientes(termo);
            }
            preencherTabela(listaAtual);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar clientes:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void preencherTabela(List<Cliente> dados) {
        model.setRowCount(0);
        for (Cliente c : dados) {
            Endereco e = c.getEndereco(); // usa o “principal”
            model.addRow(new Object[]{
                    c.getId(),
                    n(c.getNome()),
                    n(c.getEmail()),
                    n(c.getTelefone()),
                    n(c.getCpfCnpj()),
                    n(e.getTipo()),
                    n(e.getLogradouro()),
                    n(e.getNumero()),
                    n(e.getComplemento()),
                    n(e.getBairro()),
                    n(e.getCidade()),
                    n(e.getEstado()),
                    n(e.getCep()),
                    n(e.getPais())
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JanelaPrincipal::new);
    }
}