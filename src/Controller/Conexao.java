package Controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    // Dados da conexão
    private static final String URL = "jdbc:mysql://localhost:3306/arquitetura?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";      // seu usuário do MySQL
    private static final String PASSWORD = "root"; // sua senha do MySQL

    // Método para obter conexão
    public static Connection getConnection() {
        try {
            // Garantir que o driver esteja carregado
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Retornar a conexão
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC do MySQL não encontrado!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados!");
            e.printStackTrace();
        }
        return null;
    }

    // Teste rápido da conexão
    /*public static void main(String[] args) {
        Connection con = Conexao.getConnection();
        if (con != null) {
            System.out.println("Conexão realizada com sucesso!");
        } else {
            System.out.println("Falha na conexão.");
        }
    } */
}
